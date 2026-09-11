# Domain model and repositories

## Goal

One common model serves previews, deterministic fake data, and Supabase, without coupling the UI to the data provider.

Modules point inward toward domain:

```
:shared:domain  <-  :shared:data  <-  :shared:ui
```

`:shared:domain` holds `org.shareat.app.domain.model` and `org.shareat.app.domain.repository`. It depends on neither Compose, Koin, nor `data`. Repository interfaces are `suspend` so a remote implementation can replace the fake ones without changing any consumer.

`:shared:data` holds `org.shareat.app.data`, depends on domain and Koin Core, but not Compose. `:shared:ui` selects the data module, initializes Koin, and holds all shared UI/navigation.

**One repository per file.** Each repository interface lives in a file named after it (`MenuRepository.kt`), and so does each implementation (`SupabaseMenuRepository.kt`, `FakeMenuRepository.kt`). Supabase DTOs live in `org.shareat.app.data.supabase.model` and their domain mappers in `org.shareat.app.data.supabase.mapper`, grouped per aggregate (`MenuDtos.kt` / `MenuMappers.kt`), never inside the repository file that consumes them.

## Identity and restaurants

`Account` represents authentication, authorization, and access state. It never stores a password — that belongs to the auth provider, never to fixtures or domain entities.

`CustomerProfile` and `Restaurant` do **not** inherit from `Account`:

```
Account (Customer)   1 — 1 CustomerProfile
Account (Restaurant) 1 — 1 Restaurant
```

In the MVP, one restaurant account manages exactly one restaurant. `Account.loginEmail` (access) stays separate from the restaurant's optional public contact email.

## Restaurant and schedule

`Restaurant` has a structured postal address, optional coordinates, public contact info, and a weekly schedule. `address` is **nullable**: a restaurant may stay in draft without one and complete it later from settings. It becomes mandatory at exactly one boundary — publishing — and that rule lives in the database (`restaurants_published_requires_address`), not in whichever screen submits the change; `update_restaurant_settings` checks it first only so the owner gets a readable message. Half an address is treated as none, both when mapping from Postgres and in the forms. Each day can have zero or more periods (to represent closures and split hours). A period whose closing time is before its opening time ends after midnight.

Holiday/one-off closure exceptions are a future addition — don't turn the weekly schedule into free text to accommodate them.

## Menus and dishes

A dish belongs to one restaurant's catalogue and can appear on several menus — a many-to-many relationship through `MenuItem`:

```
Restaurant 1 — N Menu
Restaurant 1 — N Dish
Menu       N — N Dish   (through MenuItem)
```

A menu belongs to exactly one restaurant, and a restaurant may own several — today it publishes one. `MenuRepository.getPublishedMenus` therefore returns a **list**: the `Published` menus of a `Published` restaurant with their enabled dishes. That rule is enforced once, in `PublishedMenuAssembler` (`:shared:domain`), which both `GetRestaurantMenuUseCase` and `RestaurantDetailsAssembler` delegate to — never repeated per screen. The restaurant screen still renders the first one. "No published menu" is not an error: it is an empty list the assembler maps to `Success(null)`, keeping `Failure` for genuine read failures.

Name, description, image, and allergens belong to `Dish`. Price, position, availability, and `DishCategory` (starters / mains / desserts / small bites) within a specific menu belong to `MenuItem`, because they can vary between menus — the same dish may cost differently on the carte and on the set menu. `MenuItem.category` is nullable: fixtures populate it, and the Supabase mapper leaves it null until the column exists.

`Menu.price` is optional and holds the fixed price of a set menu; an a la carte menu leaves it null and prices dish by dish. Both prices use minor units: `Money.minorUnits`, where `1_800` = 18,00 EUR. Never `Double` for money.

Currency is determined by the restaurant, not by a (menu, dish) pair: it lives in `Restaurant.currency` (`restaurants.currency_code`) and the Supabase mapper applies it when building each `Money`. Storing it on `menu_items` repeated on every row a value the restaurant already determined.

Each dish has an optional image in the MVP. Allergens use the EU's 14-group catalogue, plus an optional note and a source that makes clear the allergen info comes from the restaurant (not verified by Shareat).

## Restaurant summary and details

Two aggregates, one per screen, and the menu is what separates them:

- `RestaurantSummary` feeds the home feed: restaurant, `RatingSummary` and review-backed dish highlights. `GetRestaurantsUseCase` (a page) returns it via `RestaurantSummariesAssembler`, which resolves the whole page in three batched queries rather than several per restaurant. It carries **no menu**: the home card never draws one, and loading it for the page was the dominant cost of home's request.
- `RestaurantDetails` feeds the restaurant screen's pull to refresh: the same plus the published menu with its already-rated dishes (`RestaurantMenu` → `RatedMenuDish`), or null when it publishes none. `GetRestaurantUseCase` (one) returns it via `RestaurantDetailsAssembler`.
- `RestaurantMenu` alone feeds that screen's opening load: arriving from home, the header already travels in the navigation arguments, so `GetRestaurantMenuUseCase` fetches **only the dishes** rather than reassembling the whole restaurant.

