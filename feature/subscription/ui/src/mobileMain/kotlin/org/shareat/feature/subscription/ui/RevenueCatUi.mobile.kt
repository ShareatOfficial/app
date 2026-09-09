package org.shareat.feature.subscription.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.revenuecat.purchases.kmp.models.CustomerInfo
import com.revenuecat.purchases.kmp.models.PurchasesError
import com.revenuecat.purchases.kmp.models.StoreTransaction
import com.revenuecat.purchases.kmp.ui.revenuecatui.CustomerCenter
import com.revenuecat.purchases.kmp.ui.revenuecatui.Paywall
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallListener
import com.revenuecat.purchases.kmp.ui.revenuecatui.PaywallOptions

@Composable
internal actual fun RevenueCatPaywall(
    onDismiss: () -> Unit,
    onPurchaseOrRestoreCompleted: () -> Unit,
    onError: (String) -> Unit,
) {
    val options = remember(onDismiss, onPurchaseOrRestoreCompleted, onError) {
        PaywallOptions(dismissRequest = onDismiss) {
            shouldDisplayDismissButton = true
            listener = object : PaywallListener {
                override fun onPurchaseCompleted(
                    customerInfo: CustomerInfo,
                    storeTransaction: StoreTransaction,
                ) = onPurchaseOrRestoreCompleted()

                override fun onRestoreCompleted(customerInfo: CustomerInfo) =
                    onPurchaseOrRestoreCompleted()

                override fun onPurchaseError(error: PurchasesError) =
                    onError(error.underlyingErrorMessage ?: error.message)

                override fun onRestoreError(error: PurchasesError) =
                    onError(error.underlyingErrorMessage ?: error.message)
            }
        }
    }
    Paywall(options)
}

@Composable
internal actual fun RevenueCatCustomerCenter(
    modifier: Modifier,
    onDismiss: () -> Unit,
) {
    CustomerCenter(modifier = modifier, onDismiss = onDismiss)
}
