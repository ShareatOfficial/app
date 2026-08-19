begin;
select plan(27);

select throws_ok(
    $$insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values ('00000000-0000-4000-8000-000000000099', 'invalid@example.test', '{"account_role":"admin"}', false, false)$$,
    '22023'
);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('10000000-0000-4000-8000-000000000001', 'customer@example.test', '{"account_role":"customer","display_name":"Customer"}', false, false),
    ('20000000-0000-4000-8000-000000000001', 'owner@example.test', '{"account_role":"restaurant"}', false, false),
    ('20000000-0000-4000-8000-000000000002', 'publisher@example.test', '{"account_role":"restaurant"}', false, false),
    ('20000000-0000-4000-8000-000000000003', 'other@example.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (
    id, owner_account_id, name, street_line, locality, postal_code, publication_state
) values
    ('30000000-0000-4000-8000-000000000001', '20000000-0000-4000-8000-000000000002', 'Published', 'Street 1', 'Madrid', '28001', 'published'),
    ('30000000-0000-4000-8000-000000000002', '20000000-0000-4000-8000-000000000001', 'Draft', 'Street 2', 'Madrid', '28002', 'draft'),
    ('30000000-0000-4000-8000-000000000003', '20000000-0000-4000-8000-000000000003', 'Other draft', 'Street 3', 'Madrid', '28003', 'draft');

insert into public.menus (id, restaurant_id, name, publication_state) values
    ('40000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 'Published menu', 'published'),
    ('40000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000003', 'Other draft menu', 'draft');
insert into public.dishes (id, restaurant_id, name, is_enabled) values
    ('50000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 'Visible dish', true),
    ('50000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000003', 'Other dish', true);
insert into public.menu_items (menu_id, dish_id, restaurant_id, price_minor_units, position) values
    ('40000000-0000-4000-8000-000000000001', '50000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 1200, 0);

select is(
    (select role from public.accounts where id = '10000000-0000-4000-8000-000000000001'),
    'customer',
    'customer registration persists the validated role'
);
select is(
    (select display_name from public.customer_profiles where account_id = '10000000-0000-4000-8000-000000000001'),
    'Customer',
    'customer registration creates its profile'
);
select is(
    (select role from public.accounts where id = '20000000-0000-4000-8000-000000000001'),
    'restaurant',
    'restaurant registration persists the validated role'
);
select is(
    (select count(*) from public.customer_profiles where account_id = '20000000-0000-4000-8000-000000000001'),
    0::bigint,
    'restaurant registration does not create a customer profile'
);
select throws_ok(
    $$insert into public.restaurants (owner_account_id, name, street_line, locality, postal_code) values ('20000000-0000-4000-8000-000000000001', 'Duplicate', 'Street', 'Madrid', '28001')$$,
    '23505'
);
select throws_ok(
    $$insert into public.menus (restaurant_id, name) values ('30000000-0000-4000-8000-000000000001', 'Duplicate menu')$$,
    '23505'
);
select throws_ok(
    $$insert into public.menu_items (menu_id, dish_id, restaurant_id, price_minor_units, position) values ('40000000-0000-4000-8000-000000000001', '50000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001', 900, 1)$$,
    '23503'
);

set local role anon;
select is(
    (select count(*) from public.restaurants where id::text like '30000000-%'),
    1::bigint,
    'anonymous sees only published restaurants'
);
select is(
    (select count(*) from public.menus where id::text like '40000000-%'),
    1::bigint,
    'anonymous sees only the published menu'
);
select is(
    (select count(*) from public.dishes where id::text like '50000000-%'),
    1::bigint,
    'anonymous sees only enabled dishes on public menus'
);
reset role;

select set_config('request.jwt.claim.sub', '20000000-0000-4000-8000-000000000001', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select is(
    (select count(*) from public.restaurants where id::text like '30000000-%'),
    2::bigint,
    'owner sees public and owned draft restaurants'
);
select is((select count(*) from public.restaurants where id = '30000000-0000-4000-8000-000000000003'), 0::bigint, 'owner cannot see another owner draft');
select lives_ok(
    $$update public.restaurants set name = 'Stolen' where id = '30000000-0000-4000-8000-000000000003' returning id$$,
    'cross-owner update is safely filtered by RLS'
);
reset role;
select is(
    (select name from public.restaurants where id = '30000000-0000-4000-8000-000000000003'),
    'Other draft',
    'cross-owner update did not change the row'
);

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000001', true);
set local role authenticated;
select lives_ok(
    $$insert into public.reviews (author_account_id, restaurant_id, rating, comment) values ('10000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 5, 'Excellent')$$,
    'customer may review a published restaurant'
);
select lives_ok(
    $$update public.reviews set rating = 4 where author_account_id = '10000000-0000-4000-8000-000000000001' and restaurant_id = '30000000-0000-4000-8000-000000000001'$$,
    'customer may update their review'
);
select throws_ok(
    $$update public.accounts set role = 'restaurant' where id = '10000000-0000-4000-8000-000000000001'$$,
    '42501'
);
select throws_ok(
    $$update public.reviews set moderation_status = 'hidden' where author_account_id = '10000000-0000-4000-8000-000000000001'$$,
    '42501'
);
select lives_ok(
    $$insert into public.reviews (author_account_id, dish_id, rating, visibility) values ('10000000-0000-4000-8000-000000000001', '50000000-0000-4000-8000-000000000001', 4, 'private')$$,
    'customer may create a private dish review'
);
reset role;

set local role anon;
select is(
    (select count(*) from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000001'),
    1::bigint,
    'anonymous cannot see private reviews'
);
select is((select rating_count from public.restaurant_rating_summaries where restaurant_id = '30000000-0000-4000-8000-000000000001'), 1::bigint, 'restaurant rating aggregate includes public reviews');
select is((select count(*) from public.dish_rating_summaries where dish_id = '50000000-0000-4000-8000-000000000001'), 0::bigint, 'dish aggregate excludes private reviews');
reset role;

select set_config('request.jwt.claim.sub', '20000000-0000-4000-8000-000000000001', true);
set local role authenticated;
select throws_ok(
    $$insert into public.reviews (author_account_id, restaurant_id, rating) values ('20000000-0000-4000-8000-000000000001', '30000000-0000-4000-8000-000000000001', 5)$$,
    '42501'
);
select throws_ok(
    $$insert into public.menu_items (menu_id, dish_id, restaurant_id, price_minor_units, position) values ('40000000-0000-4000-8000-000000000001', '50000000-0000-4000-8000-000000000002', '30000000-0000-4000-8000-000000000001', 900, 1)$$,
    '42501'
);
select lives_ok(
    $$delete from public.reviews where id in (select id from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000001')$$,
    'restaurant cannot delete customer reviews and the no-op does not leak data'
);
reset role;

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000001', true);
set local role authenticated;
select lives_ok(
    $$delete from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000001'$$,
    'customer may delete their own reviews'
);
reset role;

select * from finish();
rollback;
