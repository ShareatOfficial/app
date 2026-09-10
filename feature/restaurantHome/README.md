# Restaurant Home

Restaurant Home is the authenticated landing and catalogue-management surface for restaurant
accounts. A first access creates an idempotent private draft workspace with the “Rincón de Paco”
profile, a packaged fallback photo, one draft menu, and two example dishes. Returning owners always
receive their existing workspace unchanged.

The screen supports:

- switching between management and customer-preview modes;
- editing the restaurant photo, name, description, street, locality, postal code, and region;
- publishing or unpublishing the restaurant together with its single menu;
- filtering the menu by dish category and excluded allergens;
- adding dishes and editing their photo, name, description, price, allergens, and publication state;
- showing ratings without exposing customer review actions to restaurant accounts;
- adapting content and sheets to compact and wide windows, with 48 dp touch targets, wrapping
  filters, safe-area/IME padding, field-specific keyboards, and Next/Done focus actions.

Restaurant and dish mutations verify the active authenticated owner. Multi-step catalogue writes
use compensation so a failed menu update does not leave an orphaned or partially edited dish.
Image selection uses FileKit and accepts JPEG, PNG, or WebP files up to 512 KB.

Restaurant-level dish-type configuration remains intentionally labelled as forthcoming because the
product has no restaurant-level type model yet. Per-menu dish categories are persisted and used by
the current filters.
