# Domain model and repositories

## Goal

One common model serves previews, deterministic fake data, and Supabase, without coupling the UI to the data provider.

Modules point inward toward domain:

```
:shared:domain  <-  :shared:data  <-  :shared:ui
```

`:shared:domain` holds `org.shareat.app.domain.model` and `org.shareat.app.domain.repository`. It depends on neither Compose, Koin, nor `data`. Repository interfaces are `suspend` so a remote implementation can replace the fake ones without changing any consumer.

`:shared:data` holds `org.shareat.app.data`, depends on domain and Koin Core, but not Compose. `:shared:ui` selects the data module, initializes Koin, and holds all shared UI/navigation.

## Identity and restaurants

`Account` represents authentication, authorization, and access state. It never stores a password — that belongs to the auth provider, never to fixtures or domain entities.

`CustomerProfile` and `Restaurant` do **not** inherit from `Account`:

```
Account (Customer)   1 — 1 CustomerProfile
Account (Restaurant) 1 — 1 Restaurant
```

In the MVP, one restaurant account manages exactly one restaurant. `Account.loginEmail` (access) stays separate from the restaurant's optional public contact email.

## Restaurant and schedule

`Restaurant` has a structured postal address, optional coordinates, public contact info, and a weekly schedule. Each day can have zero or more periods (to represent closures and split hours). A period whose closing time is before its opening time ends after midnight.

Holiday/one-off closure exceptions are a future addition — don't turn the weekly schedule into free text to accommodate them.

## Menus and dishes

A dish belongs to one restaurant's catalogue and can appear on several menus — a many-to-many relationship through `MenuItem`:

```
Restaurant 1 — 1 Menu (MVP)
Restaurant 1 — N Dish
Menu       N — N Dish  (via MenuItem)
```

A restaurant publishes one menu in the MVP. Only that `Published` menu and its enabled dishes reach a public read path, via `MenuRepository.getPublishedMenu`; that rule is enforced once, in `RestaurantDetailsAssembler` (`:shared:domain`), not repeated per screen.

Name, description, image, and allergens belong to `Dish`. Price, position, availability, and `DishCategory` (starters / mains / desserts / small bites) within a specific menu belong to `MenuItem`, because they can vary between menus. `MenuItem.category` is nullable: fixtures populate it, and the Supabase mapper leaves it null until the column exists.

Price uses minor units: `Money.minorUnits`, where `1_800` = 18,00 EUR. Never `Double` for money.

Each dish has an optional image in the MVP. Allergens use the EU's 14-group catalogue, plus an optional note and a source that makes clear the allergen info comes from the restaurant (not verified by Shareat).

## Restaurant details

`RestaurantDetails` (`org.shareat.app.domain.usecase`) is the aggregate home and the restaurant screen both consume: restaurant, `RatingSummary`, review-backed dish highlights, and the published menu with its already-rated dishes (`RestaurantMenu` → `RatedMenuDish`), or null when it publishes none. Fetch it with `GetRestaurantsUseCase` (a page) or `GetRestaurantUseCase` (one); both delegate to `RestaurantDetailsAssembler`.

`RatedMenuDish` carries the dish's **public review list** (`reviews: List<Review>`), not a pre-flattened aggregate, and exposes `ratingSummary` derived from it. One source keeps the average a screen shows from contradicting the reviews rendered next to it, and lets the UI draw both without a second call. This is sound because `ReviewRepository.getPublicReviews` returns exactly the population the aggregate is defined over: `Public` reviews with `Visible` moderation.

## Reviews

A single `Review` entity uses a typed target:

```
ReviewTarget.Restaurant
ReviewTarget.Dish
```

Only an active customer account can write reviews. At most one review per author+target — `saveReview` updates the existing one instead of creating a duplicate. Rating is an integer 1–5; comment and visit date are optional; created and last-updated are tracked separately.

The deployed project stores three allergens under shorter ids than the canonical ones in the migrations (`gluten` for `cereals_containing_gluten`, `soy` for `soybeans`, `sulphites` for `sulphur_dioxide_and_sulphites`) — the remote database has drifted from `supabase/migrations/`. Until they are reconciled, reads (`String.toEuAllergenOrNull`) accept both spellings while writes (`EuAllergen.toDatabaseValue`) still emit only the canonical id.

General catalogue-mapping rule: **an unrecognised value is dropped, never allowed to fail the aggregate that contains it.** `toEuAllergenOrNull` returns null for an unknown id and `DishDto.toDomain` skips it. It previously threw, and a single unexpected allergen took down a restaurant's whole menu: `getPublishedMenu` failed, the assembler returned `menu = null`, and the screen reported "no menu published" instead of an error. When you add a mapper from a database string to a domain enum, make the unknown branch degrade, not throw.

Public reviews for several dishes are fetched in one batch via `ReviewRepository.getPublicDishReviews(dishIds)` — one query per section in `RestaurantDetailsAssembler`, not one per dish. Prefer a batched repository method over an N-per-entity loop in an assembler.

Aggregates only include public reviews with `Visible` moderation status. `RatingSummary.averageTenths` avoids floating-point error: `48` means an average of 4.8. The mean is computed in exactly one place, `RatingSummary.of(ratings)` in `:shared:domain` — used by the fakes and by anything deriving a summary from a review list (`List<Review>.toRatingSummary()`). `RatingSummary.Unrated` is the no-ratings value; don't hand-roll `RatingSummary(null, 0)` or the rounding formula again.

## Fake repositories

`FakeShareatData` is a shared in-memory store with coherent fixtures. Every repository in the same graph must receive the **same instance** so a write is visible to later reads.

`FakeDataScenario` provides deterministic states:

- `Populated` — representative fixtures.
- `Empty` — empty collections, or `NotFound` for detail views.
- `Offline` — a typed error, no real waiting.
- `Unavailable` — a recoverable service failure.

`RepositoryError.Unavailable(details: String?)` is the single fallback branch of the Supabase error mapping (`shared/data/.../supabase/SupabaseResult.kt`) and carries the exception class, HTTP status and server error code. Map known failures by code — `AuthErrorCode` for Auth, `SQLSTATE` for PostgREST — never by substring matching on the exception message; the message includes the request URL and headers, so text matching both misses real cases and risks leaking request metadata into user-facing errors.

`fakeDataModule` binds interfaces to these implementations for previews/tests. `supabaseDataModule` binds the same contracts to Auth, PostgREST, and Storage for runtime — entities, interfaces, and consumers never change because of provider details.

## Supabase persistence

Versioned migrations live in `supabase/migrations`. `accounts.id` matches `auth.users.id`; the sign-up trigger validates `customer|restaurant` exactly once, creates `accounts`, and creates `customer_profiles` when appropriate. Authorization afterward queries RLS-protected tables — never mutable JWT metadata.

The menu/dish N-N relation is materialized as `menu_items`, which includes `restaurant_id` plus composite foreign keys to prevent cross-restaurant associations. Rating aggregates are `security_invoker` views that only consider public, visible reviews.

## Reviewable rules

- No `domain` model imports Koin, Compose, DTOs, or fake classes.
- `:shared:domain` and `:shared:data` apply no Compose plugin and declare no Compose dependencies.
- Entity references use typed IDs.
- Secrets and passwords never appear in the model or in fixtures.
- The UI consumes repository interfaces, never `FakeShareatData` directly.
- A public menu never returns disabled dishes or associations.
- A private or hidden review never contributes to a public aggregate.
- Any remote implementation must preserve these contracts' observable behavior.
