# RevenueCat subscriptions

Shareat uses RevenueCat Purchases KMP `3.7.0` for Android and iOS. The implementation keeps
RevenueCat types inside `:feature:subscription:data`; the rest of the app consumes provider-neutral
models from `:feature:subscription:domain`. The paywall and Customer Center live in
`:feature:subscription:ui` and are available from Settings → Shareat Unlimited.

The web targets compile with an explicit unsupported implementation because RevenueCat's KMP SDK
supports Android and Apple targets, not Kotlin/JS or Kotlin/Wasm.

## 1. Gradle setup

The version catalog declares both required artifacts:

```toml
[versions]
purchases-kmp = "3.7.0"

[libraries]
purchases-kmp-core = { module = "com.revenuecat.purchases:purchases-kmp-core", version.ref = "purchases-kmp" }
purchases-kmp-ui = { module = "com.revenuecat.purchases:purchases-kmp-ui", version.ref = "purchases-kmp" }
```

`purchases-kmp-core` is consumed by the mobile source set in `:feature:subscription:data` and
`purchases-kmp-ui` by the mobile source set in `:feature:subscription:ui`. Both iOS source sets opt
in to `kotlinx.cinterop.ExperimentalForeignApi`, and the resulting `Shared` framework is already
static. Android's `MainActivity` uses `singleTop`, which prevents a purchase from being cancelled
when a banking app temporarily backgrounds Shareat.

## 2. API keys and environments

Debug builds use the supplied Test Store key by default. Android tasks whose name contains
`release` and Xcode builds whose `CONFIGURATION` is `Release` select the production environment.
You can override the detected environment with `-Pshareat.environment=development` or
`-Pshareat.environment=production`.

Production builds require platform-specific public SDK keys. Supply them without editing tracked
source code:

```properties
# ~/.gradle/gradle.properties or an untracked local gradle.properties
shareat.revenuecat.production.androidApiKey=goog_your_android_public_sdk_key
shareat.revenuecat.production.iosApiKey=appl_your_ios_public_sdk_key
```

The previous `shareat.revenuecat.androidApiKey` and `shareat.revenuecat.iosApiKey` properties remain
supported as fallback overrides. Environment-specific properties take precedence.

The keys are public SDK keys, not RevenueCat secret REST keys. Never place a secret key in the app.
A production build fails during Gradle configuration when its platform key does not start with
`goog_` on Android or `appl_` on iOS, so the Test Store key cannot be shipped by accident. Android
and iOS release builds only require their own platform key.

Initialization happens once through Koin when the native app starts:

```kotlin
Purchases.configure(apiKey = revenueCatApiKey) {
    verificationMode = EntitlementVerificationMode.INFORMATIONAL
}
Purchases.sharedInstance.delegate = repository
```

Informational trusted-entitlement verification is enabled so verification results are available
without denying legitimate access if verification cannot complete.

## 3. RevenueCat dashboard configuration

Create the products in each store first, then import them in RevenueCat → Product catalog →
Products. For the Test Store, create them directly in RevenueCat.

| Product ID | Type | RevenueCat package |
| --- | --- | --- |
| `lifetime` | Non-consumable, one-time purchase | Lifetime (`$rc_lifetime`) |
| `yearly` | Auto-renewing annual subscription | Annual (`$rc_annual`) |
| `monthly` | Auto-renewing monthly subscription | Monthly (`$rc_monthly`) |

Then:

1. Create the entitlement `shareat_unlimited`.
2. Attach all three products to that entitlement on every configured store.
3. Create an Offering with identifier `default`.
4. Add the three packages shown above and attach each platform's equivalent product.
5. Mark `default` as the current Offering.
6. In Paywalls, create and publish a paywall for that Offering. Include purchase, restore, terms,
   privacy, and a dismiss action.

The app rejects an incomplete Offering with a useful `PRODUCTS_MISSING` error rather than silently
showing only part of the catalog.

## 4. Customer info and entitlement checks

The repository fetches fresh `CustomerInfo`, listens for RevenueCat delegate updates, and maps it to
`CustomerSubscriptionInfo`. Access is granted only from the entitlement, never from a local product
flag:

```kotlin
val entitlement = customerInfo.entitlements["shareat_unlimited"]
val hasUnlimitedAccess = entitlement?.isActive == true
```

