begin;
select plan(10);

select is((select file_size_limit from storage.buckets where id = 'avatars'), 512000::bigint, 'avatar limit is 500 KB');
select is((select public from storage.buckets where id = 'avatars'), false, 'avatars are private');
select is((select public from storage.buckets where id = 'restaurant-images'), true, 'restaurant images are public');
select is((select public from storage.buckets where id = 'dish-images'), true, 'dish images are public');
select is(
    (select allowed_mime_types from storage.buckets where id = 'avatars'),
    array['image/jpeg', 'image/png', 'image/webp']::text[],
    'image MIME allowlist is configured'
);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('60000000-0000-4000-8000-000000000001', 'avatar@example.test', '{"account_role":"customer","display_name":"Avatar"}', false, false),
    ('70000000-0000-4000-8000-000000000001', 'images@example.test', '{"account_role":"restaurant"}', false, false),
    ('70000000-0000-4000-8000-000000000002', 'intruder@example.test', '{"account_role":"restaurant"}', false, false);
insert into public.restaurants (id, owner_account_id, name, street_line, locality, postal_code) values
    ('80000000-0000-4000-8000-000000000001', '70000000-0000-4000-8000-000000000001', 'Images', 'Street', 'Madrid', '28001');

select set_config('request.jwt.claim.sub', '60000000-0000-4000-8000-000000000001', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select lives_ok(
    $$insert into storage.objects (bucket_id, name, owner_id) values ('avatars', '60000000-0000-4000-8000-000000000001/avatar.jpg', '60000000-0000-4000-8000-000000000001')$$,
    'customer may upload inside their avatar folder'
);
select throws_ok(
    $$insert into storage.objects (bucket_id, name, owner_id) values ('avatars', '70000000-0000-4000-8000-000000000001/avatar.jpg', '60000000-0000-4000-8000-000000000001')$$,
    '42501'
);
reset role;

select set_config('request.jwt.claim.sub', '70000000-0000-4000-8000-000000000001', true);
set local role authenticated;
select lives_ok(
    $$insert into storage.objects (bucket_id, name, owner_id) values ('restaurant-images', '80000000-0000-4000-8000-000000000001/hero.jpg', '70000000-0000-4000-8000-000000000001')$$,
    'restaurant owner may upload a restaurant image'
);
select lives_ok(
    $$insert into storage.objects (bucket_id, name, owner_id) values ('dish-images', '80000000-0000-4000-8000-000000000001/dish.jpg', '70000000-0000-4000-8000-000000000001')$$,
    'restaurant owner may upload a dish image'
);
reset role;

select set_config('request.jwt.claim.sub', '70000000-0000-4000-8000-000000000002', true);
set local role authenticated;
select throws_ok(
    $$insert into storage.objects (bucket_id, name, owner_id) values ('restaurant-images', '80000000-0000-4000-8000-000000000001/intrusion.jpg', '70000000-0000-4000-8000-000000000002')$$,
    '42501'
);
reset role;

select * from finish();
rollback;
