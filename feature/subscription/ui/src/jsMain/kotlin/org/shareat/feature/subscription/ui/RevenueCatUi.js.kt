package org.shareat.feature.subscription.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal actual fun RevenueCatPaywall(
    onDismiss: () -> Unit,
    onPurchaseOrRestoreCompleted: () -> Unit,
    onError: (String) -> Unit,
) = UnsupportedRevenueCatUi(onDismiss)

@Composable
internal actual fun RevenueCatCustomerCenter(modifier: Modifier, onDismiss: () -> Unit) =
    UnsupportedRevenueCatUi(onDismiss, modifier)
