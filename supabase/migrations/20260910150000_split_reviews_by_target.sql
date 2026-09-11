-- A review is split into the row every review shares and one child row per kind of target.
--
-- reviews             author, rating, comment, visibility, moderation, dates, target_type
-- restaurant_reviews  review_id -> restaurants
-- dish_reviews        review_id -> dishes
--
-- target_type is a real attribute of the parent, and the composite key it forms with the author is
-- what keeps "one review per author and target" a declarative unique constraint on the child. The
-- author_account_id the children carry cannot drift from the parent: the foreign key includes it.

alter table public.reviews
    add column target_type text check (target_type in ('restaurant', 'dish'));

update public.reviews
set target_type = case when restaurant_id is not null then 'restaurant' else 'dish' end;

alter table public.reviews
    alter column target_type set not null,
    add constraint reviews_id_author_target_key unique (id, author_account_id, target_type);

create table public.restaurant_reviews (
    review_id uuid primary key,
    author_account_id uuid not null,
    target_type text not null default 'restaurant' check (target_type = 'restaurant'),
    restaurant_id uuid not null references public.restaurants (id) on delete cascade,
    unique (author_account_id, restaurant_id),
    foreign key (review_id, author_account_id, target_type)
        references public.reviews (id, author_account_id, target_type) on delete cascade
);

create table public.dish_reviews (
    review_id uuid primary key,
    author_account_id uuid not null,
    target_type text not null default 'dish' check (target_type = 'dish'),
    dish_id uuid not null references public.dishes (id) on delete cascade,
    unique (author_account_id, dish_id),
    foreign key (review_id, author_account_id, target_type)
        references public.reviews (id, author_account_id, target_type) on delete cascade
);

insert into public.restaurant_reviews (review_id, author_account_id, restaurant_id)
select id, author_account_id, restaurant_id from public.reviews where restaurant_id is not null;

insert into public.dish_reviews (review_id, author_account_id, dish_id)
select id, author_account_id, dish_id from public.reviews where dish_id is not null;

create index restaurant_reviews_restaurant_id_idx on public.restaurant_reviews (restaurant_id);
create index dish_reviews_dish_id_idx on public.dish_reviews (dish_id);

drop policy reviews_select_anon on public.reviews;
drop policy reviews_select_authenticated on public.reviews;
drop policy reviews_insert_customer on public.reviews;

-- The rating views read the target columns, so they are rebuilt on the child tables below.
drop view public.restaurant_rating_summaries;
drop view public.dish_rating_summaries;

alter table public.reviews
    drop column restaurant_id,
    drop column dish_id;

create function private.review_target_is_public(target_review_id uuid)
returns boolean
language sql
stable
security definer
set search_path = ''
as $$
    select exists (
        select 1 from public.restaurant_reviews rr
        where rr.review_id = target_review_id and private.is_restaurant_public(rr.restaurant_id)
    ) or exists (
        select 1 from public.dish_reviews dr
        where dr.review_id = target_review_id and private.is_dish_public(dr.dish_id)
    );
$$;

revoke execute on function private.review_target_is_public(uuid) from public;
grant execute on function private.review_target_is_public(uuid) to anon, authenticated;

-- A parent row without its child is meaningless, so the check is deferred to commit: the RPC writes
-- both inside one transaction.
create function private.assert_review_has_target()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    if not exists (select 1 from public.restaurant_reviews where review_id = new.id)
       and not exists (select 1 from public.dish_reviews where review_id = new.id) then
        raise exception 'review % has no target' , new.id using errcode = '23514';
    end if;
    return null;
end;
$$;

revoke execute on function private.assert_review_has_target() from public, anon, authenticated;

create constraint trigger reviews_have_a_target
    after insert on public.reviews
    deferrable initially deferred
    for each row execute function private.assert_review_has_target();

create policy reviews_select_anon on public.reviews for select to anon
using (
    visibility = 'public'
    and moderation_status = 'visible'
    and private.review_target_is_public(id)
);
create policy reviews_select_authenticated on public.reviews for select to authenticated
using (
    (
        visibility = 'public'
        and moderation_status = 'visible'
        and private.review_target_is_public(id)
    )
    or (select auth.uid()) = author_account_id
);
create policy reviews_insert_customer on public.reviews for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and moderation_status = 'visible'
);

alter table public.restaurant_reviews enable row level security;
alter table public.dish_reviews enable row level security;

create policy restaurant_reviews_select_anon on public.restaurant_reviews for select to anon
using (private.is_restaurant_public(restaurant_id));
create policy restaurant_reviews_select_authenticated on public.restaurant_reviews for select to authenticated
using (private.is_restaurant_public(restaurant_id) or (select auth.uid()) = author_account_id);
create policy restaurant_reviews_insert_customer on public.restaurant_reviews for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and private.is_restaurant_public(restaurant_id)
);
create policy restaurant_reviews_delete_author on public.restaurant_reviews for delete to authenticated
using ((select auth.uid()) = author_account_id);

