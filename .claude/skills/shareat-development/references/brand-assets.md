# Brand assets

Shareat brand icons and logotypes are centralized in `:shared:designsystem` as Compose Multiplatform SVG resources. Read `docs/brand-assets/README.md` for the full guide — this reference is the compressed, always-loadable version.

## Location

`shared/designsystem/src/commonMain/composeResources/drawable/`

## Available assets

### Circle icons (logo mark only)

| Resource accessor | Variant | Use on |
| --- | --- | --- |
| `Res.drawable.ic_shareat_circle_white` | White | Dark backgrounds |
| `Res.drawable.ic_shareat_circle_dark` | Dark | Light backgrounds |
| `Res.drawable.ic_shareat_circle_gradient` | Full-color gradient | Branding, splash, highlights |

### Logotypes (icon + "shareat" text)

| Resource accessor | Variant | Use on |
| --- | --- | --- |
| `Res.drawable.logotype_shareat_white` | White | Dark backgrounds |
| `Res.drawable.logotype_shareat_dark` | Dark | Light backgrounds |

## Usage

```kotlin
import org.shareat.shared.designsystem.generated.resources.Res
import org.shareat.shared.designsystem.generated.resources.ic_shareat_circle_white
import org.jetbrains.compose.resources.painterResource

Image(
    painter = painterResource(Res.drawable.ic_shareat_circle_white),
    contentDescription = "Shareat"
)
```

## Naming convention

- `ic_shareat_*` — icon mark (graphic only).
- `logotype_shareat_*` — icon + text.
- Suffix `_white`, `_dark`, `_gradient` for color variants.
- Format: SVG for vectors, PNG only for complex raster effects.

## Reviewable rules

1. Brand assets go in `:shared:designsystem`, never in feature modules.
2. Follow the naming convention above.
3. Any addition or change must update `docs/brand-assets/README.md` in the same PR.

## Platform launcher icons

Launcher icons are platform-specific and live outside `:shared:designsystem`:

- **Android:** `androidApp/src/main/res/` — adaptive icon (vector foreground/monochrome in `drawable/`, gradient background webps in `mipmap-*/`). Regenerate with Android Studio Image Asset.
- **iOS:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/` — 1024x1024 PNGs (light, dark, tinted variants).
