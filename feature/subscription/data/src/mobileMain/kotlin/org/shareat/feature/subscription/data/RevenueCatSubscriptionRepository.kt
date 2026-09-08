package org.shareat.feature.subscription.data

import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesDelegate
import com.revenuecat.purchases.kmp.configure
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import com.revenuecat.purchases.kmp.ktx.awaitLogIn
import com.revenuecat.purchases.kmp.ktx.awaitLogOut
import com.revenuecat.purchases.kmp.ktx.awaitOfferings
import com.revenuecat.purchases.kmp.ktx.awaitPurchase
import com.revenuecat.purchases.kmp.ktx.awaitRestore
import com.revenuecat.purchases.kmp.models.CacheFetchPolicy
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.EntitlementVerificationMode
import com.revenuecat.purchases.kmp.models.Package
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.PurchasesException
import com.revenuecat.purchases.kmp.models.PurchasesTransactionException
import com.revenuecat.purchases.kmp.models.StoreProduct
import com.revenuecat.purchases.kmp.models.StoreTransaction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.shareat.feature.subscription.domain.CustomerInfoState
import org.shareat.feature.subscription.domain.CustomerSubscriptionInfo
import org.shareat.feature.subscription.domain.SubscriptionConfiguration
import org.shareat.feature.subscription.domain.SubscriptionError
import org.shareat.feature.subscription.domain.SubscriptionPackage
import org.shareat.feature.subscription.domain.SubscriptionProduct
import org.shareat.feature.subscription.domain.SubscriptionRepository
import org.shareat.feature.subscription.domain.SubscriptionResult

internal class RevenueCatSubscriptionRepository : SubscriptionRepository, PurchasesDelegate {
    private val purchases: Purchases
    private val _customerInfo = MutableStateFlow<CustomerInfoState>(CustomerInfoState.Loading)
    private var packagesByProduct: Map<SubscriptionProduct, Package> = emptyMap()

    override val customerInfo: StateFlow<CustomerInfoState> = _customerInfo.asStateFlow()

    init {
        if (!Purchases.isConfigured) {
            Purchases.configure(apiKey = revenueCatApiKey) {
                verificationMode = EntitlementVerificationMode.INFORMATIONAL
            }
        }
        purchases = Purchases.sharedInstance
        purchases.delegate = this
    }

    override suspend fun refreshCustomerInfo(): SubscriptionResult<CustomerSubscriptionInfo> =
        customerOperation {
            purchases.awaitCustomerInfo(CacheFetchPolicy.FETCH_CURRENT)
        }

