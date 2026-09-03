# Modelo de dominio y repositorios del MVP

## Objetivo

Definir un modelo común para previews, datos fake deterministas y Supabase sin acoplar la UI al proveedor de datos.

La implementación se divide en módulos Gradle con dependencias dirigidas hacia dominio:

```text
:shared:domain  <-  :shared:data  <-  :shared:ui
```

`:shared:domain` contiene `org.shareat.app.domain.model` y `org.shareat.app.domain.repository`. No depende de Compose, Koin ni de `data`. Las interfaces son `suspend` para que una implementación remota pueda reemplazar a los repositorios fake sin cambiar sus consumidores.

`:shared:data` contiene `org.shareat.app.data` y depende de dominio y Koin Core, pero no de Compose. `:shared:ui` selecciona el módulo de datos, inicializa Koin y contiene toda la UI y navegación compartida.

## Identidad y restaurantes

`Account` representa autenticación, autorización y estado de acceso. No almacena una contraseña; esta pertenece al proveedor de autenticación y nunca a fixtures o entidades de dominio.

`CustomerProfile` y `Restaurant` no heredan de `Account`:

```text
Account (Customer)   1 — 1 CustomerProfile
Account (Restaurant) 1 — 1 Restaurant
```

En el MVP una cuenta de restaurante administra exactamente un restaurante. El correo de acceso (`Account.loginEmail`) se mantiene separado del correo público opcional del restaurante.

## Restaurante y horario

`Restaurant` contiene dirección postal estructurada, coordenadas opcionales, contacto público y horario semanal. Cada día admite cero o más periodos para representar cierres y horarios partidos. Un periodo cuya hora de cierre sea anterior a la apertura termina después de medianoche.

Las excepciones por festivos o cierres puntuales se añadirán más adelante sin convertir el horario semanal en texto libre.

## Platos

Un plato pertenece al catálogo de un restaurante. El nombre, la descripción, la imagen y los alérgenos pertenecen a `Dish`.

Cada plato admite una imagen opcional en el MVP. Los alérgenos usan el catálogo de 14 grupos de la UE, una nota opcional y una fuente que deja claro que la información procede del restaurante.

## Reviews

Una única entidad `Review` usa un target tipado:

```text
ReviewTarget.Restaurant
ReviewTarget.Dish
```

Solo una cuenta customer activa puede escribir reviews. Existe como máximo una por autor y target; `saveReview` actualiza la existente. La valoración es un entero entre 1 y 5, el comentario y la fecha de visita son opcionales, y creación y última actualización se registran por separado.

Los agregados incluyen únicamente reviews públicas con moderación `Visible`. `RatingSummary.averageTenths` evita errores de coma flotante: `48` representa una media de 4,8.

## Repositorios fake

`FakeShareatData` es un almacén compartido con fixtures coherentes. Todos los repositorios del mismo grafo deben recibir la misma instancia para que una escritura sea visible en lecturas posteriores.

`FakeDataScenario` ofrece estados deterministas:

- `Populated`: fixtures representativos;
- `Empty`: colecciones vacías o `NotFound` para detalles;
- `Offline`: error tipado sin esperas reales;
- `Unavailable`: fallo recuperable del servicio.

`RepositoryError.Unavailable` es la única rama de reserva del mapeo de errores de Supabase y transporta un `details` opcional con la clase de excepción, el código HTTP y el código de error del servidor. Sin ese diagnóstico un fallo de autenticación real (`email_not_confirmed`, clave de API inválida, error de red) quedaba indistinguible de una caída del servicio. Los errores conocidos de Auth y PostgREST se mapean por código (`AuthErrorCode`, `SQLSTATE`), no por coincidencia de texto en el mensaje.

`fakeDataModule` enlaza las interfaces con estas implementaciones para previews y pruebas. `supabaseDataModule` enlaza los mismos contratos con Auth, PostgREST y Storage para runtime; las entidades, interfaces y consumidores no cambian por detalles del proveedor.

## Persistencia Supabase

Las migraciones versionadas viven en `supabase/migrations`. La identidad de `accounts.id` coincide con `auth.users.id`; el trigger de registro valida una única vez `customer|restaurant`, crea `accounts` y crea `customer_profiles` cuando corresponde. La autorización posterior consulta tablas protegidas por RLS, nunca metadata mutable del JWT.

Los agregados de rating son vistas `security_invoker` que solo consideran reviews públicas y visibles.

## Reglas revisables

- Ningún modelo de `domain` importa Koin, Compose, DTO o clases fake.
- `:shared:domain` y `:shared:data` no aplican plugins Compose ni declaran dependencias Compose.
- Las referencias entre entidades usan IDs tipados.
- Los secretos y contraseñas no aparecen en el modelo ni en fixtures.
- La UI consume interfaces de repositorio, no `FakeShareatData`.
- Un menú público no devuelve platos o asociaciones deshabilitadas.
- Una review privada u oculta no contribuye al agregado público.
- Las implementaciones remotas deberán conservar el comportamiento observable de estos contratos.