`RatedMenuDish` carries the dish's **public review list** (`reviews: List<Review>`), not a pre-flattened aggregate, and exposes `ratingSummary` derived from it. One source keeps the average a screen shows from contradicting the reviews rendered next to it, and lets the UI draw both without a second call. This is sound because `ReviewRepository.getPublicReviews` returns exactly the population the aggregate is defined over: `Public` reviews with `Visible` moderation.

## Reviews

A single `Review` entity uses a typed target:

```
ReviewTarget.Restaurant
ReviewTarget.Dish
```

That is three tables in storage: `reviews` holds what every review shares (author, `target_type`,
rating, comment, visibility, moderation, dates) and one child per kind of target,
`restaurant_reviews` and `dish_reviews`, each keyed by `review_id` with the foreign key to its
entity.

`target_type` is not redundant — it is what keeps both invariants declarative. The parent's
`(id, author_account_id, target_type)` key is what the children reference, so the
`author_account_id` they carry cannot drift from the parent's and "one review per author and
target" stays an ordinary `unique` on the child. "Exactly one child" is a constraint trigger
deferred to commit, because parent and child are written in the same transaction.

Writes go through the `save_review` RPC, which does that transactional upsert. Reads go through two
`security_invoker` views that flatten parent and child, `restaurant_review_details` and
`dish_review_details`, so a client still reads one row per review. `ReviewRepository` and
`ReviewTarget` are unchanged.

Only an active customer account can write reviews. At most one review per author+target — `saveReview` updates the existing one instead of creating a duplicate. Rating is an integer 1–5; comment and visit date are optional; created and last-updated are tracked separately.

The three allergens the deployed project stored under shorter ids than the canonical ones (`gluten`, `soy`, `sulphites`) were renamed to the migration spelling in `20260910120000_normalize_menu_and_dish_catalogue.sql`. Every allergen is now addressable under exactly one id, so `String.toEuAllergenOrNull` no longer accepts alternative spellings.

General catalogue-mapping rule: **an unrecognised value is dropped, never allowed to fail the aggregate that contains it.** `toEuAllergenOrNull` returns null for an unknown id and `DishDto.toDomain` skips it. It previously threw, and a single unexpected allergen took down a restaurant's whole menu: `getPublishedMenus` failed, the assembler returned `menu = null`, and the screen reported "no menu published" instead of an error. When you add a mapper from a database string to a domain enum, make the unknown branch degrade, not throw.

Public reviews for several dishes are fetched in one batch via `ReviewRepository.getPublicDishReviews(dishIds)` — one query per section, not one per dish. The same rule governs a list of restaurants: `DishRepository.getDishesByRestaurant(restaurantIds)` and `ReviewRepository.getRestaurantRatingSummaries(restaurantIds)` each resolve a whole page in one query. **One query per page, never one per element**: an assembler that loops a list calling a repository per element multiplies network round trips by page size (see `RestaurantSummariesAssembler`). Always prefer a batched repository method over an N-per-entity loop. Ids travel in the query string, so the Supabase implementations split `in` filters into request-sized batches (`selectInBatches`).

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

`dishes.restaurant_id` names the single restaurant a dish belongs to, and `private.owns_dish(dish_id)`
hangs off it. Moving that column into a `restaurant_dishes` table was tried and reverted: keyed by
`dish_id` it was a 1:1 split of the same fact, gained nothing in normalisation — a dish belonging to
a restaurant is a functional dependency on the dish key — and cost a join on every catalogue read,
under a junction-table name that reads as a many-to-many it never was.

Deriving the restaurant through `menu_items → menus` was rejected for two reasons: it cannot express
"one restaurant per dish" (nothing would stop a dish appearing on two restaurants' menus), and
`archive_restaurant_dish` deliberately deletes the dish's menu items, so archiving would leave the
dish ownerless and unrecoverable.

The menu/dish N-N relation is materialized as `menu_items`, keyed by `(menu_id, dish_id)`, whose only own attributes are the ones that depend on that pair: price, position and availability. It does not repeat `restaurant_id` (determined by `menu_id`) or `currency` (determined by the restaurant). The invariant the composite foreign keys used to guarantee — a menu cannot list another restaurant's dish — is now enforced by the `private.assert_menu_item_restaurants_match` trigger, and the owner RLS policies hang off `private.owns_menu(menu_id)`.

For the same reason `dishes` no longer stores `allergen_source`: it was derived from the allergens and note the row already carries, and the domain rebuilds it in `AllergenDeclaration.source`.

Rating aggregates are `security_invoker` views that only consider public, visible reviews.

## Reviewable rules

- No `domain` model imports Koin, Compose, DTOs, or fake classes.
- `:shared:domain` and `:shared:data` apply no Compose plugin and declare no Compose dependencies.
- Entity references use typed IDs.
- Secrets and passwords never appear in the model or in fixtures.
- The UI consumes repository interfaces, never `FakeShareatData` directly.
- A public menu never returns disabled dishes or associations.
- A private or hidden review never contributes to a public aggregate.
- Any remote implementation must preserve these contracts' observable behavior.
