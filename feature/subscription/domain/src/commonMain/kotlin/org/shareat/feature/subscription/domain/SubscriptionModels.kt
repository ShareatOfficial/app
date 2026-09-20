package org.shareat.feature.subscription.domain

object SubscriptionConfiguration {
    const val ENTITLEMENT_ID = "shareat_unlimited"
    const val OFFERING_ID = "default"
}

enum class SubscriptionProduct(val productId: String) {
    Monthly("monthly"),
    Quarterly("quarterly"),
    Lifetime("lifetime"),
}

data class SubscriptionPackage(
    val identifier: String,
    val product: SubscriptionProduct,
    val title: String,
    val description: String?,
    val localizedPrice: String,
)

data class CustomerSubscriptionInfo(
    val appUserId: String,
    val hasUnlimitedAccess: Boolean,
    val activeProductId: String?,
    val activeSubscriptions: Set<String>,
    val purchasedProductIds: Set<String>,
    val willRenew: Boolean,
    val expirationDateMillis: Long?,
    val managementUrl: String?,
)

sealed interface CustomerInfoState {
    data object Loading : CustomerInfoState
    data class Available(val value: CustomerSubscriptionInfo) : CustomerInfoState
    data class Failed(val error: SubscriptionError) : CustomerInfoState
    data class Unsupported(val reason: String) : CustomerInfoState
}

data class SubscriptionError(
    val message: String,
    val code: String? = null,
    val isUserCancellation: Boolean = false,
)

sealed interface SubscriptionResult<out T> {
    data class Success<T>(val value: T) : SubscriptionResult<T>
    data class Failure(val error: SubscriptionError) : SubscriptionResult<Nothing>
}
