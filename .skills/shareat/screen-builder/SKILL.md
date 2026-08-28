---
name: shareat-screen-builder
description: Build or refactor Shareat Jetpack Compose screens and composables using the project MVI/MVVM architecture, NavKey destination boundaries, navigator interfaces, Koin-injected ViewModels, stateless screen content, Compose performance practices, minimal stable parameters, multi-form-factor previews, preview data for every screen state, and module-aware visibility. Use for new routes, screens, extracted UI components, repeated UI patterns, and oversized composables in Shareat.
---

# Shareat Screen Builder

Implement Compose UI features in the Shareat project. Inspect the target module and nearby screens before editing so package names, resources, theme components, and navigation patterns match the existing code.

## Architecture

- Define a `UiState` data class for each screen.
- Expose screen state from its ViewModel as a `StateFlow`.
- Annotate ViewModels with `@Stable` and use `@KoinViewModel`.
- Use `koinInject()` for navigation and other injected dependencies, following the surrounding module's pattern.
- Keep business logic in the ViewModel or domain layer. Keep composables focused on rendering state and emitting events.

## NavKey destination screens

For every screen that represents a `NavKey` route or destination:

- Define a navigator interface named `[ScreenName]Navigator` for navigation actions owned by that destination.
- Create a public `[ScreenName]Screen` entry composable that receives the navigator interface and the screen ViewModel. Inject them with `koinInject()` and `koinViewModel()` by default.
- Collect `viewModel.uiState` only in the entry composable.
- Delegate rendering to a `[ScreenName]Stateless` composable, passing the collected `uiState` and event callbacks.
- Keep the navigator and ViewModel at the entry boundary. Never pass either object to `[ScreenName]Stateless` or any child composable.
- Pass public ViewModel actions and navigator actions as lambdas, preferably callable references such as `viewModel::onLogoutClick` and `navigator::openProfile`.

```kotlin
@Composable
public fun SettingsScreen(
    navigator: SettingsNavigator = koinInject(),
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenStateless(
        uiState = uiState,
        onLogoutClick = viewModel::onLogoutClick,
        onOpenProfile = navigator::openProfile,
    )
}

@Composable
private fun SettingsScreenStateless(
    uiState: SettingsUiState,
    onLogoutClick: () -> Unit,
    onOpenProfile: () -> Unit,
) {
    // Render the complete screen state and delegate to smaller composables.
}
```

## Composable API design

- Treat `[ScreenName]Stateless` as the single exception to the minimal-object rule: it receives the screen's complete `UiState` as its rendering contract.
- In every smaller rendering composable, pass only the values and callbacks it consumes. Never pass an entire domain object, UI model, `UiState`, navigator, or ViewModel merely so a child can read a few properties.
- Prefer stable parameters whenever possible: primitives, `String`, enums, immutable values, stable collection types, and stable callbacks. Do not add `@Stable` or `@Immutable` to a type unless its contract is actually satisfied.
- Unpack aggregate state in `[ScreenName]Stateless`, then pass individual values to its child composables.

Prefer:

```kotlin
@Composable
internal fun Cat(
    text: String,
    imageUrl: String,
) {
    // Render only the supplied values.
}
```

Avoid:

```kotlin
@Composable
internal fun Cat(animal: Animal) {
    Text(animal.name)
    AsyncImage(model = animal.imageUrl, contentDescription = animal.name)
}
```

## When to extract a composable

Create a separate composable when at least one of these conditions applies:

1. A composable is too large and splitting it produces smaller, understandable sections.
2. A cohesive UI block has a clear responsibility, such as a top bar containing a back icon, title, and trailing action.
3. A UI pattern is repeated, such as a settings row containing an icon, text, and divider.

Do not extract arbitrary wrappers or one-off fragments that do not improve readability, reuse, testing, or recomposition scope.

When a composable grows beyond roughly 80 lines, and especially when it approaches 100 lines, move it to its own `.kt` file under a `composables` package inside the feature's UI module.

## Visibility

- Declare a composable `private` when it is used only in the file where it is defined.
- Declare it `internal` when it is shared within the module.
- Make it public only when another module must consume it intentionally.

Use the narrowest visibility that supports the real call sites.

## Compose performance

Follow the official [Jetpack Compose performance best practices](https://developer.android.com/develop/ui/compose/performance/bestpractices):

- Move expensive calculations outside composition, preferably into the ViewModel. If calculation must remain in a composable, cache it with `remember` and all inputs as keys.
- Give lazy layout items stable, unique keys.
- Use `derivedStateOf` when rapidly changing input state should trigger recomposition only when a derived result changes.
- Defer frequently changing state reads to the narrowest scope that needs them. Prefer lambda-based layout or draw modifiers when the change affects only layout or drawing.
- Never write to state after reading it during composition. Update state from event callbacks or an appropriate side-effect API.

Apply these techniques where relevant; do not add caching or derived state without a concrete recomposition or calculation benefit.

## Preview requirements

- Give every rendering composable preview coverage for all four supported form factors: Mobile, Foldable, Tablet, and Desktop.
- Use real form-factor dimensions or device specifications, not previews that differ only by name. Reuse the project's standard preview specifications. If none exist, define them once in a reusable multi-preview annotation rather than inventing sizes in each file.
- Prefer one reusable form-factor multi-preview annotation so one preview function produces all four configurations. Otherwise, create four explicitly named preview functions.
- Wrap previews in the Shareat theme and provide no-op callbacks.
- Keep preview data deterministic and independent of Koin, ViewModels, repositories, network calls, clocks, and random values.
- Make preview functions and preview-only data `private` unless they are intentionally shared within the module.

For a `[ScreenName]Stateless` composable:

- Create preview data for every meaningful visual `UiState`, including each sealed subtype and applicable loading, populated, empty, error, disabled, or saving state.
- Preview every state on every form factor. Treat this as a state-by-form-factor matrix, not one state per device.
- Use clearly named fixtures such as `[ScreenName]PreviewData.loading`, `.content`, and `.error`.
- Preview `[ScreenName]Stateless` directly. Do not construct or inject a navigator or ViewModel in previews.

For a dependency-injected `[ScreenName]Screen` entry composable, use its `[ScreenName]Stateless` previews as the preview surface. The entry composable contains wiring rather than independently rendered UI.

## Project coding standards

1. Name screen files with the `Screen.kt` suffix and ViewModel files with the `ViewModel.kt` suffix.
2. Never hardcode user-facing strings. Use the relevant module's generated `Res` resources.
3. Add the required Mobile, Foldable, Tablet, and Desktop preview matrix whenever creating or changing a composable.
4. Wrap authentication screens in `LoginBackground` to preserve edge-to-edge consistency.

## Workflow

1. Inspect the target UI module, adjacent screens, shared components, resources, and navigation wiring.
2. Define the screen `UiState` and ViewModel behavior.
3. For a `NavKey` destination, define its navigator interface and public `[ScreenName]Screen` entry composable.
4. Inject the navigator and ViewModel, collect `uiState`, and pass the state plus callable-reference events to `[ScreenName]Stateless`.
5. Build child rendering composables with minimal stable parameters; never pass them the navigator or ViewModel.
6. Extract only cohesive, repeated, or oversized UI blocks; then apply the visibility and file-placement rules.
7. Review calculations, lazy keys, state reads, and state writes against the Compose performance rules.
8. Add deterministic preview data and verify the required form-factor matrix, including every `[ScreenName]Stateless` state; use resource-backed strings.
9. Wire the screen into `NavigationState` and `NavDisplay` in `App.kt` when navigation changes are part of the request.
10. Compile and run the narrowest relevant tests for the changed module.
