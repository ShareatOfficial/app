-- The child's INSERT policy must not expand Storage RLS: the public image SELECT
-- policy reads this same child table. Read only the caller-owned object metadata
-- through a private definer helper to break that policy recursion.
create function private.owns_unlisted_review_image(p_image_path text)
returns boolean language sql stable security definer set search_path = '' as $$
    select (select auth.uid()) is not null and exists (
        select 1 from storage.objects o
        where o.bucket_id = 'review-images'
          and o.name = p_image_path
          and o.owner_id = (select auth.uid())::text
          and (storage.foldername(o.name))[1] = (select auth.uid())::text
    );
$$;
revoke all on function private.owns_unlisted_review_image(text) from public, anon;
grant execute on function private.owns_unlisted_review_image(text) to authenticated;

alter policy unlisted_dish_reviews_insert_customer on public.unlisted_dish_reviews
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and private.owns_unlisted_review_image(image_path)
);
