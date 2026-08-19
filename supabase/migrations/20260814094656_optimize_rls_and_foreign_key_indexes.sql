create index menu_items_menu_restaurant_idx
    on public.menu_items (menu_id, restaurant_id);

drop policy restaurants_select_public on public.restaurants;
drop policy restaurants_select_owner on public.restaurants;
create policy restaurants_select_anon on public.restaurants for select to anon
using (private.is_restaurant_public(id));
create policy restaurants_select_authenticated on public.restaurants for select to authenticated
using (private.is_restaurant_public(id) or (select auth.uid()) = owner_account_id);

drop policy opening_periods_select_public on public.restaurant_opening_periods;
drop policy opening_periods_select_owner on public.restaurant_opening_periods;
create policy opening_periods_select_anon on public.restaurant_opening_periods for select to anon
using (private.is_restaurant_public(restaurant_id));
create policy opening_periods_select_authenticated on public.restaurant_opening_periods for select to authenticated
using (private.is_restaurant_public(restaurant_id) or private.owns_restaurant(restaurant_id));

drop policy menus_select_public on public.menus;
drop policy menus_select_owner on public.menus;
create policy menus_select_anon on public.menus for select to anon
using (private.is_menu_public(id));
create policy menus_select_authenticated on public.menus for select to authenticated
using (private.is_menu_public(id) or private.owns_restaurant(restaurant_id));

drop policy dishes_select_public on public.dishes;
drop policy dishes_select_owner on public.dishes;
create policy dishes_select_anon on public.dishes for select to anon
using (private.is_dish_public(id));
create policy dishes_select_authenticated on public.dishes for select to authenticated
using (private.is_dish_public(id) or private.owns_restaurant(restaurant_id));

drop policy dish_allergens_select_public on public.dish_allergens;
drop policy dish_allergens_select_owner on public.dish_allergens;
create policy dish_allergens_select_anon on public.dish_allergens for select to anon
using (private.is_dish_public(dish_id));
create policy dish_allergens_select_authenticated on public.dish_allergens for select to authenticated
using (
    private.is_dish_public(dish_id)
    or exists (
        select 1
        from public.dishes d
        where d.id = dish_id
          and private.owns_restaurant(d.restaurant_id)
    )
);

drop policy menu_items_select_public on public.menu_items;
drop policy menu_items_select_owner on public.menu_items;
create policy menu_items_select_anon on public.menu_items for select to anon
using (is_enabled and private.is_menu_public(menu_id) and private.is_dish_public(dish_id));
create policy menu_items_select_authenticated on public.menu_items for select to authenticated
using (
    (is_enabled and private.is_menu_public(menu_id) and private.is_dish_public(dish_id))
    or private.owns_restaurant(restaurant_id)
);

drop policy reviews_select_public on public.reviews;
drop policy reviews_select_author on public.reviews;
create policy reviews_select_anon on public.reviews for select to anon
using (
    visibility = 'public'
    and moderation_status = 'visible'
    and (
        (restaurant_id is not null and private.is_restaurant_public(restaurant_id))
        or (dish_id is not null and private.is_dish_public(dish_id))
    )
);
create policy reviews_select_authenticated on public.reviews for select to authenticated
using (
    (select auth.uid()) = author_account_id
    or (
        visibility = 'public'
        and moderation_status = 'visible'
        and (
            (restaurant_id is not null and private.is_restaurant_public(restaurant_id))
            or (dish_id is not null and private.is_dish_public(dish_id))
        )
    )
);
