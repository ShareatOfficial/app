# Navegación

## Objetivo

Cada feature declara qué navegación necesita y qué destinos tiene, sin conocer el back stack global. La app conserva el control de la topología, deep links y navegación entre features.

## Responsabilidades

### Módulo `ui` de la feature

- Define una interfaz por cada pantalla que emite intenciones de navegación.
- Recibe esa interfaz desde la pantalla; no recibe el `Navigator` global.
- Declara sus propias implementaciones `NavKey` serializables (sus destinos). Una feature no importa Koin Navigation 3 ni implementaciones del módulo de app.
- Una pantalla sin acciones de navegación no necesita interfaz.
- Una key cuyo destino requiere sesión implementa `RequiresLogin` (definida en `:shared:navigation`, ver más abajo) en lugar de comprobar el estado de sesión en la pantalla.

```kotlin
interface HomeNavigation {
    fun openHomeDetails()
}

@Serializable
data object HomeKey : NavKey

@Serializable
data object HomeDetailsKey : NavKey

@Composable
fun Home(
    modifier: Modifier = Modifier,
    navigator: HomeNavigation = koinInject(),
) {
    Button(onClick = navigator::openHomeDetails) {
        Text("Open home details")
    }
}
```

### `:shared:navigation`

Módulo compartido mínimo, sin Compose, con marcadores de navegación (`NavKey` vacíos como `RequiresLogin`) que varias features pueden necesitar. Al declarar una key nueva, comprueba este módulo: si alguno de sus marcadores aplica a esa pantalla, la key debe implementarlo. Hoy solo existe `interface RequiresLogin : NavKey`; en el futuro puede haber otros (p. ej. `OnlyRestaurants`).

### Módulo de app (`:shared:ui`)

- Implementa las interfaces de navegación de cada feature, importando las `NavKey` que esa feature declaró.
- Modifica el estado/back stack mediante el `Navigator` de la app.
- Declara las entradas Koin Navigation 3 y las agrupa por feature.
- Compone todos los módulos de navegación en `navigationModule`.
- Registra serializadores (usando las keys que importa de cada feature), top-level destinations y deep links.

```kotlin
class HomeNavigationImpl(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openHomeDetails() {
        navigator.navigate(HomeDetailsKey)
    }
}
```

## Entradas y módulos Koin

Cada módulo de navegación de `:shared:ui` registra las implementaciones como factories, importando la key correspondiente desde la feature. Estas pueden recibir el `Navigator` mediante parámetros o resolverlo automáticamente desde el contenedor Koin si ha sido registrado globalmente en la `App`.

```kotlin
val homeNavigationModule = module {
    factory<HomeNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        HomeNavigationImpl(navigator = navigator)
    }

    navigation<HomeKey> {
        Home()
    }
}
```

Los módulos se agregan sin duplicar entradas:

```kotlin
val navigationModule = module {
    includes(
        homeNavigationModule,
        profileNavigationModule,
    )
}
```

`App` obtiene un único `koinEntryProvider<NavKey>()`. Koin agrega las entradas declaradas en los módulos incluidos. Para habilitar la inyección automática en las pantallas, el `Navigator` debe ser declarado en el contenedor Koin dentro de la `App`:

```kotlin
val navigator = koinInject<Navigator> { parametersOf(navigationState) }
val koin = getKoin()
remember(navigator) { koin.declare(navigator) }
```

`LocalNavigator` solo se usa en la capa de navegación de `:shared:ui`; nunca dentro de una feature.

## Top-level y subpantallas

Solo los destinos mostrados en la barra o rail forman parte de `topLevelRoutes`. Las subpantallas, como `HomeDetailsKey` y `EditProfileKey`, se añaden al back stack activo mediante `Navigator.navigate()`.

Cada key, incluida cualquier subpantalla, debe registrarse también en el `SerializersModule` de `NavigationState` para poder restaurar el back stack. `:shared:ui` importa cada key desde su feature para este registro.

## Reglas de revisión

- Una pantalla no navega directamente con una key de otra feature.
- Cada pantalla que navega declara su propia interfaz; no se reutiliza una interfaz genérica con acceso a todo el grafo.
- Cada feature declara sus propias `NavKey`; las implementaciones de navegación (`*NavigationImpl`, `*NavigationModule`) permanecen en `:shared:ui`.
- Una key que requiere sesión implementa `RequiresLogin` desde `:shared:navigation`, no un chequeo ad-hoc en la feature o en el módulo de app.
- Los argumentos de navegación son mínimos, serializables y estables; la pantalla carga el dato completo mediante dominio. Excepción registrada: cuando quien navega ya tiene el dato completo en memoria, la key puede transportar el modelo de presentación completo y serializable (patrón *payload*, ver abajo).
- La app es la única que conoce el grafo completo y puede implementar navegación entre features.
- Cada key está registrada para restauración de estado.
- Una acción que requiere autenticación o rol se valida también en dominio/backend; la navegación solo mejora la experiencia de usuario.

## Argumentos como payload completo

Origen: `feature/restaurant-screen-ui`, 2026-09-01.

`RestaurantKey` transporta un `RestaurantArgs` completo (restaurante, menús, platos, precios, alérgenos y valoraciones) en vez de un identificador. La pantalla de restaurante se pinta entera en el primer frame y **no lanza ninguna petición al abrirse**; solo consulta dominio cuando el usuario hace *pull to refresh*.

Cuándo aplica:

- Quien navega ya tiene el dato completo cargado, de modo que pedirlo otra vez sería una petición redundante.
- El payload es un modelo de presentación de la feature destino, serializable y sin tipos de dominio con lógica.
- La pantalla sigue teniendo un camino de recarga contra dominio; el payload es un punto de partida, nunca la única fuente de verdad.

Cuándo no aplica:

- Deep links y restauración fría, donde no hay origen que aporte el payload: esos casos necesitan una key con identificador.
- Payloads grandes: el back stack se serializa en el estado guardado, así que el payload debe seguir siendo del orden de unos pocos KB.

La construcción del payload vive en `:shared:ui` (`HomeNavigationImpl` mapea `RestaurantDetails` a `RestaurantArgs`), no en la feature de origen, para que `:feature:home` no dependa de `:feature:restaurant`.

## Pruebas

- Probar las implementaciones de navegación con un estado/back stack controlado.
- Probar que cada intención añade o sustituye la key esperada.
- Probar back, restauración, top-level destinations y argumentos inválidos.
- Probar cada pantalla con una implementación fake de su interfaz de navegación.

## Referencias

- [Conceptos básicos de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/basics)
- [Modularización de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/modularize)

## Trabajo relacionado

- Mover las `NavKey` al módulo de cada feature: issue "[TASK] Move NavKey's to feature module".
