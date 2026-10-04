begin;
select no_plan();

create temporary table unlisted_summary_before as
select 'restaurant'::text as kind, restaurant_id as target_id, average_tenths, rating_count
from public.restaurant_rating_summaries
union all
select 'dish'::text, dish_id, average_tenths, rating_count from public.dish_rating_summaries;

select has_table('public', 'unlisted_dish_reviews', 'unlisted targets have their own table');
select col_is_pk('public', 'unlisted_dish_reviews', 'review_id', 'one child per review');
select col_is_unique('public', 'unlisted_dish_reviews', 'image_path', 'each review owns a distinct photo');
select fk_ok('public', 'unlisted_dish_reviews', array['review_id', 'author_account_id', 'target_type'],
             'public', 'reviews', array['id', 'author_account_id', 'target_type'],
             'the parent fixes the child author and target type');
select ok((select relrowsecurity from pg_class where oid = 'public.unlisted_dish_reviews'::regclass),
          'the exposed child table has RLS');
select has_view('public', 'unlisted_dish_review_details', 'the flattened read surface exists');
select ok((select reloptions @> array['security_invoker=true'] from pg_class
           where oid = 'public.unlisted_dish_review_details'::regclass), 'the view respects caller RLS');
select columns_are('public', 'unlisted_dish_review_details', array[
    'id', 'author_account_id', 'restaurant_name', 'dish_name', 'image_path', 'rating', 'comment',
    'visibility', 'moderation_status', 'visited_at', 'created_at', 'updated_at'
], 'the read surface has the agreed columns');
select ok(not (select prosecdef from pg_proc where oid =
    'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)'::regprocedure),
    'the save RPC is security invoker');
select ok(not has_function_privilege('anon',
    'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)', 'EXECUTE'),
    'anonymous users cannot call the save RPC');
select ok(has_function_privilege('authenticated',
    'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)', 'EXECUTE'),
    'authenticated users can call the save RPC');
select ok(not has_function_privilege('anon', 'private.owns_unlisted_review_image(text)', 'EXECUTE'),
          'anonymous callers cannot use the private object ownership helper');
select ok(has_table_privilege('anon', 'public.unlisted_dish_review_details', 'SELECT'),
          'anonymous users can query the RLS-protected view');
select ok(not has_table_privilege('anon', 'public.unlisted_dish_reviews', 'INSERT'),
          'anonymous users cannot insert a target');
select ok(not has_table_privilege('authenticated', 'public.unlisted_dish_reviews', 'UPDATE'),
          'clients cannot rewrite approved names or images outside moderation');
select ok(not has_table_privilege('authenticated', 'public.unlisted_dish_reviews', 'DELETE'),
          'clients delete the parent instead of leaving an orphan');
select is((select public from storage.buckets where id = 'review-images'), false,
          'review images are in a private bucket');
select is((select file_size_limit from storage.buckets where id = 'review-images'), 512000::bigint,
          'review images are limited to 500 KB');
select is((select allowed_mime_types from storage.buckets where id = 'review-images'),
          array['image/jpeg', 'image/png', 'image/webp']::text[], 'only JPEG, PNG and WebP are allowed');

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('10000000-0000-4000-8000-000000001041', 'unlisted-author@example.test', '{"account_role":"customer"}', false, false),
    ('10000000-0000-4000-8000-000000001042', 'unlisted-other@example.test', '{"account_role":"customer"}', false, false),
    ('10000000-0000-4000-8000-000000001043', 'unlisted-disabled@example.test', '{"account_role":"customer"}', false, false),
    ('10000000-0000-4000-8000-000000001044', 'unlisted-deleting@example.test', '{"account_role":"customer"}', false, false),
    ('20000000-0000-4000-8000-000000001041', 'unlisted-restaurant@example.test', '{"account_role":"restaurant"}', false, false);
update public.accounts set status = 'disabled' where id = '10000000-0000-4000-8000-000000001043';
update public.accounts set status = 'deletion_pending' where id = '10000000-0000-4000-8000-000000001044';

