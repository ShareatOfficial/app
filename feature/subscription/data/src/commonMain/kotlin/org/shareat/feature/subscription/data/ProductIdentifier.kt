package org.shareat.feature.subscription.data

/**
 * Returns the app-level product id from a RevenueCat store product identifier.
 *
 * Google Play base plans use `<subscription-id>:<base-plan-id>`, while Apple,
 * the RevenueCat Test Store, and Google Play one-time products use a single id.
 */
internal fun String.toSubscriptionProductId(): String = substringAfterLast(':')
