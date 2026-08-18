---
slug: typography-system-fraunces-inter
status: plan-written
intent: clear
review_required: false
pending-action: write .omo/plans/typography-system-fraunces-inter.md
approach: Prune composeResources/font to Fraunces_72pt (Regular/SemiBold/Bold) + Inter_18pt (Regular/Medium/SemiBold/Bold), build FontFamily + custom Material3 Typography in :shared:ui, wire it into the existing MaterialTheme{} call in App.kt (Fraunces on display*/headline{Large,Medium}, Inter elsewhere), keep call sites using MaterialTheme.typography.* (no direct fontFamily/fontSize usage found in repo already - good baseline).
---

# Draft: typography-system-fraunces-inter

## Components (topology ledger)
<!-- Lock the SHAPE before depth. One row per top-level component that can succeed or fail independently. -->
1 | Delete unneeded font files from shared/ui/src/commonMain/composeResources/font, keeping only the 7 chosen files | active | shared/ui/src/commonMain/composeResources/font/ (glob, 90 files found)
2 | Add FontFamily builders (Inter, Fraunces) in :shared:ui commonMain using Res.font.* | active | shared/ui/build.gradle.kts:74 (libs.compose.components.resources already a dependency)
3 | Build custom Typography mapping Fraunces to display*/headlineLarge/headlineMedium, Inter to the rest | active | App.kt:37 (MaterialTheme{} called with zero args - no existing Typography/Color/Shape file in repo)
4 | Wire custom Typography (and leave colorScheme/shapes as Material3 defaults, out of scope) into the App.kt MaterialTheme call | active | App.kt:36-45
5 | Verify existing screens (Home, Profile, Login) render with new fonts without visual regressions/build breaks | active | feature/home/.../Home.kt:32, feature/profile/.../Profile.kt:32, feature/login/ui/.../SignInScreen.kt:51 etc. (all use MaterialTheme.typography.* roles already, no direct font overrides to migrate)

## Open assumptions (announced defaults)
<!-- Record any default you adopt instead of asking, so the user can veto it at the gate. -->
- Theme/Typography files live in shared/ui/src/commonMain/kotlin/org/shareat/app/theme/ (new package) as Theme.kt (FontFamily builders + AppTypography()) | rationale: shared/ui is the only module with composeResources/font and the App.kt MaterialTheme call; no existing theme package to follow, so a new `theme` package under the existing `org.shareat.app` root keeps it colocated with App.kt | reversible: yes
- colorScheme and shapes are left as Material3 defaults (not customized) since user only asked about typography/fonts | rationale: explicit request scope is fonts/typography + font-file cleanup | reversible: yes
- Role mapping follows the ChatGPT plan literally: displayLarge/Medium/Small + headlineLarge/Medium = Fraunces SemiBold; headlineSmall through labelSmall = Inter (Regular/Medium/SemiBold per weight already used in Material3 defaults) | rationale: directly matches the pasted plan | reversible: yes (just a mapping table)