-- A test helper keeps invalid-input cases focused; it preserves the caller's privileges.
create function pg_temp.save_unlisted(
    p_restaurant_name text default '  Outside restaurant  ',
    p_dish_name text default '  Outside dish  ',
    p_image_path text default '10000000-0000-4000-8000-000000001041/public.jpg',
    p_rating smallint default 5,
    p_comment text default '  Delicious  ',
    p_visibility text default 'public',
    p_visited_at timestamptz default '2026-10-01T12:00:00Z'
) returns uuid language sql security invoker as $$
    select public.save_unlisted_dish_review(p_restaurant_name, p_dish_name, p_image_path,
                                           p_rating, p_comment, p_visibility, p_visited_at);
$$;
create function pg_temp.affected_rows(p_sql text)
returns bigint language plpgsql security invoker as $$
declare
    v_count bigint;
begin
    execute p_sql;
    get diagnostics v_count = row_count;
    return v_count;
end;
$$;
do $$
declare
    v_temp_schema text;
begin
    select nspname into strict v_temp_schema
    from pg_catalog.pg_namespace where oid = pg_catalog.pg_my_temp_schema();
    execute format('grant usage on schema %I to anon, authenticated', v_temp_schema);
end;
$$;

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000001041', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select lives_ok($$insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '10000000-0000-4000-8000-000000001041/public.jpg', '10000000-0000-4000-8000-000000001041'),
    ('review-images', '10000000-0000-4000-8000-000000001041/private.jpg', '10000000-0000-4000-8000-000000001041'),
    ('review-images', '10000000-0000-4000-8000-000000001041/hidden.jpg', '10000000-0000-4000-8000-000000001041'),
    ('review-images', '10000000-0000-4000-8000-000000001041/removed.jpg', '10000000-0000-4000-8000-000000001041'),
    ('review-images', '10000000-0000-4000-8000-000000001041/orphan.jpg', '10000000-0000-4000-8000-000000001041')$$,
    'active customers can upload their review photos');
select is((select count(*) from storage.objects where bucket_id = 'review-images'), 5::bigint,
          'authors can read uploaded photos before attaching a review');
select ok(private.owns_unlisted_review_image('10000000-0000-4000-8000-000000001041/public.jpg'),
          'the private helper recognizes an existing object owned by the caller');
select ok(not private.owns_unlisted_review_image('10000000-0000-4000-8000-000000001041/missing.jpg'),
          'the private helper requires an existing object');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            'Direct', 'Direct', '10000000-0000-4000-8000-000000001041/missing.jpg')$$,
    '42501', null, 'direct inserts also reject nonexistent objects without RLS recursion');
select throws_ok($$insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '10000000-0000-4000-8000-000000001042/intrusion.jpg', '10000000-0000-4000-8000-000000001041')$$,
    '42501', null, 'customers cannot upload in another account folder');
select throws_ok($$insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '10000000-0000-4000-8000-000000001041/forged.jpg', '10000000-0000-4000-8000-000000001042')$$,
    '42501', null, 'the object owner cannot be forged');
select lives_ok($$select pg_temp.save_unlisted()$$, 'a customer saves both review rows atomically');
select is((select count(*) from public.unlisted_dish_review_details), 1::bigint,
          'authors can read their own pending review');
select results_eq($$select restaurant_name, dish_name, comment, moderation_status
                    from public.unlisted_dish_review_details$$,
                  $$values ('Outside restaurant'::text, 'Outside dish'::text, 'Delicious'::text, 'hidden'::text)$$,
                  'names and comment are trimmed and sent to moderation');
select is((select visited_at from public.unlisted_dish_review_details), '2026-10-01T12:00:00Z'::timestamptz,
          'the visit date is preserved');
select is((select rating from public.unlisted_dish_review_details), 5::smallint, 'the rating is preserved');
select throws_ok($$update public.reviews set moderation_status = 'visible' where target_type = 'unlisted_dish'$$,
                 '42501', null, 'authors cannot approve their own review');
select throws_ok($$update public.reviews set comment = null where target_type = 'unlisted_dish'$$,
                 '23514', null, 'authors cannot clear the mandatory comment to bypass moderation');
