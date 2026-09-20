# Localization

`docs/localization/README.md` is the source of truth; this is the compressed version.

## Where text lives

Every user-facing string lives in the module that paints it:

```
<module>/src/commonMain/composeResources/
  values/strings.xml      # English — the fallback for any locale without a translation
  values-es/strings.xml   # Spanish
```

Both files carry exactly the same keys. Keys are prefixed by feature (`login_`, `settings_`,
`restaurant_home_`, `home_`, `dish_review_`, `last_activity_`, `subscription_`, `nav_`) so they
read unambiguously and never collide.

A module that gains its first `composeResources` needs both of these in its `build.gradle.kts`:

```kotlin
android { androidResources { enable = true } }
commonMain.dependencies { implementation(libs.compose.components.resources) }
```

The generated package follows the Gradle path: `:feature:login:ui` →
`shareat.feature.login.ui.generated.resources`, `:shared:ui` → `shareat.shared.ui.generated.resources`.
Two modules override it explicitly in `compose.resources { packageOfResClass = … }`
(`:feature:restaurantHome`, `:shared:designsystem`) — check the build file before guessing.

## A ViewModel exposes a type, not a message

This is the rule that keeps `domain`/`data` free of Compose and the tests free of language.
A repository failure maps to an enum in the `ui` module; a `Labels.kt` beside the composables
turns it into text:

```kotlin
// HomeViewModel.kt
private fun RepositoryError.toHomeError(): HomeError = when (this) {
    RepositoryError.Offline -> HomeError.OFFLINE
    is RepositoryError.Conflict, is RepositoryError.Validation -> HomeError.UNKNOWN
    // ...
}

// composables/Labels.kt
@Composable
internal fun HomeError.label(): String = stringResource(
    when (this) {
        HomeError.OFFLINE -> Res.string.home_error_offline
        // ...
    },
)
```

`RepositoryError.Conflict`/`Validation` carry a backend-supplied `reason` that cannot be
translated — they collapse to the module's generic error rather than leaking server text.

When the error needs a parameter, it is a `sealed interface` instead of an enum
(`SettingsError.InvalidOpeningTime(day)`), and `label()` passes the parameter through
`stringResource(res, day.label())`.

The same applies to any state-derived text: `RestaurantCardUiState.ratingLabel` is `String?`
(null = unrated, the UI picks the wording), `LastActivityReviewUiState.type` is a
`LastActivityTargetType`, not `"Plato"`. A nav item holds a `StringResource`, not a `String`
(`TopLevelNavigationItem.label`).

Where a `Labels.kt` already exists in a module (`:feature:restaurant:ui`,
`:feature:restaurantHome`), extend it — do not create a second one.

## What stays a literal

Backend content (restaurant names, dish descriptions, review comments), third-party strings a
vendor SDK already localizes (RevenueCat error messages, product titles, prices), preview and
fixture data (`FakeShareatData`, `MockRestaurants`, `RestaurantPreviewData`), language endonyms in
a language picker (`English (US)`, `Español`, `Français`), and punctuation (`–`). Brand names
(`Shareat`, `Shareat Unlimited`) do go in `strings.xml`, with the same value in both files, so no
stray literal is left in a composable.

## Plurals and interpolation

`<plurals>` + `pluralStringResource` for counts; positional placeholders (`%1$s`, `%1$d`) so a
translation can reorder them. Locale-specific formatting belongs in the resource, not the code —
`restaurant_home_price` is `%1$d.%2$s €` in English and `%1$d,%2$s €` in Spanish.

## Reviewable rules

- No `Text("…")`, `label =`, `placeholder =` or `contentDescription =` literal in production code.
- No interface `String` on a ui state, mapper or ViewModel.
- A new key lands in `values/` **and** `values-es/` in the same commit.
- ViewModel tests assert the error type, never the sentence.

## The in-app language setting

Settings (both account types) offers **System language / English / Español**. It is a device
preference stored locally — not the account's `preferredLanguage`, which is loaded and saved back
unchanged and no longer has a UI control.

Layering:

- `:shared:domain` — `AppLanguage` (`System`/`English`/`Spanish`), `AppLanguageSelectionSupport`,
  `AppLanguageRepository` (`observeSelected()` returns a `StateFlow` so the composition root has the
  right value on its first frame).
- `:shared:data` — `AppLanguageStorage` (persist) and `AppLanguageApplier` (apply to the platform
  locale) as separate interfaces with per-platform classes bound in `:shared:ui`'s `platformModule`,
  the same shape as `SecureSessionStorage`. `LocalAppLanguageRepository` re-applies the stored choice
  **in its constructor**, so the locale is correct before the first `stringResource` runs.
- `:feature:settings:ui` — only sees `ObserveAppLanguageUseCase`, `GetAppLanguageSupportUseCase` and
  `SelectAppLanguageUseCase`.

### Why `App()` wraps its content in `key(appLanguage)`

CMP 1.11.1 has no public way to point `stringResource` at a chosen language: `LocalComposeEnvironment`
is `internal` and `Locale.current` is a platform static, not a `CompositionLocal`. The only lever is
changing the platform locale and rebuilding the tree. The navigation state is hoisted **above** that
`key` so switching language does not clear the back stack.

Per-platform reality, surfaced to the user as a note under the picker:

| Platform | `AppLanguageSelectionSupport` | How |
| --- | --- | --- |
| Android | `IMMEDIATE` | `LocaleList.setDefault(...)` — resources resolve against the process locale, no activity recreation |
| iOS | `NEXT_LAUNCH` | writes `AppleLanguages` to `NSUserDefaults`; `NSLocale.preferredLanguages` resolves once per process |
| Web | `UNSUPPORTED` | `Locale.current` reads `navigator.languages`; the row is disabled |

### Koin gotcha

The Koin compile-safety plugin reports `KOIN-D002 Missing definition` for a `koinInject<T>()` in
`:shared:ui` when `T` is bound in another Gradle module. Wrap it the way `SessionCoordinator` and
`RestaurantProfileCoordinator` already do — a small class declared in `applicationModule` that takes
the repository via `get()` — rather than injecting the repository into the composable directly.
