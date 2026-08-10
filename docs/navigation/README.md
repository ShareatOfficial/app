# Navegación

## Objetivo

Cada feature declara qué navegación necesita sin conocer el back stack global ni las keys concretas de la aplicación. La app conserva el control de la topología, deep links y navegación entre features.

## Responsabilidades

### Módulo `ui` de la feature

- Define una interfaz por pantalla o flujo con intenciones de navegación.
- Recibe esa interfaz desde la pantalla o el ViewModel; no recibe el `Navigator` global.
- Expone su entry builder, responsable de construir el contenido de la pantalla.
- No importa keys ni implementaciones del módulo de app.

```kotlin
interface HomeNavigation {
    fun openRestaurant(restaurantId: String)
    fun openProfile()
}

@Composable
fun HomeScreen(navigation: HomeNavigation) {
    // La UI comunica intenciones mediante HomeNavigation.
}
```

### Módulo de app

- Declara las implementaciones `NavKey` serializables.
- Implementa las interfaces de navegación de cada feature.
- Modifica el estado/back stack mediante el `Navigator` de la app.
- Compone en un único `entryProvider` los entry builders de las features.
- Registra serializadores, top-level destinations y deep links.

```kotlin
@Serializable
data object HomeKey : NavKey

class AppHomeNavigation(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openRestaurant(restaurantId: String) {
        navigator.navigate(RestaurantKey(restaurantId))
    }

    override fun openProfile() {
        navigator.navigate(ProfileKey)
    }
}
```

## Entry builders

La feature es propietaria del builder que crea su `NavEntry`; la app aporta el tipo de key y la implementación de navegación. De esta forma la feature no necesita depender de la app.

```kotlin
inline fun <reified K : NavKey> EntryProviderScope<NavKey>.homeEntry(
    noinline navigation: (K) -> HomeNavigation,
) {
    entry<K> { key ->
        HomeScreen(navigation = navigation(key))
    }
}
```

La app compone todos los builders:

```kotlin
val appEntryProvider = entryProvider<NavKey> {
    homeEntry<HomeKey> { AppHomeNavigation(navigator) }
    profileEntry<ProfileKey> { AppProfileNavigation(navigator) }
}
```

El nombre exacto de tipos o funciones podrá ajustarse al implementar #9, pero debe mantenerse la propiedad indicada arriba y no introducir una dependencia `feature -> app`.

## Reglas de revisión

- Una pantalla no navega directamente con una key de otra feature.
- Los argumentos de navegación son mínimos, serializables y estables; la pantalla carga el dato completo mediante dominio.
- La app es la única que conoce el grafo completo y puede implementar navegación entre features.
- Cada key está registrada para restauración de estado.
- Una acción que requiere autenticación o rol se valida también en dominio/backend; la navegación solo mejora la experiencia de usuario.

## Pruebas

- Probar las implementaciones de navegación con un estado/back stack controlado.
- Probar que cada intención añade o sustituye la key esperada.
- Probar back, restauración, top-level destinations y argumentos inválidos.
- Probar el entry builder con una implementación fake de la interfaz de navegación.

## Referencias

- [Conceptos básicos de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/basics)
- [Modularización de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/modularize)