## Findings (cited - path:lines)
- App.kt:36-45 (shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt) - single call site of `MaterialTheme { ... }` in the whole repo, called with NO parameters (no colorScheme/typography/shapes override yet).
- No existing Typography/Color/Shape/Theme definition files anywhere in the repo (grep for `val Typography`, `lightColorScheme(`, `val Shapes` returned zero matches).
- No existing `Font(`/`FontFamily(`/`Res.font` usage anywhere in the repo (grep zero matches) - this is a fresh integration, not a migration.
- No ad-hoc `fontFamily=`/`fontSize=`/`FontWeight=` usage inside any `Text(...)` call in the repo (grep zero matches) - all current screens already consume `MaterialTheme.typography.<role>` (headlineMedium x5, headlineSmall x2, bodySmall x4, labelLarge x1, bodyMedium x1), so switching the Typography object is a clean, low-risk swap.
- No "Dish Details" screen exists yet in the codebase (that UI only exists in the user's design mockup/screenshot, not implemented) - out of scope, nothing to update there.
- shared/ui/src/commonMain/composeResources/font/ currently has 90 .ttf files across Inter (18pt/24pt/28pt optical sizes x 9 weights x regular/italic) and Fraunces (base/Soft/SuperSoft x 9 weights x regular/italic).
- Generated resources package: `shareat.shared.ui.generated.resources` (Res.kt) - default Compose Multiplatform naming, no explicit `resourcePackage` configured in shared/ui/build.gradle.kts; no change needed, Res.font.* will be auto-available in commonMain.
- shared/ui/build.gradle.kts:74 already declares `implementation(libs.compose.components.resources)` in commonMain - no new Gradle dependency required.
- Settings.gradle.kts modules relevant to theme: :shared:ui (owns composeResources/font + App.kt), consumed by :feature:home, :feature:profile, :feature:login:ui, :androidApp, :webApp (all just consume MaterialTheme.typography.*, no changes needed there).

## Decisions (with rationale)
- Fraunces variant to KEEP: `Fraunces_72pt` (base, sharp/editorial) — user chose this over Soft/SuperSoft.
- Inter optical size to KEEP: `Inter_18pt` — user chose this (tuned for small/body UI text) over 24pt/28pt.
- Weights to KEEP: Inter Regular, Medium, SemiBold, Bold (4 files); Fraunces Regular, SemiBold, Bold (3 files, no Medium exists for Fraunces in this file set).
- Italics: DELETE all italic files for both families (user chose minimal set, no italic usage in the plan).
- Exact 7 files to KEEP (all others in the font/ folder are deleted):
  - Fraunces_72pt-Regular.ttf
  - Fraunces_72pt-SemiBold.ttf
  - Fraunces_72pt-Bold.ttf
  - Inter_18pt-Regular.ttf
  - Inter_18pt-Medium.ttf
  - Inter_18pt-SemiBold.ttf
  - Inter_18pt-Bold.ttf
- Role → font → weight mapping (Material3 Typography, sizes/line-heights untouched, only fontFamily/fontWeight overridden):
  - displayLarge, displayMedium, displaySmall → Fraunces, SemiBold
  - headlineLarge, headlineMedium → Fraunces, SemiBold
  - headlineSmall → Inter, SemiBold
  - titleLarge → Inter, SemiBold
  - titleMedium, titleSmall → Inter, Medium
  - bodyLarge, bodyMedium, bodySmall → Inter, Regular
  - labelLarge → Inter, SemiBold
  - labelMedium, labelSmall → Inter, Medium

## Scope IN
- Delete the 83 unneeded font files from shared/ui/src/commonMain/composeResources/font/, keeping exactly the 7 listed above.
- Create `InterFontFamily()` and `FrauncesFontFamily()` composable builders using `Font(resource = Res.font.*, weight = ...)`.
- Create `AppTypography()` composable returning a `Typography` built from `Typography()` defaults with the role→font→weight overrides above (fontFamily + fontWeight only; sizes/lineHeight/letterSpacing left at Material3 defaults for now).
- Wire `AppTypography()` into the existing `MaterialTheme { ... }` call in App.kt by adding `typography = AppTypography()` as a parameter.
- Confirm the project still builds/compiles for at least one target (Android) after the change (agent-executed QA, no human intervention).

## Scope OUT (Must NOT have)
- No changes to colorScheme or shapes (Material3 defaults stay as-is).
- No new "Dish Details" screen or any new UI screens - out of scope, doesn't exist yet.
- No changes to font SIZES, line-heights, or letter-spacing of Material3 Typography roles - only fontFamily/fontWeight are overridden.
- No introduction of a 3rd font family.
- No renaming/reorganizing of unrelated resources (icons, drawables, etc.).
- No addition of `resourcePackage` Gradle config - default generated package is fine.
- No keeping of italics or any of the 83 pruned weight/optical-size variants "just in case" - they are deleted, not moved/archived.

## Open questions
(none - all forks resolved via the pre-plan question round: Fraunces variant, Inter optical size, weight set, italics.)

## Approval gate
status: awaiting-approval
<!-- When exploration is exhausted and unknowns are answered, set status: awaiting-approval. -->
<!-- That durable record is the loop guard: on a later turn read it and resume at the gate instead of re-running exploration. -->
next action: write .omo/plans/typography-system-fraunces-inter.md following the full-workflow template, with implementation todos for (1) font file deletion, (2) FontFamily builders, (3) AppTypography() + role mapping, (4) App.kt wiring, plus a final verification wave (build check across Android/Web targets, grep confirming zero remaining unused font references, visual smoke-check via explore agent reading rendered Home/Profile/Login screens style refs).

