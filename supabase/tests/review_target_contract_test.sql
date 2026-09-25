begin;
select plan(11);

select hasnt_column('public', 'reviews', 'restaurant_id', 'the review target lives in its own table');
select hasnt_column('public', 'reviews', 'dish_id', 'the review target lives in its own table');
select has_column('public', 'reviews', 'target_type', 'the parent records which kind of target it has');
select has_view('public', 'restaurant_review_details', 'restaurant reviews are readable as one row');
select has_view('public', 'dish_review_details', 'dish reviews are readable as one row');

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('10000000-0000-4000-8000-000000000201', 'review-customer@example.test', '{"account_role":"customer","display_name":"Customer"}', false, false),
    ('20000000-0000-4000-8000-000000000201', 'review-owner@example.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (
    id, owner_account_id, name, street_line, locality, postal_code, publication_state
) values
    ('30000000-0000-4000-8000-000000000201', '20000000-0000-4000-8000-000000000201', 'Reviewed', 'Street 1', 'Madrid', '28001', 'published');

insert into public.dishes (id, restaurant_id, name, is_enabled) values
    ('50000000-0000-4000-8000-000000000201', '30000000-0000-4000-8000-000000000201', 'Reviewed dish', true);
insert into public.menus (id, restaurant_id, name, publication_state) values
    ('40000000-0000-4000-8000-000000000201', '30000000-0000-4000-8000-000000000201', 'Carta', 'published');
insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled) values
    ('40000000-0000-4000-8000-000000000201', '50000000-0000-4000-8000-000000000201', 1000, 0, true);

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000201', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;

select lives_ok(
    $$select public.save_review('restaurant', '30000000-0000-4000-8000-000000000201', null, 5::smallint, 'Great', 'public', null)$$,
    'a restaurant review writes the parent and its restaurant row'
);
select lives_ok(
    $$select public.save_review('dish', null, '50000000-0000-4000-8000-000000000201', 4::smallint, 'Tasty', 'public', null)$$,
    'a dish review writes the parent and its dish row'
);
select is(
    (select count(*) from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000201'),
    2::bigint,
    'the author has one review per target'
);

select lives_ok(
    $$select public.save_review('restaurant', '30000000-0000-4000-8000-000000000201', null, 2::smallint, 'Changed my mind', 'public', null)$$,
    'saving the same target updates the review instead of creating a second one'
);
select is(
    (select count(*) from public.restaurant_reviews where author_account_id = '10000000-0000-4000-8000-000000000201'),
    1::bigint,
    'one review per author and restaurant stays a unique constraint'
);

set constraints reviews_have_a_target immediate;
select throws_ok(
    $$insert into public.reviews (author_account_id, target_type, rating, visibility)
      values ('10000000-0000-4000-8000-000000000201', 'dish', 3, 'public')$$,
    '23514',
    null,
    'a parent review without a target row never commits'
);

reset role;
select * from finish();
rollback;
