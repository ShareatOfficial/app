alter table public.restaurants
    alter column street_line drop not null,
    alter column locality drop not null,
    alter column postal_code drop not null,
    alter column country_code drop not null;
