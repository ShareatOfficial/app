package org.shareat.feature.restauranthome.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.shareat.shared.navigation.RequiresLogin

/** Private top-level landing for an active restaurant owner. */
@Serializable
data object RestaurantHomeKey : NavKey, RequiresLogin
