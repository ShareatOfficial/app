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
    -- create two restaurants or duplicate opening periods.
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
        btrim(p_street_line),
        btrim(p_locality),
        btrim(p_postal_code),
        nullif(btrim(p_region), ''),
        'ES',
        null,
        null,
        'draft'
    )
    returning id into v_restaurant_id;

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
