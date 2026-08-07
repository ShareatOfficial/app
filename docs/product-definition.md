# Shareat — Product definition and kickoff notes

> Working document created from the kickoff meeting. It separates agreed scope from proposals and open decisions so that assumptions do not silently become requirements.

## 1. Product objective

Shareat aims to make it easy for people to discover restaurants, explore their menus, and keep a personal record of the dishes and restaurants they have tried. At the same time, it gives restaurants a simple way to publish and maintain their digital menus and learn what customers think.

The initial value proposition is:

- For customers: find restaurants and dishes, consult menus, rate experiences, and revisit their own reviews.
- For restaurants: create a profile, publish a menu, and receive structured customer feedback.
- For visitors without an account: browse restaurants and menus with as little friction as possible.

### Expected business result

Validate that customers are willing to review individual dishes—not only restaurants—and that restaurants see enough value in maintaining their menu and consulting feedback to support a future freemium product.

## 2. Users and main use cases

### Visitor

No authentication is required for public browsing.

- View the restaurant list.
- Search or filter restaurants when the feature is available.
- View restaurant information, menus, dishes, and public reviews.
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
- Create, update, publish, disable, and delete menus and dishes.
- Offer more than one menu, such as vegetarian, weekend, or seasonal menus.
- Generate a QR code that opens an active menu.
- Receive notifications and, in a paid tier, consult value-added analytics.
- Request or obtain verification of the restaurant profile.

Restaurants cannot rate dishes or restaurants.

## 3. Scope and product rules

### Included in the initial product direction

- Restaurant and customer accounts.
- Restaurant profiles, menus, and dishes.
- Restaurant and dish reviews.
- Restaurant discovery and public menu browsing.
- A personal review history.
- Monetisation experiments around access to reviews or restaurant analytics.

### Explicitly outside the MVP

- Social feed.
- Follow and unfollow relationships.
- Instagram-like activity experience.
- Public collaborative or Spotify-like lists.
- Advanced map search.
- Multiple menus per restaurant unless capacity allows after the core flow works.

### Rules to validate

- **Authentication:** browsing public content does not require an account. Creating reviews, favourites, personal content, or restaurant content does.
- **Review authorship:** only customer accounts can create ratings and reviews.
- **Review targets:** a review targets either one restaurant or one dish.
- **Personal entries:** a customer may record a restaurant that is not on Shareat. The team must decide whether this entry is private, can become public, and how duplicates are merged.
- **Menu visibility:** only enabled menus and dishes are visible to visitors.
- **Ratings:** the rating scale, editing policy, aggregation rules, and whether one review per user and target is enforced remain open.
- **Allergens:** the source of allergen information and responsibility for its accuracy must be defined before the filter is released.

## 4. MVP prioritisation

### Must

- Restaurant registration and sign-in.
- Customer registration and sign-in.
- Restaurant list.
- Restaurant detail with its profile, menu, and dishes.
- A restaurant can create and publish one menu.
- A customer can rate and optionally review a restaurant.
- A customer can rate and optionally review a dish.
- A customer can see their own reviews.
- A first monetisation experiment.

> **Open decision:** RevenueCat supports in-app purchases and subscriptions, not ad delivery. Decide whether the experiment is an ad placement using a separate ad provider, a RevenueCat-managed paywall/subscription, or rewarded access with both systems.

### Should

- Restaurant verification.
- Customer email verification.
- Freemium restaurant tier with useful analytics.
- Allergen filters.
- Filters for favourites, eaten dishes, and rated content.
- Restaurant types or categories.
- Personal entry for a restaurant not yet on Shareat.
- QR menu.

### Could

- Restaurant search on a map.
- Public personal lists for restaurants and dishes.
- Multiple menus per restaurant.
- Push notifications.
- Deep links and native sharing for a dish, restaurant, list, or profile.

### Not now

- Social feed.
- Follow/unfollow.
- Followers and following lists.
- Instagram-like feed based on followed users and menu changes.

## 5. Screens and critical flows

### MVP screens

| Area | Screen | Main purpose |
| --- | --- | --- |
| Access | Welcome / sign-in / registration | Enter as customer or restaurant; allow public browsing |
| Discovery | Home | Show the restaurant list and entry point to search/filtering |
| Restaurant | Restaurant detail | Show profile, rating summary, menus, dishes, and reviews |
| Restaurant | Menu detail | Show active menu information and its dishes |
| Customer | Customer profile | View and edit customer information |
| Customer | Review history | See reviews created by the signed-in customer |
| Review | Restaurant review form | Add or edit a restaurant rating and comment |
| Review | Dish review form | Add or edit a dish rating and comment |
| Restaurant admin | Restaurant profile | View and edit restaurant information |
| Restaurant admin | My menu | See menu status and manage its dishes |
| Restaurant admin | Create/edit menu | Create or update the menu and publish it |
| Restaurant admin | Create/edit dish | Manage dish data within the menu |

