# Product scope and MVP rules

Source: `docs/product-definition.md`, closed for the MVP on 2026-08-12 (issue #7). This file summarizes the rules most likely to matter while writing code; the full document also covers metrics, delivery phases, risks, and the decision register — read it directly for those.

## Who does what

- **Visitor** (no account): browse restaurant list, search/filter when available, view restaurant/menu/dish info and public reviews, open deep links.
- **Customer** (account required for writes): register/sign in/manage profile, rate and review a restaurant or a dish, see their own review history, save favourites, mark dishes eaten (post-MVP filtering), add a personal restaurant (post-MVP).
- **Restaurant** (account required): register/sign in/manage profile, create/update/publish/disable/delete its menu and dishes, generate a QR for an active menu (post-MVP), receive notifications and paid analytics (post-MVP). **Restaurants cannot rate dishes or restaurants.**

## Hard product rules (check these before implementing a related feature)

- Browsing public content never requires an account; writing (reviews, favourites, personal content, restaurant content) always does.
- Only customer accounts can create ratings/reviews — enforce this in domain/backend, not just UI, and it must hold even through direct API calls.
- A review targets exactly one restaurant or one dish (`ReviewTarget`), never both.
- Rating is an integer 1–5. At most one review per customer+target; resubmitting **edits** the existing review, it never creates a duplicate.
- The aggregate is the arithmetic mean of public ratings, rounded to one decimal, shown with the public rating count. Private/hidden reviews are excluded.
- A review's comment is optional; the author chooses public or private per review. Private reviews are visible only to their author.
- Public reviews publish immediately (no pre-moderation). Signed-in users can report them; moderators can hide/restore/remove with a reason and an audit trail. Restaurant owners cannot suppress reviews themselves.
- Only enabled menus and dishes are visible to visitors — draft/disabled content never appears in a public read path.
- Allergen info is optional and restaurant-supplied; Shareat must show its source and a "confirm with the restaurant" notice. Allergen filtering/verification is post-MVP.
- One structured menu per restaurant in the MVP (multiple menus is post-MVP). A dish needs a name and price; description, image, and allergens are optional.
- Billing/subscription state never blocks sign-in, account recovery, subscription management, data export, privacy settings, or account deletion. When a subscription lapses, the menu unpublishes but stays editable/exportable by its owner.

## Explicitly out of scope for the MVP

Social feed, follow/unfollow, an Instagram-like activity feed, public/collaborative lists, advanced map search, multiple menus per restaurant, personal (customer-added) restaurants outside the catalogue, QR menus, push notifications, deep-link sharing beyond basic open, restaurant verification, customer email verification, freemium analytics tier, allergen filters.

If a request would add one of these, flag it as post-MVP scope rather than silently implementing it — the product definition treats "explicitly outside the MVP" as a real boundary the team agreed to, not a soft suggestion.

## Non-functional bar worth knowing

- Compatibility: Android API 24+, iOS 18.2+, current + previous major of Chrome/Firefox/Safari/Edge. Phone layouts required; tablet/desktop-web must stay usable but aren't a dedicated MVP target.
- Performance: p75 initial usable content and restaurant/menu detail ≤ 3s on mid-range/4G; interaction feedback ≤ 100ms; list scroll targets 60fps; dish images ≤ 500KB; slow/failed network shows a recoverable timeout state within 10s.
- Every flow needs loading, empty, success, recoverable-error, and offline states, plus unauthenticated/unauthorised states and confirmation before destructive actions.
- Localisation: Spanish is the launch language; user-facing copy must not be hard-coded (so more languages can follow post-MVP).
