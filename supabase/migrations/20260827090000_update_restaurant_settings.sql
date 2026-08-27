create or replace function public.update_restaurant_settings(
    p_restaurant_id uuid,
    p_name text,
    p_description text,
    p_public_email text,
    p_public_phone text,
    p_street_line text,
    p_locality text,
    p_postal_code text,
    p_publication_state text,
    p_opening_periods jsonb
)
returns void
language plpgsql
security invoker
set search_path = ''
as $$
begin
    if not private.owns_restaurant(p_restaurant_id) then
        raise exception 'restaurant not found or not owned by current account'
            using errcode = '42501';
    end if;

    update public.restaurants
    set name = btrim(p_name),
        description = nullif(btrim(p_description), ''),
        public_email = nullif(btrim(p_public_email), ''),
        public_phone = nullif(btrim(p_public_phone), ''),
        street_line = btrim(p_street_line),
        locality = btrim(p_locality),
        postal_code = btrim(p_postal_code),
        publication_state = p_publication_state,
        updated_at = now()
    where id = p_restaurant_id;

    if not found then
        raise exception 'restaurant not found' using errcode = 'P0002';
    end if;

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
    uuid, text, text, text, text, text, text, text, text, jsonb
) from public, anon;
grant execute on function public.update_restaurant_settings(
    uuid, text, text, text, text, text, text, text, text, jsonb
) to authenticated, service_role;