select throws_ok($$update public.reviews set comment = repeat('x', 2001) where target_type = 'unlisted_dish'$$,
                 '23514', null, 'direct updates also enforce the comment limit');

select throws_ok(format('select pg_temp.save_unlisted(p_restaurant_name => %L)', invalid_name),
                 '22023', null, description)
from (values (null::text, 'restaurant name is mandatory'), ('   ', 'blank restaurant names are rejected'),
             (repeat('r', 121), 'restaurant names are limited to 120 characters')) v(invalid_name, description);
select throws_ok(format('select pg_temp.save_unlisted(p_dish_name => %L)', invalid_name),
                 '22023', null, description)
from (values (null::text, 'dish name is mandatory'), ('   ', 'blank dish names are rejected'),
             (repeat('d', 121), 'dish names are limited to 120 characters')) v(invalid_name, description);
select throws_ok(format('select pg_temp.save_unlisted(p_comment => %L)', invalid_comment),
                 '22023', null, description)
from (values (null::text, 'comment is mandatory'), ('   ', 'blank comments are rejected'),
             (repeat('c', 2001), 'comments are limited to 2000 characters')) v(invalid_comment, description);
select throws_ok(format('select pg_temp.save_unlisted(p_image_path => %L)', invalid_path),
                 '22023', null, description)
from (values (null::text, 'photo is mandatory'), ('', 'blank image paths are rejected'),
             ('10000000-0000-4000-8000-000000001041/missing.jpg', 'the photo must already exist'),
             ('https://example.test/image.jpg', 'external image URLs are rejected'),
             (repeat('x', 1025), 'image paths are limited to 1024 characters')) v(invalid_path, description);
select throws_ok($$select pg_temp.save_unlisted(p_rating => 0::smallint)$$,
                 '23514', null, 'ratings below one are rejected');
select throws_ok($$select pg_temp.save_unlisted(p_rating => 6::smallint)$$,
                 '23514', null, 'ratings above five are rejected');
select throws_ok($$select pg_temp.save_unlisted(p_rating => null)$$,
                 '23502', null, 'rating is mandatory');
select throws_ok($$select pg_temp.save_unlisted(p_visibility => 'friends')$$,
                 '23514', null, 'unsupported visibility is rejected');
select throws_ok($$select pg_temp.save_unlisted()$$, '23505', null,
                 'the same photo cannot be shared by two reviews');
select is((select count(*) from public.reviews where target_type = 'unlisted_dish'), 1::bigint,
          'validation and child insertion failures roll back the whole submission');
select lives_ok($$select pg_temp.save_unlisted(p_dish_name => 'Private dish',
    p_image_path => '10000000-0000-4000-8000-000000001041/private.jpg', p_visibility => 'private')$$,
    'private reviews are supported');
select lives_ok($$select pg_temp.save_unlisted(p_dish_name => 'Hidden dish',
    p_image_path => '10000000-0000-4000-8000-000000001041/hidden.jpg')$$, 'a second unlisted dish is independent');
select lives_ok($$select pg_temp.save_unlisted(p_dish_name => 'Removed dish',
    p_image_path => '10000000-0000-4000-8000-000000001041/removed.jpg')$$, 'a third unlisted dish is independent');
set constraints reviews_have_a_target immediate;
select throws_ok($$insert into public.reviews (author_account_id, target_type, rating, comment, visibility)
    values ('10000000-0000-4000-8000-000000001041', 'unlisted_dish', 5, 'Orphan', 'public')$$,
    '23514', null, 'the deferred invariant rejects an unlisted parent without its child');
set constraints reviews_have_a_target deferred;
reset role;

-- A deliberately mismatched owner verifies the RPC does not trust just the folder.
insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '10000000-0000-4000-8000-000000001041/mismatched.jpg', '10000000-0000-4000-8000-000000001042'),
    ('review-images', '10000000-0000-4000-8000-000000001042/theirs.jpg', '10000000-0000-4000-8000-000000001042');
set local role authenticated;
select ok(not private.owns_unlisted_review_image('10000000-0000-4000-8000-000000001041/mismatched.jpg'),
          'the private helper checks object owner as well as folder');
