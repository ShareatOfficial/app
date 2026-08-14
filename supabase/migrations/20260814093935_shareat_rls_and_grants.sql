create function private.is_active_customer(user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select user_id = (select auth.uid())
       and exists (
           select 1 from public.accounts
           where id = user_id and role = 'customer' and status = 'active'
       );
$$;

create function private.is_active_restaurant_account(user_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select user_id = (select auth.uid())
       and exists (
           select 1 from public.accounts
           where id = user_id and role = 'restaurant' and status = 'active'
       );
$$;

create function private.owns_restaurant(target_restaurant_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select (select auth.uid()) is not null
       and exists (
           select 1 from public.restaurants
           where id = target_restaurant_id
             and owner_account_id = (select auth.uid())
       );
$$;

create function private.is_restaurant_public(target_restaurant_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.restaurants
        where id = target_restaurant_id and publication_state = 'published'
    );
$$;

create function private.is_menu_public(target_menu_id uuid)
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
          and m.publication_state = 'published'
          and r.publication_state = 'published'
    );
$$;

create function private.is_dish_public(target_dish_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1
        from public.dishes d
        join public.menu_items mi on mi.dish_id = d.id and mi.restaurant_id = d.restaurant_id
        join public.menus m on m.id = mi.menu_id
        join public.restaurants r on r.id = d.restaurant_id
        where d.id = target_dish_id
          and d.is_enabled
          and mi.is_enabled
          and m.publication_state = 'published'
          and r.publication_state = 'published'
    );
$$;

revoke execute on all functions in schema private from public, anon, authenticated;
grant usage on schema private to anon, authenticated;
grant execute on function private.is_active_customer(uuid) to authenticated;
grant execute on function private.is_active_restaurant_account(uuid) to authenticated;
grant execute on function private.owns_restaurant(uuid) to authenticated;
grant execute on function private.is_restaurant_public(uuid) to anon, authenticated;
grant execute on function private.is_menu_public(uuid) to anon, authenticated;
grant execute on function private.is_dish_public(uuid) to anon, authenticated;

alter table public.accounts enable row level security;
alter table public.customer_profiles enable row level security;
alter table public.restaurants enable row level security;
alter table public.restaurant_opening_periods enable row level security;
alter table public.menus enable row level security;
alter table public.dishes enable row level security;
alter table public.allergens enable row level security;
alter table public.dish_allergens enable row level security;
alter table public.menu_items enable row level security;
alter table public.reviews enable row level security;

create policy accounts_select_self on public.accounts for select to authenticated
using ((select auth.uid()) = id);

create policy customer_profiles_select_self on public.customer_profiles for select to authenticated
using ((select auth.uid()) = account_id);
create policy customer_profiles_update_self on public.customer_profiles for update to authenticated
using ((select auth.uid()) = account_id)
with check ((select auth.uid()) = account_id);

create policy restaurants_select_public on public.restaurants for select to anon, authenticated
using (private.is_restaurant_public(id));
create policy restaurants_select_owner on public.restaurants for select to authenticated
using ((select auth.uid()) = owner_account_id);
create policy restaurants_insert_owner on public.restaurants for insert to authenticated
with check (
    (select auth.uid()) = owner_account_id
    and private.is_active_restaurant_account(owner_account_id)
);
create policy restaurants_update_owner on public.restaurants for update to authenticated
using ((select auth.uid()) = owner_account_id)
with check (
    (select auth.uid()) = owner_account_id
    and private.is_active_restaurant_account(owner_account_id)
);
create policy restaurants_delete_owner on public.restaurants for delete to authenticated
using ((select auth.uid()) = owner_account_id);

create policy opening_periods_select_public on public.restaurant_opening_periods for select to anon, authenticated
using (private.is_restaurant_public(restaurant_id));
create policy opening_periods_select_owner on public.restaurant_opening_periods for select to authenticated
using (private.owns_restaurant(restaurant_id));
create policy opening_periods_insert_owner on public.restaurant_opening_periods for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy opening_periods_update_owner on public.restaurant_opening_periods for update to authenticated
using (private.owns_restaurant(restaurant_id)) with check (private.owns_restaurant(restaurant_id));
create policy opening_periods_delete_owner on public.restaurant_opening_periods for delete to authenticated
using (private.owns_restaurant(restaurant_id));

create policy menus_select_public on public.menus for select to anon, authenticated
using (private.is_menu_public(id));
create policy menus_select_owner on public.menus for select to authenticated
using (private.owns_restaurant(restaurant_id));
create policy menus_insert_owner on public.menus for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy menus_update_owner on public.menus for update to authenticated
using (private.owns_restaurant(restaurant_id)) with check (private.owns_restaurant(restaurant_id));
create policy menus_delete_owner on public.menus for delete to authenticated
using (private.owns_restaurant(restaurant_id));

create policy dishes_select_public on public.dishes for select to anon, authenticated
using (private.is_dish_public(id));
create policy dishes_select_owner on public.dishes for select to authenticated
using (private.owns_restaurant(restaurant_id));
create policy dishes_insert_owner on public.dishes for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy dishes_update_owner on public.dishes for update to authenticated
using (private.owns_restaurant(restaurant_id)) with check (private.owns_restaurant(restaurant_id));
create policy dishes_delete_owner on public.dishes for delete to authenticated
using (private.owns_restaurant(restaurant_id));

create policy allergens_select on public.allergens for select to anon, authenticated using (true);

create policy dish_allergens_select_public on public.dish_allergens for select to anon, authenticated
using (private.is_dish_public(dish_id));
create policy dish_allergens_select_owner on public.dish_allergens for select to authenticated
using (exists (select 1 from public.dishes d where d.id = dish_id and private.owns_restaurant(d.restaurant_id)));
create policy dish_allergens_insert_owner on public.dish_allergens for insert to authenticated
with check (exists (select 1 from public.dishes d where d.id = dish_id and private.owns_restaurant(d.restaurant_id)));
create policy dish_allergens_delete_owner on public.dish_allergens for delete to authenticated
using (exists (select 1 from public.dishes d where d.id = dish_id and private.owns_restaurant(d.restaurant_id)));

create policy menu_items_select_public on public.menu_items for select to anon, authenticated
using (is_enabled and private.is_menu_public(menu_id) and private.is_dish_public(dish_id));
create policy menu_items_select_owner on public.menu_items for select to authenticated
using (private.owns_restaurant(restaurant_id));
create policy menu_items_insert_owner on public.menu_items for insert to authenticated
with check (private.owns_restaurant(restaurant_id));
create policy menu_items_update_owner on public.menu_items for update to authenticated
using (private.owns_restaurant(restaurant_id)) with check (private.owns_restaurant(restaurant_id));
create policy menu_items_delete_owner on public.menu_items for delete to authenticated
using (private.owns_restaurant(restaurant_id));

create policy reviews_select_public on public.reviews for select to anon, authenticated
using (
    visibility = 'public'
    and moderation_status = 'visible'
    and (
        (restaurant_id is not null and private.is_restaurant_public(restaurant_id))
        or (dish_id is not null and private.is_dish_public(dish_id))
    )
);
create policy reviews_select_author on public.reviews for select to authenticated
using ((select auth.uid()) = author_account_id);
create policy reviews_insert_customer on public.reviews for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and moderation_status = 'visible'
    and (
        (restaurant_id is not null and private.is_restaurant_public(restaurant_id))
        or (dish_id is not null and private.is_dish_public(dish_id))
    )
);
create policy reviews_update_author on public.reviews for update to authenticated
using ((select auth.uid()) = author_account_id and private.is_active_customer(author_account_id))
with check ((select auth.uid()) = author_account_id and private.is_active_customer(author_account_id));
create policy reviews_delete_author on public.reviews for delete to authenticated
using ((select auth.uid()) = author_account_id);

revoke all on all tables in schema public from public, anon, authenticated;
grant usage on schema public to anon, authenticated, service_role;

grant select on public.restaurants, public.restaurant_opening_periods, public.menus,
    public.dishes, public.allergens, public.dish_allergens, public.menu_items,
    public.reviews, public.restaurant_rating_summaries, public.dish_rating_summaries to anon;

grant select on public.accounts to authenticated;
grant select on public.customer_profiles to authenticated;
grant update (display_name, avatar_path, avatar_alt_text) on public.customer_profiles to authenticated;
grant select, insert, update, delete on public.restaurants, public.restaurant_opening_periods,
    public.menus, public.dishes, public.dish_allergens, public.menu_items to authenticated;
grant select on public.allergens to authenticated;
grant select, delete on public.reviews to authenticated;
grant insert (author_account_id, restaurant_id, dish_id, rating, comment, visibility, visited_at)
    on public.reviews to authenticated;
grant update (rating, comment, visibility, visited_at) on public.reviews to authenticated;
grant select on public.restaurant_rating_summaries, public.dish_rating_summaries to authenticated;

grant all on all tables in schema public to service_role;
grant all on all sequences in schema public to service_role;
