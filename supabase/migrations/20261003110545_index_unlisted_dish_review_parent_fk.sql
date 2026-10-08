-- Cover the complete parent FK used when PostgreSQL checks or cascades review changes.
create index if not exists unlisted_dish_reviews_parent_fk_idx
    on public.unlisted_dish_reviews (review_id, author_account_id, target_type);
