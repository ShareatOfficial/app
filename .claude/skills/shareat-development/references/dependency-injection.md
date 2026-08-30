# Dependency injection with Koin

## Goal

Assemble implementations at the app's edge with Koin, so mock vs. remote repositories can be swapped without touching domain or UI.

## Principles

- `domain`'s entities, use cases, and repositories stay free of Koin and any service locator — they receive dependencies through their constructor, never resolve them themselves.
- `domain` may publish its own Koin module, isolated in a separate `di` package (e.g. `org.shareat.feature.<name>.domain.di`), that binds a use case interface to its impl (`factory<XxxUseCase> { XxxUseCaseImpl(...) }`). This keeps `ui` agnostic of which class implements a use case — `ui` depends only on the interface.
- `data` publishes its repository and data-source definitions.
- `ui` publishes its ViewModel definitions, and `includes(...)` the feature's `domain` DI module when one exists, instead of instantiating the use case impl itself.
- `:shared:ui` is the composition root: it starts Koin exactly once, combines modules, and picks the environment's data mode.
- Nothing pulls a dependency globally from inside an entity, use case, or repository.

## Data modules

```kotlin
val fakeDataModule = module { /* deterministic repositories */ }
fun supabaseDataModule(config: SupabaseConfig) = module { /* runtime repositories */ }
```

`sharedModule` uses Supabase on Android, iOS, and web. `previewSharedModule` uses the fakes and is selected explicitly via `initKoin(useFakeData = true)` for demos, previews, and tests.

## Composition root

The app decides which modules load. The mock/remote selection comes from environment configuration — never from conditions scattered across screens or repositories.

```kotlin
modules(sharedModule, platformModule)
```

Each platform calls a shared initialization entry point. Android supplies Keystore-encrypted session storage; iOS supplies Keychain; web uses browser storage under a restrictive CSP.

## Scope rules

- `single` — clients, storage, shared sources, and repositories with no screen-level state.
- `factory` — lightweight use cases and objects with no shared identity.
- `viewModel` — ViewModels bound to the lifecycle Compose Multiplatform supports.
- Any other scope needs an explicit justified lifetime and a test proving it closes correctly.

## Overrides for tests

Unit tests build the class directly with fakes, by constructor. Koin is reserved for wiring tests that check the full graph starts up and resolves everything.

Don't start a global Koin container just to test a domain rule or an isolated ViewModel transition.

## Checklist

- [ ] All required dependencies come in through the constructor.
- [ ] Only the composition root selects fake vs. Supabase.
- [ ] `ui` includes `domain`'s `di` module instead of referencing the `XxxUseCaseImpl` class directly.
- [ ] A resolution test exists for the data graphs.
- [ ] Every scope has a justified lifetime.

## References

- [Koin for Compose and Compose Multiplatform](https://insert-koin.io/docs/reference/koin-compose/compose/)
- [Multiplatform ViewModels with Koin](https://insert-koin.io/docs/reference/koin-core/viewmodel/)