    override suspend fun getCurrentOffering(): SubscriptionResult<List<SubscriptionPackage>> =
        try {
            val offerings = purchases.awaitOfferings()
            val offering = offerings.current
                ?: offerings[SubscriptionConfiguration.OFFERING_ID]
                ?: return failure(
                    message = "RevenueCat has no current offering. Configure and publish the default offering.",
                    code = "OFFERING_NOT_FOUND",
                )

            val mapped = offering.availablePackages.mapNotNull { rcPackage ->
                rcPackage.toDomain()?.also { domainPackage ->
                    packagesByProduct = packagesByProduct + (domainPackage.product to rcPackage)
                }
            }.sortedBy { it.product.ordinal }

            val missing = SubscriptionProduct.entries - mapped.map { it.product }.toSet()
            if (missing.isNotEmpty()) {
                return failure(
                    message = "The current RevenueCat offering is missing: " +
                        missing.joinToString { it.productId },
                    code = "PRODUCTS_MISSING",
                )
            }
            SubscriptionResult.Success(mapped)
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (error: PurchasesException) {
            error.toFailure()
        } catch (error: Throwable) {
            unexpectedFailure(error)
        }

    override suspend fun purchase(
        product: SubscriptionProduct,
    ): SubscriptionResult<CustomerSubscriptionInfo> {
        if (packagesByProduct[product] == null) {
            when (val offerings = getCurrentOffering()) {
                is SubscriptionResult.Failure -> return offerings
                is SubscriptionResult.Success -> Unit
            }
        }
        val rcPackage = packagesByProduct[product] ?: return failure(
            message = "${product.productId} is not attached to the current RevenueCat offering.",
            code = "PACKAGE_NOT_FOUND",
        )
        return customerOperation { purchases.awaitPurchase(rcPackage).customerInfo }
    }

    override suspend fun restorePurchases(): SubscriptionResult<CustomerSubscriptionInfo> =
        customerOperation { purchases.awaitRestore() }

    override suspend fun identify(appUserId: String): SubscriptionResult<CustomerSubscriptionInfo> {
        if (purchases.appUserID == appUserId) return refreshCustomerInfo()
        return customerOperation { purchases.awaitLogIn(appUserId).customerInfo }
    }

    override suspend fun logOut(): SubscriptionResult<CustomerSubscriptionInfo> {
        if (purchases.isAnonymous) return refreshCustomerInfo()
        return customerOperation { purchases.awaitLogOut() }
    }

    override fun onCustomerInfoUpdated(customerInfo: CustomerInfo) {
        _customerInfo.value = CustomerInfoState.Available(customerInfo.toDomain())
    }

    override fun onPurchasePromoProduct(
        product: StoreProduct,
        startPurchase: (
            onError: (error: PurchasesError, userCancelled: Boolean) -> Unit,
            onSuccess: (storeTransaction: StoreTransaction, customerInfo: CustomerInfo) -> Unit,
        ) -> Unit,
    ) {
        startPurchase(
            { error, userCancelled ->
                if (!userCancelled) _customerInfo.value = CustomerInfoState.Failed(error.toDomain())
            },
            { _, customerInfo -> onCustomerInfoUpdated(customerInfo) },
        )
    }

    private suspend fun customerOperation(
        operation: suspend () -> CustomerInfo,
    ): SubscriptionResult<CustomerSubscriptionInfo> = try {
        val customerInfo = operation().toDomain()
        _customerInfo.value = CustomerInfoState.Available(customerInfo)
        SubscriptionResult.Success(customerInfo)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (error: PurchasesTransactionException) {
        val failure = error.toDomain()
        if (!failure.isUserCancellation) _customerInfo.value = CustomerInfoState.Failed(failure)
        SubscriptionResult.Failure(failure)
    } catch (error: PurchasesException) {
        val failure = error.toDomain()
        _customerInfo.value = CustomerInfoState.Failed(failure)
        SubscriptionResult.Failure(failure)
    } catch (error: Throwable) {
        val failure = SubscriptionError(
            message = error.message ?: "An unexpected subscription error occurred.",
            code = "UNEXPECTED",
        )
        _customerInfo.value = CustomerInfoState.Failed(failure)
        SubscriptionResult.Failure(failure)
    }
}

private fun CustomerInfo.toDomain(): CustomerSubscriptionInfo {
    val entitlement = entitlements[SubscriptionConfiguration.ENTITLEMENT_ID]
    return CustomerSubscriptionInfo(
        appUserId = originalAppUserId,
        hasUnlimitedAccess = entitlement?.isActive == true,
        activeProductId = entitlement?.productIdentifier,
        activeSubscriptions = activeSubscriptions,
        purchasedProductIds = allPurchasedProductIdentifiers,
        willRenew = entitlement?.willRenew == true,
        expirationDateMillis = entitlement?.expirationDateMillis,
        managementUrl = managementUrlString,
    )
}

private fun Package.toDomain(): SubscriptionPackage? {
    val normalizedProductId = storeProduct.id.substringBefore(':')
    val product = SubscriptionProduct.entries.firstOrNull { candidate ->
        candidate.productId == normalizedProductId || candidate.productId == identifier
    } ?: return null
    return SubscriptionPackage(
        identifier = identifier,
        product = product,
        title = storeProduct.title,
        description = storeProduct.localizedDescription,
        localizedPrice = storeProduct.price.formatted,
    )
}

private fun PurchasesException.toDomain(): SubscriptionError = SubscriptionError(
    message = underlyingErrorMessage ?: message,
    code = code.name,
    isUserCancellation = (this as? PurchasesTransactionException)?.userCancelled == true,
)

private fun PurchasesError.toDomain(): SubscriptionError = SubscriptionError(
    message = underlyingErrorMessage ?: message,
    code = code.name,
)

private fun PurchasesException.toFailure(): SubscriptionResult.Failure =
    SubscriptionResult.Failure(toDomain())

private fun <T> failure(message: String, code: String): SubscriptionResult<T> =
    SubscriptionResult.Failure(SubscriptionError(message = message, code = code))

private fun <T> unexpectedFailure(error: Throwable): SubscriptionResult<T> =
    SubscriptionResult.Failure(
        SubscriptionError(
            message = error.message ?: "An unexpected subscription error occurred.",
            code = "UNEXPECTED",
        ),
    )
