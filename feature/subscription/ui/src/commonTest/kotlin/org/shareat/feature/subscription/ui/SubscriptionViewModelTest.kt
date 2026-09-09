package org.shareat.feature.subscription.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.feature.subscription.domain.CustomerInfoState
import org.shareat.feature.subscription.domain.CustomerSubscriptionInfo
import org.shareat.feature.subscription.domain.SubscriptionError
import org.shareat.feature.subscription.domain.SubscriptionPackage
import org.shareat.feature.subscription.domain.SubscriptionProduct
import org.shareat.feature.subscription.domain.SubscriptionRepository
import org.shareat.feature.subscription.domain.SubscriptionResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class SubscriptionViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsCustomerInfoAndAllConfiguredProducts() = runTest(dispatcher) {
        val repository = FakeSubscriptionRepository()
        val viewModel = SubscriptionViewModel(repository)

        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.customerInfo?.hasUnlimitedAccess == true)
        assertEquals(SubscriptionProduct.entries.size, viewModel.uiState.value.packages.size)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun purchaseCancellationIsNotShownAsAnError() = runTest(dispatcher) {
        val repository = FakeSubscriptionRepository(
            purchaseResult = SubscriptionResult.Failure(
                SubscriptionError("Purchase cancelled", isUserCancellation = true),
            ),
        )
        val viewModel = SubscriptionViewModel(repository)
        advanceUntilIdle()

        viewModel.purchase(SubscriptionProduct.Monthly)
        advanceUntilIdle()

        assertEquals(null, viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isProcessing)
    }
}

private class FakeSubscriptionRepository(
    private val purchaseResult: SubscriptionResult<CustomerSubscriptionInfo> =
        SubscriptionResult.Success(customerInfo()),
) : SubscriptionRepository {
    override val customerInfo = MutableStateFlow<CustomerInfoState>(CustomerInfoState.Loading)

    override suspend fun refreshCustomerInfo() = SubscriptionResult.Success(customerInfo())

    override suspend fun getCurrentOffering() = SubscriptionResult.Success(
        SubscriptionProduct.entries.map { product ->
            SubscriptionPackage(
                identifier = product.productId,
                product = product,
                title = product.name,
                description = null,
                localizedPrice = "€9.99",
            )
        },
    )

    override suspend fun purchase(product: SubscriptionProduct) = purchaseResult
    override suspend fun restorePurchases() = SubscriptionResult.Success(customerInfo())
    override suspend fun identify(appUserId: String) = SubscriptionResult.Success(customerInfo())
    override suspend fun logOut() = SubscriptionResult.Success(customerInfo())
}

private fun customerInfo() = CustomerSubscriptionInfo(
    appUserId = "anonymous",
    hasUnlimitedAccess = true,
    activeProductId = SubscriptionProduct.Monthly.productId,
    activeSubscriptions = setOf(SubscriptionProduct.Monthly.productId),
    purchasedProductIds = setOf(SubscriptionProduct.Monthly.productId),
    willRenew = true,
    expirationDateMillis = null,
    managementUrl = null,
)
