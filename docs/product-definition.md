# Shareat — Product definition and MVP decisions

> Product source of truth. MVP decisions in this document were closed on 2026-08-12 for [issue #7](https://github.com/ShareatOfficial/app/issues/7). Anything explicitly labelled post-MVP is not required for the first release.

## 1. Product objective

Shareat aims to make it easy for people to discover restaurants and keep a personal record of the dishes and restaurants they have tried. At the same time, it gives restaurants a simple way to publish their profile and learn what customers think.

The initial value proposition is:

- For customers: find restaurants and dishes, rate experiences, and revisit their own reviews.
- For restaurants: create a profile and receive structured customer feedback.
- For visitors without an account: browse restaurants with as little friction as possible.

### Expected business result

Validate that customers are willing to review individual dishes—not only restaurants.

## 2. Users and main use cases

### Primary launch user

The primary MVP user is a customer in Spain who wants to discover restaurants and rate restaurants or individual dishes. Restaurants are the supply-side user: the pilot will onboard as many willing restaurants as the team can support rather than waiting for a fixed minimum cohort.

The pilot scope is:

- Geography: Spain; no city-level restriction for the initial pilot.
- Product language: Spanish. User-facing copy must remain localisable.
- Platforms: Android, iOS, and web launch together.
- Restaurant cohort: invitation/onboarding based on availability, with no numeric launch gate.
- Customer access: open beta; anonymous browsing is allowed.

### Visitor

No authentication is required for public browsing.

- View the restaurant list.
- Search or filter restaurants when the feature is available.
- View restaurant information, dishes, and public reviews.
- Open shared content through deep links.

### Customer

Authentication is required for personal or write operations.

- Register, sign in, and manage a profile.
- Rate and review a restaurant.
- Rate and review a dish.
- See their own review history.
- Save restaurants or dishes as favourites.
- Mark dishes as eaten and filter their personal history.
- Add a personal restaurant or review when the restaurant is not yet on Shareat.
- In later phases, create public lists and follow other users.

### Restaurant

- Register, sign in, and manage its profile.
- Receive notifications and, in a paid tier, consult value-added analytics.
- Request or obtain verification of the restaurant profile.

Restaurants cannot rate dishes or restaurants.

## 3. Scope and product rules

### Included in the initial product direction

- Restaurant and customer accounts.
- Restaurant profiles and dishes.
- Restaurant and dish reviews.
- Restaurant discovery and public dish browsing.
- A personal review history.

### Explicitly outside the MVP

- Social feed.
- Follow and unfollow relationships.
- Instagram-like activity experience.
- Public collaborative or Spotify-like lists.
- Advanced map search.

### Agreed MVP rules

- **Authentication:** browsing public content does not require an account. Creating reviews, favourites, personal content, or restaurant content does.
- **Review authorship:** only customer accounts can create ratings and reviews.
- **Review targets:** a review targets either one restaurant or one dish.
- **Personal entries:** customer-created restaurants outside the official catalogue are post-MVP.
- **Dish visibility:** only enabled dishes from published restaurants are visible to visitors.
- **Ratings:** integer scale from 1 to 5, with one review per customer and target. The customer can edit or delete it.
- **Review aggregate:** arithmetic mean of public ratings, rounded to one decimal, displayed with the public rating count. Private reviews are excluded.
- **Comments and visibility:** a rating may include an optional comment. The author chooses whether the whole review is public or private. A private review is visible only to its author.
- **Moderation:** public reviews are published immediately. Signed-in users can report them; moderators can hide them while investigating and restore or remove them, with an audit trail. Restaurant owners cannot suppress reviews themselves.
- **Allergens:** optional, restaurant-supplied information in the MVP. The restaurant is responsible for keeping it accurate; Shareat must display its source and a notice to confirm allergens directly with the restaurant. Allergen filtering and verification are post-MVP.

## 4. MVP and post-MVP boundary

### MVP

- Restaurant registration and sign-in.
- Customer registration and sign-in.
- Restaurant list.
- Restaurant detail with its profile and dishes.
- A customer can rate and optionally review a restaurant.
- A customer can rate and optionally review a dish.
- A customer can see their own reviews.

### Post-MVP — next candidates

- Restaurant verification.
- Customer email verification.
- Freemium restaurant tier with useful analytics.
- Allergen filters.
- Filters for favourites, eaten dishes, and rated content.
- Restaurant types or categories.
- Personal entry for a restaurant not yet on Shareat.

### Post-MVP — later candidates

- Restaurant search on a map.
- Public personal lists for restaurants and dishes.
- Multiple menus per restaurant.
- Images remain available as optional dish media in the MVP.
- Push notifications.
- Deep links and native sharing for a dish, restaurant, list, or profile.

### Post-MVP — not planned until the core product shows retention

- Social feed.
- Follow/unfollow.
- Followers and following lists.
- Instagram-like feed based on followed users and restaurant activity.

## 5. Screens and critical flows

### MVP screens

| Area | Screen | Main purpose |
| --- | --- | --- |
| Access | Welcome / sign-in / registration | Enter as customer or restaurant; allow public browsing |
| Discovery | Home | Show the restaurant list and entry point to search/filtering |
| Restaurant | Restaurant detail | Show profile, rating summary, dishes, and reviews |
| Customer | Customer profile | View and edit customer information |
| Customer | Review history | See reviews created by the signed-in customer |
| Review | Restaurant review form | Add or edit a restaurant rating and comment |
| Review | Dish review form | Add or edit a dish rating and comment |
| Restaurant admin | Restaurant profile | View and edit restaurant information |

### Critical customer flow

1. Open the app without signing in.
2. Browse the restaurant list.
3. Open a restaurant and a dish.
4. Choose to rate the restaurant or dish.
5. Register or sign in if necessary.
6. Submit the rating and optional comment.
7. See the new entry in review history.

### Critical restaurant flow

1. Register or sign in as a restaurant.
2. Complete the restaurant profile.
3. Confirm that a visitor can view the profile from the restaurant detail.

### States required on every flow

- Loading, empty, success, recoverable error, and offline/network error.
- Form validation and prevention of accidental duplicate submission.
- Unauthenticated and unauthorised states.
- Deleted, disabled, or unavailable content.
- Confirmation before destructive actions.

## 6. Functional requirements

### Identity and access

- Accounts have one role: customer or restaurant.
- Public content can be read without an account.
- Protected actions validate both authentication and role on the server.
- Sign-out and account recovery are required even if they need separate UI tasks.

### Restaurant catalogue

- The system lists public restaurants and opens a restaurant detail.
- A restaurant owns its profile and dishes.
- Draft or disabled content is not shown publicly.

### Reviews

- A customer selects an integer rating from 1 to 5 and may add a comment.
- There is at most one review per customer and target; resubmitting edits that review instead of creating a duplicate.
- The author can edit, delete, and switch their review between public and private.
- Public aggregates use only currently visible public ratings and show the arithmetic mean rounded to one decimal plus the rating count.
- Reviews store author, target, timestamps, and moderation status.
- The restaurant and dish views show the agreed rating aggregate.
- A customer can view their own reviews even if a target is later disabled, subject to retention policy.
- Public reviews appear immediately and can be reported. Moderator actions require a reason and an audit entry.

### Personal organisation

- Favourites, eaten status, personal lists, and private restaurants are separate concepts and should not be represented by a single generic flag.
- These features are post-MVP unless this decision register is explicitly revised.

### Notifications and sharing

Potential notification events include a new restaurant nearby, a new restaurant review, or a new follower. Each event requires consent, frequency controls, and a deep-link destination. The exact MVP event set is not yet agreed.

## 7. Preliminary data model

This is a product model, not a final database schema.

- **Account:** identity, role, status, verification state.
- **CustomerProfile:** display name, avatar, preferences, privacy settings.
- **Restaurant:** owner account, name, description, address/location, category, verification state, publication state.
- **Dish:** restaurant, name, description, image, allergen data, enabled state.
- **Review:** customer, target type, target ID, rating, comment, moderation state, timestamps.
- **Favourite:** customer and restaurant/dish target.
- **DishHistory:** customer, dish, eaten state and date.
- **PersonalRestaurant:** customer-owned external restaurant reference and visibility.
- **List / ListItem:** owner, visibility, ordered restaurant/dish entries; post-MVP.
- **Follow:** follower and followed customer; not now.
- **Notification:** recipient, event type, payload, delivery/read state; post-MVP.

## 8. Non-functional requirements

- **Security:** secure session/token storage, server-side authorisation, rate limiting, input validation, secrets outside source control, and protection against account enumeration.
- **Privacy:** collect only data needed for the documented purpose and record consent where required. Customers and restaurants can export their account and authored content in a portable format and request account deletion without a subscription. Account access is disabled immediately; active personal data is deleted or irreversibly anonymised within 30 days, and encrypted backup copies expire within 90 days. Public reviews disappear immediately on deletion; a restricted moderation copy may be retained for up to 30 days for appeals or abuse investigation, unless a documented legal obligation requires a longer hold.
- **Accessibility:** semantic labels, scalable text, sufficient contrast, keyboard/focus support where applicable, and no flow that relies on colour alone.
- **Performance:** on a supported mid-range device and a stable 4G connection, the 75th percentile for initial usable content and restaurant detail is at most 3 seconds; visible feedback after an interaction appears within 100 ms; list scrolling targets 60 frames per second; and a delivered dish image is at most 500 KB. A recoverable timeout state appears within 10 seconds on a slow or failed network.
- **Reliability:** graceful error handling, retry policy, idempotent writes, backups, and a recovery plan.
- **Compatibility:** Android API 24 (Android 7.0) and later; iOS 18.2 and later; and the current and immediately previous major versions of Chrome, Firefox, Safari, and Edge at release time. Phone layouts are required; tablet and desktop-web layouts must remain usable but do not require dedicated MVP optimisation.
- **Localisation:** Spanish is the launch language; user-facing copy must not be hard-coded so more languages can be added post-MVP.
- **Observability:** structured logs, crash reporting, API health monitoring, and alerts without sensitive data.
- **Moderation:** reporting, temporary hiding, removal/restoration, a reason for every action, and an audit trail are required before public comments launch.

## 9. Data and integrations

### Expected data

- Account and profile data.
- Restaurant location and business information.
- Menus, dishes, prices, images, and allergen information.
- Ratings, comments, favourites, and personal history.
- Consent, verification, moderation, and audit state.
- Product analytics events using pseudonymous identifiers where possible.

### Integration scope

MVP integrations whose provider or implementation remains a technical decision in section 17:

- Authentication, account recovery, and transactional email.
- Backend/API, database, and image/file storage and transformation.
- Analytics, crash reporting, and monitoring.
- Monthly subscription billing, using RevenueCat or another provider that meets the platform and store requirements.

Maps/places, push notifications, deep links, and advertising integrations are post-MVP.

## 10. Current stack and architecture

### Verified in the repository

- Kotlin Multiplatform project.
- Shared Kotlin Multiplatform code split into `:shared:domain`, `:shared:data`, and `:shared:ui`; only UI applies Compose.
- Android application.
- iOS application entry point and shared framework integration.
- Web applications targeting JavaScript and WebAssembly.
- Gradle version catalogue and Kotlin tests available in `commonTest`.

### Recorded frontend architecture decisions

- Each new feature is split into `data`, `domain`, and `ui` Gradle modules with inward dependencies toward domain.
- Frontend development starts with repository mocks in `data`; infrastructure DTOs do not cross into domain or UI.
- Koin is the dependency injection framework, with the app module acting as the composition root.
- Each screen declares a navigation interface in its `ui` module. The app owns its implementation and `NavKey`, while the feature owns its entry builder.
- Repositories, use cases, and ViewModels have JUnit unit tests.
- Implementation guidance lives in the [technical documentation index](./README.md).

### Architecture decisions still open

- Backend technology, API style, database, storage, and hosting ([#34](https://github.com/ShareatOfficial/app/issues/34)).
- Refined data model and representation contracts ([#35](https://github.com/ShareatOfficial/app/issues/35)).
- Authentication/session model and role-based authorisation ([#33](https://github.com/ShareatOfficial/app/issues/33)).
- Networking, serialisation, persistence, and image loading libraries.
- Environment strategy for local, development, staging, and production.
- CI/CD, signing, store distribution, feature flags, and secrets management.
- Observability and analytics providers.

These are technical follow-ups, not unresolved MVP product scope. Their owners and deadlines are recorded in section 17. An Architecture Decision Record should document each material choice and its trade-offs before implementation depends on it.

## 11. Acceptance criteria for the MVP

The MVP is functionally complete when all of the following are demonstrable in a staging-like environment:

1. A visitor can browse the restaurant list and published dishes without signing in.
2. A new customer can register, sign in, sign out, and recover access.
3. A new restaurant can register, sign in, and manage only its own content.
4. Published restaurant and dish data is visible to visitors; drafts are not.
6. A customer can submit a valid restaurant rating and a valid dish rating.
7. A restaurant account cannot create a review, including through the API.
8. The customer can see the reviews they authored.
9. Invalid, duplicate, unauthorised, offline, loading, and empty states have agreed behaviour.
10. The chosen monetisation experiment works in its supported environment and does not block essential account recovery or privacy actions.
11. Critical flows pass automated and manual acceptance tests on the supported platforms.
12. Production monitoring, rollback, privacy documentation, and support ownership are ready.

## 12. Delivery plan

### Phase 0 — Product and technical definition

- Resolve the dated technical decisions in section 17.
- Produce low-fidelity critical flows and a small design system.
- Define the API/data model and choose the backend and identity strategy.
- Set up environments, CI, quality checks, analytics taxonomy, and Definition of Done.

### Phase 1 — Vertical foundation

- Implement identity and roles.
- Deliver a thin end-to-end restaurant list/detail using the real backend.
- Establish navigation, state handling, observability, and deployment.

### Phase 2 — Restaurant publishing

- Restaurant profile.
- Dish catalogue and visibility states.

### Phase 3 — Customer reviews

- Restaurant and dish rating flows.
- Personal review history.
- Aggregates and basic moderation.

### Phase 4 — MVP hardening and release

- Monetisation experiment.
- Accessibility, security, privacy, performance, and compatibility work.
- End-to-end QA, beta, monitoring, rollback rehearsal, and launch.

Estimation should happen by epic after the Phase 0 decisions. Use ranges and record assumptions rather than assigning precise dates to unresolved scope.

## 13. Quality and ways of working

### Ownership

The current team—[@javigp2002](https://github.com/javigp2002), [@tonela10](https://github.com/tonela10), and [@TorrellesN](https://github.com/TorrellesN)—shares responsibility for product, design, engineering, QA/acceptance, moderation, and launch. Any member can perform any role; a release or moderation action must still record which member executed and approved it. All three are global code owners in `.github/CODEOWNERS`.

### Workflow

- Short-lived feature branches and pull requests into the agreed integration branch.
- At least one review for product code; additional review for security-sensitive changes.
- Small, independently testable tickets linked to an epic and acceptance criteria.
- Changes to scope are recorded here or in the product backlog, not only in chat.

### Definition of Ready

A ticket has a user outcome, scope, acceptance criteria, designs or states when relevant, dependencies, analytics needs, and no unresolved product decision that changes implementation materially.

### Definition of Done

Implementation and error states are complete; tests and quality checks pass; accessibility and analytics are covered where relevant; documentation is updated; the change has been reviewed and validated in the agreed environment.

### Test strategy

- Unit tests for domain rules, validation, and rating aggregation.
- Integration tests for authentication, authorisation, persistence, and API contracts.
- UI tests for critical states and role-specific behaviour.
- End-to-end tests for the two critical flows.
- Manual exploratory testing on the supported devices/browsers.
- Security and accessibility checks before release.

## 14. Success metrics

Final targets require a baseline and measurement window. Candidate metrics are:

- Visitor-to-registration conversion after attempting a protected action.
- Customer activation: first valid rating/review created.
- Review completion rate and median time to submit.
- Weekly active customers and restaurants.
- Restaurant/detail load success and latency.
- Crash-free sessions and failed write rate.
- Review report/abuse rate.
- Conversion or revenue for the selected monetisation experiment.
- Restaurant retention and usage of paid analytics in a later phase.

## 15. Risks and mitigations

| Risk | Impact | Initial mitigation |
| --- | --- | --- |
| Scope spans customer, restaurant, social, maps, notifications, and monetisation | MVP delay | Enforce Must scope and treat everything else as a separate phase |
| Launching Android, iOS, and web simultaneously | Larger QA and delivery surface | Choose the MVP platforms explicitly |
| Empty marketplace at launch | Low customer value | Seed a pilot area and onboard a small restaurant cohort |
| Duplicate or incorrect restaurant data | Poor trust and fragmented reviews | Define ownership, claim, merge, and verification flows |
| Fraudulent or abusive reviews | Reputational and legal risk | Add reporting, moderation states, rate limits, and auditability |
| Incorrect allergen information | Health and legal risk | Define provenance, disclaimers, edit responsibility, and verification before launch |
| Monetisation harms the core experience | Lower activation/retention | Test a narrow experiment with guardrail metrics |
| Billing provider or store rules conflict with the subscription flow | Blocked monetisation implementation | Validate the provider and store-specific rules before estimating billing work |
| Location and push permissions reduce trust | Lower opt-in | Ask contextually, explain value, and support denial gracefully |
| Architecture chosen before platform scope | Unnecessary complexity | Decide launch platforms and vertical slice first |

## 16. Launch and post-MVP

### Launch readiness

- The pilot covers Spain, launches in Spanish on Android, iOS, and web, and onboards restaurants as the team can support. The three shared owners are acceptance owners.
- Store, signing, domain, privacy, and support materials ready for chosen platforms.
- Production data migration/seed plan tested.
- Feature flags or kill switches for risky integrations.
- Monitoring dashboards and alerts active.
- Rollback procedure tested and decision owner available.
- Support, moderation, and incident channels staffed.

### Known post-MVP backlog

- Verification improvements.
- Allergen and personal-history filters.
- Favourites and eaten status.
- Personal restaurants outside the catalogue.
- QR menus.
- Multiple or scheduled menus.
- Restaurant analytics and freemium packaging.
- Maps and nearby restaurant discovery.
- Sharing and deep links.
- Public lists.
- Notifications.
- Social graph and feed, only after the core marketplace shows retention.

## 17. Decision register

### Closed for the MVP on 2026-08-12

| Decision | Outcome | Owners |
| --- | --- | --- |
| Objective and primary user | Help customers in Spain discover restaurants and rate restaurants and dishes | Entire team |
| Launch scope | Spain, Spanish, Android, iOS, and web; open customer beta and availability-based restaurant cohort | Entire team |
| Public access | Anonymous browsing; authentication for write and personal actions | Entire team |
| Reviews | Integer 1–5; one per customer/target; editable/deletable; optional comment; public/private choice; immediate public publication with reporting | Entire team |
| Aggregation | Arithmetic mean of visible public ratings, one decimal, with rating count | Entire team |
| Compatibility and performance | Targets in section 8 | Entire team |
| Privacy and moderation | Export/deletion and retention limits in section 8; immediate publication, reporting, moderator controls, and audit trail | Entire team |
| Responsibilities | `@javigp2002`, `@tonela10`, and `@TorrellesN` share every delivery role and global code ownership | Entire team |
| Delivery policy | Definition of Ready, Definition of Done, short-lived branches, linked PRs, review, and staged validation as defined in this document | Entire team |

### Pending technical or experiment implementation decisions

These items do not change the agreed MVP product boundary. “Entire team” means all three owners above; the person implementing an item records the final choice in its issue or ADR.

| Pending decision | Owner | Decision due | Tracking/deliverable |
| --- | --- | --- | --- |
| Authentication, session, recovery, and role/ownership authorisation | Entire team | 2026-08-26 | [Issue #33](https://github.com/ShareatOfficial/app/issues/33) |
| Backend, API style, database, storage, hosting, and environments | Entire team | 2026-08-26 | [Issue #34](https://github.com/ShareatOfficial/app/issues/34) |
| Data model and representation contracts | Entire team | 2026-08-26 | [Issue #35](https://github.com/ShareatOfficial/app/issues/35) |
| Networking, serialisation, local persistence, and image-loading libraries | Entire team | 2026-09-02 | Architecture proposal/ADR |
| CI/CD, signing, distribution, feature flags, secrets, observability, and analytics providers | Entire team | 2026-09-02 | Architecture proposal/ADRs |
| Subscription price, trial, billing provider, grace/retry behaviour, and store-specific rules | Entire team | 2026-09-09 | Monetisation experiment specification |
| Analytics event taxonomy, baselines, success thresholds, and monetisation guardrails | Entire team | 2026-09-09 | Measurement plan |
| Validation of privacy notice, allergen wording, and retention policy for launch | Entire team | 2026-09-09 | Launch/privacy checklist |
| Release cadence and the first release window | Entire team | 2026-09-09 | Release plan |

The next Phase 0 deliverable is a validated set of critical-flow wireframes plus the technical architecture proposal. It is owned by the entire team and due on 2026-09-09.
