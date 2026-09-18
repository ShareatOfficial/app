drop policy dish_images_update_owner on storage.objects;
create policy dish_images_update_owner on storage.objects for update to authenticated
using (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
)
with check (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);

drop policy dish_images_delete_owner on storage.objects;
create policy dish_images_delete_owner on storage.objects for delete to authenticated
using (
    bucket_id = 'dish-images'
    and private.owns_restaurant(private.storage_folder_uuid(name))
);
