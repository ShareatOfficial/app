package org.shareat.feature.subscription.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.shareat.feature.subscription.domain.CustomerInfoState
import org.shareat.feature.subscription.domain.CustomerSubscriptionInfo
import org.shareat.feature.subscription.domain.SubscriptionError
import org.shareat.feature.subscription.domain.SubscriptionPackage
import org.shareat.feature.subscription.domain.SubscriptionProduct
import org.shareat.feature.subscription.domain.SubscriptionRepository
import org.shareat.feature.subscription.domain.SubscriptionResult

internal class UnsupportedSubscriptionRepository(
    private val reason: String = "Subscriptions are available only in the Android and iOS apps.",
) : SubscriptionRepository {
    override val customerInfo: StateFlow<CustomerInfoState> =
        MutableStateFlow(CustomerInfoState.Unsupported(reason))

    override suspend fun refreshCustomerInfo() = unsupported<CustomerSubscriptionInfo>()
    override suspend fun getCurrentOffering() = unsupported<List<SubscriptionPackage>>()
    override suspend fun purchase(product: SubscriptionProduct) =
        unsupported<CustomerSubscriptionInfo>()
    override suspend fun restorePurchases() = unsupported<CustomerSubscriptionInfo>()
    override suspend fun identify(appUserId: String) = unsupported<CustomerSubscriptionInfo>()
    override suspend fun logOut() = unsupported<CustomerSubscriptionInfo>()

    private fun <T> unsupported(): SubscriptionResult<T> = SubscriptionResult.Failure(
        SubscriptionError(message = reason, code = "UNSUPPORTED_PLATFORM"),
    )
}
