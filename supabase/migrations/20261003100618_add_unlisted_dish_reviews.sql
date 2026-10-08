-- Unlisted dishes are user-supplied descriptions, never catalogue entities.
alter table public.reviews drop constraint reviews_target_type_check;
alter table public.reviews
    add constraint reviews_target_type_check
        check (target_type in ('restaurant', 'dish', 'unlisted_dish')),
    add constraint reviews_unlisted_comment_check check (
        target_type <> 'unlisted_dish'
        or (comment is not null and char_length(btrim(comment)) between 1 and 2000)
    );

create table public.unlisted_dish_reviews (
    review_id uuid primary key,
    author_account_id uuid not null,
    target_type text not null default 'unlisted_dish' check (target_type = 'unlisted_dish'),
    restaurant_name text not null check (char_length(btrim(restaurant_name)) between 1 and 120),
    dish_name text not null check (char_length(btrim(dish_name)) between 1 and 120),
    image_path text not null unique check (
        char_length(image_path) between 38 and 1024
        and image_path = btrim(image_path)
        and split_part(image_path, '/', 1) = author_account_id::text
        and right(image_path, 1) <> '/'
    ),
    foreign key (review_id, author_account_id, target_type)
        references public.reviews (id, author_account_id, target_type) on delete cascade
);
create index unlisted_dish_reviews_author_idx on public.unlisted_dish_reviews (author_account_id);

create or replace function private.review_target_is_public(target_review_id uuid)
returns boolean language sql stable security definer set search_path = '' as $$
    select exists (
        select 1 from public.restaurant_reviews rr
        where rr.review_id = target_review_id and private.is_restaurant_public(rr.restaurant_id)
    ) or exists (
        select 1 from public.dish_reviews dr
        where dr.review_id = target_review_id and private.is_dish_public(dr.dish_id)
    ) or exists (
        select 1 from public.unlisted_dish_reviews ur where ur.review_id = target_review_id
    );
$$;

create or replace function private.assert_review_has_target()
returns trigger language plpgsql security definer set search_path = '' as $$
begin
    if not exists (select 1 from public.restaurant_reviews where review_id = new.id)
       and not exists (select 1 from public.dish_reviews where review_id = new.id)
       and not exists (select 1 from public.unlisted_dish_reviews where review_id = new.id) then
        raise exception 'review % has no target', new.id using errcode = '23514';
    end if;
    return null;
end;
$$;

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('review-images', 'review-images', false, 512000, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

alter table public.unlisted_dish_reviews enable row level security;
-- Looking through the parent also applies its visibility, moderation and blocking policies.
create policy unlisted_dish_reviews_select on public.unlisted_dish_reviews
for select to anon, authenticated
using (exists (select 1 from public.reviews r where r.id = review_id));
create policy unlisted_dish_reviews_insert_customer on public.unlisted_dish_reviews
for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and exists (
        select 1 from storage.objects o
        where o.bucket_id = 'review-images' and o.name = image_path
          and o.owner_id = (select auth.uid())::text
          and (storage.foldername(o.name))[1] = (select auth.uid())::text
    )
);
-- Delete the parent to remove a review; the child is immutable and cascades with it.
revoke all on public.unlisted_dish_reviews from public, anon, authenticated;
grant select on public.unlisted_dish_reviews to anon;
grant select, insert on public.unlisted_dish_reviews to authenticated;
grant all on public.unlisted_dish_reviews to service_role;

create policy review_images_select_author on storage.objects for select to authenticated
using (
    bucket_id = 'review-images'
    and owner_id = (select auth.uid())::text
    and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy review_images_select_public on storage.objects for select to anon, authenticated
using (
    bucket_id = 'review-images'
    and exists (
        select 1 from public.unlisted_dish_reviews ur
        join public.reviews r on r.id = ur.review_id
        where ur.image_path = name and r.visibility = 'public' and r.moderation_status = 'visible'
    )
);
create policy review_images_insert_customer on storage.objects for insert to authenticated
with check (
    bucket_id = 'review-images'
    and owner_id = (select auth.uid())::text
    and (storage.foldername(name))[1] = (select auth.uid())::text
    and private.is_active_customer((select auth.uid()))
);
create policy review_images_delete_author on storage.objects for delete to authenticated
using (
    bucket_id = 'review-images'
    and owner_id = (select auth.uid())::text
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

create view public.unlisted_dish_review_details with (security_invoker = true) as
select
    r.id, r.author_account_id, ur.restaurant_name, ur.dish_name, ur.image_path,
    r.rating, r.comment, r.visibility, r.moderation_status,
    r.visited_at, r.created_at, r.updated_at
from public.reviews r
join public.unlisted_dish_reviews ur on ur.review_id = r.id;
revoke all on public.unlisted_dish_review_details from public, anon, authenticated;
grant select on public.unlisted_dish_review_details to anon, authenticated, service_role;

create function public.save_unlisted_dish_review(
    p_restaurant_name text,
    p_dish_name text,
    p_image_path text,
    p_rating smallint,
    p_comment text,
    p_visibility text,
    p_visited_at timestamptz
)
returns uuid language plpgsql security invoker set search_path = '' as $$
declare
    v_author uuid := (select auth.uid());
    v_review_id uuid;
begin
    if v_author is null or not private.is_active_customer(v_author) then
        raise exception 'an active customer account is required' using errcode = '42501';
    end if;
    if p_restaurant_name is null or char_length(btrim(p_restaurant_name)) not between 1 and 120
       or p_dish_name is null or char_length(btrim(p_dish_name)) not between 1 and 120 then
        raise exception 'restaurant and dish names must contain 1 to 120 characters' using errcode = '22023';
    end if;
    if p_comment is null or char_length(btrim(p_comment)) not between 1 and 2000 then
        raise exception 'a comment of 1 to 2000 characters is required' using errcode = '22023';
    end if;
    if p_image_path is null or char_length(p_image_path) not between 38 and 1024
       or p_image_path <> btrim(p_image_path)
       or not exists (
           select 1 from storage.objects o
           where o.bucket_id = 'review-images' and o.name = p_image_path
             and o.owner_id = v_author::text
             and (storage.foldername(o.name))[1] = v_author::text
       ) then
        raise exception 'an existing review image owned by the customer is required' using errcode = '22023';
    end if;

    -- The existing reviews_queue_comment trigger sends mandatory comments to moderation.
    insert into public.reviews (author_account_id, target_type, rating, comment, visibility, visited_at)
    values (v_author, 'unlisted_dish', p_rating, btrim(p_comment), p_visibility, p_visited_at)
    returning id into v_review_id;

    insert into public.unlisted_dish_reviews
        (review_id, author_account_id, restaurant_name, dish_name, image_path)
    values (v_review_id, v_author, btrim(p_restaurant_name), btrim(p_dish_name), p_image_path);
    return v_review_id;
end;
$$;
revoke all on function public.save_unlisted_dish_review(text, text, text, smallint, text, text, timestamptz)
    from public, anon;
grant execute on function public.save_unlisted_dish_review(text, text, text, smallint, text, text, timestamptz)
    to authenticated;
