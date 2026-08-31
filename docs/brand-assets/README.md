# Assets de marca

## Objetivo y alcance

Centralizar los iconos y logotipos de Shareat para que cualquier módulo pueda usarlos sin duplicar recursos ni depender de assets específicos de plataforma.

## Recursos compartidos (Compose Multiplatform)

Los assets de marca viven en `:shared:designsystem` como recursos SVG de Compose Multiplatform.

**Ruta:** `shared/designsystem/src/commonMain/composeResources/drawable/`

### Iconos circulares (solo el icono, sin fondo)

| Archivo | Variante | Uso |
| --- | --- | --- |
| `ic_shareat_circle_white.svg` | Blanco | Sobre fondos oscuros |
| `ic_shareat_circle_dark.svg` | Oscuro | Sobre fondos claros |
| `ic_shareat_circle_gradient.svg` | Gradiente a color | Branding, splash, elementos destacados |

### Logotipos (icono + texto "shareat")

| Archivo | Variante | Uso |
| --- | --- | --- |
| `logotype_shareat_white.svg` | Blanco | Sobre fondos oscuros |
| `logotype_shareat_dark.svg` | Oscuro | Sobre fondos claros |

## Patrón de uso en Compose

```kotlin
import org.shareat.shared.designsystem.generated.resources.Res
import org.shareat.shared.designsystem.generated.resources.ic_shareat_circle_white
import org.jetbrains.compose.resources.painterResource

Image(
    painter = painterResource(Res.drawable.ic_shareat_circle_white),
    contentDescription = "Shareat"
)
```

Los accessors `Res.drawable.*` se generan automáticamente a partir de los SVG por el plugin de Compose Multiplatform.

## Reglas para PR

1. Los nuevos assets de marca van en `shared/designsystem/src/commonMain/composeResources/drawable/`, nunca en módulos de feature.
2. Convención de nombres:
   - `ic_shareat_*` para el icono (marca gráfica).
   - `logotype_shareat_*` para icono + texto.
   - Sufijo `_white`, `_dark`, `_gradient` para variantes de color.
3. Formato SVG para vectores. PNG solo si el asset tiene efectos raster complejos.
4. Toda adición o cambio de asset debe actualizar esta guía.

## Iconos de lanzador (por plataforma)

Los launcher icons son recursos específicos de plataforma y se gestionan fuera de `:shared:designsystem`:

### Android

- **Ruta:** `androidApp/src/main/res/`
- **Adaptive icon** (API 26+): foreground y monochrome como vector drawables en `drawable/`, background como webp en `mipmap-*/`.
- **Fallback** (pre-API 26): webps prerrenderizados en `mipmap-mdpi/` a `mipmap-xxxhdpi/`.
- **Configuración:** `mipmap-anydpi-v26/ic_launcher.xml` y `ic_launcher_round.xml`.
- **Herramienta:** Android Studio > Image Asset para regenerar todas las densidades.

### iOS

- **Ruta:** `iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/`
- **Formato moderno** (Xcode 15+): un solo PNG de 1024x1024 por variante.
- **Variantes:** `app-icon-light.png`, `app-icon-dark.png`, `app-icon-tinted.png`.
- **Configuración:** `Contents.json` en el mismo directorio.

## Decisiones pendientes

- Añadir variante de logotipo con gradiente si se necesita.
