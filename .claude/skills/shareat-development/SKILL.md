---
name: shareat-development
description: "Guides Claude when reading, writing, reviewing, or planning code in the Shareat (also called ShareEat) Kotlin Multiplatform repository, the restaurant discovery and review app with Android, iOS, and web clients, Koin dependency injection, Navigation 3, and a Supabase backend. Load this whenever working inside this project, including implementing or modifying a feature module, wiring dependency injection, adding a screen or navigation intent, writing repository/use case/ViewModel/mock code, adding JUnit tests, touching Supabase migrations/RLS/Storage, or answering questions about the app's architecture, data model, MVP scope, or product rules. Trigger on mentions of \"Shareat\", \"ShareEat\", the feature module pattern feature-name-domain/data/ui, `:shared:domain`/`:shared:data`/`:shared:ui`, Koin modules, `NavKey`/`Navigator`, `FakeShareatData`, `MockRepository`, or `supabase/migrations`, even if the user doesn't name this skill explicitly."
---

# Shareat development

Shareat is a Kotlin Multiplatform app (Android, iOS, web) where customers discover restaurants, browse structured menus, and rate restaurants or individual dishes, while restaurants publish and maintain a menu. `docs/` in the repo root is the project's living source of truth for these conventions — this skill is a compressed, always-loaded version of it. When the two disagree, **re-read `docs/` first**: it may have moved since this skill was generated.

## Before writing any code here

1. Identify which layer you're touching — `domain`, `data`, or `ui` — for either a `:feature:<name>:*` module or the shared `:shared:*` base. The dependency and framework rules differ per layer; get this wrong and the module won't build or will break the architecture's whole point (swapping mock data for Supabase without touching domain/UI).
2. Check whether the change crosses a boundary this skill has a reference for (dependency injection, navigation, the domain data model, testing, Supabase). Read the matching file in `references/` before writing — they carry the concrete patterns and the reviewable rules a PR would be checked against.
3. If you're adding a new feature end to end, walk through `references/architecture.md`'s checklist before considering it done.

## Repo shape

```
androidApp/   iosApp/   webApp/         # platform entry points
shared/{domain,data,navigation,ui}/     # shared KMP base; :shared:navigation is a tiny leaf module
                                         # for shared navigation contracts (e.g. RequiresLogin);
                                         # :shared:ui is the composition root
feature/<name>/{domain,data,ui}/        # per-feature modules (some features are still single-module
                                         # while they're being split — check settings.gradle.kts for
                                         # what actually exists before assuming all three are present)
supabase/{migrations,tests,seed.sql}/   # backend: Postgres migrations, pgTAP tests, local seed
docs/                                   # source-of-truth guides this skill summarizes
```

Package root for shared Kotlin code is `org.shareat.app`.

## The one rule that matters most

`domain` never imports Koin, Compose, DTOs, storage/network clients, or fake/mock classes. `data` depends on `domain` and implements its repository interfaces but applies no Compose plugin and holds no Compose dependencies. `ui` depends on `domain` only — never on a concrete `data` implementation or the app module. Everything flows one direction:

```
Android / iOS / Web
        |
        v
  :shared:ui --------------> :shared:data
        |                          |
        |                          v
        +------------------> :shared:domain
        |
        +------------------> :feature:<name>:ui -> :feature:<name>:domain
```

A feature never imports another feature's internals to reuse a screen, repository, or ViewModel — that goes through an explicit API. Data flow: `Screen -> ViewModel -> Use case -> Repository interface -> {MockRepository | RemoteRepository}`. `data` maps fixtures/remote responses to domain models before returning; `ui` maps domain to presentation state.

## Reference files

Read the one(s) relevant to your change — don't load all of them speculatively.

- `references/architecture.md` — the three-module-per-feature split in full, the new-feature checklist, and what belongs in each layer.
- `references/data-model.md` — domain entities (Account, Restaurant, Menu/Dish/MenuItem, Review, etc.), their relationships, `FakeShareatData`/`FakeDataScenario`, and the Supabase persistence mapping.
- `references/dependency-injection.md` — Koin module structure, `single`/`factory`/`viewModel` scope rules, the fake-vs-Supabase module selection, and testing overrides.
- `references/navigation.md` — how a feature declares a navigation interface, how the app module implements it with `NavKey` and Koin Navigation 3 entries, and the top-level-vs-subscreen split.
- `references/testing.md` — mock repository conventions, deterministic scenarios, what to test at each layer (repository/use case/ViewModel), and the local Supabase test flow (pgTAP, JVM contract tests).
- `references/supabase.md` — security boundaries (publishable key only, RLS, Storage limits), the local dev loop, and the safe deployment sequence.
- `references/product.md` — MVP scope, product rules (reviews, ratings, menu visibility, moderation, monetisation), and what's explicitly post-MVP. Use this when a request would add scope the product definition puts outside the MVP, or when unsure whether a rule is a product decision vs. a technical one.

## Reviewable rules that cut across every layer

- No secrets, passwords, or `service_role`/secret Supabase keys anywhere in client code, fixtures, or domain models — the client only ever gets a publishable key.
- Money is `Money.minorUnits` (an `Int`, e.g. `1_800` = 18,00 EUR), never `Double`.
- Entity references between domain models use typed IDs, not raw strings.
- A disabled/draft/unpublished menu, dish, or review never reaches a public read path or a public aggregate.
- Every repository interface method is `suspend`, so a mock implementation can be replaced by a remote one without touching callers.
- New Gradle modules for a feature are registered in `settings.gradle.kts`.
- No comments by default — this is the default, not a fallback to reach for. Name classes, functions, and variables well enough (and apply SOLID — especially single responsibility — well enough) that the code reads without narration. This also covers module-placement/dependency-direction rationale ("lives here to avoid a circular dependency") — that belongs in the PR description or `docs/`, not a code comment. The only exception is logic that is itself genuinely tricky (a non-obvious algorithm, a workaround for a specific bug/platform quirk, a subtle invariant) — and even then, at most a two-line `//` comment, never a `/** KDoc */` block, explaining the *why*, not the *what*.

## Keeping this skill and `docs/` in sync

`docs/README.md` states plainly: architecture guides are living documentation, and any architecture change must update the relevant `docs/<topic>/README.md` in the same change, referencing the ticket or ADR that decided it. Treat that as binding on this skill too. Whenever a session working in this repo changes something this skill or its references describe — a new module layer rule, a changed Koin pattern, a new navigation convention, an updated data model, a revised MVP boundary — do both of the following before calling the work done:

1. Update the affected `docs/<topic>/README.md` file(s) directly on the user's machine, in the same change as the code (this is already expected by the repo's own conventions, independent of this skill).
2. Update this skill's `SKILL.md` and/or the relevant `references/*.md` file directly at `.claude/skills/shareat-development/` in this repo so future sessions immediately load correct guidance — no repackaging or resending needed for the project copy, since it lives in the repo itself. If the user also has this skill saved to their personal account (delivered as a `.skill` file), mention that the account copy is now stale and offer to regenerate and resend it too.

Don't defer this to a separate follow-up: a code change that shifts a convention and a docs/skill update are one unit of work, the same way the repo already treats docs and ADRs as inseparable from the decision they record.
