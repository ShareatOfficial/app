package org.shareat.feature.restaurant.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.shareat.feature.restaurant.ui.model.RestaurantArgs

@Serializable
data class RestaurantKey(val restaurant: RestaurantArgs) : NavKey
