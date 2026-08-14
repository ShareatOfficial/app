# Supabase: desarrollo y despliegue

## Límites de seguridad

El cliente solo recibe la URL y una publishable key generadas por BuildKonfig. Nunca se usa una secret key ni `service_role` en Android, iOS o web. Los permisos del Data API se conceden explícitamente después de crear las políticas RLS.

Auth usa email/contraseña, PKCE y el callback `shareat://auth-callback`; web usa su origen. La confirmación de email y OAuth están fuera del MVP. Android declara el intent filter, iOS el URL scheme y web una CSP que limita conexiones e imágenes al origen y al proyecto Supabase.

Storage contiene `avatars` privado y `restaurant-images`/`dish-images` públicos. Todos limitan JPEG, PNG y WebP a 500 KB. El reemplazo sube una ruta aleatoria nueva, actualiza la fila y solo entonces intenta borrar la anterior.

## Flujo local

```bash
npm install
npx supabase start
npx supabase db reset --local
npx supabase test db
npx supabase db lint --local --schema private,public --level warning --fail-on warning
```

Crear migraciones únicamente con `npx supabase migration new <name>`. El seed es local y no forma parte del despliegue.

## Despliegue seguro

1. Ejecutar reset, pgTAP y lint local.
2. Revisar `supabase db push --dry-run` cuando el CLI esté enlazado.
3. Aplicar solo las migraciones pendientes al proyecto de desarrollo.
4. Comparar el historial alojado con `supabase/migrations`.
5. Ejecutar smoke queries y los advisors de seguridad y rendimiento.

No ejecutar un reset remoto. Los cambios de Auth URL/redirect se configuran en el entorno de desarrollo y deben incluir `http://localhost:8080/**` y `shareat://auth-callback`.
