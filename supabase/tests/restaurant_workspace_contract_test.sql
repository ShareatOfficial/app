begin;
select plan(20);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('63000000-0000-4000-8000-000000000001', 'workspace-owner@example.test', '{"account_role":"restaurant"}', false, false),
    ('63000000-0000-4000-8000-000000000002', 'workspace-customer@example.test', '{"account_role":"customer","display_name":"Customer"}', false, false),
    ('63000000-0000-4000-8000-000000000003', 'workspace-disabled@example.test', '{"account_role":"restaurant"}', false, false),
    ('63000000-0000-4000-8000-000000000004', 'workspace-other@example.test', '{"account_role":"restaurant"}', false, false);

update public.accounts
set status = 'disabled'
where id = '63000000-0000-4000-8000-000000000003';

select ok(
    has_function_privilege('authenticated', 'public.ensure_restaurant_workspace()', 'execute'),
    'authenticated may bootstrap its restaurant workspace'
);
select ok(
    not has_function_privilege('anon', 'public.ensure_restaurant_workspace()', 'execute'),
    'anonymous users may not bootstrap a workspace'
);

select set_config('request.jwt.claim.sub', '63000000-0000-4000-8000-000000000002', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select throws_ok(
    $$select public.ensure_restaurant_workspace()$$,
    '42501',
    'an active restaurant account is required',
    'customers cannot bootstrap a restaurant workspace'
);
reset role;

select set_config('request.jwt.claim.sub', '63000000-0000-4000-8000-000000000003', true);
set local role authenticated;
select throws_ok(
    $$select public.ensure_restaurant_workspace()$$,
    '42501',
    'an active restaurant account is required',
    'inactive restaurant accounts cannot bootstrap a workspace'
);
reset role;

select set_config('request.jwt.claim.sub', '63000000-0000-4000-8000-000000000001', true);
set local role authenticated;
create temporary table workspace_result as
select public.ensure_restaurant_workspace() as restaurant_id;

select is(
    (select owner_account_id from public.restaurants where id = (select restaurant_id from workspace_result)),
    '63000000-0000-4000-8000-000000000001'::uuid,
    'the workspace owner is always auth.uid'
);
select is(
    (select name from public.restaurants where id = (select restaurant_id from workspace_result)),
    'Rincón de Paco',
    'the editable default name is seeded'
);
select is(
    (select description from public.restaurants where id = (select restaurant_id from workspace_result)),
    'Lugar típico para tomarse unas tapas.',
    'the editable default description is seeded'
);
select is(
    (select publication_state from public.restaurants where id = (select restaurant_id from workspace_result)),
    'draft',
    'the restaurant starts unpublished'
);
select ok(
    (
        select street_line = 'Calle de las Tapas, 1'
           and locality = 'Madrid'
           and postal_code = '28001'
           and country_code = 'ES'
           and hero_image_path is null
           and hero_image_alt_text is null
        from public.restaurants where id = (select restaurant_id from workspace_result)
    ),
    'a valid Spanish placeholder address is seeded without a fake image path'
);
select is(
    (select count(*) from public.menus where restaurant_id = (select restaurant_id from workspace_result)),
    1::bigint,
    'one draft menu is created atomically'
);
select is(
    (select publication_state from public.menus where restaurant_id = (select restaurant_id from workspace_result)),
    'draft',
    'the starter menu is unpublished'
);
select is(
    (select count(*) from public.dishes where restaurant_id = (select restaurant_id from workspace_result)),
    2::bigint,
    'two starter dishes are created atomically'
);
select is(
    (
        select array_agg(name order by name)
        from public.dishes where restaurant_id = (select restaurant_id from workspace_result)
    ),
    array['Croquetas de jamón', 'Patatas bravas'],
    'the starter dishes are recognizable tapas'
);
select is(
    (
        select array_agg(price_minor_units order by position)
        from public.menu_items where restaurant_id = (select restaurant_id from workspace_result)
    ),
    array[650::bigint, 850::bigint],
    'the starter dish prices are seeded in euro cents'
);
select is(
    (
        select array_agg(category order by position)
        from public.menu_items where restaurant_id = (select restaurant_id from workspace_result)
    ),
    array['small_bites', 'starters'],
    'starter dish categories are persisted'
);
select is(
    (
        select count(*)
        from public.dish_allergens da
        join public.dishes d on d.id = da.dish_id
        where d.restaurant_id = (select restaurant_id from workspace_result)
    ),
    4::bigint,
    'starter allergen declarations are seeded'
);

update public.restaurants set name = 'Mi restaurante' where id = (select restaurant_id from workspace_result);
select is(
    public.ensure_restaurant_workspace(),
    (select restaurant_id from workspace_result),
    'a retry returns the same workspace'
);
select is(
    (select name from public.restaurants where id = (select restaurant_id from workspace_result)),
    'Mi restaurante',
    'a retry never overwrites the owner changes'
);
select is(
    (select count(*) from public.menus where restaurant_id = (select restaurant_id from workspace_result)),
    1::bigint,
    'a retry does not create another menu or seed missing records'
);
reset role;

select set_config('request.jwt.claim.sub', '63000000-0000-4000-8000-000000000004', true);
set local role authenticated;
select is(
    (select count(*) from public.restaurants where id = (select restaurant_id from workspace_result)),
    0::bigint,
    'another restaurant owner cannot read a private starter workspace'
);
reset role;

select * from finish();
rollback;
