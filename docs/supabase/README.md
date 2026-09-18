# Supabase: desarrollo y despliegue

## Límites de seguridad

El cliente solo recibe la URL y una publishable key generadas por BuildKonfig. Nunca se usa una secret key ni `service_role` en Android, iOS o web. Los permisos del Data API se conceden explícitamente después de crear las políticas RLS.

## Entornos por tipo de build

BuildKonfig selecciona Supabase al configurar Gradle:

- Las tareas `debug` y las ejecuciones Gradle sin variante explícita usan `shareat develop`.
- Las tareas cuyo nombre contiene `release` usan `shareat production`.
- Xcode usa develop con la configuración Debug y production con Release.
- Para web, CI o una ejecución especial se puede forzar el entorno con
  `-Pshareat.environment=development` o `-Pshareat.environment=production`.

Las URLs y publishable keys incluidas son públicas y específicas del cliente. Se pueden reemplazar
sin tocar el repositorio mediante propiedades Gradle específicas del entorno:

```properties
shareat.supabase.development.url=https://example.supabase.co
shareat.supabase.development.publishableKey=sb_publishable_example
shareat.supabase.production.url=https://example.supabase.co
shareat.supabase.production.publishableKey=sb_publishable_example
```

Las propiedades anteriores tienen prioridad sobre los valores incluidos. Las propiedades heredadas
`shareat.supabase.url` y `shareat.supabase.publishableKey` siguen funcionando como reemplazo global.

Auth usa email/contraseña, PKCE y el callback `shareat://auth-callback`; web usa su origen. La confirmación de email y OAuth están fuera del MVP. Android declara el intent filter, iOS el URL scheme y web una CSP que limita conexiones e imágenes al origen y al proyecto Supabase.

Storage contiene `avatars` privado y `restaurant-images`/`dish-images` públicos. Todos limitan JPEG, PNG y WebP a 500 KB. El reemplazo sube una ruta aleatoria nueva, actualiza la fila y solo entonces intenta borrar la anterior. Antes de subir, el cliente redimensiona y recodifica la imagen a JPEG (lado mayor de 1600 px y calidad 80, bajando a 1280/70 y 1024/60 si aún supera el límite), así que las fotos de cámara y los formatos como HEIC caben sin pedir al usuario que las reduzca. Todas las políticas de Storage obtienen la carpeta con `private.storage_folder_uuid(name)`, nunca con un cast directo a `uuid`.

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
