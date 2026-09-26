begin;
select plan(12);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('10000000-0000-4000-8000-000000000301', 'review-author@example.test', '{"account_role":"customer"}', false, false),
    ('10000000-0000-4000-8000-000000000302', 'review-reporter@example.test', '{"account_role":"customer"}', false, false),
    ('20000000-0000-4000-8000-000000000301', 'review-owner@example.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (id, owner_account_id, name, street_line, locality, postal_code, publication_state)
values ('30000000-0000-4000-8000-000000000301', '20000000-0000-4000-8000-000000000301',
        'Reviewed', 'Street 1', 'Madrid', '28001', 'published');
insert into public.dishes (id, restaurant_id, name, is_enabled)
values ('50000000-0000-4000-8000-000000000301', '30000000-0000-4000-8000-000000000301', 'Dish', true);
insert into public.menus (id, restaurant_id, name, publication_state)
values ('40000000-0000-4000-8000-000000000301', '30000000-0000-4000-8000-000000000301', 'Menu', 'published');
insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled)
values ('40000000-0000-4000-8000-000000000301', '50000000-0000-4000-8000-000000000301', 1000, 0, true);

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000301', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select public.save_review('dish', null, '50000000-0000-4000-8000-000000000301',
                          5::smallint, 'Good food', 'public', null);
reset role;

select is((select moderation_status from public.reviews
           where author_account_id = '10000000-0000-4000-8000-000000000301'),
          'hidden', 'new comments wait for moderation');
set local role anon;
select is((select count(*) from public.dish_review_details
           where dish_id = '50000000-0000-4000-8000-000000000301'),
          0::bigint, 'unapproved comments are invisible to the public');
reset role;
select ok(not has_function_privilege('anon', 'public.report_review(uuid,text)', 'EXECUTE'),
          'anonymous users cannot report through the RPC');
select ok(not has_function_privilege('anon', 'public.block_review_author(uuid)', 'EXECUTE'),
          'anonymous users cannot block through the RPC');

update public.reviews set moderation_status = 'visible'
where author_account_id = '10000000-0000-4000-8000-000000000301';
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000302', true);
set local role authenticated;
select is((select count(*) from public.dish_review_details
           where dish_id = '50000000-0000-4000-8000-000000000301'),
          1::bigint, 'another user sees the approved comment');
select lives_ok($$select public.report_review(
    (select id from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000301'),
    'offensive')$$, 'a user can report an approved review');
select lives_ok($$select public.block_review_author(
    (select id from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000301'))$$,
    'a user can block the review author');
select is((select count(*) from public.dish_review_details
           where dish_id = '50000000-0000-4000-8000-000000000301'),
          0::bigint, 'blocked authors disappear from the blocker’s review feed');
reset role;

select is((select count(*) from private.review_reports
           where reporter_account_id = '10000000-0000-4000-8000-000000000302'),
          1::bigint, 'the private report queue stores the report');
select is((select count(*) from private.blocked_review_authors
           where blocker_account_id = '10000000-0000-4000-8000-000000000302'),
          1::bigint, 'the private block list stores the relationship');

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000000301', true);
set local role authenticated;
select throws_ok($$select public.block_review_author(
    (select id from public.reviews where author_account_id = '10000000-0000-4000-8000-000000000301'))$$,
    '42501', null, 'authors cannot block themselves');
select public.save_review('dish', null, '50000000-0000-4000-8000-000000000301',
                          4::smallint, 'Edited comment', 'public', null);
reset role;
select is((select moderation_status from public.reviews
           where author_account_id = '10000000-0000-4000-8000-000000000301'),
          'hidden', 'edited comments return to the moderation queue');

select * from finish();
rollback;
