begin;
select plan(6);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('71000000-0000-4000-8000-000000000001', 'publication-owner@example.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (
    id, owner_account_id, name, street_line, locality, postal_code, publication_state
) values (
    '72000000-0000-4000-8000-000000000001',
    '71000000-0000-4000-8000-000000000001',
    'Publication owner', 'Calle Mayor 1', 'Madrid', '28001', 'draft'
);
insert into public.menus (id, restaurant_id, name, publication_state) values
    ('73000000-0000-4000-8000-000000000001', '72000000-0000-4000-8000-000000000001', 'Main menu', 'unpublished');

select set_config('request.jwt.claim.sub', '71000000-0000-4000-8000-000000000001', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;

select throws_ok(
    $$select public.update_restaurant_settings(
        '72000000-0000-4000-8000-000000000001', 'Publication owner', null, null, null,
        'Calle Mayor 1', 'Madrid', '28001', null, 'published', '[]'::jsonb
    )$$,
    '22023',
    'a published restaurant requires an enabled dish',
    'publication rejects a menu with no enabled dish'
);
select is(
    (select publication_state from public.restaurants where id = '72000000-0000-4000-8000-000000000001'),
    'draft',
    'failed publication leaves the restaurant unchanged'
);
select is(
    (select publication_state from public.menus where id = '73000000-0000-4000-8000-000000000001'),
    'unpublished',
    'failed publication leaves the menu unchanged'
);

insert into public.dishes (id, restaurant_id, name, is_enabled) values
    ('74000000-0000-4000-8000-000000000001', '72000000-0000-4000-8000-000000000001', 'Enabled dish', true);
insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled) values
    ('73000000-0000-4000-8000-000000000001', '74000000-0000-4000-8000-000000000001', 1250, 0, true);

select lives_ok(
    $$select public.update_restaurant_settings(
        '72000000-0000-4000-8000-000000000001', 'Publication owner', null, null, null,
        'Calle Mayor 1', 'Madrid', '28001', null, 'published', '[]'::jsonb
    )$$,
    'publication updates restaurant and menu in one RPC'
);
select is(
    (select publication_state from public.restaurants where id = '72000000-0000-4000-8000-000000000001'),
    'published',
    'successful publication makes the restaurant public'
);
select is(
    (select publication_state from public.menus where id = '73000000-0000-4000-8000-000000000001'),
    'published',
    'successful publication makes its menu public too'
);

reset role;
select * from finish();
rollback;
