-- restaurant_dishes was a 1:1 split of what dishes.restaurant_id already said: one row per dish,
-- naming its single restaurant. It bought no normalisation — a dish belonging to a restaurant is a
-- plain functional dependency on the dish key — and cost a join on every catalogue read, plus a
-- junction-table name that reads as a many-to-many it never was.
--
-- The menu/dish many-to-many stays where it belongs, in menu_items.

alter table public.dishes
    add column restaurant_id uuid references public.restaurants (id) on delete cascade;

update public.dishes d
set restaurant_id = rd.restaurant_id
from public.restaurant_dishes rd
where rd.dish_id = d.id;

alter table public.dishes
    alter column restaurant_id set not null;

create index dishes_restaurant_id_idx on public.dishes (restaurant_id);
create index dishes_restaurant_enabled_idx on public.dishes (restaurant_id, is_enabled);

create or replace function private.owns_dish(target_dish_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.dishes d
        join public.restaurants r on r.id = d.restaurant_id
        where d.id = target_dish_id
          and r.owner_account_id = (select auth.uid())
    );
$$;

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
        join public.menu_items mi on mi.dish_id = d.id
        join public.menus m on m.id = mi.menu_id and m.restaurant_id = d.restaurant_id
        join public.restaurants r on r.id = d.restaurant_id
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
        join public.dishes d on d.restaurant_id = m.restaurant_id
        where m.id = new.menu_id and d.id = new.dish_id
    ) then
        raise exception 'menu % and dish % belong to different restaurants', new.menu_id, new.dish_id
            using errcode = '23514';
    end if;
    return new;
end;
$$;

-- With the restaurant back on the row every dish policy can read it straight off the row, instead
-- of looking the dish up again. That also fixes `insert ... returning`: owns_dish is STABLE, so
-- evaluating the SELECT policy on the row the same statement is inserting would never see it.
drop policy dishes_insert_authenticated on public.dishes;
drop policy dishes_select_authenticated on public.dishes;
drop policy dishes_update_owner on public.dishes;
drop policy dishes_delete_owner on public.dishes;

create policy dishes_insert_owner on public.dishes for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy dishes_select_authenticated on public.dishes for select to authenticated
using (private.is_dish_public(id) or private.owns_restaurant(restaurant_id));
create policy dishes_update_owner on public.dishes for update to authenticated
using (private.owns_restaurant(restaurant_id)) with check (private.owns_restaurant(restaurant_id));
create policy dishes_delete_owner on public.dishes for delete to authenticated
using (private.owns_restaurant(restaurant_id));

drop table public.restaurant_dishes;

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
        insert into public.dishes (restaurant_id, name, description, allergen_note, is_enabled)
        values (
            p_restaurant_id, btrim(p_name), nullif(btrim(p_description), ''),
            nullif(btrim(p_allergen_note), ''), p_is_enabled
        )
        returning id into v_dish_id;
    else
        update public.dishes
        set name = btrim(p_name), description = nullif(btrim(p_description), ''),
            allergen_note = nullif(btrim(p_allergen_note), ''),
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
