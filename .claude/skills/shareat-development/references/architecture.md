# Feature architecture

## Goal

Let features evolve in parallel and let the data source switch from mocks to the real backend without touching use cases, ViewModels, or screens.

## Module split

Each feature is three Gradle modules:

```
:feature:<name>:domain
:feature:<name>:data
:feature:<name>:ui
```

The shared base mirrors this, plus two extra leaf modules: one for shared navigation contracts, one for shared UI.

```
:shared:domain
:shared:data
:shared:navigation
:shared:designsystem
:shared:ui
```

`:shared:navigation` applies no Compose plugin and depends on nothing else in the project. It holds only navigation contracts that multiple features and `:shared:ui` need in common — today just the `RequiresLogin` marker interface — so that a feature can declare a key requiring login without depending back on `:shared:ui` (which would be circular, since `:shared:ui` already depends on every feature). Keep it that way deliberately: `Navigator`, `NavigationState`, and each feature's `*NavigationImpl`/`*NavigationModule` stay in `:shared:ui`, not here — they reference concrete feature Composables, and pulling them into `:shared:navigation` would make it depend on the same features that depend on it, recreating the cycle it exists to avoid. See the "Decided" note in `navigation.md`.

`:shared:designsystem` applies the Compose plugin but depends on nothing else — no `domain`, no `data`, no Koin, no feature module. It holds presentation-only visual building blocks shared across features (today, `Modifier.shimmerEffect` for loading skeletons). Any `:feature:<name>:ui` module can depend on it directly, the same way it already depends on `:shared:navigation`. See `ui.md`.

`:shared:ui` is the app's composition module: it assembles features, navigation, and dependencies for Android, iOS, and web, and produces the `Shared` framework iOS consumes.

Note: not every feature in the repo has all three modules split out yet (some are still a single module while migration is in progress) — check `settings.gradle.kts` for what's actually declared before assuming the full split exists for a given feature.

### `domain`

Contains: domain entities and value objects, repository interfaces, use cases and business rules, errors/results expressed in product terms.

Does **not** depend on `data`, `ui`, Compose, DTOs, storage, or network clients. `:shared:domain` follows the same rule.

Models always live in a `model/` package inside their own layer and their own module — never declared alongside the use case, repository, or ViewModel that consumes them. `:feature:<name>:domain` declares them in `…feature/<name>/domain/model/`; `:shared:domain` uses `org.shareat.app.domain.model`. A use case file holds the use case, not the types it takes or returns.

One Koin exception: a `domain` module may publish its own Koin module, isolated in a `di` package, binding a use case interface to its impl. Entities, use cases, and repositories still receive dependencies by constructor and never resolve anything themselves. `:shared:domain` does this in `org.shareat.app.domain.usecase.di.sharedDomainModule`.

A use case that more than one feature needs lives in `:shared:domain`, not in whichever feature wrote it first — a feature never imports another feature's `domain`. That is why `GetRestaurantsUseCase` (home's list) and `GetRestaurantUseCase` (the detail screen) sit there sharing `RestaurantDetailsAssembler` and the same repositories. **Each use case exposes exactly one public method**; two different queries are two use cases, never two methods on one interface.

### `data`

Contains: mock and remote repository implementations, data sources and fixtures, DTOs and persistence models, mappers between external representations and domain.

Depends on `domain`. DTOs and provider-specific details never leak into the public API `domain` or `ui` consume. `data` (including `:shared:data`) applies no Compose plugin and declares no Compose dependencies or resources.

### `ui`

Contains: the feature's screens and components, ViewModels/state/effects, navigation interfaces expressed as intents (not concrete navigation).

Depends on `domain` only — never on a concrete `data` implementation or the app module. The same model rule applies here: ui states, args, and presentation models are declared in `…feature/<name>/ui/model/`, not inside the screen or ViewModel file. A feature declares its own `NavKey`s (its destinations) alongside its navigation interface. The navigation interface implementations and the Koin Navigation 3 entries live in `:shared:ui`, which imports each feature's keys and wires each screen into the app's graph (see `navigation.md`).

## Dependency direction

```
 Android / iOS / Web
          |
          v
    :shared:ui --------------> :shared:data
          |                          |
          |                          v
          +------------------> :shared:domain
          |
          +------------------> :feature:<name>:ui
                                      |
                                      v
                            :feature:<name>:domain
```

A dependency between two features must go through an explicit API. Never import another feature's internal implementation to reuse a screen, repository, or ViewModel.

## Data flow

```
Screen -> ViewModel -> Use case -> Repository (interface)
                                      |
                                      +-> MockRepository
                                      +-> RemoteRepository (Supabase)
```

`data` maps fixtures or remote responses into domain models before the repository call completes. The UI maps domain into presentation state only when it actually needs to.

## Checklist for a new feature

- [ ] All three modules are declared in `settings.gradle.kts`.
- [ ] `domain` knows nothing about frameworks or infrastructure.
- [ ] `:shared:domain` and `:shared:data` apply no Compose plugin and contain no Compose imports or resources.
- [ ] `ui` depends only on `domain` and presentation libraries.
- [ ] `data` implements the interfaces `domain` defines.
- [ ] The app module wires navigation and dependencies (composition root, see `dependency-injection.md`).
- [ ] Repository, use case, and ViewModel tests exist (see `testing.md`).
- [ ] The affected `docs/` guide is updated if a new pattern shows up (see the "Keeping this skill and docs/ in sync" section of SKILL.md).

## Related tickets

- Backend and mock contracts: [#34](https://github.com/ShareatOfficial/app/issues/34)
- Data model: [#35](https://github.com/ShareatOfficial/app/issues/35)
- Authentication and authorization: [#33](https://github.com/ShareatOfficial/app/issues/33)
- Shared base implementation: [#9](https://github.com/ShareatOfficial/app/issues/9)