### Critical customer flow

1. Open the app without signing in.
2. Browse the restaurant list.
3. Open a restaurant, one of its menus, and a dish.
4. Choose to rate the restaurant or dish.
5. Register or sign in if necessary.
6. Submit the rating and optional comment.
7. See the new entry in review history.

### Critical restaurant flow

1. Register or sign in as a restaurant.
2. Complete the restaurant profile.
3. Create a menu.
4. Add at least one dish.
5. Publish the menu.
6. Confirm that a visitor can view it from the restaurant detail.

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
- A restaurant owns its profile, menus, and dishes.
- A published menu contains one or more dishes.
- Draft or disabled content is not shown publicly.

### Reviews

- A customer selects a rating and may add a comment.
- Reviews store author, target, timestamps, and moderation status.
- The restaurant and dish views show the agreed rating aggregate.
- A customer can view their own reviews even if a target is later disabled, subject to retention policy.

### Personal organisation

- Favourites, eaten status, personal lists, and private restaurants are separate concepts and should not be represented by a single generic flag.
- These features are post-core unless explicitly promoted into the MVP.

### Notifications and sharing

Potential notification events include a new restaurant nearby, a new restaurant review, a new follower, or a relevant menu change. Each event requires consent, frequency controls, and a deep-link destination. The exact MVP event set is not yet agreed.

## 7. Preliminary data model

This is a product model, not a final database schema.

- **Account:** identity, role, status, verification state.
- **CustomerProfile:** display name, avatar, preferences, privacy settings.
- **Restaurant:** owner account, name, description, address/location, category, verification state, publication state.
- **Menu:** restaurant, name, description, schedule/type, enabled state, publication timestamps.
- **Dish:** menu, name, description, price, image, allergen data, enabled state.
- **Review:** customer, target type, target ID, rating, comment, moderation state, timestamps.
- **Favourite:** customer and restaurant/dish target.
- **DishHistory:** customer, dish, eaten state and date.
- **PersonalRestaurant:** customer-owned external restaurant reference and visibility.
- **List / ListItem:** owner, visibility, ordered restaurant/dish entries; post-MVP.
- **Follow:** follower and followed customer; not now.
- **Notification:** recipient, event type, payload, delivery/read state; post-core.

## 8. Non-functional requirements

The targets below must be made measurable during technical refinement.

- **Security:** secure session/token storage, server-side authorisation, rate limiting, input validation, secrets outside source control, and protection against account enumeration.
- **Privacy:** GDPR-compliant consent, privacy notice, data export/deletion, retention rules, and minimum necessary personal data.
- **Accessibility:** semantic labels, scalable text, sufficient contrast, keyboard/focus support where applicable, and no flow that relies on colour alone.
- **Performance:** define budgets for initial load, restaurant list, menu detail, image weight, and slow-network behaviour.
- **Reliability:** graceful error handling, retry policy, idempotent writes, backups, and a recovery plan.
- **Compatibility:** decide the supported Android/iOS versions and browsers; the current Android minimum is API 24.
- **Localisation:** decide launch language or languages; do not hard-code user-facing copy.
- **Observability:** structured logs, crash reporting, API health monitoring, and alerts without sensitive data.
- **Moderation:** reporting, review visibility, abuse handling, and an audit trail before public comments scale.

## 9. Data and integrations

### Expected data

- Account and profile data.
- Restaurant location and business information.
- Menus, dishes, prices, images, and allergen information.
- Ratings, comments, favourites, and personal history.
- Consent, verification, moderation, and audit state.
- Product analytics events using pseudonymous identifiers where possible.

### Integrations to decide

- Authentication and email verification provider.
- Backend/API and database.
- Image/file storage and transformation.
- Maps, places, geocoding, and nearby search.
- Push notifications.
- Deep links.
- Transactional email.
- Analytics, crash reporting, and monitoring.
- RevenueCat for subscriptions or in-app purchases if that monetisation model is selected.
- An ad network if advertising is selected.

## 10. Current stack and architecture

### Verified in the repository

