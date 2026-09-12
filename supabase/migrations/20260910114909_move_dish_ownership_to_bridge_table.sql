-- A dish no longer carries the restaurant it belongs to. The link lives in restaurant_dishes,
-- keyed by dish_id so a dish still belongs to exactly one restaurant, and ownership survives for a
-- catalogue dish that is not on any menu yet.

create table public.restaurant_dishes (
    dish_id uuid primary key references public.dishes (id) on delete cascade,
    restaurant_id uuid not null references public.restaurants (id) on delete cascade
);

create index restaurant_dishes_restaurant_id_idx on public.restaurant_dishes (restaurant_id);

insert into public.restaurant_dishes (dish_id, restaurant_id)
select id, restaurant_id from public.dishes;

create function private.owns_dish(target_dish_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.restaurant_dishes rd
        join public.restaurants r on r.id = rd.restaurant_id
        where rd.dish_id = target_dish_id
          and r.owner_account_id = (select auth.uid())
    );
$$;

revoke execute on function private.owns_dish(uuid) from public, anon;
grant execute on function private.owns_dish(uuid) to authenticated;

drop policy dishes_select_authenticated on public.dishes;
drop policy dishes_insert_owner on public.dishes;
drop policy dishes_update_owner on public.dishes;
drop policy dish_allergens_select_authenticated on public.dish_allergens;
drop policy dish_allergens_insert_owner on public.dish_allergens;
drop policy dish_allergens_delete_owner on public.dish_allergens;

alter table public.dishes drop column restaurant_id;

create or replace function private.is_dish_public(target_dish_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.dishes d
        join public.restaurant_dishes rd on rd.dish_id = d.id
        join public.menu_items mi on mi.dish_id = d.id
        join public.menus m on m.id = mi.menu_id and m.restaurant_id = rd.restaurant_id
        join public.restaurants r on r.id = rd.restaurant_id
        where d.id = target_dish_id
          and d.is_enabled
          and mi.is_enabled
          and m.publication_state = 'published'
          and r.publication_state = 'published'
    );
$$;

create or replace function private.assert_menu_item_restaurants_match()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    if not exists (
        select 1
        from public.menus m
        join public.restaurant_dishes rd on rd.restaurant_id = m.restaurant_id
        where m.id = new.menu_id and rd.dish_id = new.dish_id
    ) then
        raise exception 'menu % and dish % belong to different restaurants', new.menu_id, new.dish_id
            using errcode = '23514';
    end if;
    return new;
end;
$$;

-- A dish row alone has no owner, so creating one only requires an active restaurant account; what
-- binds it to a restaurant is the restaurant_dishes insert, and save_restaurant_dish writes both in
-- the same transaction. An unbound dish is visible to nobody.
create policy dishes_select_authenticated on public.dishes for select to authenticated
using (private.is_dish_public(id) or private.owns_dish(id));
create policy dishes_insert_authenticated on public.dishes for insert to authenticated
with check (private.is_active_restaurant_account((select auth.uid())));
create policy dishes_update_owner on public.dishes for update to authenticated
using (private.owns_dish(id)) with check (private.owns_dish(id));
create policy dishes_delete_owner on public.dishes for delete to authenticated
using (private.owns_dish(id));

create policy dish_allergens_select_authenticated on public.dish_allergens for select to authenticated
using (private.is_dish_public(dish_id) or private.owns_dish(dish_id));
create policy dish_allergens_insert_owner on public.dish_allergens for insert to authenticated
with check (private.owns_dish(dish_id));
create policy dish_allergens_delete_owner on public.dish_allergens for delete to authenticated
using (private.owns_dish(dish_id));

alter table public.restaurant_dishes enable row level security;

create policy restaurant_dishes_select_anon on public.restaurant_dishes for select to anon
using (private.is_dish_public(dish_id));
create policy restaurant_dishes_select_authenticated on public.restaurant_dishes for select to authenticated
using (private.is_dish_public(dish_id) or private.owns_restaurant(restaurant_id));
create policy restaurant_dishes_insert_owner on public.restaurant_dishes for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy restaurant_dishes_delete_owner on public.restaurant_dishes for delete to authenticated
using (private.owns_restaurant(restaurant_id));

grant select on public.restaurant_dishes to anon;
grant select, insert, delete on public.restaurant_dishes to authenticated;
grant all on public.restaurant_dishes to service_role;

create or replace function public.save_restaurant_menu(
    p_restaurant_id uuid,
    p_menu_id uuid,
    p_name text,
    p_description text,
    p_publication_state text,
    p_price_minor_units bigint,
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
    if p_price_minor_units is not null and p_price_minor_units < 0 then
        raise exception 'menu price must not be negative' using errcode = '22023';
    end if;

    if p_menu_id is not null then
        select id into v_menu_id
        from public.menus
        where id = p_menu_id and restaurant_id = p_restaurant_id;
        if v_menu_id is null then
            raise exception 'menu does not belong to restaurant' using errcode = '42501';
        end if;
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
        left join public.restaurant_dishes rd
            on rd.dish_id = item.dish_id and rd.restaurant_id = p_restaurant_id
        where rd.dish_id is null or item.price_minor_units < 0 or item.position < 0
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
        insert into public.menus (restaurant_id, name, description, publication_state, price_minor_units)
        values (
            p_restaurant_id, btrim(p_name), nullif(btrim(p_description), ''),
            p_publication_state, p_price_minor_units
        )
        returning id into v_menu_id;
    else
        update public.menus
        set name = btrim(p_name),
            description = nullif(btrim(p_description), ''),
            publication_state = p_publication_state,
            price_minor_units = p_price_minor_units
        where id = v_menu_id;
    end if;

    delete from public.menu_items where menu_id = v_menu_id;
    insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled)
    select v_menu_id, item.dish_id, item.price_minor_units, item.position, item.is_enabled
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
        -- The id is generated here rather than read back with RETURNING: a dish is only readable
        -- once restaurant_dishes says whose it is, and that row cannot exist before the dish does.
        v_dish_id := gen_random_uuid();
        insert into public.dishes (id, name, description, allergen_note, is_enabled)
        values (
            v_dish_id, btrim(p_name), nullif(btrim(p_description), ''),
            nullif(btrim(p_allergen_note), ''), p_is_enabled
        );
        insert into public.restaurant_dishes (dish_id, restaurant_id)
        values (v_dish_id, p_restaurant_id);
    else
        if not exists (
            select 1 from public.restaurant_dishes
            where dish_id = p_dish_id and restaurant_id = p_restaurant_id
        ) then
            raise exception 'dish not found or not owned' using errcode = '42501';
        end if;
        update public.dishes
        set name = btrim(p_name), description = nullif(btrim(p_description), ''),
            allergen_note = nullif(btrim(p_allergen_note), ''),
            is_enabled = p_is_enabled
        where id = p_dish_id
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
    if not private.owns_dish(p_dish_id) then
        raise exception 'dish not found or not owned' using errcode = '42501';
    end if;
    update public.dishes set is_enabled = false where id = p_dish_id;
    delete from public.menu_items where dish_id = p_dish_id;
end;
$$;

create or replace function public.delete_restaurant_dish(p_dish_id uuid)
returns void language plpgsql security invoker set search_path = '' as $$
begin
    if not private.owns_dish(p_dish_id) then
        raise exception 'dish not found or not owned' using errcode = '42501';
    end if;
    if exists (select 1 from public.reviews where dish_id = p_dish_id) then
        raise exception 'dishes with reviews must be archived' using errcode = 'P0001';
    end if;
    delete from public.dishes where id = p_dish_id;
end;
$$;
