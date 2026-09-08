package org.shareat.feature.subscription.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
internal expect fun RevenueCatPaywall(
    onDismiss: () -> Unit,
    onPurchaseOrRestoreCompleted: () -> Unit,
    onError: (String) -> Unit,
)

@Composable
internal expect fun RevenueCatCustomerCenter(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
)
