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
```text
Restaurant 1 — 1 Menu (MVP)
Restaurant 1 — N Dish
Menu       N — N Dish  (MenuItem)
```

Un restaurante publica un único menú en el MVP. Solo ese menú `Published` y sus platos habilitados llegan a una lectura pública, vía `MenuRepository.getPublishedMenu`; el ensamblado de esa regla vive en `RestaurantDetailsAssembler` (`:shared:domain`), no repetido en cada pantalla.

El nombre, la descripción, la imagen y los alérgenos pertenecen a `Dish`. El precio, la posición, la disponibilidad y la categoría (`DishCategory`: entrantes, principales, postres, para picar) dentro de un menú pertenecen a `MenuItem`, porque pueden variar entre menús. `MenuItem.category` es opcional: los fixtures la rellenan y el mapper de Supabase la deja a `null` hasta que exista la columna correspondiente (issue #64).

El precio usa unidades menores (`Money.minorUnits`): `1_800` representa 18,00 EUR. No se usa `Double` para valores monetarios.

Cada plato admite una imagen opcional en el MVP. Los alérgenos usan el catálogo de 14 grupos de la UE, una nota opcional y una fuente que deja claro que la información procede del restaurante.

## Reviews

Una única entidad `Review` usa un target tipado:

```text
ReviewTarget.Restaurant
ReviewTarget.Dish
```

Solo una cuenta customer activa puede escribir reviews. Existe como máximo una por autor y target; `saveReview` actualiza la existente. La valoración es un entero entre 1 y 5, el comentario y la fecha de visita son opcionales, y creación y última actualización se registran por separado.

El proyecto desplegado guarda tres alérgenos con ids más cortos que los canónicos de las migraciones (`gluten` en vez de `cereals_containing_gluten`, `soy` en vez de `soybeans`, `sulphites` en vez de `sulphur_dioxide_and_sulphites`). La base de datos remota ha divergido de `supabase/migrations/`. Hasta que se reconcilien, la lectura (`String.toEuAllergenOrNull`) acepta ambas grafías y la escritura (`EuAllergen.toDatabaseValue`) sigue emitiendo solo la canónica.

Regla general de mapeo de catálogo: **un valor desconocido se descarta, nunca hace fallar el agregado que lo contiene**. `toEuAllergenOrNull` devuelve `null` para un id no reconocido y `DishDto.toDomain` lo omite. Antes lanzaba, y un único alérgeno inesperado tumbaba la carta entera del restaurante: `getPublishedMenu` fallaba, el ensamblador devolvía `menu = null` y la pantalla mostraba "todavía no ha publicado su carta" en lugar de un error.

Las reviews públicas de varios platos se piden en lote con `ReviewRepository.getPublicDishReviews(dishIds)`, una sola consulta por sección en `RestaurantDetailsAssembler`, en vez de una por plato.

Los agregados incluyen únicamente reviews públicas con moderación `Visible`. `RatingSummary.averageTenths` evita errores de coma flotante: `48` representa una media de 4,8. La media se calcula en un único sitio, `RatingSummary.of(ratings)` (`:shared:domain`), que usan tanto los fakes como todo cálculo derivado de una lista de reviews (`List<Review>.toRatingSummary()`); `RatingSummary.Unrated` es el valor sin valoraciones.

## Repositorios fake

`FakeShareatData` es un almacén compartido con fixtures coherentes. Todos los repositorios del mismo grafo deben recibir la misma instancia para que una escritura sea visible en lecturas posteriores.

`FakeDataScenario` ofrece estados deterministas:

- `Populated`: fixtures representativos;
- `Empty`: colecciones vacías o `NotFound` para detalles;
- `Offline`: error tipado sin esperas reales;
- `Unavailable`: fallo recuperable del servicio.

`RepositoryError.Unavailable` es la única rama de reserva del mapeo de errores de Supabase y transporta un `details` opcional con la clase de excepción, el código HTTP y el código de error del servidor. Sin ese diagnóstico un fallo de autenticación real (`email_not_confirmed`, clave de API inválida, error de red) quedaba indistinguible de una caída del servicio. Los errores conocidos de Auth y PostgREST se mapean por código (`AuthErrorCode`, `SQLSTATE`), no por coincidencia de texto en el mensaje.

`fakeDataModule` enlaza las interfaces con estas implementaciones para previews y pruebas. `supabaseDataModule` enlaza los mismos contratos con Auth, PostgREST y Storage para runtime; las entidades, interfaces y consumidores no cambian por detalles del proveedor.

## Detalle de restaurante

`RestaurantDetails` (`org.shareat.app.domain.usecase`) es el agregado que consumen home y la pantalla de restaurante: restaurante, `RatingSummary`, platos destacados por reviews y el menú publicado con sus platos ya valorados (`RestaurantMenu` → `RatedMenuDish`), o `null` si todavía no publica ninguno. Se obtiene con `GetRestaurantsUseCase` (página) o `GetRestaurantUseCase` (uno), ambos apoyados en `RestaurantDetailsAssembler`.

`RatedMenuDish` lleva la **lista de reviews públicas** del plato (`reviews: List<Review>`), no un agregado ya aplanado, y expone `ratingSummary` derivado de esa lista. Una única fuente evita que la media que ve el usuario contradiga la lista de reseñas que se pinta a su lado, y permite que la UI muestre ambas cosas sin una segunda llamada. Es correcto porque `ReviewRepository.getPublicReviews` devuelve exactamente la población sobre la que se define el agregado: reviews `Public` con moderación `Visible`.

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
