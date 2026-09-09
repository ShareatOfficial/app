package org.shareat.app.domain.model

/** Owner-editable representation of the restaurant's single menu. */
data class RestaurantMenuDraft(
    val restaurantId: RestaurantId,
    val menuId: MenuId? = null,
    val name: String,
    val description: String? = null,
    val publicationState: MenuPublicationState = MenuPublicationState.Draft,
    val items: List<MenuItemDraft>,
) {
    init {
        require(name.isNotBlank())
        require(items.map(MenuItemDraft::dishId).distinct().size == items.size)
    }
}

data class MenuItemDraft(
    val dishId: DishId,
    val price: Money,
    val position: Int,
    val isEnabled: Boolean = true,
) {
    init { require(position >= 0) }
}

/** Owner-editable dish fields. A null [id] creates a new catalog dish. */
data class DishDraft(
    val restaurantId: RestaurantId,
    val id: DishId? = null,
    val name: String,
    val description: String? = null,
    val allergenDeclaration: AllergenDeclaration? = null,
    val isEnabled: Boolean = true,
) {
    init { require(name.isNotBlank()) }
}
