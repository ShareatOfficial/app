package org.shareat.app.domain.model

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
