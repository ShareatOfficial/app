# typography-system-fraunces-inter - Work Plan

## TL;DR (For humans)

**What you'll get:** The app will use Fraunces (a warm editorial serif) for big titles/dish-name-style headings, and Inter (a clean sans) for everything else in the interface — buttons, body text, labels, navigation. The font folder will be cleaned up from 90 font files down to the 7 that are actually used.

**Why this approach:** Material3's `Typography` roles (display/headline/title/body/label) already exist and are already used everywhere in the app, so overriding just the `fontFamily`/`fontWeight` on those roles gives every screen the new look automatically, with zero per-screen edits.

**What it will NOT do:** It will not change any text sizes, spacing, or colors. It will not touch colorScheme or shapes. It will not add a third font or build any new screens (like "Dish Details" — that doesn't exist in the app yet).

**Effort:** Quick
**Risk:** Low - purely additive typography wiring plus a bulk file deletion; no existing call sites need to change since none override fonts today.
**Decisions to sanity-check:** Fraunces base variant (not Soft/SuperSoft) and Inter 18pt optical size were chosen per your answers; italics are all deleted.

Your next move: approve, or ask for the optional high-accuracy review. Full execution detail follows below.

---

> TL;DR (machine): Quick effort, Low risk - prune 83 unused font files, add FontFamily builders + custom Material3 Typography, wire into the single existing MaterialTheme call.

## Scope
### Must have
- Delete 83 unneeded `.ttf` files from `shared/ui/src/commonMain/composeResources/font/`, keeping exactly 7.
- `InterFontFamily()` and `FrauncesFontFamily()` composable `FontFamily` builders using `Font(resource = Res.font.*, weight = ...)`.
- `AppTypography()` composable returning a Material3 `Typography` with Fraunces/Inter role mapping (fontFamily + fontWeight only).
- `MaterialTheme { ... }` in `App.kt` updated to pass `typography = AppTypography()`.
- Project compiles for the Android target after the change (agent-executed build check).

### Must NOT have (guardrails, anti-slop, scope boundaries)
- No changes to `colorScheme` or `shapes` — Material3 defaults stay as-is.
- No changes to font sizes, line-heights, or letter-spacing of any Typography role — only `fontFamily`/`fontWeight` are overridden.
- No new screens (e.g. no "Dish Details" screen — it does not exist in the codebase; out of scope).
- No third font family introduced.
- No `resourcePackage` Gradle config added — default generated package (`shareat.shared.ui.generated.resources`) is used as-is.
- No archiving/moving of the 83 pruned files "just in case" — they are deleted outright, not relocated.
- No per-screen `fontFamily`/`fontSize`/`FontWeight` overrides added to any `Text(...)` call — all styling must flow through `MaterialTheme.typography.*` (already the case everywhere in the repo).

## Verification strategy
> Zero human intervention - all verification is agent-executed.
- Test decision: none (no existing UI test suite for typography/theme in this repo; this is Compose theme config, not business logic) - verification is via compile/build checks + grep-based structural checks + an explore-agent visual/code read-through.
- Evidence: `.omo/evidence/task-<N>-typography-system-fraunces-inter.<ext>` (this workflow runs outside ulw-loop, so use `.omo/evidence/` directly; create the directory if absent).

## Execution strategy
### Parallel execution waves
- Wave 1 (sequential prerequisite, single todo): Todo 1 (font file cleanup) — must finish first since Todo 2/3 reference the exact 7 filenames that must exist as `Res.font.*` symbols.
- Wave 2 (after Wave 1, can run together): Todo 2 (FontFamily builders) — Todo 3 (AppTypography mapping) depends on Todo 2's output directly, so in practice Todo 2 then Todo 3 run in sequence, but both are small enough to treat as one wave executed in order.
- Wave 3 (after Wave 2): Todo 4 (wire into App.kt).
- Final verification wave: after Todo 4, runs F1-F4 in parallel.

### Dependency matrix
| Todo | Depends on | Blocks | Can parallelize with |
| --- | --- | --- | --- |
| 1 | none | 2, 3 | none |
| 2 | 1 | 3, 4 | none |
| 3 | 2 | 4 | none |
| 4 | 3 | F1-F4 | none |

## Todos
> Implementation + Test = ONE todo. Never separate.
- [x] 1. Prune shared/ui composeResources/font to the 7 approved files
  What to do / Must NOT do: In `/Users/sedilant/ProjectsPersonal/shareat/shared/ui/src/commonMain/composeResources/font/`, delete every `.ttf` file EXCEPT these 7 (KEEP list, exact filenames):
  - `Fraunces_72pt-Regular.ttf`
  - `Fraunces_72pt-SemiBold.ttf`
  - `Fraunces_72pt-Bold.ttf`
  - `Inter_18pt-Regular.ttf`
  - `Inter_18pt-Medium.ttf`
  - `Inter_18pt-SemiBold.ttf`
  - `Inter_18pt-Bold.ttf`

  DELETE list (exact filenames, all 83 in the same folder):
  `Fraunces_72pt_SuperSoft-BlackItalic.ttf`, `Fraunces_72pt_SuperSoft-SemiBold.ttf`, `Fraunces_72pt_SuperSoft-Black.ttf`, `Inter_24pt-LightItalic.ttf`, `Inter_24pt-Italic.ttf`, `Fraunces_72pt-ThinItalic.ttf`, `Inter_24pt-ExtraLight.ttf`, `Fraunces_72pt_Soft-SemiBold.ttf`, `Fraunces_72pt-Italic.ttf`, `Fraunces_72pt_SuperSoft-Regular.ttf`, `Inter_24pt-ThinItalic.ttf`, `Inter_24pt-BoldItalic.ttf`, `Inter_18pt-SemiBoldItalic.ttf`, `Fraunces_72pt_SuperSoft-Italic.ttf`, `Fraunces_72pt-Black.ttf`, `Fraunces_72pt-BoldItalic.ttf`, `Inter_28pt-Black.ttf`, `Inter_18pt-Italic.ttf`, `Fraunces_72pt_Soft-ThinItalic.ttf`, `Fraunces_72pt_Soft-Regular.ttf`, `Inter_24pt-Bold.ttf`, `Fraunces_72pt_Soft-BoldItalic.ttf`, `Fraunces_72pt-SemiBoldItalic.ttf`, `Inter_28pt-ExtraBold.ttf`, `Inter_28pt-MediumItalic.ttf`, `Inter_28pt-Regular.ttf`, `Inter_18pt-Light.ttf`, `Fraunces_72pt_SuperSoft-LightItalic.ttf`, `Inter_24pt-Black.ttf`, `Inter_24pt-SemiBoldItalic.ttf`, `Inter_24pt-Thin.ttf`, `Fraunces_72pt_Soft-Italic.ttf`, `Fraunces_72pt_Soft-Light.ttf`, `Inter_28pt-Italic.ttf`, `Inter_24pt-BlackItalic.ttf`, `Inter_18pt-MediumItalic.ttf`, `Fraunces_72pt_SuperSoft-ThinItalic.ttf`, `Inter_28pt-SemiBold.ttf`, `Fraunces_72pt_Soft-BlackItalic.ttf`, `Fraunces_72pt_Soft-SemiBoldItalic.ttf`, `Inter_24pt-MediumItalic.ttf`, `Inter_24pt-ExtraBoldItalic.ttf`, `Inter_28pt-SemiBoldItalic.ttf`, `Inter_24pt-Light.ttf`, `Inter_18pt-Black.ttf`, `Inter_24pt-ExtraLightItalic.ttf`, `Inter_18pt-ExtraLightItalic.ttf`, `Inter_28pt-BlackItalic.ttf`, `Fraunces_72pt_SuperSoft-BoldItalic.ttf`, `Fraunces_72pt_Soft-Black.ttf`, `Inter_18pt-BlackItalic.ttf`, `Inter_28pt-Medium.ttf`, `Fraunces_72pt-LightItalic.ttf`, `Inter_18pt-Thin.ttf`, `Inter_28pt-Light.ttf`, `Inter_18pt-BoldItalic.ttf`, `Inter_18pt-ExtraBold.ttf`, `Inter_18pt-ThinItalic.ttf`, `Inter_18pt-ExtraLight.ttf`, `Inter_18pt-LightItalic.ttf`, `Fraunces_72pt-BlackItalic.ttf`, `Inter_28pt-ExtraBoldItalic.ttf`, `Inter_24pt-Regular.ttf`, `Inter_28pt-BoldItalic.ttf`, `Inter_24pt-SemiBold.ttf`, `Fraunces_72pt-Light.ttf`, `Inter_24pt-ExtraBold.ttf`, `Inter_28pt-ThinItalic.ttf`, `Inter_28pt-Thin.ttf`, `Inter_28pt-ExtraLight.ttf`, `Fraunces_72pt_Soft-Thin.ttf`, `Fraunces_72pt_SuperSoft-Bold.ttf`, `Fraunces_72pt_SuperSoft-Thin.ttf`, `Inter_28pt-Bold.ttf`, `Inter_28pt-ExtraLightItalic.ttf`, `Fraunces_72pt_Soft-Bold.ttf`, `Fraunces_72pt_Soft-LightItalic.ttf`, `Inter_24pt-Medium.ttf`, `Fraunces_72pt_SuperSoft-SemiBoldItalic.ttf`, `Fraunces_72pt-Thin.ttf`, `Inter_18pt-ExtraBoldItalic.ttf`, `Inter_28pt-LightItalic.ttf`, `Fraunces_72pt_SuperSoft-Light.ttf`

  Must NOT do: do not delete any file not in the DELETE list; do not rename any KEPT file; do not touch any other `composeResources` subfolder (drawable/, string/, etc.).
  Parallelization: Wave 1 | Blocked by: none | Blocks: Todo 2, Todo 3
  References (executor has NO interview context - be exhaustive): `/Users/sedilant/ProjectsPersonal/shareat/shared/ui/src/commonMain/composeResources/font/` (folder, currently 90 `.ttf` files)
  Acceptance criteria (agent-executable): `ls shared/ui/src/commonMain/composeResources/font/ | wc -l` returns exactly `7`; `ls shared/ui/src/commonMain/composeResources/font/` output exactly matches the 7-file KEEP list (order-independent).
  QA scenarios (name the exact tool + invocation): happy - run `ls -1 shared/ui/src/commonMain/composeResources/font/` and diff against the KEEP list, save to evidence file; failure - re-run `git status --porcelain shared/ui/src/commonMain/composeResources/font/` and confirm exactly 83 deletions (`D`) and 0 modifications, save to evidence. Evidence: `.omo/evidence/task-1-typography-system-fraunces-inter.txt`
  Commit: Y | `chore(fonts): prune composeResources/font to Fraunces + Inter subset`

- [x] 2. Add InterFontFamily() and FrauncesFontFamily() builders
  What to do: Create a new file `/Users/sedilant/ProjectsPersonal/shareat/shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Font.kt` in package `org.shareat.app.theme`. Define two `@Composable` private-or-internal functions returning `androidx.compose.ui.text.font.FontFamily`:
  ```kotlin
  package org.shareat.app.theme

  import androidx.compose.runtime.Composable
  import androidx.compose.ui.text.font.FontFamily
  import androidx.compose.ui.text.font.FontWeight
  import org.jetbrains.compose.resources.Font
  import shareat.shared.ui.generated.resources.Res
  import shareat.shared.ui.generated.resources.inter_18pt_regular
  import shareat.shared.ui.generated.resources.inter_18pt_medium
  import shareat.shared.ui.generated.resources.inter_18pt_semibold
  import shareat.shared.ui.generated.resources.inter_18pt_bold
  import shareat.shared.ui.generated.resources.fraunces_72pt_regular
  import shareat.shared.ui.generated.resources.fraunces_72pt_semibold
  import shareat.shared.ui.generated.resources.fraunces_72pt_bold

  @Composable
  internal fun InterFontFamily(): FontFamily = FontFamily(
      Font(resource = Res.font.inter_18pt_regular, weight = FontWeight.Normal),
      Font(resource = Res.font.inter_18pt_medium, weight = FontWeight.Medium),
      Font(resource = Res.font.inter_18pt_semibold, weight = FontWeight.SemiBold),
      Font(resource = Res.font.inter_18pt_bold, weight = FontWeight.Bold),
  )

  @Composable
  internal fun FrauncesFontFamily(): FontFamily = FontFamily(
      Font(resource = Res.font.fraunces_72pt_regular, weight = FontWeight.Normal),
      Font(resource = Res.font.fraunces_72pt_semibold, weight = FontWeight.SemiBold),
      Font(resource = Res.font.fraunces_72pt_bold, weight = FontWeight.Bold),
  )
  ```
  Must NOT do: do not hardcode `fontFamily=` in any screen composable; do not add more weights than the 7 kept files; the exact generated resource identifiers (`Res.font.<name>`) MUST be verified against the actual generated `Res.kt` after Todo 1's Gradle resource regeneration (compose resource identifiers are lowercase-with-underscores derived from the filename minus extension — confirm exact generated names by running a build/sync and reading the generated `Res.kt` file under `shared/ui/build/generated/compose/resourceGenerator/kotlin/commonResClass/shareat/shared/ui/generated/resources/Res.kt` before finalizing the imports; adjust names if the generator produces different casing).
  Parallelization: Wave 2 | Blocked by: Todo 1 | Blocks: Todo 3, Todo 4
  References (executor has NO interview context - be exhaustive): `shared/ui/build.gradle.kts:74` (compose.components.resources dependency already present); `shared/ui/build/generated/compose/resourceGenerator/kotlin/commonResClass/shareat/shared/ui/generated/resources/Res.kt` (generated Res class, package `shareat.shared.ui.generated.resources`)
  Acceptance criteria (agent-executable): project compiles for Android target: `./gradlew :shared:ui:compileDebugKotlinAndroid` succeeds with the new `Font.kt` file present.
  QA scenarios (name the exact tool + invocation): happy - run `./gradlew :shared:ui:compileDebugKotlinAndroid` and confirm BUILD SUCCESSFUL, save log to evidence; failure - intentionally reference a non-existent `Res.font.xxx` symbol first to confirm the build fails with an unresolved reference (proves the generated symbols are being checked, not stubbed), then revert to the correct symbol and re-run to confirm success. Evidence: `.omo/evidence/task-2-typography-system-fraunces-inter.txt`
  Commit: Y | `feat(theme): add Inter and Fraunces FontFamily builders`

- [x] 3. Add AppTypography() with Fraunces/Inter role mapping
  What to do: In the same package, create `/Users/sedilant/ProjectsPersonal/shareat/shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Typography.kt` with a `@Composable fun AppTypography(): androidx.compose.material3.Typography` that starts from `androidx.compose.material3.Typography()` defaults and applies `.copy(fontFamily = ..., fontWeight = ...)` per role using this exact mapping:
  - `displayLarge`, `displayMedium`, `displaySmall` → `FrauncesFontFamily()`, `FontWeight.SemiBold`
  - `headlineLarge`, `headlineMedium` → `FrauncesFontFamily()`, `FontWeight.SemiBold`
  - `headlineSmall` → `InterFontFamily()`, `FontWeight.SemiBold`
  - `titleLarge` → `InterFontFamily()`, `FontWeight.SemiBold`
  - `titleMedium`, `titleSmall` → `InterFontFamily()`, `FontWeight.Medium`
  - `bodyLarge`, `bodyMedium`, `bodySmall` → `InterFontFamily()`, `FontWeight.Normal`
  - `labelLarge` → `InterFontFamily()`, `FontWeight.SemiBold`
  - `labelMedium`, `labelSmall` → `InterFontFamily()`, `FontWeight.Medium`
  Must NOT do: do not change `fontSize`, `lineHeight`, or `letterSpacing` on any role — only `fontFamily` and `fontWeight` fields are overridden via `.copy(...)`.
  Parallelization: Wave 2 (sequential after Todo 2 within the wave) | Blocked by: Todo 2 | Blocks: Todo 4
  References (executor has NO interview context - be exhaustive): `shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Font.kt` (from Todo 2, for `InterFontFamily()`/`FrauncesFontFamily()`)
  Acceptance criteria (agent-executable): `./gradlew :shared:ui:compileDebugKotlinAndroid` succeeds with `Typography.kt` present; every one of the 15 Material3 `Typography` roles is explicitly assigned in the `.copy(...)` call (grep count of role names in the file = 15).
  QA scenarios (name the exact tool + invocation): happy - `grep -c "FrauncesFontFamily()\|InterFontFamily()" shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Typography.kt` returns `15` (one font-family assignment per role), save to evidence; failure - temporarily omit one role's override, confirm the omission is caught by a grep count check (< 15), then restore the full mapping. Evidence: `.omo/evidence/task-3-typography-system-fraunces-inter.txt`
  Commit: Y | `feat(theme): add AppTypography with Fraunces/Inter role mapping`

- [x] 4. Wire AppTypography() into the App.kt MaterialTheme call
  What to do: In `/Users/sedilant/ProjectsPersonal/shareat/shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt`, change line 37 from `MaterialTheme {` to:
  ```kotlin
  MaterialTheme(
      typography = org.shareat.app.theme.AppTypography(),
  ) {
  ```
  (or add a proper `import org.shareat.app.theme.AppTypography` at the top and call `AppTypography()` directly — either style is acceptable, but must not introduce a wildcard import).
  Must NOT do: do not pass `colorScheme` or `shapes` parameters — leave them as Material3 defaults; do not change anything else in `App.kt` (navigation, session handling, etc. stay untouched).
  Parallelization: Wave 3 | Blocked by: Todo 3 | Blocks: F1-F4
  References (executor has NO interview context - be exhaustive): `shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt:36-45` (the only `MaterialTheme` call site in the repo); `shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Typography.kt` (from Todo 3)
  Acceptance criteria (agent-executable): `./gradlew :shared:ui:compileDebugKotlinAndroid` succeeds; `grep -n "typography = " shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt` shows the new parameter; `git diff shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt` shows ONLY the `MaterialTheme{...}` line(s) changed, nothing else in the file.
  QA scenarios (name the exact tool + invocation): happy - run `./gradlew :androidApp:assembleDebug` and confirm BUILD SUCCESSFUL (proves the whole app, not just `:shared:ui`, compiles with the new theme wired in), save log to evidence; failure - confirm via `git diff --stat shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt` that exactly one file changed with a minimal diff (no unrelated navigation/session changes leaked in). Evidence: `.omo/evidence/task-4-typography-system-fraunces-inter.txt`
  Commit: Y | `feat(theme): wire AppTypography into MaterialTheme in App.kt`

## Final verification wave
> Runs in parallel after ALL todos. ALL must APPROVE. Surface results and wait for the user's explicit okay before declaring complete.
- [x] F1. Plan compliance audit - re-check every Scope IN item against the diff: exactly 7 font files remain, `Font.kt` + `Typography.kt` exist with the exact role mapping table above, `App.kt` passes `typography = AppTypography()` and nothing else changed. Evidence: `.omo/evidence/f1-typography-system-fraunces-inter.txt`
- [x] F2. Code quality review - confirm no wildcard imports, no hardcoded `fontFamily=`/`FontWeight=` leaked into any screen composable (`grep -rn "fontFamily = " shared/ feature/ | grep -v theme/Typography.kt | grep -v theme/Font.kt` returns empty), Kotlin style matches repo conventions (4-space indent, trailing commas per existing `Font.kt`-adjacent files). Evidence: `.omo/evidence/f2-typography-system-fraunces-inter.txt`
- [x] F3. Real manual QA - run `./gradlew :androidApp:assembleDebug` end to end and, if a Compose Preview or screenshot tool is available in this repo, capture Home/Profile/Login screens to visually confirm headlineMedium now renders in Fraunces SemiBold and body/label text renders in Inter (use the `explore` agent to read the generated APK/preview output or, if no visual tooling exists, verify via a `@Preview` composable added temporarily for the check and removed after, OR by inspecting the compiled font references). Evidence: `.omo/evidence/f3-typography-system-fraunces-inter.txt`
- [x] F4. Scope fidelity - confirm colorScheme/shapes are untouched (`git diff` shows no `colorScheme`/`shapes` parameter added), no new screens were created, no 83 deleted files are referenced anywhere (`grep -rn "<any deleted filename stem>" shared/ feature/` returns empty for a sample of 5 deleted filenames). Evidence: `.omo/evidence/f4-typography-system-fraunces-inter.txt`

## Commit strategy
- One commit per todo (4 commits total), each scoped exactly as listed in each todo's `Commit:` line, in order 1→2→3→4 (font prune must land before the Kotlin files reference the surviving filenames).

## Success criteria
- `shared/ui/src/commonMain/composeResources/font/` contains exactly the 7 approved files.
- `AppTypography()` exists and is wired into the single `MaterialTheme` call in `App.kt`, mapping Fraunces to display*/headlineLarge/headlineMedium and Inter to every other role, per the table in Todo 3.
- `./gradlew :androidApp:assembleDebug` builds successfully with no other files modified beyond `Font.kt`, `Typography.kt`, and the one-line `App.kt` change.
- No screen composable contains ad-hoc `fontFamily=`/`fontSize=`/`FontWeight=` overrides (all typography flows through `MaterialTheme.typography.*`, matching the pre-existing convention).
