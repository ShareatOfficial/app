# Tramitar solicitudes de eliminación de cuenta

La app permite que clientes y restaurantes envíen una solicitud autenticada desde Ajustes > Eliminar cuenta. La solicitud queda en `private.account_deletion_requests` y se registra una sola vez por cuenta. No borra datos automáticamente.

La función expuesta usa `SECURITY DEFINER` para escribir en esa tabla privada; no acepta parámetros y usa `auth.uid()` como identificador de cuenta. Por eso aparece un aviso genérico en el asesor de seguridad de Supabase. La cola no tiene permisos de lectura para los usuarios de la app.

El texto de la app promete completar la eliminación en un máximo de 30 días y confirmar el resultado al correo de la cuenta. **El titular del proyecto debe revisar y tramitar la cola con frecuencia.**

## Consultar solicitudes pendientes

En el editor SQL del proyecto Supabase de producción, ejecuta:

```sql
select r.account_id, u.email, a.role, r.requested_at,
       r.requested_at + interval '30 days' as deadline
from private.account_deletion_requests r
join auth.users u on u.id = r.account_id
join public.accounts a on a.id = r.account_id
order by r.requested_at;
```

El acceso a esta consulta debe limitarse a administradores; los correos son datos personales.

## Tramitar una solicitud

1. Comprueba que la cuenta y el correo coinciden con la solicitud. Si hay una suscripción, recuerda al usuario que la cancele en App Store o Google Play; eliminar la cuenta no cancela la renovación.
2. Si es un restaurante, elimina primero sus imágenes de `restaurant-images` y `dish-images`, y después su registro de `public.restaurants` (sus menús, platos y reseñas relacionadas se eliminan por cascada). Si es un cliente, elimina sus imágenes de `avatars`. Revisa también cualquier archivo almacenado fuera de estos tres buckets.
3. Elimina al usuario de Supabase Auth. La relación `auth.users` → `public.accounts` elimina por cascada el perfil, las reseñas del autor y la solicitud de esta cola. En restaurantes, el registro debe borrarse antes porque la relación del propietario restringe la eliminación.
4. Comprueba que ya no existen la cuenta, sus datos ni sus archivos. Envía al correo registrado la confirmación de eliminación, sin revelar datos de otras personas.

Conserva solo la información que tengas obligación legal de mantener. No elimines datos de terceros sin revisar las relaciones y la política de privacidad.
