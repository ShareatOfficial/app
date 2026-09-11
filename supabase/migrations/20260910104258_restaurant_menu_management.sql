-- Owner-facing menu and dish mutations. Public access continues to be governed
-- by the existing publication and enabled-state policies.

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
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean
        )
        group by dish_id having count(*) > 1
    ) then
        raise exception 'a dish can appear only once in a menu' using errcode = '22023';
    end if;
    if exists (
        select 1
        from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean
        )
        left join public.dishes d on d.id = item.dish_id and d.restaurant_id = p_restaurant_id
        where d.id is null or item.price_minor_units < 0 or item.position < 0
    ) then
        raise exception 'invalid menu item' using errcode = '22023';
    end if;
    if p_publication_state = 'published' and not exists (
        select 1
        from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
            dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean
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
    insert into public.menu_items (menu_id, dish_id, restaurant_id, price_minor_units, currency, position, is_enabled)
    select v_menu_id, item.dish_id, p_restaurant_id, item.price_minor_units, 'EUR', item.position, item.is_enabled
    from jsonb_to_recordset(coalesce(p_items, '[]'::jsonb)) as item(
        dish_id uuid, price_minor_units bigint, position integer, is_enabled boolean
    );
    return v_menu_id;
end;
$$;

create or replace function public.save_restaurant_dish(
    p_restaurant_id uuid,
    p_dish_id uuid,
    p_name text,
    p_description text,
    p_is_enabled boolean,
    p_allergen_ids jsonb,
    p_allergen_note text
)
returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare v_dish_id uuid;
begin
    if not private.owns_restaurant(p_restaurant_id) then
        raise exception 'restaurant not found or not owned by current account' using errcode = '42501';
    end if;
    if jsonb_typeof(coalesce(p_allergen_ids, '[]'::jsonb)) <> 'array' then
        raise exception 'allergens must be an array' using errcode = '22023';
    end if;
    if exists (
        select 1 from jsonb_array_elements_text(coalesce(p_allergen_ids, '[]'::jsonb)) allergen(id)
        left join public.allergens a on a.id = allergen.id where a.id is null
    ) then
        raise exception 'unknown allergen' using errcode = '22023';
    end if;

    if p_dish_id is null then
        insert into public.dishes (restaurant_id, name, description, allergen_note, allergen_source, is_enabled)
        values (p_restaurant_id, btrim(p_name), nullif(btrim(p_description), ''), nullif(btrim(p_allergen_note), ''),
            case when jsonb_array_length(coalesce(p_allergen_ids, '[]'::jsonb)) > 0 or nullif(btrim(p_allergen_note), '') is not null then 'restaurant' end,
            p_is_enabled)
        returning id into v_dish_id;
    else
        update public.dishes
        set name = btrim(p_name), description = nullif(btrim(p_description), ''),
            allergen_note = nullif(btrim(p_allergen_note), ''),
            allergen_source = case when jsonb_array_length(coalesce(p_allergen_ids, '[]'::jsonb)) > 0 or nullif(btrim(p_allergen_note), '') is not null then 'restaurant' end,
            is_enabled = p_is_enabled
        where id = p_dish_id and restaurant_id = p_restaurant_id
        returning id into v_dish_id;
        if v_dish_id is null then raise exception 'dish not found or not owned' using errcode = '42501'; end if;
    end if;
    delete from public.dish_allergens where dish_id = v_dish_id;
    insert into public.dish_allergens (dish_id, allergen_id)
    select v_dish_id, value from jsonb_array_elements_text(coalesce(p_allergen_ids, '[]'::jsonb));
    return v_dish_id;
end;
$$;

create or replace function public.archive_restaurant_dish(p_dish_id uuid)
returns void language plpgsql security invoker set search_path = '' as $$
begin
    update public.dishes set is_enabled = false where id = p_dish_id
        and private.owns_restaurant(restaurant_id);
    if not found then raise exception 'dish not found or not owned' using errcode = '42501'; end if;
    delete from public.menu_items where dish_id = p_dish_id;
end;
$$;

create or replace function public.delete_restaurant_dish(p_dish_id uuid)
returns void language plpgsql security invoker set search_path = '' as $$
begin
    if exists (select 1 from public.reviews where dish_id = p_dish_id) then
        raise exception 'dishes with reviews must be archived' using errcode = 'P0001';
    end if;
    delete from public.dishes where id = p_dish_id and private.owns_restaurant(restaurant_id);
    if not found then raise exception 'dish not found or not owned' using errcode = '42501'; end if;
end;
$$;

revoke all on function public.save_restaurant_menu(uuid, uuid, text, text, text, jsonb) from public, anon;
revoke all on function public.save_restaurant_dish(uuid, uuid, text, text, boolean, jsonb, text) from public, anon;
revoke all on function public.archive_restaurant_dish(uuid) from public, anon;
revoke all on function public.delete_restaurant_dish(uuid) from public, anon;
grant execute on function public.save_restaurant_menu(uuid, uuid, text, text, text, jsonb) to authenticated;
grant execute on function public.save_restaurant_dish(uuid, uuid, text, text, boolean, jsonb, text) to authenticated;
grant execute on function public.archive_restaurant_dish(uuid) to authenticated;
grant execute on function public.delete_restaurant_dish(uuid) to authenticated;

drop policy if exists dishes_delete_owner on public.dishes;
