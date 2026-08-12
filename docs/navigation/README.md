# Navegación

## Objetivo

Cada feature declara qué navegación necesita sin conocer el back stack global ni las keys concretas de la aplicación. La app conserva el control de la topología, deep links y navegación entre features.

## Responsabilidades

### Módulo `ui` de la feature

- Define una interfaz por cada pantalla que emite intenciones de navegación.
- Recibe esa interfaz desde la pantalla; no recibe el `Navigator` global.
- No importa `NavKey`, Koin Navigation 3 ni implementaciones del módulo de app.
- Una pantalla sin acciones de navegación no necesita interfaz.

```kotlin
interface HomeNavigation {
    fun openHomeDetails()
}

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

### Módulo de app

- Declara las implementaciones `NavKey` serializables.
- Implementa las interfaces de navegación de cada feature.
- Modifica el estado/back stack mediante el `Navigator` de la app.
- Declara las entradas Koin Navigation 3 y las agrupa por feature.
- Compone todos los módulos de navegación en `navigationModule`.
- Registra serializadores, top-level destinations y deep links.

```kotlin
@Serializable
data object HomeKey : NavKey

@Serializable
data object HomeDetailsKey : NavKey

class HomeNavigationImpl(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openHomeDetails() {
        navigator.navigate(HomeDetailsKey)
    }
}
```

## Entradas y módulos Koin

Cada módulo de navegación de `shared` registra las implementaciones como factories. Estas pueden recibir el `Navigator` mediante parámetros o resolverlo automáticamente desde el contenedor Koin si ha sido registrado globalmente en la `App`.

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

`LocalNavigator` solo se usa en la capa de navegación de `shared`; nunca dentro de una feature.

## Top-level y subpantallas

Solo los destinos mostrados en la barra o rail forman parte de `topLevelRoutes`. Las subpantallas, como `HomeDetailsKey` y `EditProfileKey`, se añaden al back stack activo mediante `Navigator.navigate()`.

Cada key, incluida cualquier subpantalla, debe registrarse también en el `SerializersModule` de `NavigationState` para poder restaurar el back stack.

## Reglas de revisión

- Una pantalla no navega directamente con una key de otra feature.
- Cada pantalla que navega declara su propia interfaz; no se reutiliza una interfaz genérica con acceso a todo el grafo.
- Las implementaciones de navegación y las keys permanecen en `shared`.
- Los argumentos de navegación son mínimos, serializables y estables; la pantalla carga el dato completo mediante dominio.
- La app es la única que conoce el grafo completo y puede implementar navegación entre features.
- Cada key está registrada para restauración de estado.
- Una acción que requiere autenticación o rol se valida también en dominio/backend; la navegación solo mejora la experiencia de usuario.

## Pruebas

- Probar las implementaciones de navegación con un estado/back stack controlado.
- Probar que cada intención añade o sustituye la key esperada.
- Probar back, restauración, top-level destinations y argumentos inválidos.
- Probar cada pantalla con una implementación fake de su interfaz de navegación.

## Referencias

- [Conceptos básicos de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/basics)
- [Modularización de Navigation 3](https://developer.android.com/guide/navigation/navigation-3/modularize)
