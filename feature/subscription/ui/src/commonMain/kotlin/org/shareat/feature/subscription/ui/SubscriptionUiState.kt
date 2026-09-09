package org.shareat.feature.subscription.ui

import org.shareat.feature.subscription.domain.CustomerSubscriptionInfo
import org.shareat.feature.subscription.domain.SubscriptionPackage

data class SubscriptionUiState(
    val isLoading: Boolean = true,
    val isProcessing: Boolean = false,
    val customerInfo: CustomerSubscriptionInfo? = null,
    val packages: List<SubscriptionPackage> = emptyList(),
    val errorMessage: String? = null,
    val showPaywall: Boolean = false,
    val showCustomerCenter: Boolean = false,
) {
    val canManageSubscription: Boolean
        get() = customerInfo?.let {
            it.hasUnlimitedAccess || it.purchasedProductIds.isNotEmpty()
        } == true
}
