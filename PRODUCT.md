# Product

<!-- impeccable:product-schema 1 -->

## Platform

adaptive

## Users

Shareat serves anonymous visitors, customer accounts, and restaurant accounts in Spain. Customers discover restaurants and review restaurants or individual dishes. Restaurant owners maintain the public profile and the single menu attached to their restaurant.

## Product Purpose

Shareat makes restaurant and dish discovery easy while letting customers keep structured reviews and giving restaurants a simple way to publish accurate profile and menu information.

## Positioning

The MVP validates dish-level reviews rather than stopping at restaurant-level ratings, while keeping public browsing available without authentication.

## Operating Context

The product launches on Android, iOS, and web. Spanish is the launch language and all user-facing copy must remain localisable. A restaurant account lands directly in its management experience after authentication. On first access Shareat creates a private draft workspace with editable example content, so the owner can replace the profile details and build the real menu without completing a separate onboarding form.

## Capabilities and Constraints

- A restaurant publishes one menu; multiple menus and menu selection are out of MVP scope.
- The first restaurant workspace is created once and never reseeded; later edits or deletions belong to the owner.
- New workspaces start as drafts with a packaged placeholder image, example address, and two example dishes.
- Only published restaurants and enabled dishes from their published menu are visible publicly.
- Restaurant and dish images are optional.
- Every dish requires a name and price; description, image, category, and allergen declaration are optional.
- Allergen data is supplied by the restaurant and public UI must not interpret a missing declaration as allergen-free.
- Restaurant owners cannot create restaurant or dish reviews.
- Authentication and role authorisation are enforced by the backend; navigation guards are only a UX layer.

## Brand Commitments

Use the existing Shareat Material 3 theme, responsive navigation, typography, brand assets, and the established customer-facing Restaurant screen as the visual authority for related restaurant surfaces.

## Evidence on Hand

- Product decisions: `docs/product-definition.md`.
- Architecture, navigation, testing, and UI conventions: `docs/architecture/README.md`, `docs/navigation/README.md`, `docs/testing/README.md`, and `docs/ui/README.md`.
- Existing restaurant screen and reusable visual patterns: `feature/restaurant/ui`.
- Restaurant-owner scope notes: `feature/restaurantHome/README.md`.
- Domain repositories and Supabase-backed mutation contracts for restaurants, menus, dishes, and images already exist in `shared/domain` and `shared/data`.

## Product Principles

- Keep public discovery frictionless.
- Make dish-level feedback the distinctive product behavior.
- Let restaurants manage the same information customers actually see.
- Preserve honest publication and allergen states instead of implying unavailable data.
- Maintain one coherent adaptive experience across Android, iOS, and web.

## Accessibility & Inclusion

Follow Material 3 interaction conventions, 48 dp minimum touch targets, system font scaling, dark theme, window insets, keyboard/IME behavior, localisable copy, and reduced-motion settings on supported platforms.
