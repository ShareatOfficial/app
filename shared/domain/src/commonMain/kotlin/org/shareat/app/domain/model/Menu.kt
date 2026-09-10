package org.shareat.app.domain.model

enum class MenuPublicationState {
    Draft,
    Published,
    Unpublished,
    Disabled,
}

/** [price] is the fixed price of a set menu; an a la carte menu leaves it null and prices each dish. */
data class Menu(
    val id: MenuId,
    val restaurantId: RestaurantId,
    val name: String,
    val description: String? = null,
    val publicationState: MenuPublicationState,
    val price: Money? = null,
) {
    init { require(name.isNotBlank()) }
}

/** Join entity for the many-to-many relationship between menus and dishes. */
data class MenuItem(
    val menuId: MenuId,
    val dishId: DishId,
    val price: Money,
    val position: Int,
    val isEnabled: Boolean,
    val category: DishCategory? = null,
) {
    init { require(position >= 0) }
}

data class MenuDetails(
    val menu: Menu,
    val items: List<MenuDish>,
)

data class MenuDish(
    val dish: Dish,
    val price: Money,
    val position: Int,
    val isEnabled: Boolean = true,
    val category: DishCategory? = null,
)
