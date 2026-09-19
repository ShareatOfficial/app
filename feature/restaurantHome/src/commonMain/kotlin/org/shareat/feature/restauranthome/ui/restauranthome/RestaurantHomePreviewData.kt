package org.shareat.feature.restauranthome.ui.restauranthome

import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.feature.restauranthome.ui.model.RestaurantAddressUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeEditor
import org.shareat.feature.restauranthome.ui.model.RestaurantEditFormUiState
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState

internal object RestaurantHomePreviewData {
    private fun previewDish(
        id: String,
        name: String,
        description: String,
        priceMinorUnits: Long,
        category: DishCategory?,
        allergens: Set<EuAllergen> = emptySet(),
        ratingLabel: String? = null,
        reviewCount: Int = 0,
        isEnabled: Boolean = true,
    ) = RestaurantDish(
        id = id,
        name = name,
        description = description,
        imageUrl = null,
        imageDescription = null,
        priceMinorUnits = priceMinorUnits,
        ratingLabel = ratingLabel,
        reviewCount = reviewCount,
        allergens = allergens,
        category = category,
        isEnabled = isEnabled,
        isMenuItemEnabled = isEnabled,
    )

    private val restaurant = RestaurantHomeData(
        id = "casa-naranja",
        name = "Casa Naranja",
        description = "Cocina de temporada, producto local y platos pensados para compartir.",
        imageUrl = null,
        imageDescription = null,
        address = RestaurantAddressUiState("Calle del Olmo, 18", "Madrid", "28012", null, "ES"),
        ratingLabel = "4,8",
        reviewCount = 128,
        publicationState = RestaurantPublicationState.Published,
        categories = DishCategory.entries.toList(),
        allergens = listOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk, EuAllergen.Nuts),
        dishes = listOf(
            previewDish(
                id = "croquetas-jamon",
                name = "Croquetas de jamón",
                description = "Cremosas, con jamón ibérico y rebozado crujiente.",
                priceMinorUnits = 950,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk),
                ratingLabel = "4,8",
                reviewCount = 41,
            ),
            previewDish(
                id = "ensalada-tomate",
                name = "Ensalada de tomate",
                description = "Tomate de temporada, cebolleta y aceite de oliva virgen extra.",
                priceMinorUnits = 890,
                category = DishCategory.Starters,
                ratingLabel = "4,6",
                reviewCount = 18,
            ),
            previewDish(
                id = "burrata-pesto",
                name = "Burrata al pesto",
                description = "Burrata, pesto de albahaca y tomates asados.",
                priceMinorUnits = 1190,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.Milk, EuAllergen.Nuts),
                ratingLabel = "4,9",
                reviewCount = 27,
            ),
            previewDish(
                id = "gazpacho-cereza",
                name = "Gazpacho de cereza",
                description = "Gazpacho suave con cereza, pepino y albahaca.",
                priceMinorUnits = 750,
                category = DishCategory.Starters,
            ),
            previewDish(
                id = "pizza-naranja",
                name = "Pizza Naranja",
                description = "Mozzarella, calabaza asada y romero.",
                priceMinorUnits = 1450,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk),
                ratingLabel = "4,7",
                reviewCount = 32,
            ),
            previewDish(
                id = "arroz-setas",
                name = "Arroz meloso de setas",
                description = "Arroz bomba, setas de temporada y parmesano.",
                priceMinorUnits = 1690,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.Milk),
                ratingLabel = "4,8",
                reviewCount = 22,
            ),
            previewDish(
                id = "pollo-limon",
                name = "Pollo asado al limón",
                description = "Pollo de corral, patatas y salsa de limón confitado.",
                priceMinorUnits = 1590,
                category = DishCategory.MainCourses,
                ratingLabel = "4,5",
                reviewCount = 15,
            ),
            previewDish(
                id = "bacalao-confitado",
                name = "Bacalao confitado",
                description = "Bacalao, crema de coliflor y aceite de pimentón.",
                priceMinorUnits = 1890,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.Fish),
            ),
            previewDish(
                id = "tarta-almendra",
                name = "Tarta de almendra",
                description = "Con naranja amarga y crema ligera.",
                priceMinorUnits = 750,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.Nuts),
                ratingLabel = "4,7",
                reviewCount = 12,
            ),
            previewDish(
                id = "torrija-brioche",
                name = "Torrija de brioche",
                description = "Brioche caramelizado, crema inglesa y canela.",
                priceMinorUnits = 790,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Milk),
                ratingLabel = "4,9",
                reviewCount = 36,
            ),
            previewDish(
                id = "helado-pistacho",
                name = "Helado de pistacho",
                description = "Helado artesano de pistacho tostado.",
                priceMinorUnits = 650,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.Milk, EuAllergen.Nuts),
            ),
            previewDish(
                id = "flan-vainilla",
                name = "Flan de vainilla",
                description = "Flan casero de vainilla con caramelo salado.",
                priceMinorUnits = 620,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.Eggs, EuAllergen.Milk),
            ),
            previewDish(
                id = "pan-masa-madre",
                name = "Pan de masa madre",
                description = "Pan tostado con aceite de oliva y sal en escamas.",
                priceMinorUnits = 390,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.CerealsContainingGluten),
            ),
            previewDish(
                id = "aceitunas-alinadas",
                name = "Aceitunas aliñadas",
                description = "Aceitunas verdes con cítricos y hierbas frescas.",
                priceMinorUnits = 420,
                category = DishCategory.SmallBites,
            ),
            previewDish(
                id = "patatas-bravas",
                name = "Patatas bravas",
                description = "Patata crujiente, salsa brava y alioli suave.",
                priceMinorUnits = 690,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.Eggs),
                ratingLabel = "4,6",
                reviewCount = 29,
            ),
            previewDish(
                id = "tabla-quesos",
                name = "Tabla de quesos",
                description = "Selección de quesos artesanos con compota de temporada.",
                priceMinorUnits = 1250,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.Milk),
            ),
            previewDish(
                id = "plato-del-dia",
                name = "Plato del día",
                description = "Nuestra propuesta diaria elaborada con producto de mercado.",
                priceMinorUnits = 1350,
                category = null,
            ),
            previewDish(
                id = "fuera-carta",
                name = "Fuera de carta",
                description = "Una elaboración especial disponible por tiempo limitado.",
                priceMinorUnits = 1490,
                category = null,
                isEnabled = false,
            ),
        ),
    )

    val loaded = RestaurantHomeUiState(content = RestaurantHomeContent.Loaded(restaurant))
    val preview = loaded.copy(visonMode = RestaurantHomeMode.CUSTOMER_PREVIEW)
    val error = RestaurantHomeUiState(content = RestaurantHomeContent.Error(RestaurantHomeError.OFFLINE))
    val empty = RestaurantHomeUiState(content = RestaurantHomeContent.Empty)
    val restaurantEditor = loaded.copy(
        editor = RestaurantHomeEditor.Restaurant(
            RestaurantEditFormUiState(
                name = restaurant.name,
                streetLine = restaurant.address.streetLine,
                locality = restaurant.address.locality,
                postalCode = restaurant.address.postalCode,
                region = restaurant.address.region.orEmpty(),
                description = restaurant.description.orEmpty(),
                imageUrl = restaurant.imageUrl,
            ),
        ),
    )
    val newDishEditor = loaded.copy(
        editor = RestaurantHomeEditor.Dish(
            DishEditFormUiState(
                name = "",
                description = "",
                price = "",
                allergens = emptySet(),
                imageUrl = null,
                isPublished = false,
            ),
        ),
    )
    val editDishEditor = loaded.copy(
        editor = RestaurantHomeEditor.Dish(
            DishEditFormUiState(
                dishId = restaurant.dishes.first().id,
                name = restaurant.dishes.first().name,
                description = restaurant.dishes.first().description.orEmpty(),
                price = "9,50",
                allergens = restaurant.dishes.first().allergens,
                imageUrl = restaurant.dishes.first().imageUrl,
                isPublished = true,
            ),
        ),
    )
}
