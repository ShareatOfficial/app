package org.shareat.feature.restauranthome.domain.model

import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.MenuDetails

data class OwnerDishUpdate(
    val dish: Dish,
    val menu: MenuDetails,
)
