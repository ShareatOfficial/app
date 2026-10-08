-- Extension-free smoke test for an explicitly selected staging/local database.
-- Run this entire file in ONE session as postgres (or an equivalent test admin).
-- Do not run in production. It creates only transaction-scoped SQL fixtures and
-- temporary helpers; ROLLBACK removes everything. No Storage HTTP uploads occur.
-- On an error, the transaction is aborted: issue ROLLBACK before reusing the session.
-- Minimal TAP output also keeps this file compatible with `supabase test db`.
begin isolation level repeatable read;
select '1..1';

create temporary table unlisted_smoke_summaries_before as
select 'restaurant'::text as kind, restaurant_id as target_id, average_tenths, rating_count
from public.restaurant_rating_summaries
union all
select 'dish'::text, dish_id, average_tenths, rating_count from public.dish_rating_summaries;

create function pg_temp.unlisted_smoke_check(p_ok boolean, p_message text)
returns void language plpgsql security invoker set search_path = '' as $$
begin
    if p_ok is not true then
        raise exception 'unlisted review smoke: %', p_message;
    end if;
end;
$$;
create function pg_temp.unlisted_smoke_throws(p_sql text, p_sqlstate text, p_message text)
returns void language plpgsql security invoker set search_path = '' as $$
declare
    v_actual text;
begin
    begin
        execute p_sql;
    exception when others then
        get stacked diagnostics v_actual = returned_sqlstate;
    end;
    if v_actual is distinct from p_sqlstate then
        raise exception 'unlisted review smoke: % (expected %, got %)',
            p_message, p_sqlstate, coalesce(v_actual, 'success');
    end if;
end;
$$;
-- GRANT does not resolve the pg_temp alias like qualified function calls do.
-- The temporary table above has already created this session's real namespace.
do $$
declare
    v_temp_schema text;
begin
    select nspname into strict v_temp_schema
    from pg_catalog.pg_namespace where oid = pg_catalog.pg_my_temp_schema();
    execute format('grant usage on schema %I to anon, authenticated', v_temp_schema);
    execute format(
        'grant execute on function %I.unlisted_smoke_check(boolean, text),
         %I.unlisted_smoke_throws(text, text, text) to anon, authenticated',
        v_temp_schema, v_temp_schema);
end;
$$;

do $$
declare
    v_author uuid := gen_random_uuid();
    v_reader uuid := gen_random_uuid();
    v_disabled uuid := gen_random_uuid();
    v_restaurant uuid := gen_random_uuid();
    v_public_id uuid;
    v_private_id uuid;
    v_hidden_id uuid;
    v_removed_id uuid;
    v_rpc text;
    v_count bigint;
    v_account uuid;
