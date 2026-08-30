# UI: estados de carga y skeletons

## Objetivo

Que cualquier pantalla en estado de carga se sienta parte de la misma app: la misma velocidad de parpadeo y la misma forma de placeholder, en vez de que cada feature invente su propio spinner o su propia animación.

## Decisión vigente

- El efecto de shimmer vive en `:shared:designsystem` (`org.shareat.shared.designsystem.shimmerEffect`), un módulo hoja de Compose sin dependencias de `domain`, `data`, Koin ni de otras features — igual que `:shared:navigation`, pero para UI en vez de navegación. Cualquier `:feature:<nombre>:ui` o `:shared:ui` puede depender de él.
- `Modifier.shimmerEffect(color, shape = RectangleShape)` anima el alpha de `color` en bucle (misma duración y rango de alpha para toda la app) y lo aplica como fondo con la forma indicada. Se usa para pintar el cuerpo de cualquier caja placeholder de un skeleton.
- El `Loading` de un ViewModel siempre se renderiza como un **skeleton con la forma esperada de la pantalla ya cargada**, no como un spinner centrado ni una pantalla en blanco. Ejemplo de referencia: `HomeScreen.HomeLoading` (`feature/home/ui`), que muestra la sección de highlights (`RestaurantHighlightsSectionSkeleton`, 4 tarjetas) seguida de una tarjeta standalone (`RestaurantCardSkeleton`), reproduciendo el layout real (`RestaurantHighlightsSection` + `RestaurantStandaloneCard`).

## Responsabilidades por módulo

- `:shared:designsystem`: solo el efecto visual reutilizable (`shimmerEffect`). No conoce ninguna feature ni modelo de dominio.
- `:feature:<nombre>:ui`: define el skeleton concreto de cada componente (`XyzCardSkeleton`) reutilizando `shimmerEffect`, y decide cómo se compone el skeleton de pantalla completa a partir de esos componentes.

## Patrón de implementación

```kotlin
@Composable
internal fun RestaurantCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColor = MaterialTheme.colorScheme.onSurface
    Card(modifier = modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(RestaurantHeroHeight)
                .shimmerEffect(shimmerColor),
        )
        // ... más placeholders con shimmerEffect(shimmerColor, RoundedCornerShape(4.dp))
    }
}
```

El skeleton de una pantalla completa compone los skeletons de sus componentes en el mismo layout que usará el contenido cargado (mismas secciones, mismo orden), para que la transición Loading -> Loaded no salte.

## Reglas revisables en PR

- Un nuevo estado `Loading` de pantalla no se resuelve con un `CircularProgressIndicator` suelto: debe construir un skeleton que reproduzca el layout esperado.
- Cualquier placeholder animado usa `Modifier.shimmerEffect(...)` de `:shared:designsystem`; no se implementa una animación de alpha propia por componente.
- `:shared:designsystem` no importa Koin, `domain`, `data` ni ninguna feature — solo Compose.

## Pendiente

- Extraer más efectos compartidos (por ejemplo, badges o estados vacíos comunes) a `:shared:designsystem` a medida que se repitan entre features.
- Trabajo relacionado: [#52](https://github.com/ShareatOfficial/app/issues/52) (Home screen UI, donde se introdujo el primer skeleton y el módulo `:shared:designsystem`).