- Kotlin Multiplatform project.
- Shared Compose Multiplatform UI/code in `shared`.
- Android application.
- iOS application entry point and shared framework integration.
- Web applications targeting JavaScript and WebAssembly.
- Gradle version catalogue and Kotlin tests available in `commonTest`.

### Architecture decisions still open

- Which platforms are included in the MVP; repository support does not automatically mean simultaneous launch.
- Backend technology, API style, database, storage, and hosting.
- Domain/data/presentation boundaries in shared code.
- Navigation, dependency injection, networking, serialisation, persistence, and image loading libraries.
- Authentication/session model and role-based authorisation.
- Environment strategy for local, development, staging, and production.
- CI/CD, signing, store distribution, feature flags, and secrets management.
- Observability and analytics providers.

An Architecture Decision Record should document each material choice and its trade-offs before implementation depends on it.

## 11. Acceptance criteria for the MVP

The MVP is functionally complete when all of the following are demonstrable in a staging-like environment:

1. A visitor can browse the restaurant list and a published menu without signing in.
2. A new customer can register, sign in, sign out, and recover access.
3. A new restaurant can register, sign in, and manage only its own content.
4. A restaurant can create a menu with a dish and publish it.
5. Published restaurant, menu, and dish data is visible to visitors; drafts are not.
6. A customer can submit a valid restaurant rating and a valid dish rating.
7. A restaurant account cannot create a review, including through the API.
8. The customer can see the reviews they authored.
9. Invalid, duplicate, unauthorised, offline, loading, and empty states have agreed behaviour.
10. The chosen monetisation experiment works in its supported environment and does not block essential account recovery or privacy actions.
11. Critical flows pass automated and manual acceptance tests on the supported platforms.
12. Production monitoring, rollback, privacy documentation, and support ownership are ready.

## 12. Delivery plan

### Phase 0 — Product and technical definition

- Resolve the open decisions in this document.
- Produce low-fidelity critical flows and a small design system.
- Define the API/data model and choose the backend and identity strategy.
- Set up environments, CI, quality checks, analytics taxonomy, and Definition of Done.

### Phase 1 — Vertical foundation

- Implement identity and roles.
- Deliver a thin end-to-end restaurant list/detail using the real backend.
- Establish navigation, state handling, observability, and deployment.

### Phase 2 — Restaurant publishing

- Restaurant profile.
- One menu and dish CRUD.
- Draft/enabled/published states.

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

### Proposed workflow to validate

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
- Restaurant activation: first menu with at least one dish published.
- Percentage of registered restaurants with an active menu.
- Review completion rate and median time to submit.
- Weekly active customers and restaurants.
- Menu/detail load success and latency.
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
| RevenueCat mistaken for an ad provider | Blocked monetisation implementation | Decide the business model and integration before estimating it |
| Location and push permissions reduce trust | Lower opt-in | Ask contextually, explain value, and support denial gracefully |
| Architecture chosen before platform scope | Unnecessary complexity | Decide launch platforms and vertical slice first |

## 16. Launch and post-MVP

### Launch readiness

- Pilot/beta cohort and acceptance owner identified.
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

## 17. Decisions required before refinement

The kickoff should assign an owner and deadline to each item:

- [ ] Confirm the one-sentence objective and primary launch user.
- [ ] Choose MVP platforms: Android, iOS, web, or a subset.
- [ ] Confirm that public browsing is anonymous and define which actions require authentication.
- [ ] Define rating scale, review editing/deletion, uniqueness, aggregate, and moderation rules.
- [ ] Define what “subir un menú” includes: images/PDF, structured dishes, or both.
- [ ] Decide whether comments are optional and whether public reviews appear in the MVP.
- [ ] Decide how external/personal restaurants work and how they become official listings.
- [ ] Select backend, database, authentication, storage, hosting, and environments.
- [ ] Resolve monetisation: ads, subscription/paywall, rewarded access, or a staged experiment.
- [ ] Choose the launch geography, language, and seeded restaurant cohort.
- [ ] Define supported devices/OS/browser versions and measurable performance targets.
- [ ] Define privacy, moderation, allergen liability, retention, export, and account deletion policies.
- [ ] Agree analytics events, success targets, and guardrail metrics.
- [ ] Assign product, design, technical, QA/acceptance, moderation, and launch owners.
- [ ] Agree Definition of Ready, Definition of Done, branching/PR policy, and release cadence.
- [ ] Set the next deliverable: validated critical-flow wireframes plus a technical architecture proposal.
