package org.shareat.feature.subscription.domain

import kotlinx.coroutines.flow.StateFlow

interface SubscriptionRepository {
    val customerInfo: StateFlow<CustomerInfoState>

    suspend fun refreshCustomerInfo(): SubscriptionResult<CustomerSubscriptionInfo>
    suspend fun getCurrentOffering(): SubscriptionResult<List<SubscriptionPackage>>
    suspend fun purchase(product: SubscriptionProduct): SubscriptionResult<CustomerSubscriptionInfo>
    suspend fun restorePurchases(): SubscriptionResult<CustomerSubscriptionInfo>
    suspend fun identify(appUserId: String): SubscriptionResult<CustomerSubscriptionInfo>
    suspend fun logOut(): SubscriptionResult<CustomerSubscriptionInfo>
}