select throws_ok($$select pg_temp.save_unlisted(
    p_image_path => '10000000-0000-4000-8000-000000001041/mismatched.jpg')$$,
    '22023', null, 'a folder match without object ownership is insufficient');
select throws_ok($$select pg_temp.save_unlisted(
    p_image_path => '10000000-0000-4000-8000-000000001042/theirs.jpg')$$,
    '22023', null, 'a customer cannot attach another customer photo');
reset role;

select set_config('request.jwt.claim.sub', '', true);
select set_config('request.jwt.claim.role', 'anon', true);
set local role anon;
select is((select count(*) from public.unlisted_dish_reviews), 0::bigint,
          'pending target names and image paths are hidden from anonymous users');
select is((select count(*) from public.unlisted_dish_review_details), 0::bigint,
          'pending reviews are hidden from anonymous users');
select is((select count(*) from storage.objects where bucket_id = 'review-images'), 0::bigint,
          'anonymous users cannot sign pending or unattached photos');
select throws_ok($$select pg_temp.save_unlisted()$$, '42501', null, 'anonymous saving is denied');
reset role;

update public.reviews set moderation_status = 'visible'
where id in (select review_id from public.unlisted_dish_reviews
             where dish_name in ('Outside dish', 'Private dish'));
update public.reviews set moderation_status = 'removed'
where id in (select review_id from public.unlisted_dish_reviews where dish_name = 'Removed dish');
select results_eq($$select 'restaurant'::text as kind, restaurant_id as target_id, average_tenths, rating_count
                    from public.restaurant_rating_summaries
                    union all
                    select 'dish'::text, dish_id, average_tenths, rating_count from public.dish_rating_summaries
                    order by kind, target_id$$,
                  $$select * from unlisted_summary_before order by kind, target_id$$,
                  'public approved unlisted reviews do not affect official rating summaries');
set local role anon;
select results_eq($$select dish_name from public.unlisted_dish_review_details$$,
                  $$values ('Outside dish'::text)$$, 'only public approved reviews are visible anonymously');
select results_eq($$select name from storage.objects where bucket_id = 'review-images'$$,
                  $$values ('10000000-0000-4000-8000-000000001041/public.jpg'::text)$$,
                  'only the public approved photo can be signed anonymously');
reset role;

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000001042', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select is((select count(*) from public.unlisted_dish_review_details), 1::bigint,
          'another customer cannot see private, hidden or removed reviews');
select ok(not private.owns_unlisted_review_image('10000000-0000-4000-8000-000000001041/public.jpg'),
          'a public image is not considered owned by its reader');
select results_eq($$select name from storage.objects where bucket_id = 'review-images' order by name$$,
                  $$values ('10000000-0000-4000-8000-000000001041/public.jpg'::text),
                           ('10000000-0000-4000-8000-000000001042/theirs.jpg'::text)$$,
                  'another customer can sign a public approved image and their own image');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001042',
            'Forged', 'Forged', '10000000-0000-4000-8000-000000001041/public.jpg')$$,
    '42501', null, 'a direct child insert cannot link a different author folder');
select is(pg_temp.affected_rows($$update public.reviews set rating = 1 where target_type = 'unlisted_dish'$$),
          0::bigint, 'other customers cannot edit the reviews');
select set_config('storage.allow_delete_query', 'true', true);
select is(pg_temp.affected_rows($$delete from storage.objects where bucket_id = 'review-images'
           and name = '10000000-0000-4000-8000-000000001041/public.jpg'$$),
          0::bigint, 'other customers cannot delete public review photos');
select lives_ok($$select public.block_review_author((select id from public.unlisted_dish_review_details))$$,
               'the existing blocking RPC supports unlisted reviews');
select is((select count(*) from public.unlisted_dish_review_details), 0::bigint,
          'blocking also hides unlisted review details');
select is((select count(*) from storage.objects where bucket_id = 'review-images'
           and name = '10000000-0000-4000-8000-000000001041/public.jpg'), 0::bigint,
          'blocked authors images cannot be signed by the blocker');
reset role;

