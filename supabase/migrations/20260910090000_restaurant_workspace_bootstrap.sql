-- A newly registered restaurant starts with an editable private workspace.  This is deliberately
-- one RPC so the client can neither forge ownership nor leave a half-created restaurant behind.

alter table public.menu_items
    add column if not exists category text
        check (category is null or category in ('starters', 'main_courses', 'desserts', 'small_bites'));

create or replace function public.save_restaurant_menu(
    p_restaurant_id uuid,
    p_menu_id uuid,
    p_name text,
    p_description text,
    p_publication_state text,
    p_items jsonb
)
returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_menu_id uuid;
begin
    if not private.owns_restaurant(p_restaurant_id) then
        raise exception 'restaurant not found or not owned by current account' using errcode = '42501';
    end if;
    if jsonb_typeof(coalesce(p_items, '[]'::jsonb)) <> 'array' then
        raise exception 'menu items must be an array' using errcode = '22023';
    end if;

    select id into v_menu_id from public.menus where restaurant_id = p_restaurant_id;
    if p_menu_id is not null and v_menu_id is not null and p_menu_id <> v_menu_id then
        raise exception 'menu does not belong to restaurant' using errcode = '42501';
    end if;

    if exists (
        select 1
        from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean, category text
        )
        group by dish_id having count(*) > 1
    ) then
        raise exception 'a dish can appear only once in a menu' using errcode = '22023';
    end if;
    if exists (
        select 1
        from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean, category text
        )
        left join public.dishes d on d.id = item.dish_id and d.restaurant_id = p_restaurant_id
        where d.id is null
           or item.price_minor_units < 0
           or item.position < 0
           or item.category not in ('starters', 'main_courses', 'desserts', 'small_bites')
    ) then
        raise exception 'invalid menu item' using errcode = '22023';
    end if;
    if p_publication_state = 'published' and not exists (
        select 1
        from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean, category text
        )
        join public.dishes d on d.id = item.dish_id
        where item.is_enabled and d.is_enabled
    ) then
        raise exception 'a published menu requires an enabled dish' using errcode = '22023';
    end if;

    if v_menu_id is null then
        insert into public.menus (restaurant_id, name, description, publication_state)
        values (p_restaurant_id, btrim(p_name), nullif(btrim(p_description), ''), p_publication_state)
        returning id into v_menu_id;
    else
        update public.menus
        set name = btrim(p_name),
            description = nullif(btrim(p_description), ''),
            publication_state = p_publication_state
        where id = v_menu_id;
    end if;

    delete from public.menu_items where menu_id = v_menu_id;
    insert into public.menu_items (
        menu_id, dish_id, restaurant_id, price_minor_units, currency, position, is_enabled, category
    )
    select
        v_menu_id,
        item.dish_id,
        p_restaurant_id,
        item.price_minor_units,
        'EUR',
        item.position,
        item.is_enabled,
        item.category
    from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
        dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean, category text
    );
    return v_menu_id;
end;
$$;

create or replace function public.ensure_restaurant_workspace()
returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_owner_account_id uuid := (select auth.uid());
    v_restaurant_id uuid;
    v_menu_id uuid;
    v_bravas_id uuid;
    v_croquettes_id uuid;
begin
    if v_owner_account_id is null
        or not private.is_active_restaurant_account(v_owner_account_id) then
        raise exception 'an active restaurant account is required' using errcode = '42501';
    end if;

    -- A per-owner transaction lock handles retry races.  The early return intentionally happens
    -- before any seed write: an existing workspace is never changed or completed implicitly.
    perform pg_catalog.pg_advisory_xact_lock(
        pg_catalog.hashtextextended(v_owner_account_id::text, 0)
    );
    select id into v_restaurant_id
    from public.restaurants
    where owner_account_id = v_owner_account_id;
    if v_restaurant_id is not null then
        return v_restaurant_id;
    end if;

    insert into public.restaurants (
        owner_account_id,
        name,
        description,
        street_line,
        locality,
        postal_code,
        region,
        country_code,
        publication_state
    ) values (
        v_owner_account_id,
        'Rincón de Paco',
        'Lugar típico para tomarse unas tapas.',
        'Calle de las Tapas, 1',
        'Madrid',
        '28001',
        'Comunidad de Madrid',
        'ES',
        'draft'
    ) returning id into v_restaurant_id;

    insert into public.menus (restaurant_id, name, description, publication_state)
    values (
        v_restaurant_id,
        'Carta',
        'Tu carta inicial. Edita o añade platos cuando quieras.',
        'draft'
    ) returning id into v_menu_id;

    insert into public.dishes (restaurant_id, name, description, allergen_source, is_enabled)
    values (
        v_restaurant_id,
        'Patatas bravas',
        'Patatas crujientes con salsa brava casera.',
        'restaurant',
        true
    ) returning id into v_bravas_id;

    insert into public.dishes (restaurant_id, name, description, allergen_source, is_enabled)
    values (
        v_restaurant_id,
        'Croquetas de jamón',
        'Croquetas cremosas de jamón ibérico.',
        'restaurant',
        true
    ) returning id into v_croquettes_id;

    insert into public.dish_allergens (dish_id, allergen_id) values
        (v_bravas_id, 'eggs'),
        (v_croquettes_id, 'cereals_containing_gluten'),
        (v_croquettes_id, 'eggs'),
        (v_croquettes_id, 'milk');

    insert into public.menu_items (
        menu_id, dish_id, restaurant_id, price_minor_units, currency, position, is_enabled, category
    ) values
        (v_menu_id, v_bravas_id, v_restaurant_id, 650, 'EUR', 0, true, 'small_bites'),
        (v_menu_id, v_croquettes_id, v_restaurant_id, 850, 'EUR', 1, true, 'starters');

    return v_restaurant_id;
end;
$$;

revoke all on function public.ensure_restaurant_workspace() from public, anon;
grant execute on function public.ensure_restaurant_workspace() to authenticated;
