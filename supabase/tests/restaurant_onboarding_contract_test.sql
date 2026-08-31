begin;
select plan(16);

insert into auth.users (id, email, raw_user_meta_data, is_sso_user, is_anonymous) values
    ('61000000-0000-4000-8000-000000000001', 'onboarding-customer@example.test', '{"account_role":"customer","display_name":"Customer"}', false, false),
    ('62000000-0000-4000-8000-000000000001', 'onboarding-owner@example.test', '{"account_role":"restaurant"}', false, false),
    ('62000000-0000-4000-8000-000000000002', 'onboarding-disabled@example.test', '{"account_role":"restaurant"}', false, false),
    ('62000000-0000-4000-8000-000000000003', 'onboarding-other@example.test', '{"account_role":"restaurant"}', false, false);

update public.accounts
set status = 'disabled'
where id = '62000000-0000-4000-8000-000000000002';

select ok(
    has_function_privilege(
        'authenticated',
        'public.create_restaurant_profile(text,text,text,text,text,text,text,text,jsonb)',
        'execute'
    ),
    'authenticated may execute onboarding RPC'
);
select ok(
    not has_function_privilege(
        'anon',
        'public.create_restaurant_profile(text,text,text,text,text,text,text,text,jsonb)',
        'execute'
    ),
    'anonymous may not execute onboarding RPC'
);

select set_config('request.jwt.claim.sub', '61000000-0000-4000-8000-000000000001', true);
select set_config('request.jwt.claim.role', 'authenticated', true);
set local role authenticated;
select throws_ok(
    $$select public.create_restaurant_profile('Customer restaurant', null, null, null, 'Street', 'Madrid', '28001', null, '[]'::jsonb)$$,
    '42501',
    'an active restaurant account is required',
    'customer accounts are rejected'
);
reset role;

select set_config('request.jwt.claim.sub', '62000000-0000-4000-8000-000000000002', true);
set local role authenticated;
select throws_ok(
    $$select public.create_restaurant_profile('Disabled restaurant', null, null, null, 'Street', 'Madrid', '28001', null, '[]'::jsonb)$$,
    '42501',
    'an active restaurant account is required',
    'inactive restaurant accounts are rejected'
);
reset role;

select set_config('request.jwt.claim.sub', '62000000-0000-4000-8000-000000000001', true);
set local role authenticated;
create temporary table onboarding_result as
select public.create_restaurant_profile(
    '  Casa Onboarding  ',
    '',
    'hola@example.test',
    '',
    '  Calle Mayor 1 ',
    ' Madrid ',
    ' 28001 ',
    ' Comunidad de Madrid ',
    '[
      {"weekday":1,"opens_at":"11:00:00","closes_at":"22:00:00"},
      {"weekday":5,"opens_at":"12:00:00","closes_at":"23:00:00"}
    ]'::jsonb
) as restaurant_id;

select is(
    (select owner_account_id from public.restaurants where id = (select restaurant_id from onboarding_result)),
    '62000000-0000-4000-8000-000000000001'::uuid,
    'owner is derived from the authenticated JWT'
);
select is(
    (select name from public.restaurants where id = (select restaurant_id from onboarding_result)),
    'Casa Onboarding',
    'required text is normalized'
);
select is(
    (select publication_state from public.restaurants where id = (select restaurant_id from onboarding_result)),
    'draft',
    'restaurant is created as a draft'
);
select is(
    (select country_code from public.restaurants where id = (select restaurant_id from onboarding_result)),
    'ES',
    'country is fixed to Spain'
);
select is(
    (select region from public.restaurants where id = (select restaurant_id from onboarding_result)),
    'Comunidad de Madrid',
    'optional province is stored when supplied'
);
select ok(
    (select description is null and public_phone is null and latitude is null and longitude is null
     from public.restaurants where id = (select restaurant_id from onboarding_result)),
    'omitted optionals and coordinates remain null'
);
select is(
    (select count(*) from public.restaurant_opening_periods where restaurant_id = (select restaurant_id from onboarding_result)),
    2::bigint,
    'opening periods are created atomically'
);
select is(
    public.create_restaurant_profile('Ignored retry', null, null, null, 'Other', 'Other', '00000', null, '[]'::jsonb),
    (select restaurant_id from onboarding_result),
    'retry returns the existing restaurant id'
);
select is(
    (select count(*) from public.restaurants where owner_account_id = '62000000-0000-4000-8000-000000000001'),
    1::bigint,
    'retry does not create a second restaurant'
);
select is(
    (select count(*) from public.restaurant_opening_periods where restaurant_id = (select restaurant_id from onboarding_result)),
    2::bigint,
    'retry does not overwrite opening periods'
);
reset role;

select set_config('request.jwt.claim.sub', '62000000-0000-4000-8000-000000000003', true);
set local role authenticated;
select is(
    (select count(*) from public.restaurants where id = (select restaurant_id from onboarding_result)),
    0::bigint,
    'another owner cannot read the new draft'
);
select is(
    (select count(*) from public.restaurant_opening_periods where restaurant_id = (select restaurant_id from onboarding_result)),
    0::bigint,
    'another owner cannot read its opening periods'
);
reset role;

select * from finish();
rollback;
