-- Normalises the restaurant / menu / dish catalogue.
--
-- * currency is determined by the restaurant, not by a (menu, dish) pair, so it stops being
--   repeated on every menu_items row
-- * menu_items.restaurant_id was determined by menu_id alone — a partial dependency on the
--   (menu_id, dish_id) key; the cross-restaurant guard those composite keys provided moves to
--   private.assert_menu_item_restaurants_match
-- * dishes.allergen_source was derived from the allergens and the note already stored
-- * a restaurant may own more than one menu, and a menu may carry an optional fixed price
-- * three allergen ids on the deployed project had drifted from their canonical spelling, so the
--   same allergen was addressable under two values

alter table public.restaurants
    add column currency_code text not null default 'EUR'
        check (currency_code ~ '^[A-Z]{3}$');

create function private.owns_menu(target_menu_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.menus m
        join public.restaurants r on r.id = m.restaurant_id
        where m.id = target_menu_id
          and r.owner_account_id = (select auth.uid())
    );
$$;

revoke execute on function private.owns_menu(uuid) from public, anon;
grant execute on function private.owns_menu(uuid) to authenticated;

-- The owner policies below are rebuilt on private.owns_menu once restaurant_id is gone.
drop policy menu_items_select_authenticated on public.menu_items;
drop policy menu_items_insert_owner on public.menu_items;
drop policy menu_items_update_owner on public.menu_items;
drop policy menu_items_delete_owner on public.menu_items;

-- Dropping restaurant_id also drops every index and composite foreign key built on it.
alter table public.menu_items
    drop column restaurant_id,
    drop column currency,
    add constraint menu_items_menu_id_fkey
        foreign key (menu_id) references public.menus (id) on delete cascade,
    add constraint menu_items_dish_id_fkey
        foreign key (dish_id) references public.dishes (id) on delete cascade;

create index menu_items_dish_id_idx on public.menu_items (dish_id);

alter table public.menus
    drop constraint menus_restaurant_id_key,
    drop constraint menus_id_restaurant_id_key,
    add column price_minor_units bigint
        check (price_minor_units is null or price_minor_units >= 0);

create index menus_restaurant_id_idx on public.menus (restaurant_id);

alter table public.dishes
    drop constraint dishes_id_restaurant_id_key,
    drop column allergen_source;

create function private.assert_menu_item_restaurants_match()
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

revoke execute on function private.assert_menu_item_restaurants_match() from public, anon, authenticated;

create trigger menu_items_restaurants_match
    before insert or update of menu_id, dish_id on public.menu_items
    for each row execute function private.assert_menu_item_restaurants_match();

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
        join public.menus m on m.id = mi.menu_id
        join public.restaurants r on r.id = d.restaurant_id
        where d.id = target_dish_id
          and d.is_enabled
          and mi.is_enabled
          and m.restaurant_id = d.restaurant_id
          and m.publication_state = 'published'
          and r.publication_state = 'published'
    );
$$;

create policy menu_items_select_authenticated on public.menu_items for select to authenticated
using (
    (is_enabled and private.is_menu_public(menu_id) and private.is_dish_public(dish_id))
    or private.owns_menu(menu_id)
);
create policy menu_items_insert_owner on public.menu_items for insert to authenticated
with check (private.owns_menu(menu_id));
create policy menu_items_update_owner on public.menu_items for update to authenticated
using (private.owns_menu(menu_id)) with check (private.owns_menu(menu_id));
create policy menu_items_delete_owner on public.menu_items for delete to authenticated
using (private.owns_menu(menu_id));

insert into public.allergens (id) values
    ('cereals_containing_gluten'),
    ('soybeans'),
    ('sulphur_dioxide_and_sulphites')
on conflict (id) do nothing;

update public.dish_allergens set allergen_id = 'cereals_containing_gluten' where allergen_id = 'gluten';
update public.dish_allergens set allergen_id = 'soybeans' where allergen_id = 'soy';
update public.dish_allergens set allergen_id = 'sulphur_dioxide_and_sulphites' where allergen_id = 'sulphites';
delete from public.allergens where id in ('gluten', 'soy', 'sulphites');

drop function public.save_restaurant_menu(uuid, uuid, text, text, text, jsonb);

create function public.save_restaurant_menu(
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

revoke all on function public.save_restaurant_menu(uuid, uuid, text, text, text, bigint, jsonb) from public, anon;
grant execute on function public.save_restaurant_menu(uuid, uuid, text, text, text, bigint, jsonb) to authenticated;

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
