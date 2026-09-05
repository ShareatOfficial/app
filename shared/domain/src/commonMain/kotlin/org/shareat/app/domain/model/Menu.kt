package org.shareat.app.domain.model

enum class MenuPublicationState {
    Draft,
    Published,
    Unpublished,
    Disabled,
}

data class Menu(
    val id: MenuId,
    val restaurantId: RestaurantId,
    val name: String,
    val description: String? = null,
    val publicationState: MenuPublicationState,
) {
    init { require(name.isNotBlank()) }
}
