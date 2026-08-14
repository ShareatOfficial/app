insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values
    ('avatars', 'avatars', false, 512000, array['image/jpeg', 'image/png', 'image/webp']),
    ('restaurant-images', 'restaurant-images', true, 512000, array['image/jpeg', 'image/png', 'image/webp']),
    ('dish-images', 'dish-images', true, 512000, array['image/jpeg', 'image/png', 'image/webp'])
on conflict (id) do update set
    public = excluded.public,
    file_size_limit = excluded.file_size_limit,
    allowed_mime_types = excluded.allowed_mime_types;

create function private.storage_folder_uuid(object_name text)
returns uuid
language sql
immutable
set search_path = ''
as $$
    select case
        when (storage.foldername(object_name))[1] ~
            '^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[1-5][0-9a-fA-F]{3}-[89abAB][0-9a-fA-F]{3}-[0-9a-fA-F]{12}$'
        then ((storage.foldername(object_name))[1])::uuid
    end;
$$;

revoke execute on function private.storage_folder_uuid(text) from public, anon;
grant execute on function private.storage_folder_uuid(text) to authenticated;

create policy avatars_select_owner on storage.objects for select to authenticated
using (
    bucket_id = 'avatars'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy avatars_insert_owner on storage.objects for insert to authenticated
with check (
    bucket_id = 'avatars'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy avatars_update_owner on storage.objects for update to authenticated
using (
    bucket_id = 'avatars'
    and (storage.foldername(name))[1] = (select auth.uid())::text
)
with check (
    bucket_id = 'avatars'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);
create policy avatars_delete_owner on storage.objects for delete to authenticated
using (
    bucket_id = 'avatars'
    and (storage.foldername(name))[1] = (select auth.uid())::text
);

create policy restaurant_images_select_owner on storage.objects for select to authenticated
using (
    bucket_id = 'restaurant-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
create policy restaurant_images_insert_owner on storage.objects for insert to authenticated
with check (
    bucket_id = 'restaurant-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
create policy restaurant_images_update_owner on storage.objects for update to authenticated
using (
    bucket_id = 'restaurant-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
)
with check (
    bucket_id = 'restaurant-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
create policy restaurant_images_delete_owner on storage.objects for delete to authenticated
using (
    bucket_id = 'restaurant-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);

create policy dish_images_select_owner on storage.objects for select to authenticated
using (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
create policy dish_images_insert_owner on storage.objects for insert to authenticated
with check (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
create policy dish_images_update_owner on storage.objects for update to authenticated
using (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
)
with check (
    bucket_id = 'dish-images'
    and private.owns_restaurant(((storage.foldername(name))[1])::uuid)
);
create policy dish_images_delete_owner on storage.objects for delete to authenticated
using (
    bucket_id = 'dish-images'
    and private.owns_restaurant(((storage.foldername(name))[1])::uuid)
);
