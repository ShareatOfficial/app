# UI: estados de carga y skeletons

## Objetivo

Que cualquier pantalla en estado de carga se sienta parte de la misma app: la misma velocidad de parpadeo y la misma forma de placeholder, en vez de que cada feature invente su propio spinner o su propia animación.

## Decisión vigente

- `:shared:designsystem` (`org.shareat.shared.designsystem`) es un módulo hoja de Compose sin dependencias de `domain`, `data`, Koin ni de otras features — igual que `:shared:navigation`, pero para UI en vez de navegación. Cualquier `:feature:<nombre>:ui` o `:shared:ui` puede depender de él. Aloja dos cosas: efectos visuales reutilizables (`shimmerEffect`) y **componentes compartidos entre features** en `org.shareat.shared.designsystem.components` (`ReviewCard`, `RatingBadge`).
- Cuando dos features necesitan el mismo componente, se promociona a `:shared:designsystem` en vez de duplicarlo o de que una feature importe los internals de otra. `ReviewCard` nació como `DishReviewCard` en `:feature:home:ui` y se promocionó al necesitarlo también el panel de reseñas de plato de `:feature:restaurant:ui`. Un componente compartido recibe primitivas (`String`, `Int`), nunca modelos de dominio ni ui states de una feature.
- `Modifier.shimmerEffect(color, shape = RectangleShape)` anima el alpha de `color` en bucle (misma duración y rango de alpha para toda la app) y lo aplica como fondo con la forma indicada. Se usa para pintar el cuerpo de cualquier caja placeholder de un skeleton.
- El `Loading` de un ViewModel siempre se renderiza como un **skeleton con la forma esperada de la pantalla ya cargada**, no como un spinner centrado ni una pantalla en blanco. Ejemplo de referencia: `HomeScreen.HomeLoading` (`feature/home/ui`), que muestra la sección de highlights (`RestaurantHighlightsSectionSkeleton`, 4 tarjetas) seguida de una tarjeta standalone (`RestaurantCardSkeleton`), reproduciendo el layout real (`RestaurantHighlightsSection` + `RestaurantStandaloneCard`).
- Una pantalla que se abre con el dato ya en la mano (patrón *payload* en [Navegación](../navigation/README.md)) no tiene estado `Loading`: pinta el contenido cargado en el primer frame. Aun así mantiene sus skeletons y los muestra mientras hay una **recarga** en curso, de modo que la regla se cumple en lo que importa: siempre que la pantalla espera datos, enseña la forma de esos datos. `RestaurantScreen` lo hace con *pull to refresh*.

## Responsabilidades por módulo

- `:shared:designsystem`: efectos visuales reutilizables (`shimmerEffect`), componentes compartidos entre features (`components/ReviewCard`, `components/RatingBadge`) y la anotación `preview/FormFactorPreviews` con los tamaños de referencia. No conoce ninguna feature ni modelo de dominio.
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
- `:shared:designsystem` no importa Koin, `domain`, `data` ni ninguna feature — solo Compose. Sus componentes reciben primitivas, no modelos de dominio.
- Ninguna feature importa un composable `internal` de otra feature; si hace falta compartirlo, se promociona a `:shared:designsystem`.
- Una preview multi-dispositivo usa `@FormFactorPreviews` de `:shared:designsystem`; no se redeclara la lista de `@Preview` con anchos y altos propios por módulo. Una preview que varía el *estado* (no el tamaño) sigue siendo un `@Preview` suelto.

## Pendiente

- Extraer más componentes y efectos compartidos (por ejemplo, estados vacíos comunes) a `:shared:designsystem` a medida que se repitan entre features.
- Trabajo relacionado: [#52](https://github.com/ShareatOfficial/app/issues/52) (Home screen UI, donde se introdujo el primer skeleton y el módulo `:shared:designsystem`).
