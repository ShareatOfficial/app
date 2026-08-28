alter table public.customer_profiles
    add column full_name text,
    add column phone_number text,
    add column preferred_language text not null default 'en-US';

update public.customer_profiles
set full_name = display_name
where full_name is null;

alter table public.customer_profiles
    alter column full_name set not null,
    add constraint customer_profiles_full_name_not_blank
        check (length(btrim(full_name)) > 0),
    add constraint customer_profiles_phone_number_not_blank
        check (phone_number is null or length(btrim(phone_number)) > 0),
    add constraint customer_profiles_preferred_language_not_blank
        check (length(btrim(preferred_language)) > 0);

grant update (full_name, phone_number, preferred_language)
    on public.customer_profiles to authenticated;
