create schema if not exists private;
revoke all on schema private from public, anon, authenticated;

create table public.accounts (
    id uuid primary key references auth.users (id) on delete cascade,
    role text not null check (role in ('customer', 'restaurant')),
    status text not null default 'active' check (status in ('active', 'disabled', 'deletion_pending')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table public.customer_profiles (
    account_id uuid primary key references public.accounts (id) on delete cascade,
    display_name text not null check (length(btrim(display_name)) > 0),
    avatar_path text,
    avatar_alt_text text,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create table public.restaurants (
    id uuid primary key default gen_random_uuid(),
    owner_account_id uuid not null unique references public.accounts (id) on delete restrict,
    name text not null check (length(btrim(name)) > 0),
    description text,
    hero_image_path text,
    hero_image_alt_text text,
    public_email text check (public_email is null or public_email ~* '^[^@[:space:]]+@[^@[:space:]]+$'),
    public_phone text check (public_phone is null or length(btrim(public_phone)) > 0),
    street_line text not null check (length(btrim(street_line)) > 0),
    locality text not null check (length(btrim(locality)) > 0),
    postal_code text not null check (length(btrim(postal_code)) > 0),
    region text,
    country_code text not null default 'ES' check (country_code ~ '^[A-Z]{2}$'),
    latitude double precision check (latitude between -90 and 90),
    longitude double precision check (longitude between -180 and 180),
    publication_state text not null default 'draft'
        check (publication_state in ('draft', 'published', 'disabled')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    check ((latitude is null) = (longitude is null))
);

create table public.restaurant_opening_periods (
    restaurant_id uuid not null references public.restaurants (id) on delete cascade,
    weekday smallint not null check (weekday between 1 and 7),
    position integer not null check (position >= 0),
    opens_at time not null,
    closes_at time not null,
    primary key (restaurant_id, weekday, position),
    check (opens_at <> closes_at)
);

create table public.menus (
    id uuid primary key default gen_random_uuid(),
    restaurant_id uuid not null unique references public.restaurants (id) on delete cascade,
    name text not null check (length(btrim(name)) > 0),
    description text,
    publication_state text not null default 'draft'
        check (publication_state in ('draft', 'published', 'unpublished', 'disabled')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (id, restaurant_id)
);

create table public.dishes (
    id uuid primary key default gen_random_uuid(),
    restaurant_id uuid not null references public.restaurants (id) on delete cascade,
    name text not null check (length(btrim(name)) > 0),
    description text,
    image_path text,
    image_alt_text text,
    allergen_note text check (allergen_note is null or length(btrim(allergen_note)) > 0),
    allergen_source text check (allergen_source is null or allergen_source = 'restaurant'),
    is_enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    unique (id, restaurant_id)
);

create table public.allergens (
    id text primary key check (id ~ '^[a-z0-9_]+$')
);

insert into public.allergens (id) values
    ('celery'),
    ('cereals_containing_gluten'),
    ('crustaceans'),
    ('eggs'),
    ('fish'),
    ('lupin'),
    ('milk'),
    ('molluscs'),
    ('mustard'),
    ('nuts'),
    ('peanuts'),
    ('sesame'),
    ('soybeans'),
    ('sulphur_dioxide_and_sulphites');

create table public.dish_allergens (
    dish_id uuid not null references public.dishes (id) on delete cascade,
    allergen_id text not null references public.allergens (id) on delete restrict,
    primary key (dish_id, allergen_id)
);

create table public.menu_items (
    menu_id uuid not null,
    dish_id uuid not null,
    restaurant_id uuid not null references public.restaurants (id) on delete cascade,
    price_minor_units bigint not null check (price_minor_units >= 0),
    currency text not null default 'EUR' check (currency = 'EUR'),
    position integer not null check (position >= 0),
    is_enabled boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    primary key (menu_id, dish_id),
    unique (menu_id, position),
    foreign key (menu_id, restaurant_id)
        references public.menus (id, restaurant_id) on delete cascade,
    foreign key (dish_id, restaurant_id)
        references public.dishes (id, restaurant_id) on delete cascade
);

create table public.reviews (
    id uuid primary key default gen_random_uuid(),
    author_account_id uuid not null references public.accounts (id) on delete cascade,
    restaurant_id uuid references public.restaurants (id) on delete cascade,
    dish_id uuid references public.dishes (id) on delete cascade,
    rating smallint not null check (rating between 1 and 5),
    comment text check (comment is null or length(btrim(comment)) > 0),
    visibility text not null default 'public' check (visibility in ('public', 'private')),
    moderation_status text not null default 'visible'
        check (moderation_status in ('visible', 'hidden', 'removed')),
    visited_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    check ((restaurant_id is not null)::integer + (dish_id is not null)::integer = 1)
);

create unique index reviews_author_restaurant_uidx
    on public.reviews (author_account_id, restaurant_id)
    where restaurant_id is not null;
create unique index reviews_author_dish_uidx
    on public.reviews (author_account_id, dish_id)
    where dish_id is not null;

create index dishes_restaurant_id_idx on public.dishes (restaurant_id);
create index dish_allergens_allergen_id_idx on public.dish_allergens (allergen_id);
create index menu_items_dish_restaurant_idx on public.menu_items (dish_id, restaurant_id);
create index menu_items_restaurant_id_idx on public.menu_items (restaurant_id);
create index reviews_author_account_id_idx on public.reviews (author_account_id);
create index reviews_restaurant_id_idx on public.reviews (restaurant_id);
create index reviews_dish_id_idx on public.reviews (dish_id);
create index accounts_role_status_idx on public.accounts (role, status);
create index restaurants_publication_state_idx on public.restaurants (publication_state, id);
create index menus_publication_state_idx on public.menus (publication_state, restaurant_id);
create index dishes_restaurant_enabled_idx on public.dishes (restaurant_id, is_enabled);
create index reviews_public_restaurant_idx
    on public.reviews (restaurant_id, created_at desc)
    where visibility = 'public' and moderation_status = 'visible';
create index reviews_public_dish_idx
    on public.reviews (dish_id, created_at desc)
    where visibility = 'public' and moderation_status = 'visible';

create function private.set_updated_at()
returns trigger
language plpgsql
set search_path = ''
as $$
begin
    new.updated_at = now();
    return new;
end;
$$;

create trigger accounts_set_updated_at before update on public.accounts
    for each row execute function private.set_updated_at();
create trigger customer_profiles_set_updated_at before update on public.customer_profiles
    for each row execute function private.set_updated_at();
create trigger restaurants_set_updated_at before update on public.restaurants
    for each row execute function private.set_updated_at();
create trigger menus_set_updated_at before update on public.menus
    for each row execute function private.set_updated_at();
create trigger dishes_set_updated_at before update on public.dishes
    for each row execute function private.set_updated_at();
create trigger menu_items_set_updated_at before update on public.menu_items
    for each row execute function private.set_updated_at();
create trigger reviews_set_updated_at before update on public.reviews
    for each row execute function private.set_updated_at();

create function private.handle_new_auth_user()
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
    requested_display_name := nullif(btrim(new.raw_user_meta_data ->> 'display_name'), '');

    insert into public.accounts (id, role)
    values (new.id, requested_role);

    if requested_role = 'customer' then
        insert into public.customer_profiles (account_id, display_name)
        values (
            new.id,
            coalesce(requested_display_name, nullif(split_part(new.email, '@', 1), ''), 'Shareat user')
        );
    end if;
    return new;
end;
$$;

revoke execute on function private.handle_new_auth_user() from public, anon, authenticated;

create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function private.handle_new_auth_user();

create view public.restaurant_rating_summaries
with (security_invoker = true)
as
select
    restaurant_id,
    round(avg(rating) * 10)::integer as average_tenths,
    count(*)::bigint as rating_count
from public.reviews
where restaurant_id is not null
  and visibility = 'public'
  and moderation_status = 'visible'
group by restaurant_id;

create view public.dish_rating_summaries
with (security_invoker = true)
as
select
    dish_id,
    round(avg(rating) * 10)::integer as average_tenths,
    count(*)::bigint as rating_count
from public.reviews
where dish_id is not null
  and visibility = 'public'
  and moderation_status = 'visible'
group by dish_id;
