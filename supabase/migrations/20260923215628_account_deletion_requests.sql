-- An authenticated user can initiate account deletion in the app. The request is
-- kept private so only the project administrator can process it.
create table private.account_deletion_requests (
    account_id uuid primary key references auth.users (id) on delete cascade,
    requested_at timestamptz not null default now()
);

alter table private.account_deletion_requests enable row level security;
revoke all on private.account_deletion_requests from public, anon, authenticated;

-- This is SECURITY DEFINER so the app can write to the private queue without
-- direct table access. There are no parameters: auth.uid() fixes the row to
-- the caller. Supabase's generic SECURITY DEFINER advisor flags this by design.
create function public.request_account_deletion()
returns timestamptz
language plpgsql
security definer
set search_path = ''
as $$
declare
    requester uuid := auth.uid();
    requested_at_value timestamptz;
begin
    if requester is null then
        raise exception 'Authentication required' using errcode = '28000';
    end if;

    insert into private.account_deletion_requests (account_id)
    values (requester)
    on conflict (account_id) do nothing;

    select requested_at into requested_at_value
    from private.account_deletion_requests
    where account_id = requester;

    return requested_at_value;
end;
$$;

revoke all on function public.request_account_deletion() from public, anon;
grant execute on function public.request_account_deletion() to authenticated;
