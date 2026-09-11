begin;
select plan(13);

select hasnt_column(
    'public', 'menu_items', 'restaurant_id',
    'a menu item does not repeat the restaurant its menu already determines'
);
select hasnt_column(
    'public', 'menu_items', 'currency',
    'a menu item does not repeat the currency its restaurant already determines'
);
select hasnt_column(
    'public', 'dishes', 'allergen_source',
    'the allergen source is derived from the declaration, not stored beside it'
);
select hasnt_column(
    'public', 'dishes', 'restaurant_id',
    'the restaurant a dish belongs to lives in restaurant_dishes'
);
select has_column('public', 'restaurants', 'currency_code', 'a restaurant prices in exactly one currency');
select has_column('public', 'menus', 'price_minor_units', 'a menu may carry an optional fixed price');
select has_column('public', 'menu_items', 'price_minor_units', 'the menu/dish relation carries its own price');

select is(
    (select count(*) from public.allergens where id in ('gluten', 'soy', 'sulphites')),
    0::bigint,
    'every allergen is addressable under exactly one id'
);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('20000000-0000-4000-8000-000000000101', 'catalogue-owner@example.test', '{"account_role":"restaurant"}', false, false),
    ('20000000-0000-4000-8000-000000000102', 'catalogue-other@example.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (
    id, owner_account_id, name, street_line, locality, postal_code, publication_state
) values
    ('30000000-0000-4000-8000-000000000101', '20000000-0000-4000-8000-000000000101', 'Owner', 'Street 1', 'Madrid', '28001', 'published'),
    ('30000000-0000-4000-8000-000000000102', '20000000-0000-4000-8000-000000000102', 'Other', 'Street 2', 'Madrid', '28002', 'published');

insert into public.dishes (id, name, is_enabled) values
    ('50000000-0000-4000-8000-000000000101', 'Owner dish', true),
    ('50000000-0000-4000-8000-000000000102', 'Other dish', true);
insert into public.restaurant_dishes (dish_id, restaurant_id) values
    ('50000000-0000-4000-8000-000000000101', '30000000-0000-4000-8000-000000000101'),
    ('50000000-0000-4000-8000-000000000102', '30000000-0000-4000-8000-000000000102');

insert into public.menus (id, restaurant_id, name, publication_state, price_minor_units) values
    ('40000000-0000-4000-8000-000000000101', '30000000-0000-4000-8000-000000000101', 'A la carte', 'published', null),
    ('40000000-0000-4000-8000-000000000102', '30000000-0000-4000-8000-000000000101', 'Set menu', 'published', 2950);

select is(
    (select count(*) from public.menus where restaurant_id = '30000000-0000-4000-8000-000000000101'),
    2::bigint,
    'a restaurant may own more than one menu'
);
select is(
    (select price_minor_units from public.menus where id = '40000000-0000-4000-8000-000000000102'),
    2950::bigint,
    'a set menu stores its fixed price'
);
select is(
    (select price_minor_units from public.menus where id = '40000000-0000-4000-8000-000000000101'),
    null::bigint,
    'an a la carte menu prices its dishes instead'
);

insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled) values
    ('40000000-0000-4000-8000-000000000101', '50000000-0000-4000-8000-000000000101', 1800, 0, true),
    ('40000000-0000-4000-8000-000000000102', '50000000-0000-4000-8000-000000000101', 1500, 0, true);

select is(
    (
        select count(distinct price_minor_units)
        from public.menu_items
        where dish_id = '50000000-0000-4000-8000-000000000101'
    ),
    2::bigint,
    'the same dish is priced per menu it appears on'
);

select throws_ok(
    $$insert into public.menu_items (menu_id, dish_id, price_minor_units, position, is_enabled)
      values ('40000000-0000-4000-8000-000000000101', '50000000-0000-4000-8000-000000000102', 1200, 1, true)$$,
    '23514',
    null,
    'a menu cannot list a dish from another restaurant'
);

select * from finish();
rollback;
