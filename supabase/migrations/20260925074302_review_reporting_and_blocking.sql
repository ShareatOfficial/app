-- Moderation records stay outside the exposed Data API schema.
create table private.review_reports (
    id uuid primary key default gen_random_uuid(),
    reporter_account_id uuid not null references public.accounts (id) on delete cascade,
    review_id uuid not null references public.reviews (id) on delete cascade,
    reason text not null check (reason in ('offensive', 'spam', 'other')),
    status text not null default 'open' check (status in ('open', 'resolved', 'dismissed')),
    created_at timestamptz not null default now(),
    unique (reporter_account_id, review_id)
);
create index review_reports_open_idx on private.review_reports (created_at)
    where status = 'open';
alter table private.review_reports enable row level security;
create policy review_reports_select_self on private.review_reports for select to authenticated
using ((select auth.uid()) = reporter_account_id);
create policy review_reports_insert_self on private.review_reports for insert to authenticated
with check ((select auth.uid()) = reporter_account_id);
revoke all on private.review_reports from public, anon, authenticated;
grant select on private.review_reports to authenticated;
grant insert (reporter_account_id, review_id, reason) on private.review_reports to authenticated;
grant all on private.review_reports to service_role;

create table private.blocked_review_authors (
    blocker_account_id uuid not null references public.accounts (id) on delete cascade,
    blocked_account_id uuid not null references public.accounts (id) on delete cascade,
    created_at timestamptz not null default now(),
    primary key (blocker_account_id, blocked_account_id),
    check (blocker_account_id <> blocked_account_id)
);
create index blocked_review_authors_blocked_idx
    on private.blocked_review_authors (blocked_account_id);
alter table private.blocked_review_authors enable row level security;
create policy blocked_review_authors_select_self on private.blocked_review_authors
for select to authenticated using ((select auth.uid()) = blocker_account_id);
create policy blocked_review_authors_insert_self on private.blocked_review_authors
for insert to authenticated with check ((select auth.uid()) = blocker_account_id);
revoke all on private.blocked_review_authors from public, anon, authenticated;
grant select, insert on private.blocked_review_authors to authenticated;
grant all on private.blocked_review_authors to service_role;

create function private.is_review_author_blocked(p_author_account_id uuid)
returns boolean language sql stable security definer set search_path = '' as $$
    select (select auth.uid()) is not null and exists (
        select 1 from private.blocked_review_authors b
        where b.blocker_account_id = (select auth.uid())
          and b.blocked_account_id = p_author_account_id
    );
$$;
revoke execute on function private.is_review_author_blocked(uuid) from public, anon;
grant execute on function private.is_review_author_blocked(uuid) to authenticated;

drop policy reviews_select_authenticated on public.reviews;
create policy reviews_select_authenticated on public.reviews for select to authenticated
using (
    (
        visibility = 'public'
        and moderation_status = 'visible'
        and private.review_target_is_public(id)
        and not private.is_review_author_blocked(author_account_id)
    )
    or (select auth.uid()) = author_account_id
);

-- Every submitted comment is screened by a human before it becomes public.
-- Star-only ratings remain visible immediately. A removed review cannot be
-- republished by its author by editing or clearing its comment.
create function private.queue_review_comment()
returns trigger language plpgsql security invoker set search_path = '' as $$
begin
    if tg_op = 'INSERT' then
        new.moderation_status := case when new.comment is null then 'visible' else 'hidden' end;
    elsif new.comment is distinct from old.comment and old.moderation_status <> 'removed' then
        new.moderation_status := case when new.comment is null then 'visible' else 'hidden' end;
    end if;
    return new;
end;
$$;
revoke execute on function private.queue_review_comment() from public, anon, authenticated;
create trigger reviews_queue_comment before insert or update of comment on public.reviews
for each row execute function private.queue_review_comment();

drop policy reviews_insert_customer on public.reviews;
create policy reviews_insert_customer on public.reviews for insert to authenticated
with check (
    (select auth.uid()) = author_account_id
    and private.is_active_customer(author_account_id)
    and moderation_status in ('visible', 'hidden')
);

create function public.report_review(p_review_id uuid, p_reason text)
returns void language plpgsql security invoker set search_path = '' as $$
declare
    v_reporter uuid := (select auth.uid());
    v_author uuid;
begin
    if v_reporter is null then
        raise exception 'sign in to report a review' using errcode = '42501';
    end if;
    if p_reason not in ('offensive', 'spam', 'other') then
        raise exception 'unsupported report reason' using errcode = '22023';
    end if;
    select author_account_id into v_author from public.reviews
    where id = p_review_id and visibility = 'public' and moderation_status = 'visible';
    if v_author is null or v_author = v_reporter then
        raise exception 'review cannot be reported' using errcode = '42501';
    end if;
    insert into private.review_reports (reporter_account_id, review_id, reason)
    values (v_reporter, p_review_id, p_reason)
    on conflict (reporter_account_id, review_id) do nothing;
end;
$$;
revoke execute on function public.report_review(uuid, text) from public, anon;
grant execute on function public.report_review(uuid, text) to authenticated;

create function public.block_review_author(p_review_id uuid)
returns uuid language plpgsql security invoker set search_path = '' as $$
declare
    v_blocker uuid := (select auth.uid());
    v_author uuid;
begin
    if v_blocker is null then
        raise exception 'sign in to block a reviewer' using errcode = '42501';
    end if;
    select author_account_id into v_author from public.reviews
    where id = p_review_id and visibility = 'public' and moderation_status = 'visible';
    if v_author is null or v_author = v_blocker then
        raise exception 'reviewer cannot be blocked' using errcode = '42501';
    end if;
    insert into private.blocked_review_authors (blocker_account_id, blocked_account_id)
    values (v_blocker, v_author)
    on conflict do nothing;
    return v_author;
end;
$$;
revoke execute on function public.block_review_author(uuid) from public, anon;
grant execute on function public.block_review_author(uuid) to authenticated;
