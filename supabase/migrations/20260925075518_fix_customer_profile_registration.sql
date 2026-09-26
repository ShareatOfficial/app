-- full_name became required after the original auth trigger was created.
-- Keep the account and profile creation in the same auth.users transaction.
create or replace function private.handle_new_auth_user()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    requested_role text;
    requested_display_name text;
begin
    requested_role := new.raw_user_meta_data ->> 'account_role';
    if requested_role is null or requested_role not in ('customer', 'restaurant') then
        raise exception 'account_role must be customer or restaurant' using errcode = '22023';
    end if;
    requested_display_name := coalesce(
        nullif(btrim(new.raw_user_meta_data ->> 'display_name'), ''),
        nullif(split_part(new.email, '@', 1), ''),
        'Shareat user'
    );

    insert into public.accounts (id, role)
    values (new.id, requested_role);

    if requested_role = 'customer' then
        insert into public.customer_profiles (account_id, display_name, full_name)
        values (new.id, requested_display_name, requested_display_name);
    end if;
    return new;
end;
$$;

revoke execute on function private.handle_new_auth_user() from public, anon, authenticated;