Use `SubscriptionRepository.customerInfo` anywhere premium UI must react to refunds, expiry,
renewal, or purchases made through the paywall. Before gating an irreversible/server-side action,
also verify the entitlement in a trusted backend; the client state is appropriate for UI gating but
must not be the sole authorization control.

## 5. Offerings and direct purchases

The Settings screen fetches the current Offering and displays store-localized titles and prices.
Direct purchase calls use the RevenueCat Package (preserving its presented-offering context):

```kotlin
when (val result = subscriptionRepository.purchase(SubscriptionProduct.Monthly)) {
    is SubscriptionResult.Success -> {
        if (result.value.hasUnlimitedAccess) unlockUnlimitedUi()
    }
    is SubscriptionResult.Failure -> {
        if (!result.error.isUserCancellation) showError(result.error.message)
    }
}
```

Errors from `PurchasesException` retain the RevenueCat code and underlying message. Coroutine
cancellation is rethrown, while a user-cancelled store sheet is treated as a normal outcome and is
not shown as an error. Purchase success always uses the returned `CustomerInfo` instead of assuming
that payment implies entitlement activation.

## 6. Paywall

`RevenueCatPaywall` wraps the server-driven `Paywall` composable with a dismiss button and listeners
for purchase, restore, and errors:

```kotlin
val options = PaywallOptions(dismissRequest = onDismiss) {
    shouldDisplayDismissButton = true
    listener = paywallListener
}
Paywall(options)
```

After purchase or restore, the ViewModel explicitly refreshes customer info. Paywall content and
experiments can therefore change in RevenueCat without an app release.

## 7. Customer Center

Customer Center is shown only after the customer has an entitlement or purchase history, when
self-service management is relevant. Configure its support email, cancellation paths, feedback,
and promotional offers in RevenueCat. It requires a RevenueCat Pro or Enterprise plan. Customers
without purchase history see Restore Purchases instead of a management entry point.

## 8. Customer identity (privacy decision required)

The shipped implementation uses RevenueCat anonymous App User IDs persisted by the SDK. This avoids
sending Shareat account identifiers to a third party, but subscriptions will not automatically
follow a Shareat login onto another device.

If product and privacy owners approve cross-device identity, call
`SubscriptionRepository.identify()` with a stable, non-email, non-guessable identifier after login,
and `logOut()` after account logout. Update the privacy disclosure and deletion/export workflows at
the same time. Never use email addresses, advertising identifiers, or a hard-coded shared ID.

## 9. Release checklist

- Inject `shareat.revenuecat.production.androidApiKey` and
  `shareat.revenuecat.production.iosApiKey` through CI.
- Test success, cancellation, failure, restore, refund, expiry, billing issue, and offline launch.
- Verify `shareat_unlimited` for all three products in sandbox customer records.
- Configure Apple/Google server notifications so refunds and renewals propagate quickly.

## 10. Manual Android release

The `Android Release` GitHub Action accepts `version_code` (the integer Google Play requires to
increase for every upload), `version_name` (the user-visible version), and a destination:

- `artifact` builds a signed APK and AAB and keeps them as downloadable GitHub artifacts.
- `internal-sharing` also creates a temporary Google Play Internal App Sharing link.
- `internal` also publishes the AAB to the Google Play internal testing track.

The workflow reads the signing material and production integrations from repository secrets; never
commit the upload keystore, passwords, service-account JSON, or production RevenueCat key.
- Confirm localized price, renewal terms, privacy policy, and terms links in the paywall.
- Add App Privacy / Google Data Safety disclosures for RevenueCat.
- Test Customer Center cancellation and support paths if the RevenueCat plan enables it.

References: [KMP installation](https://www.revenuecat.com/docs/getting-started/installation/kotlin-multiplatform),
[customer info](https://www.revenuecat.com/docs/customers/customer-info),
[product configuration](https://www.revenuecat.com/docs/projects/configuring-products),
[paywalls](https://www.revenuecat.com/docs/tools/paywalls), and
[Customer Center](https://www.revenuecat.com/docs/tools/customer-center/customer-center-kmp).

Tracking: [#79](https://github.com/ShareatOfficial/app/issues/79).
