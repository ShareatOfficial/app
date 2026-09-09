package org.shareat.feature.subscription.ui

import kotlinx.serialization.Serializable
import org.shareat.shared.navigation.RequiresLogin

@Serializable
data object SubscriptionKey : RequiresLogin