begin
    perform pg_temp.unlisted_smoke_check(
        (select relrowsecurity from pg_class where oid = 'public.unlisted_dish_reviews'::regclass),
        'child table must enable RLS');
    perform pg_temp.unlisted_smoke_check(
        (select reloptions @> array['security_invoker=true'] from pg_class
         where oid = 'public.unlisted_dish_review_details'::regclass), 'view must invoke caller RLS');
    perform pg_temp.unlisted_smoke_check(
        not (select prosecdef from pg_proc where oid =
          'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)'::regprocedure),
        'RPC must be security invoker');
    perform pg_temp.unlisted_smoke_check(
        not has_function_privilege('anon',
          'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)', 'EXECUTE')
        and has_function_privilege('authenticated',
          'public.save_unlisted_dish_review(text,text,text,smallint,text,text,timestamptz)', 'EXECUTE'),
        'RPC grants must exclude anonymous callers');
    perform pg_temp.unlisted_smoke_check(
        (select not public and file_size_limit = 512000
                and allowed_mime_types = array['image/jpeg', 'image/png', 'image/webp']::text[]
         from storage.buckets where id = 'review-images'), 'private image bucket configuration');
    perform pg_temp.unlisted_smoke_check(
        not has_function_privilege('anon', 'private.owns_unlisted_review_image(text)', 'EXECUTE'),
        'private object ownership helper is unavailable to anonymous callers');

    -- Some staging snapshots still have the old registration trigger, which omits
    -- the now-required customer_profiles.full_name. Bootstrap through its restaurant
    -- branch (accounts only), then materialize complete customer fixtures explicitly.
    -- This tests review authorization independently of signup without changing or
    -- disabling any trigger. All role/profile writes belong to these random fixtures.
    insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous)
    select id, 'unlisted-smoke-' || id::text || '@example.invalid',
           jsonb_build_object('account_role', 'restaurant'), false, false
    from unnest(array[v_author, v_reader, v_disabled, v_restaurant]) fixture(id);
    update public.accounts set role = 'customer'
    where id in (v_author, v_reader, v_disabled);
    update auth.users set raw_user_meta_data = jsonb_build_object('account_role', 'customer')
    where id in (v_author, v_reader, v_disabled);
    insert into public.customer_profiles (account_id, display_name, full_name)
    select id, 'Smoke customer', 'Smoke customer'
    from unnest(array[v_author, v_reader, v_disabled]) fixture(id);
    update public.accounts set status = 'disabled' where id = v_disabled;

    perform set_config('request.jwt.claim.sub', v_author::text, true);
    perform set_config('request.jwt.claim.role', 'authenticated', true);
    set local role authenticated;
    insert into storage.objects (bucket_id, name, owner_id)
    select 'review-images', v_author::text || '/' || filename, v_author::text
    from unnest(array['public.jpg', 'private.jpg', 'hidden.jpg', 'removed.jpg', 'orphan.jpg']) filename;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 5 from storage.objects
         where bucket_id = 'review-images' and name like v_author::text || '/%'),
        'author can upload and read their own unattached photos');
    perform pg_temp.unlisted_smoke_check(
        private.owns_unlisted_review_image(v_author::text || '/public.jpg')
        and not private.owns_unlisted_review_image(v_author::text || '/missing.jpg'),
        'private helper requires an existing caller-owned image');
    perform pg_temp.unlisted_smoke_throws(format(
        'insert into public.unlisted_dish_reviews
         (review_id,author_account_id,restaurant_name,dish_name,image_path)
         values (%L,%L,%L,%L,%L)', gen_random_uuid(), v_author, 'Direct', 'Direct',
        v_author::text || '/missing.jpg'), '42501',
        'direct target insertion validates ownership without Storage policy recursion');
    perform pg_temp.unlisted_smoke_throws(format(
        'insert into storage.objects (bucket_id,name,owner_id) values (%L,%L,%L)',
        'review-images', v_reader::text || '/intrusion.jpg', v_author::text),
        '42501', 'uploads outside the caller folder are denied');

    v_public_id := public.save_unlisted_dish_review('  Restaurant  ', '  Dish  ',
        v_author::text || '/public.jpg', 5::smallint, '  Delicious  ', 'public', null);
    v_private_id := public.save_unlisted_dish_review('Restaurant', 'Private dish',
        v_author::text || '/private.jpg', 4::smallint, 'Private comment', 'private', null);
    v_hidden_id := public.save_unlisted_dish_review('Restaurant', 'Hidden dish',
        v_author::text || '/hidden.jpg', 3::smallint, 'Pending comment', 'public', null);
    v_removed_id := public.save_unlisted_dish_review('Restaurant', 'Removed dish',
        v_author::text || '/removed.jpg', 2::smallint, 'Removed comment', 'public', null);
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 4 and bool_and(moderation_status = 'hidden')
         from public.unlisted_dish_review_details where author_account_id = v_author),
        'all submitted comments are pending moderation and readable by their author');
    perform pg_temp.unlisted_smoke_check(
        (select restaurant_name = 'Restaurant' and dish_name = 'Dish' and comment = 'Delicious'
         from public.unlisted_dish_review_details where id = v_public_id), 'RPC trims required text');

    v_rpc := format('select public.save_unlisted_dish_review(%L,%L,%L,5::smallint,%L,%L,null)',
                    'Restaurant', 'Dish', v_author::text || '/public.jpg', 'Comment', 'public');
    perform pg_temp.unlisted_smoke_throws(v_rpc, '23505', 'a photo cannot belong to two reviews');
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 4 from public.reviews where author_account_id = v_author),
        'a child insertion failure rolls back its parent');
    perform pg_temp.unlisted_smoke_throws(format(
        'select public.save_unlisted_dish_review(%L,%L,%L,5::smallint,null,%L,null)',
        'Restaurant', 'Dish', v_author::text || '/orphan.jpg', 'public'),
        '22023', 'comment is required');
    perform pg_temp.unlisted_smoke_throws(format(
        'select public.save_unlisted_dish_review(%L,%L,%L,5::smallint,%L,%L,null)',
        'Restaurant', 'Dish', v_author::text || '/missing.jpg', 'Comment', 'public'),
        '22023', 'image object must exist');
    perform pg_temp.unlisted_smoke_throws(format(
        'update public.reviews set comment = null where id = %L', v_public_id),
        '23514', 'direct writes cannot remove the mandatory comment');
    perform pg_temp.unlisted_smoke_throws(format(
        'update public.reviews set moderation_status = %L where id = %L', 'visible', v_public_id),
        '42501', 'authors cannot approve their own reviews');
    set constraints reviews_have_a_target immediate;
    perform pg_temp.unlisted_smoke_throws(format(
        'insert into public.reviews(author_account_id,target_type,rating,comment,visibility)
         values (%L,%L,5,%L,%L)', v_author, 'unlisted_dish', 'No child', 'public'),
        '23514', 'an unlisted parent without its child cannot commit');
    set constraints reviews_have_a_target deferred;
    reset role;

    perform set_config('request.jwt.claim.sub', '', true);
    perform set_config('request.jwt.claim.role', 'anon', true);
    set local role anon;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 0 from public.unlisted_dish_reviews where author_account_id = v_author),
        'anonymous callers cannot read pending target details');
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 0 from storage.objects
         where bucket_id = 'review-images' and name like v_author::text || '/%'),
        'anonymous callers cannot read pending or orphan images');
    perform pg_temp.unlisted_smoke_throws(v_rpc, '42501', 'anonymous callers cannot save');
    reset role;

    update public.reviews set moderation_status = 'visible' where id in (v_public_id, v_private_id);
    update public.reviews set moderation_status = 'removed' where id = v_removed_id;
    perform pg_temp.unlisted_smoke_check(not exists (
        (select 'restaurant'::text, restaurant_id, average_tenths, rating_count
         from public.restaurant_rating_summaries
         union all select 'dish'::text, dish_id, average_tenths, rating_count from public.dish_rating_summaries
         except select * from unlisted_smoke_summaries_before)
        union all
        (select * from unlisted_smoke_summaries_before
         except (select 'restaurant'::text, restaurant_id, average_tenths, rating_count
                 from public.restaurant_rating_summaries
                 union all select 'dish'::text, dish_id, average_tenths, rating_count
                 from public.dish_rating_summaries))
    ), 'approved unlisted reviews leave official rating summaries unchanged');
    set local role anon;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 1 and bool_and(id = v_public_id)
         from public.unlisted_dish_review_details where author_account_id = v_author),
        'anonymous visibility requires public and approved');
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 1 and bool_and(name = v_author::text || '/public.jpg')
         from storage.objects where bucket_id = 'review-images' and name like v_author::text || '/%'),
        'only the public approved photo is readable anonymously');
    reset role;

    perform set_config('request.jwt.claim.sub', v_reader::text, true);
    perform set_config('request.jwt.claim.role', 'authenticated', true);
    set local role authenticated;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 1 from public.unlisted_dish_review_details where author_account_id = v_author),
        'another customer cannot read private, pending or removed reviews');
    perform pg_temp.unlisted_smoke_check(
        not private.owns_unlisted_review_image(v_author::text || '/public.jpg'),
        'the helper never treats another authors public image as caller-owned');
    perform pg_temp.unlisted_smoke_throws(v_rpc, '22023', 'another customer cannot attach the author photo');
    update public.reviews set rating = 1 where id = v_public_id;
    get diagnostics v_count = row_count;
    perform pg_temp.unlisted_smoke_check(v_count = 0, 'another customer cannot edit the review');
    perform set_config('storage.allow_delete_query', 'true', true);
    delete from storage.objects where bucket_id = 'review-images' and name = v_author::text || '/public.jpg';
    get diagnostics v_count = row_count;
    perform pg_temp.unlisted_smoke_check(v_count = 0, 'another customer cannot delete the photo');
    perform public.block_review_author(v_public_id);
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 0 from public.unlisted_dish_review_details where author_account_id = v_author),
        'blocked author details are hidden');
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 0 from storage.objects
         where bucket_id = 'review-images' and name like v_author::text || '/%'),
        'blocked author photos are hidden');
    reset role;

    foreach v_account in array array[v_disabled, v_restaurant] loop
        perform set_config('request.jwt.claim.sub', v_account::text, true);
        set local role authenticated;
        perform pg_temp.unlisted_smoke_throws(v_rpc, '42501', 'only active customers can save');
        reset role;
    end loop;

    perform set_config('request.jwt.claim.sub', v_author::text, true);
    set local role authenticated;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 4 from public.unlisted_dish_review_details where author_account_id = v_author),
        'author retains access to all moderation and visibility states');
    update public.reviews set comment = 'Edited comment' where id = v_public_id;
    perform pg_temp.unlisted_smoke_check(
        (select moderation_status = 'hidden' from public.reviews where id = v_public_id),
        'editing an approved comment returns it to moderation');
    update public.reviews set comment = 'Edited removed comment' where id = v_removed_id;
    perform pg_temp.unlisted_smoke_check(
        (select moderation_status = 'removed' from public.reviews where id = v_removed_id),
        'editing does not restore removed reviews');
    delete from storage.objects where bucket_id = 'review-images' and name = v_author::text || '/orphan.jpg';
    get diagnostics v_count = row_count;
    perform pg_temp.unlisted_smoke_check(v_count = 1, 'author can clean up their unattached image');
    delete from public.reviews where author_account_id = v_author;
    reset role;
    perform pg_temp.unlisted_smoke_check(
        (select count(*) = 0 from public.unlisted_dish_reviews where author_account_id = v_author),
        'deleting the parent cascades to its target');
end;
$$;

select 'ok 1 - unlisted dish review SQL smoke contract';
rollback;
