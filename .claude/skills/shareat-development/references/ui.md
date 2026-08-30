# UI: loading states and skeletons

## Goal

Every screen's loading state looks and feels like the rest of the app — same shimmer pace, same idea of "show the shape of what's coming" — instead of each feature inventing its own spinner or blank screen.

## The rule

**A screen's `Loading` ui state renders a skeleton shaped like its own `Loaded` state, not a bare spinner.** Build the skeleton out of the same sections/components the loaded screen uses, each swapped for a placeholder version, in the same order and layout. The Loading -> Loaded transition should not jump.

Reference implementation: `feature/home/ui/.../home/HomeScreen.kt`'s `HomeLoading` composes `RestaurantHighlightsSectionSkeleton` (from `composables/RestaurantHighlightsSection.kt`) followed by a `RestaurantCardSkeleton`, mirroring exactly the `RestaurantHighlightsSection` + standalone `RestaurantCard` layout the loaded feed renders.

## `:shared:designsystem`

A Compose-enabled leaf module with no dependency on `domain`, `data`, Koin, or any feature — parallel to `:shared:navigation`, but for shared UI instead of shared navigation contracts. It holds cross-feature, presentation-only visual effects. Today it has one: `Modifier.shimmerEffect(color: Color, shape: Shape = RectangleShape)`, which pulses `color`'s alpha in a loop and paints it as this shape's background.

Any `:feature:<name>:ui` module can add `implementation(project(":shared:designsystem"))` directly, the same way it already depends on `:shared:navigation`.

## Building a skeleton component

Each feature owns its own skeleton composables (`XyzCardSkeleton`, `XyzSectionSkeleton`) — `:shared:designsystem` only supplies the shimmer effect, not feature-shaped placeholders.

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
        // additional placeholder bars: .shimmerEffect(shimmerColor, RoundedCornerShape(4.dp))
    }
}
```

## Review rules

- A new `Loading` ui state is never just a `CircularProgressIndicator` centered on the screen — it composes skeleton versions of the real sections.
- Any placeholder animation uses `Modifier.shimmerEffect(...)` from `:shared:designsystem` — no per-component `rememberInfiniteTransition` reimplementing the same alpha pulse.
- `:shared:designsystem` stays framework-thin: Compose only, no `domain`/`data`/Koin/feature imports. If a shared visual needs domain types, that mapping happens in the feature's `ui`, not in `:shared:designsystem`.

## Related

- [Architecture](architecture.md) — where `:shared:designsystem` sits in the module graph.
- `docs/ui/README.md` — the living doc version of this convention.
