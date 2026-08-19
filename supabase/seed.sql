-- Local-only deterministic fixture. `supabase db push` does not apply this file.
insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('91000000-0000-4000-8000-000000000001', 'local-customer@shareat.test', '{"account_role":"customer","display_name":"Local customer"}', false, false),
    ('92000000-0000-4000-8000-000000000001', 'local-owner@shareat.test', '{"account_role":"restaurant"}', false, false);

insert into public.restaurants (
    id, owner_account_id, name, description, public_email, public_phone,
    street_line, locality, postal_code, region, country_code, latitude, longitude, publication_state
) values (
    '93000000-0000-4000-8000-000000000001',
    '92000000-0000-4000-8000-000000000001',
    'Local Shareat Kitchen',
    'Local-only catalogue fixture.',
    'hello@local.shareat.test',
    '+34 910 000 000',
    'Calle Local 1',
    'Madrid',
    '28001',
    'Madrid',
    'ES',
    40.4168,
    -3.7038,
    'published'
);
insert into public.restaurant_opening_periods (restaurant_id, weekday, position, opens_at, closes_at) values
    ('93000000-0000-4000-8000-000000000001', 1, 0, '13:00', '16:00');
insert into public.menus (id, restaurant_id, name, publication_state) values
    ('94000000-0000-4000-8000-000000000001', '93000000-0000-4000-8000-000000000001', 'Local menu', 'published');
insert into public.dishes (id, restaurant_id, name, description, allergen_source, is_enabled) values
    ('95000000-0000-4000-8000-000000000001', '93000000-0000-4000-8000-000000000001', 'Local dish', 'For repository tests.', 'restaurant', true);
insert into public.dish_allergens (dish_id, allergen_id) values
    ('95000000-0000-4000-8000-000000000001', 'milk');
insert into public.menu_items (menu_id, dish_id, restaurant_id, price_minor_units, position, is_enabled) values
    ('94000000-0000-4000-8000-000000000001', '95000000-0000-4000-8000-000000000001', '93000000-0000-4000-8000-000000000001', 1250, 0, true);
insert into public.reviews (id, author_account_id, restaurant_id, rating, comment) values
    ('96000000-0000-4000-8000-000000000001', '91000000-0000-4000-8000-000000000001', '93000000-0000-4000-8000-000000000001', 5, 'Local public review');
