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

enum class EuAllergen {
    Celery,
    CerealsContainingGluten,
    Crustaceans,
    Eggs,
    Fish,
    Lupin,
    Milk,
    Molluscs,
    Mustard,
    Nuts,
    Peanuts,
    Sesame,
    Soybeans,
    SulphurDioxideAndSulphites,
}

enum class AllergenInformationSource {
    Restaurant,
}

data class AllergenDeclaration(
    val allergens: Set<EuAllergen>,
    val note: String? = null,
    val source: AllergenInformationSource = AllergenInformationSource.Restaurant,
) {
    init { require(note == null || note.isNotBlank()) }
}

data class Dish(
    val id: DishId,
    val restaurantId: RestaurantId,
    val name: String,
    val description: String? = null,
    val image: ImageRef? = null,
    val allergenDeclaration: AllergenDeclaration? = null,
    val isEnabled: Boolean,
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
)