create policy dish_reviews_select_anon on public.dish_reviews for select to anon
using (private.is_dish_public(dish_id));
create policy dish_reviews_select_authenticated on public.dish_reviews for select to authenticated
using (private.is_dish_public(dish_id) or (select auth.uid()) = author_account_id);
create policy dish_reviews_insert_customer on public.dish_reviews for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and private.is_dish_public(dish_id)
);
create policy dish_reviews_delete_author on public.dish_reviews for delete to authenticated
using ((select auth.uid()) = author_account_id);

-- The insert grant on reviews is column-level, and it listed the two target columns this migration
-- drops. It has to name target_type instead or no client could ever write a review again.
grant insert (author_account_id, target_type, rating, comment, visibility, visited_at)
    on public.reviews to authenticated;

grant select on public.restaurant_reviews, public.dish_reviews to anon;
grant select, insert, delete on public.restaurant_reviews, public.dish_reviews to authenticated;
grant all on public.restaurant_reviews, public.dish_reviews to service_role;

create view public.restaurant_rating_summaries
with (security_invoker = true)
as
select
    rr.restaurant_id,
    round(avg(r.rating) * 10)::integer as average_tenths,
    count(*)::bigint as rating_count
from public.reviews r
join public.restaurant_reviews rr on rr.review_id = r.id
where r.visibility = 'public'
  and r.moderation_status = 'visible'
group by rr.restaurant_id;

create view public.dish_rating_summaries
with (security_invoker = true)
as
select
    dr.dish_id,
    round(avg(r.rating) * 10)::integer as average_tenths,
    count(*)::bigint as rating_count
from public.reviews r
join public.dish_reviews dr on dr.review_id = r.id
where r.visibility = 'public'
  and r.moderation_status = 'visible'
group by dr.dish_id;

grant select on public.restaurant_rating_summaries, public.dish_rating_summaries to anon, authenticated;

-- Flattened read surfaces so a client reads one row per review instead of joining two tables.
create view public.restaurant_review_details
with (security_invoker = true)
as
select
    r.id, r.author_account_id, rr.restaurant_id, r.rating, r.comment,
    r.visibility, r.moderation_status, r.visited_at, r.created_at, r.updated_at
from public.reviews r
join public.restaurant_reviews rr on rr.review_id = r.id;

create view public.dish_review_details
with (security_invoker = true)
as
select
    r.id, r.author_account_id, dr.dish_id, r.rating, r.comment,
    r.visibility, r.moderation_status, r.visited_at, r.created_at, r.updated_at
from public.reviews r
join public.dish_reviews dr on dr.review_id = r.id;

grant select on public.restaurant_review_details, public.dish_review_details to anon, authenticated;

create function public.save_review(
    p_target_type text,
    p_restaurant_id uuid,
    p_dish_id uuid,
    p_rating smallint,
    p_comment text,
    p_visibility text,
    p_visited_at timestamptz
)
returns uuid
language plpgsql
security invoker
set search_path = ''
as $$
declare
    v_author uuid := (select auth.uid());
    v_review_id uuid;
begin
    if v_author is null then
        raise exception 'an authenticated account is required' using errcode = '42501';
    end if;
    if p_target_type not in ('restaurant', 'dish') then
        raise exception 'unsupported review target' using errcode = '22023';
    end if;
    if (p_target_type = 'restaurant') <> (p_restaurant_id is not null)
        or (p_target_type = 'dish') <> (p_dish_id is not null) then
        raise exception 'the target id must match the target type' using errcode = '22023';
    end if;

    if p_target_type = 'restaurant' then
        select review_id into v_review_id from public.restaurant_reviews
        where author_account_id = v_author and restaurant_id = p_restaurant_id;
    else
        select review_id into v_review_id from public.dish_reviews
        where author_account_id = v_author and dish_id = p_dish_id;
    end if;

    if v_review_id is null then
        insert into public.reviews (
            author_account_id, target_type, rating, comment, visibility, visited_at
        )
        values (
            v_author, p_target_type, p_rating, nullif(btrim(p_comment), ''),
            p_visibility, p_visited_at
        )
        returning id into v_review_id;

        if p_target_type = 'restaurant' then
            insert into public.restaurant_reviews (review_id, author_account_id, restaurant_id)
            values (v_review_id, v_author, p_restaurant_id);
        else
            insert into public.dish_reviews (review_id, author_account_id, dish_id)
            values (v_review_id, v_author, p_dish_id);
        end if;
    else
        update public.reviews
        set rating = p_rating,
            comment = nullif(btrim(p_comment), ''),
            visibility = p_visibility,
            visited_at = p_visited_at
        where id = v_review_id;
    end if;

    return v_review_id;
end;
$$;

revoke all on function public.save_review(text, uuid, uuid, smallint, text, text, timestamptz)
    from public, anon;
grant execute on function public.save_review(text, uuid, uuid, smallint, text, text, timestamptz)
    to authenticated;

create or replace function public.delete_restaurant_dish(p_dish_id uuid)
returns void language plpgsql security invoker set search_path = '' as $$
begin
    if not private.owns_dish(p_dish_id) then
        raise exception 'dish not found or not owned' using errcode = '42501';
    end if;
    if exists (select 1 from public.dish_reviews where dish_id = p_dish_id) then
        raise exception 'dishes with reviews must be archived' using errcode = 'P0001';
    end if;
    delete from public.dishes where id = p_dish_id;
end;
$$;
