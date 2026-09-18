package org.shareat.feature.subscription.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.AllInclusive
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.feature.subscription.domain.SubscriptionPackage
import org.shareat.shared.designsystem.layout.safeDrawingTopPadding

@Composable
fun SubscriptionScreen(
    modifier: Modifier = Modifier,
    navigator: SubscriptionNavigation = koinInject(),
    viewModel: SubscriptionViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

    when {
        state.showPaywall -> RevenueCatPaywall(
            onDismiss = viewModel::dismissPaywall,
            onPurchaseOrRestoreCompleted = viewModel::onRevenueCatUiCompleted,
            onError = viewModel::onRevenueCatUiError,
        )
        state.showCustomerCenter -> RevenueCatCustomerCenter(
            modifier = Modifier.fillMaxSize(),
            onDismiss = viewModel::dismissCustomerCenter,
        )
        else -> SubscriptionContent(
            state = state,
            modifier = modifier,
            onBack = navigator::goBack,
            onRetry = viewModel::retry,
            onShowPaywall = viewModel::showPaywall,
            onPurchase = { viewModel.purchase(it.product) },
            onRestore = viewModel::restorePurchases,
            onManage = viewModel::showCustomerCenter,
        )
    }
}

@Composable
private fun SubscriptionContent(
    state: SubscriptionUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onShowPaywall: () -> Unit,
    onPurchase: (SubscriptionPackage) -> Unit,
    onRestore: () -> Unit,
    onManage: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingTopPadding()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Go back")
                }
                Text(
                    "Shareat Unlimited",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AccessStatusCard(state)

                if (state.isLoading) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                state.errorMessage?.let { message ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                    ) {
                        Column(Modifier.fillMaxWidth().padding(16.dp)) {
                            Text(message, color = MaterialTheme.colorScheme.onErrorContainer)
                            TextButton(onClick = onRetry) { Text("Try again") }
                        }
                    }
                }

                if (!state.customerInfo?.hasUnlimitedAccess.orFalse()) {
                    Button(
                        onClick = onShowPaywall,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isProcessing,
                    ) {
                        Icon(Icons.Outlined.AllInclusive, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("View subscription options")
                    }
                }

                state.packages.forEach { subscriptionPackage ->
                    ProductCard(
                        subscriptionPackage = subscriptionPackage,
                        enabled = !state.isProcessing,
                        onPurchase = { onPurchase(subscriptionPackage) },
                    )
                }

                if (state.isProcessing) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CircularProgressIndicator(Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Updating subscription…")
                    }
                }

                if (state.canManageSubscription) {
                    OutlinedButton(
                        onClick = onManage,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !state.isProcessing,
                    ) {
                        Icon(Icons.Outlined.ManageAccounts, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Manage subscription")
                    }
                }

                TextButton(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isProcessing,
                ) {
                    Text("Restore purchases")
                }
            }
        }
    }
}

@Composable
private fun AccessStatusCard(state: SubscriptionUiState) {
    val hasAccess = state.customerInfo?.hasUnlimitedAccess == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hasAccess) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceContainerLow
            },
        ),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                if (hasAccess) Icons.Outlined.CheckCircle else Icons.Outlined.AllInclusive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Column {
                Text(
                    if (hasAccess) "Unlimited access active" else "Unlock Shareat Unlimited",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                state.customerInfo?.activeProductId?.let { productId ->
                    Text(
                        text = if (state.customerInfo.willRenew) {
                            "$productId · renews automatically"
                        } else {
                            productId
                        },
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductCard(
    subscriptionPackage: SubscriptionPackage,
    enabled: Boolean,
    onPurchase: () -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(subscriptionPackage.title, style = MaterialTheme.typography.titleMedium)
                subscriptionPackage.description?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    subscriptionPackage.localizedPrice,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Button(onClick = onPurchase, enabled = enabled) { Text("Choose") }
        }
    }
}

private fun Boolean?.orFalse(): Boolean = this == true
