# Navigation

## Goal

Each feature declares what navigation it needs and what destinations it has, without knowing the global back stack. The app module owns topology, deep links, and cross-feature navigation.

## Responsibilities

### The feature's `ui` module

- Defines one interface per screen that emits navigation intents.
- Receives that interface via the screen's parameters — never the global `Navigator`.
- Declares its own serializable `NavKey` implementations (its destinations). A feature never imports Koin Navigation 3 or app-module implementations.
- A screen with no navigation actions doesn't need an interface at all.
- A key whose destination requires an authenticated session implements `RequiresLogin` (from `:shared:navigation`, see below) instead of checking session state in the screen.

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

A minimal, non-Compose shared module holding navigation markers (empty `NavKey` interfaces like `RequiresLogin`) that multiple features may need. **When declaring a new key, check this module: if one of its markers applies to that screen, the key must implement it.** Today it declares exactly one: `interface RequiresLogin : NavKey`. More may be added later (e.g. `OnlyRestaurants`).

### The app module (`:shared:ui`)

- Implements each feature's navigation interface, importing the `NavKey`s that feature declared.
- Mutates the back stack through the app's `Navigator`.
- Declares the Koin Navigation 3 entries, grouped by feature.
- Composes every navigation module into `navigationModule`.
- Registers serializers (using the keys it imports from each feature), top-level destinations, and deep links.

```kotlin
class HomeNavigationImpl(
    private val navigator: Navigator,
) : HomeNavigation {
    override fun openHomeDetails() {
        navigator.navigate(HomeDetailsKey)
    }
}
```

## Entries and Koin modules

Each `:shared:ui` navigation module registers implementations as factories, importing the matching key from the feature. They can receive the `Navigator` via parameters, or resolve it automatically from the Koin container if it's been registered globally in `App`.

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

Modules are aggregated without duplicating entries:

```kotlin
val navigationModule = module {
    includes(
        homeNavigationModule,
        profileNavigationModule,
    )
}
```

`App` obtains a single `koinEntryProvider<NavKey>()`; Koin aggregates the entries from every included module. For screens to get automatic `Navigator` injection, it must be declared in the Koin container inside `App`:

```kotlin
val navigator = koinInject<Navigator> { parametersOf(navigationState) }
val koin = getKoin()
remember(navigator) { koin.declare(navigator) }
```

`LocalNavigator` is only used in `:shared:ui`'s navigation layer — never inside a feature.

## Top-level vs. subscreens

Only destinations shown in the bar/rail are `topLevelRoutes`. Subscreens (e.g. `HomeDetailsKey`, `EditProfileKey`) are pushed onto the active back stack via `Navigator.navigate()`.

Every key — including subscreens — must be registered in `NavigationState`'s `SerializersModule` so the back stack can be restored. `:shared:ui` imports each key from its feature for this registration.

## Review rules

- A screen never navigates directly with another feature's key.
- Each navigating screen declares its own interface — no reusing a generic interface with access to the whole graph.
- Each feature declares its own `NavKey`s; navigation implementations (`*NavigationImpl`, `*NavigationModule`) stay in `:shared:ui`.
- A key that requires a session implements `RequiresLogin` from `:shared:navigation` — not an ad-hoc check in the feature or the app module.
- Navigation arguments are minimal, serializable, and stable; the destination screen loads the full data through domain — **except** for the registered payload pattern below.
- Only the app knows the complete graph and can implement cross-feature navigation.
- Every key is registered for state restoration.
- An action requiring auth or a role is also validated in domain/backend — navigation only improves UX, it's not the authorization boundary.

## Arguments as a complete payload

Registered exception (`feature/restaurant-screen-ui`, 2026-09-01). `RestaurantKey` carries a complete `RestaurantArgs` (restaurant, menus, dishes, prices, allergens, ratings) instead of an id, so the restaurant screen paints on the first frame and **issues no call when it opens**; it queries domain only on pull to refresh.

Use it when the caller already holds the loaded data (re-fetching would be a redundant call), the payload is a serializable presentation model owned by the destination feature, and the screen still has a domain refresh path — the payload is a starting point, never the only source of truth.

Don't use it for deep links or cold restoration (no caller supplies the payload — those need an id key), or for large payloads: the back stack is serialized into saved state, so keep it to a few KB.

The payload is built in `:shared:ui` (`HomeNavigationImpl` maps `RestaurantDetails` to `RestaurantArgs`), never in the calling feature, so `:feature:home` never depends on `:feature:restaurant`.

## Tests

- Test navigation implementations against a controlled state/back stack.
- Test that each intent pushes or replaces the expected key.
- Test back, restoration, top-level destinations, and invalid arguments.
- Test each screen with a fake implementation of its navigation interface.

## References

- [Navigation 3 basics](https://developer.android.com/guide/navigation/navigation-3/basics)
- [Navigation 3 modularization](https://developer.android.com/guide/navigation/navigation-3/modularize)
