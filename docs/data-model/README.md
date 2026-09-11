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
Restaurant 1 — N Menu
Restaurant 1 — N Dish
Menu       N — N Dish   (MenuItem)
```

Un menú pertenece a un único restaurante y un restaurante puede tener varios, aunque hoy publique uno solo. `MenuRepository.getPublishedMenus` devuelve por eso una lista: los menús `Published` de un restaurante `Published`, con sus platos habilitados. La regla se ensambla en `PublishedMenuAssembler` (`:shared:domain`), del que dependen tanto `GetRestaurantMenuUseCase` como `RestaurantDetailsAssembler`, no repetida en cada pantalla. La pantalla de restaurante todavía muestra el primero; «sin menú publicado» no es un error, sino una lista vacía que el assembler traduce a `Success(null)`, reservando `Failure` para fallos reales de lectura.

El nombre, la descripción, la imagen y los alérgenos pertenecen a `Dish`. El precio, la posición, la disponibilidad y la categoría (`DishCategory`: entrantes, principales, postres, para picar) dentro de un menú pertenecen a `MenuItem`, porque pueden variar entre menús: el mismo plato puede costar distinto en la carta y en el menú del día. `MenuItem.category` es opcional: los fixtures la rellenan y el mapper de Supabase la deja a `null` hasta que exista la columna correspondiente (issue #64).

`Menu.price` es opcional y representa el precio cerrado de un menú de precio fijo; una carta lo deja a `null` y cobra plato a plato. Ambos precios usan unidades menores (`Money.minorUnits`): `1_800` representa 18,00 EUR. No se usa `Double` para valores monetarios.

La moneda la determina el restaurante, no cada relación menú-plato: vive en `Restaurant.currency` (`restaurants.currency_code`) y el mapper de Supabase la aplica al construir cada `Money`. Guardarla en `menu_items` repetía en cada fila un dato que ya determinaba el restaurante.

Cada plato admite una imagen opcional en el MVP. Los alérgenos usan el catálogo de 14 grupos de la UE, una nota opcional y una fuente que deja claro que la información procede del restaurante.

## Reviews

Una única entidad `Review` usa un target tipado:

```text
ReviewTarget.Restaurant
ReviewTarget.Dish
```

En persistencia eso son tres tablas: `reviews` con lo que toda reseña comparte (autor, `target_type`, valoración, comentario, visibilidad, moderación y fechas) y una hija por tipo de target, `restaurant_reviews` y `dish_reviews`, cada una con `review_id` como clave primaria y el FK a su entidad.

`target_type` no es redundante: es lo que hace declarativas las dos invariantes del diseño. La clave `(id, author_account_id, target_type)` de la madre es a la que apuntan las hijas, de modo que el `author_account_id` que llevan no puede divergir del de la madre y «una reseña por autor y target» sigue siendo un `unique` normal sobre la hija. Que exista exactamente una hija lo garantiza un *constraint trigger* diferido a commit, porque madre e hija se escriben en la misma transacción.

Las escrituras van por el RPC `save_review`, que hace ese upsert transaccional. Las lecturas van por dos vistas `security_invoker` que aplanan madre e hija, `restaurant_review_details` y `dish_review_details`, para que el cliente siga leyendo una fila por reseña. `ReviewRepository` y `ReviewTarget` no cambian.

Solo una cuenta customer activa puede escribir reviews. Existe como máximo una por autor y target; `saveReview` actualiza la existente. La valoración es un entero entre 1 y 5, el comentario y la fecha de visita son opcionales, y creación y última actualización se registran por separado.

Los tres alérgenos que el proyecto desplegado guardaba con ids más cortos que los canónicos (`gluten`, `soy`, `sulphites`) se renombraron a la grafía de las migraciones en `20260910120000_normalize_menu_and_dish_catalogue.sql`. Cada alérgeno es direccionable ahora por un único id, así que `String.toEuAllergenOrNull` ya no acepta grafías alternativas.

Regla general de mapeo de catálogo: **un valor desconocido se descarta, nunca hace fallar el agregado que lo contiene**. `toEuAllergenOrNull` devuelve `null` para un id no reconocido y `DishDto.toDomain` lo omite. Antes lanzaba, y un único alérgeno inesperado tumbaba la carta entera del restaurante: `getPublishedMenus` fallaba, el ensamblador devolvía `menu = null` y la pantalla mostraba "todavía no ha publicado su carta" en lugar de un error.

Las reviews públicas de varios platos se piden en lote con `ReviewRepository.getPublicDishReviews(dishIds)`, una sola consulta por sección, en vez de una por plato. La misma regla aplica a una lista de restaurantes: `DishRepository.getDishesByRestaurant(restaurantIds)` y `ReviewRepository.getRestaurantRatingSummaries(restaurantIds)` resuelven una página entera en una consulta cada uno. **Una consulta por página, nunca una por elemento**: un ensamblador que itera una lista llamando a un repositorio por elemento multiplica los viajes de red por el tamaño de la página (ver `RestaurantSummariesAssembler`). Los ids viajan en la query string, así que las implementaciones de Supabase parten los filtros `in` en lotes (`selectInBatches`).

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

Hay dos agregados, uno por pantalla, y la diferencia entre ambos es el menú:

- `RestaurantSummary` alimenta el feed de home: restaurante, `RatingSummary` y platos destacados por reviews. Lo devuelve `GetRestaurantsUseCase` (página) mediante `RestaurantSummariesAssembler`, que resuelve la página entera en tres consultas en lote, no en unas cuantas por restaurante. **No incluye menú**: la tarjeta de home no lo pinta, y cargarlo para toda la página era el coste dominante de la petición de home.
- `RestaurantDetails` alimenta el *pull to refresh* de la pantalla de restaurante: lo anterior más el menú publicado con sus platos ya valorados (`RestaurantMenu` → `RatedMenuDish`), o `null` si todavía no publica ninguno. Lo devuelve `GetRestaurantUseCase` (uno) mediante `RestaurantDetailsAssembler`.
- `RestaurantMenu` por sí solo alimenta la apertura de esa pantalla: viniendo de home, la cabecera ya viaja en los argumentos de navegación, así que `GetRestaurantMenuUseCase` pide **solo los platos** en vez de reensamblar el restaurante entero.

`RatedMenuDish` lleva la **lista de reviews públicas** del plato (`reviews: List<Review>`), no un agregado ya aplanado, y expone `ratingSummary` derivado de esa lista. Una única fuente evita que la media que ve el usuario contradiga la lista de reseñas que se pinta a su lado, y permite que la UI muestre ambas cosas sin una segunda llamada. Es correcto porque `ReviewRepository.getPublicReviews` devuelve exactamente la población sobre la que se define el agregado: reviews `Public` con moderación `Visible`.

## Persistencia Supabase

Las migraciones versionadas viven en `supabase/migrations`. La identidad de `accounts.id` coincide con `auth.users.id`; el trigger de registro valida una única vez `customer|restaurant`, crea `accounts` y crea `customer_profiles` cuando corresponde. La autorización posterior consulta tablas protegidas por RLS, nunca metadata mutable del JWT.

`dishes.restaurant_id` nombra el único restaurante al que pertenece el plato, y de ahí cuelga `private.owns_dish(dish_id)`. Se probó a sacar esa columna a una tabla `restaurant_dishes` y se revirtió: con `dish_id` como clave primaria era un desdoble 1:1 de la misma información, no ganaba nada en normalización —que un plato sea de un restaurante es una dependencia funcional de la clave del plato— y costaba un join en cada lectura de catálogo, además de un nombre de tabla puente que se lee como una N—N que nunca fue.

Derivar el restaurante del plato a través de `menu_items → menus` se descartó por dos motivos: no puede expresar «un plato de un solo restaurante» (nada impediría que estuviera en las cartas de dos), y `archive_restaurant_dish` borra a propósito las líneas de menú del plato, así que archivar dejaría el plato sin dueño y sin forma de recuperarlo.

La relación N-N menú/plato se materializa en `menu_items`, cuya clave es `(menu_id, dish_id)` y cuyos únicos atributos propios son los que dependen de ese par: precio, posición y disponibilidad. No repite `restaurant_id` —lo determina `menu_id`— ni `currency` —lo determina el restaurante—. La invariante que antes garantizaban las claves foráneas compuestas (un menú no puede listar el plato de otro restaurante) la impone ahora el trigger `private.assert_menu_item_restaurants_match`, y las políticas RLS de propietario cuelgan de `private.owns_menu(menu_id)`.

Por la misma razón `dishes` ya no guarda `allergen_source`: se derivaba de los alérgenos y la nota que la propia fila ya contiene, y el dominio lo reconstruye en `AllergenDeclaration.source`.

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
