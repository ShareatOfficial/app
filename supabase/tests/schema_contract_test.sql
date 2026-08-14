begin;
select plan(18);

select is(
    (select count(*) from pg_tables where schemaname = 'public' and tablename in (
        'accounts', 'customer_profiles', 'restaurants', 'restaurant_opening_periods', 'menus',
        'dishes', 'allergens', 'dish_allergens', 'menu_items', 'reviews'
    )),
    10::bigint,
    'all ten public tables exist'
);

select is(
    (select count(*) from pg_tables where schemaname = 'public' and rowsecurity and tablename in (
        'accounts', 'customer_profiles', 'restaurants', 'restaurant_opening_periods', 'menus',
        'dishes', 'allergens', 'dish_allergens', 'menu_items', 'reviews'
    )),
    10::bigint,
    'RLS is enabled on every public table'
);

select is((select count(*) from public.allergens), 14::bigint, 'the fixed EU allergen set is seeded');
select has_view('public', 'restaurant_rating_summaries', 'restaurant rating view exists');
select has_view('public', 'dish_rating_summaries', 'dish rating view exists');
select ok(
    exists (
        select 1 from pg_class
        where oid = 'public.restaurant_rating_summaries'::regclass
          and 'security_invoker=true' = any(coalesce(reloptions, array[]::text[]))
    ),
    'restaurant rating view uses invoker security'
);
select ok(
    exists (
        select 1 from pg_class
        where oid = 'public.dish_rating_summaries'::regclass
          and 'security_invoker=true' = any(coalesce(reloptions, array[]::text[]))
    ),
    'dish rating view uses invoker security'
);

select has_index('public', 'restaurants', 'restaurants_owner_account_id_key', 'one restaurant per owner');
select has_index('public', 'menus', 'menus_restaurant_id_key', 'one menu per restaurant');
select has_index('public', 'menu_items', 'menu_items_menu_id_position_key', 'menu positions are unique');
select has_index('public', 'reviews', 'reviews_author_restaurant_uidx', 'restaurant reviews are unique per author');
select has_index('public', 'reviews', 'reviews_author_dish_uidx', 'dish reviews are unique per author');

select is(
    (
        select count(*)
        from pg_constraint c
        join pg_class t on t.oid = c.conrelid
        join pg_namespace n on n.oid = t.relnamespace
        where n.nspname = 'public' and t.relname = 'reviews' and c.contype = 'c'
    ),
    5::bigint,
    'reviews has target, rating, comment, visibility and moderation checks'
);

select ok(has_table_privilege('anon', 'public.restaurants', 'select'), 'anonymous may select restaurants');
select ok(not has_table_privilege('anon', 'public.restaurants', 'insert'), 'anonymous may not insert restaurants');
select ok(
    has_column_privilege('authenticated', 'public.reviews', 'rating', 'insert'),
    'authenticated role has the allowed review insert columns'
);
select ok(
    not has_column_privilege('authenticated', 'public.accounts', 'role', 'update'),
    'authenticated clients cannot update account roles'
);
select ok(
    not has_column_privilege('authenticated', 'public.reviews', 'moderation_status', 'update'),
    'authenticated clients cannot update moderation status'
);

select * from finish();
rollback;
