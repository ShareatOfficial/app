-- Each restaurant-owner workspace manages one menu. Older workspaces created before
-- restaurant menu onboarding was introduced can be missing it, so repair only that
-- absence; do not alter restaurants that already have menus or invent catalogue items.
insert into public.menus (restaurant_id, name, publication_state)
select r.id, 'Main menu', 'unpublished'
from public.restaurants r
where not exists (
    select 1
    from public.menus m
    where m.restaurant_id = r.id
);

create or replace function public.create_restaurant_profile(
    p_name text,
    p_description text,
    p_public_email text,
    p_public_phone text,
    p_street_line text,
    p_locality text,
    p_postal_code text,
    p_region text,
    p_opening_periods jsonb
)
returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_owner_account_id uuid := (select auth.uid());
    v_restaurant_id uuid;
begin
    if v_owner_account_id is null
        or not private.is_active_restaurant_account(v_owner_account_id) then
        raise exception 'an active restaurant account is required'
            using errcode = '42501';
    end if;

    -- Serialize retries for the same account so concurrent submissions cannot
    -- create two restaurants, menus, or duplicate opening periods.
    perform pg_catalog.pg_advisory_xact_lock(
        pg_catalog.hashtextextended(v_owner_account_id::text, 0)
    );

    select id
    into v_restaurant_id
    from public.restaurants
    where owner_account_id = v_owner_account_id;

    if v_restaurant_id is not null then
        return v_restaurant_id;
    end if;

    if jsonb_typeof(coalesce(p_opening_periods, '[]'::jsonb)) <> 'array' then
        raise exception 'opening periods must be a JSON array'
            using errcode = '22023';
    end if;

    if exists (
        select 1
        from jsonb_to_recordset(coalesce(p_opening_periods, '[]'::jsonb)) as period(
            weekday smallint,
            opens_at time,
            closes_at time
        )
        group by period.weekday
        having count(*) > 1
    ) then
        raise exception 'only one opening period per weekday is supported'
            using errcode = '22023';
    end if;

    insert into public.restaurants (
        owner_account_id,
        name,
        description,
        public_email,
        public_phone,
        street_line,
        locality,
        postal_code,
        region,
        country_code,
        latitude,
        longitude,
        publication_state
    ) values (
        v_owner_account_id,
        btrim(p_name),
        nullif(btrim(p_description), ''),
        nullif(btrim(p_public_email), ''),
        nullif(btrim(p_public_phone), ''),
        nullif(btrim(p_street_line), ''),
        nullif(btrim(p_locality), ''),
        nullif(btrim(p_postal_code), ''),
        nullif(btrim(p_region), ''),
        'ES',
        null,
        null,
        'draft'
    )
    returning id into v_restaurant_id;

    insert into public.menus (restaurant_id, name, publication_state)
    values (v_restaurant_id, 'Main menu', 'unpublished');

    insert into public.restaurant_opening_periods (
        restaurant_id,
        weekday,
        position,
        opens_at,
        closes_at
    )
    select
        v_restaurant_id,
        period.weekday,
        0,
        period.opens_at,
        period.closes_at
    from jsonb_to_recordset(coalesce(p_opening_periods, '[]'::jsonb)) as period(
        weekday smallint,
        opens_at time,
        closes_at time
    );

    return v_restaurant_id;
end;
$$;

revoke all on function public.create_restaurant_profile(
    text, text, text, text, text, text, text, text, jsonb
) from public, anon;
grant execute on function public.create_restaurant_profile(
    text, text, text, text, text, text, text, text, jsonb
) to authenticated;

-- PostgREST resolves RPCs from their argument names. Remove the old signature before
-- publishing the region-aware replacement so requests cannot resolve ambiguously.
drop function if exists public.update_restaurant_settings(
    uuid, text, text, text, text, text, text, text, text, jsonb
);

create or replace function public.update_restaurant_settings(
    p_restaurant_id uuid,
    p_name text,
    p_description text,
    p_public_email text,
    p_public_phone text,
    p_street_line text,
    p_locality text,
    p_postal_code text,
    p_region text,
    p_publication_state text,
    p_opening_periods jsonb
)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_menu_id uuid;
    v_menu_count bigint;
    v_menu_publication_state text;
begin
    if not private.owns_restaurant(p_restaurant_id) then
        raise exception 'restaurant not found or not owned by current account'
            using errcode = '42501';
    end if;

    if p_publication_state not in ('draft', 'published', 'disabled') then
        raise exception 'unsupported restaurant publication state'
            using errcode = '22023';
    end if;
    if jsonb_typeof(coalesce(p_opening_periods, '[]'::jsonb)) <> 'array' then
        raise exception 'opening periods must be a JSON array'
            using errcode = '22023';
    end if;

    -- Lock the aggregate root before checking its menu and updating its schedule.
    perform 1
    from public.restaurants
    where id = p_restaurant_id
    for update;

    if p_publication_state = 'published' and (
        nullif(btrim(p_street_line), '') is null
        or nullif(btrim(p_locality), '') is null
        or nullif(btrim(p_postal_code), '') is null
    ) then
        raise exception 'a published restaurant needs a street, a town and a postcode'
            using errcode = '22023';
    end if;

    select count(*)
    into v_menu_count
    from public.menus
    where restaurant_id = p_restaurant_id;

    if p_publication_state = 'published' then
        if v_menu_count <> 1 then
            raise exception 'a published restaurant requires exactly one menu'
                using errcode = '22023';
        end if;
        select id into v_menu_id
        from public.menus
        where restaurant_id = p_restaurant_id;
        perform 1 from public.menus where id = v_menu_id for update;
        if not exists (
            select 1
            from public.menu_items mi
            join public.dishes d on d.id = mi.dish_id
            where mi.menu_id = v_menu_id
              and mi.is_enabled
              and d.is_enabled
        ) then
            raise exception 'a published restaurant requires an enabled dish'
                using errcode = '22023';
        end if;
        v_menu_publication_state := 'published';
    elsif p_publication_state = 'disabled' then
        v_menu_publication_state := 'disabled';
    else
        v_menu_publication_state := 'unpublished';
    end if;

    update public.restaurants
    set name = btrim(p_name),
        description = nullif(btrim(p_description), ''),
        public_email = nullif(btrim(p_public_email), ''),
        public_phone = nullif(btrim(p_public_phone), ''),
        street_line = nullif(btrim(p_street_line), ''),
        locality = nullif(btrim(p_locality), ''),
        postal_code = nullif(btrim(p_postal_code), ''),
        region = nullif(btrim(p_region), ''),
        publication_state = p_publication_state,
        updated_at = now()
    where id = p_restaurant_id;

    update public.menus
    set publication_state = v_menu_publication_state,
        updated_at = now()
    where restaurant_id = p_restaurant_id;

    delete from public.restaurant_opening_periods
    where restaurant_id = p_restaurant_id;

    insert into public.restaurant_opening_periods (
        restaurant_id,
        weekday,
        position,
        opens_at,
        closes_at
    )
    select
        p_restaurant_id,
        period.weekday,
        period.position,
        period.opens_at,
        period.closes_at
    from jsonb_to_recordset(coalesce(p_opening_periods, '[]'::jsonb)) as period(
        weekday smallint,
        position integer,
        opens_at time,
        closes_at time
    );
end;
$$;

revoke all on function public.update_restaurant_settings(
    uuid, text, text, text, text, text, text, text, text, text, jsonb
) from public, anon;
grant execute on function public.update_restaurant_settings(
    uuid, text, text, text, text, text, text, text, text, text, jsonb
) to authenticated, service_role;
