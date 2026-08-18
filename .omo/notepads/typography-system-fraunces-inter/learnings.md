# Learnings — typography-system-fraunces-inter

Conventions, patterns, and successful approaches discovered during work on this plan.

_Auto-scaffolded by /start-work. Append new entries below - never overwrite._

---

## Task 1: Font File Cleanup Complete

**Final font list (7 files):**
- Fraunces_72pt-Regular.ttf
- Fraunces_72pt-SemiBold.ttf
- Fraunces_72pt-Bold.ttf
- Inter_18pt-Regular.ttf
- Inter_18pt-Medium.ttf
- Inter_18pt-SemiBold.ttf
- Inter_18pt-Bold.ttf

**Deleted:** 83 files (all other .ttf variants)
- Removed all Fraunces Soft/SuperSoft variants
- Removed all Inter 24pt/28pt sizes
- Removed all italic variants
- Removed extra weights (Thin, Light, ExtraLight, ExtraBold, Black)

**Rationale:** Fraunces 72pt for display (3 weights), Inter 18pt for body/UI (4 weights). Reduces bundle size while maintaining design flexibility.

---

## Task 2: Font.kt Builder Functions Created

**File created:** `shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Font.kt`

**Exact generated font resource symbols (from Font0.commonMain.kt):**
- `Res.font.Fraunces_72pt_Bold`
- `Res.font.Fraunces_72pt_Regular`
- `Res.font.Fraunces_72pt_SemiBold`
- `Res.font.Inter_18pt_Bold`
- `Res.font.Inter_18pt_Medium`
- `Res.font.Inter_18pt_Regular`
- `Res.font.Inter_18pt_SemiBold`

**Functions implemented:**
- `InterFontFamily()`: Returns FontFamily with 4 weights (Normal, Medium, SemiBold, Bold)
- `FrauncesFontFamily()`: Returns FontFamily with 3 weights (Normal, SemiBold, Bold)

**Build verification:** `./gradlew :shared:ui:compileKotlinIosArm64` → BUILD SUCCESSFUL in 14s

---

## Task 3: Typography.kt System Created

**File created:** `shared/ui/src/commonMain/kotlin/org/shareat/app/theme/Typography.kt`

**Function:** `@Composable fun AppTypography(): androidx.compose.material3.Typography`

**15-Role Mapping (all Material3 roles explicitly assigned):**
- Display roles (3): displayLarge, displayMedium, displaySmall → FrauncesFontFamily(), FontWeight.SemiBold
- Headline roles (3): headlineLarge, headlineMedium → FrauncesFontFamily(), FontWeight.SemiBold; headlineSmall → InterFontFamily(), FontWeight.SemiBold
- Title roles (3): titleLarge → InterFontFamily(), FontWeight.SemiBold; titleMedium, titleSmall → InterFontFamily(), FontWeight.Medium
- Body roles (3): bodyLarge, bodyMedium, bodySmall → InterFontFamily(), FontWeight.Normal
- Label roles (3): labelLarge → InterFontFamily(), FontWeight.SemiBold; labelMedium, labelSmall → InterFontFamily(), FontWeight.Medium

**Implementation pattern:** `val default = Typography(); return default.copy(displayLarge = default.displayLarge.copy(fontFamily = ..., fontWeight = ...), ...)` for all 15 roles. Only fontFamily and fontWeight modified; fontSize, lineHeight, letterSpacing untouched.

**Font family count:** 15 matches (5 Fraunces + 10 Inter) ✓

**Build verification:** `./gradlew :shared:ui:compileAndroidMain` → BUILD SUCCESSFUL in 1s ✓

---

## Task 4: MaterialTheme Typography System Integration

**File modified:** `shared/ui/src/commonMain/kotlin/org/shareat/app/App.kt`

**Changes:**
- Added import: `import org.shareat.app.theme.AppTypography` (non-wildcard)
- Changed MaterialTheme call from `MaterialTheme {` to `MaterialTheme(typography = AppTypography()) {`
- No colorScheme or shapes parameters added (Material3 defaults used)
- All navigation, session handling, and other logic unchanged

**Build verification:** `./gradlew :androidApp:assembleDebug` → BUILD SUCCESSFUL in 7s
- 134 actionable tasks: 17 executed, 117 up-to-date
- App compiles end-to-end with typography system integrated
- Only App.kt modified (5 lines added, 1 line removed)

**Status:** App.kt wiring complete. Typography system now active across entire app.