-- Role/status authorization is checked even before any input or object validation.
select set_config('request.jwt.claim.sub', '20000000-0000-4000-8000-000000001041', true);
set local role authenticated;
select throws_ok($$select pg_temp.save_unlisted()$$, '42501', null, 'restaurant accounts cannot review');
select throws_ok($$insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '20000000-0000-4000-8000-000000001041/photo.jpg', '20000000-0000-4000-8000-000000001041')$$,
    '42501', null, 'restaurant accounts cannot upload review photos');
reset role;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000001043', true);
set local role authenticated;
select throws_ok($$select pg_temp.save_unlisted()$$, '42501', null, 'disabled customers cannot review');
select throws_ok($$insert into storage.objects (bucket_id, name, owner_id) values
    ('review-images', '10000000-0000-4000-8000-000000001043/photo.jpg', '10000000-0000-4000-8000-000000001043')$$,
    '42501', null, 'disabled customers cannot upload review photos');
reset role;
select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000001044', true);
set local role authenticated;
select throws_ok($$select pg_temp.save_unlisted()$$, '42501', null, 'customers pending deletion cannot review');
reset role;
select set_config('request.jwt.claim.sub', '', true);
set local role authenticated;
select throws_ok($$select pg_temp.save_unlisted()$$, '42501', null, 'a valid authenticated subject is required');
reset role;

select set_config('request.jwt.claim.sub', '10000000-0000-4000-8000-000000001041', true);
set local role authenticated;
select is((select count(*) from public.unlisted_dish_review_details), 4::bigint,
          'the author can read public, private, hidden and removed reviews');
select is((select count(*) from storage.objects where bucket_id = 'review-images'), 5::bigint,
          'the author can sign all of their own photos regardless of moderation');
select is(pg_temp.affected_rows($$update storage.objects set name = '10000000-0000-4000-8000-000000001041/replaced.jpg'
           where bucket_id = 'review-images'$$), 0::bigint,
          'photos cannot be overwritten after review approval');
select lives_ok($$update public.reviews set comment = 'Edited comment'
    where id = (select id from public.unlisted_dish_review_details where dish_name = 'Outside dish')$$,
    'authors can edit the parent comment through its existing policy');
select is((select moderation_status from public.unlisted_dish_review_details where dish_name = 'Outside dish'),
          'hidden', 'editing an approved comment returns it to moderation');
select lives_ok($$update public.reviews set comment = 'Edited removed comment'
    where id = (select id from public.unlisted_dish_review_details where dish_name = 'Removed dish')$$,
    'the parent update contract is preserved for removed reviews');
select is((select moderation_status from public.unlisted_dish_review_details where dish_name = 'Removed dish'),
          'removed', 'editing never republishes a removed review');
select is(pg_temp.affected_rows($$delete from storage.objects where bucket_id = 'review-images'
           and name = '10000000-0000-4000-8000-000000001041/orphan.jpg'$$),
          1::bigint, 'authors can clean up an unattached upload');
select lives_ok($$delete from public.reviews where target_type = 'unlisted_dish'$$,
               'authors delete unlisted reviews through the parent');
reset role;
select is((select count(*) from public.unlisted_dish_reviews), 0::bigint,
          'deleting parent reviews cascades to their unlisted targets');

-- Table constraints also protect trusted/direct SQL writes independently of the RPC.
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            '', 'Dish', '10000000-0000-4000-8000-000000001041/public.jpg')$$,
    '23514', null, 'table constraints reject blank restaurant names');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            'Restaurant', repeat('d', 121), '10000000-0000-4000-8000-000000001041/public.jpg')$$,
    '23514', null, 'table constraints enforce dish name length');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            'Restaurant', 'Dish', null)$$,
    '23502', null, 'table constraints require an image path');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            'Restaurant', 'Dish', '10000000-0000-4000-8000-000000001042/theirs.jpg')$$,
    '23514', null, 'table constraints keep image paths under their authors folder');
select throws_ok($$insert into public.unlisted_dish_reviews
    (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values ('90000000-0000-4000-8000-000000001041', '10000000-0000-4000-8000-000000001041',
            'Restaurant', 'Dish', '10000000-0000-4000-8000-000000001041/public.jpg')$$,
    '23503', null, 'child rows require their matching parent');

select * from finish();
rollback;
