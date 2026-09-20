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

internal const val RestaurantHomePreviewImagePrefix = "preview://restaurant-home/"

private fun previewImageUrl(fileName: String): String = "$RestaurantHomePreviewImagePrefix$fileName"

internal object RestaurantHomePreviewData {
    private fun previewDish(
        id: String,
        name: String,
        description: String,
        imagePath: String,
        imageDescription: String,
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
        imageUrl = previewImageUrl(imagePath),
        imageDescription = imageDescription,
        priceMinorUnits = priceMinorUnits,
        ratingLabel = ratingLabel,
        reviewCount = reviewCount,
        allergens = allergens,
        category = category,
        isEnabled = isEnabled,
        isMenuItemEnabled = isEnabled,
    )

    private val restaurant = RestaurantHomeData(
        id = "baratie",
        name = "Baratie",
        description = "Alta cocina en alta mar: pescados frescos, recetas de todos los mares y ningún cliente se marcha con hambre.",
        imageUrl = previewImageUrl("baratie_hero.png"),
        imageDescription = "El restaurante flotante Baratie navegando al atardecer.",
        address = RestaurantAddressUiState("Mar del Este, Muelle 3", "Sambas", "00003", null, "OP"),
        ratingLabel = "4,9",
        reviewCount = 986,
        publicationState = RestaurantPublicationState.Published,
        categories = DishCategory.entries.toList(),
        allergens = listOf(
            EuAllergen.CerealsContainingGluten,
            EuAllergen.Crustaceans,
            EuAllergen.Fish,
            EuAllergen.Molluscs,
        ),
        dishes = listOf(
            previewDish(
                id = "bisque-all-blue",
                name = "Bisque del All Blue",
                description = "Crema intensa de crustáceos, azafrán, tomate y pan tostado.",
                imagePath = "baratie_bouillabaisse.png",
                imageDescription = "Bisque marinera de crustáceos servida en un cuenco de latón.",
                priceMinorUnits = 1250,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.Crustaceans, EuAllergen.Fish, EuAllergen.Molluscs),
                ratingLabel = "4,9",
                reviewCount = 164,
            ),
            previewDish(
                id = "pulpo-east-blue",
                name = "Pulpo del East Blue",
                description = "Pulpo a la brasa, patata baby, pimentón ahumado y aceite de perejil.",
                imagePath = "baratie_grilled_octopus.png",
                imageDescription = "Pulpo a la brasa con patatas y aceite de pimentón.",
                priceMinorUnits = 1490,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.Molluscs),
                ratingLabel = "4,8",
                reviewCount = 97,
            ),
            previewDish(
                id = "mejillones-grand-line",
                name = "Mejillones Grand Line",
                description = "Mejillones al vapor con tomate, ajo, guindilla y hierbas de cubierta.",
                imagePath = "baratie_bouillabaisse.png",
                imageDescription = "Cazuela marinera de mejillones en salsa de tomate.",
                priceMinorUnits = 1090,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.Molluscs, EuAllergen.SulphurDioxideAndSulphites),
                ratingLabel = "4,7",
                reviewCount = 73,
            ),
            previewDish(
                id = "ensalada-navegante",
                name = "Ensalada del navegante",
                description = "Hojas tiernas, cítricos, hinojo, algas y vinagreta de mostaza.",
                imagePath = "baratie_seafood_fried_rice.png",
                imageDescription = "Ensalada fresca de cítricos y hierbas servida junto al mar.",
                priceMinorUnits = 850,
                category = DishCategory.Starters,
                allergens = setOf(EuAllergen.Mustard),
            ),
            previewDish(
                id = "arroz-all-blue",
                name = "Arroz frito del All Blue",
                description = "Arroz al wok con gambas, calamar, mejillones, huevo y cebolleta.",
                imagePath = "baratie_seafood_fried_rice.png",
                imageDescription = "Arroz frito con gambas, calamar y mejillones.",
                priceMinorUnits = 1890,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.Crustaceans, EuAllergen.Eggs, EuAllergen.Molluscs, EuAllergen.Soybeans),
                ratingLabel = "5,0",
                reviewCount = 241,
            ),
            previewDish(
                id = "atun-pimienta",
                name = "Atún a la pimienta de Sanji",
                description = "Lomo de atún marcado, mantequilla de hierbas y patatas asadas.",
                imagePath = "baratie_tuna_steak.png",
                imageDescription = "Atún a la pimienta con mantequilla de hierbas y patatas.",
                priceMinorUnits = 2390,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.Fish, EuAllergen.Milk),
                ratingLabel = "4,9",
                reviewCount = 218,
            ),
            previewDish(
                id = "bouillabaisse-zeff",
                name = "Bouillabaisse del chef Zeff",
                description = "Pescado blanco, gambas y mejillones en caldo de tomate y azafrán.",
                imagePath = "baratie_bouillabaisse.png",
                imageDescription = "Bouillabaisse de pescado, gambas y mejillones con pan tostado.",
                priceMinorUnits = 2190,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Crustaceans, EuAllergen.Fish, EuAllergen.Molluscs),
                ratingLabel = "4,9",
                reviewCount = 189,
            ),
            previewDish(
                id = "bacalao-crujiente",
                name = "Bacalao crujiente Baratie",
                description = "Bacalao dorado, patatas gajo, limón y salsa tártara de la casa.",
                imagePath = "baratie_fish_and_chips.png",
                imageDescription = "Bacalao crujiente con patatas, limón y salsa tártara.",
                priceMinorUnits = 1790,
                category = DishCategory.MainCourses,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Fish, EuAllergen.Mustard),
                ratingLabel = "4,8",
                reviewCount = 126,
            ),
            previewDish(
                id = "ola-grand-line",
                name = "Ola de la Grand Line",
                description = "Panna cotta de vainilla, gel de cítricos azules, frutos rojos y vela de caramelo.",
                imagePath = "baratie_ocean_panna_cotta.png",
                imageDescription = "Panna cotta con una ola azul de cítricos y frutos rojos.",
                priceMinorUnits = 890,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.Milk),
                ratingLabel = "4,9",
                reviewCount = 152,
            ),
            previewDish(
                id = "tarta-mandarina",
                name = "Tarta de mandarina de Cocoyashi",
                description = "Crema de mandarina, bizcocho de almendra y merengue tostado.",
                imagePath = "baratie_ocean_panna_cotta.png",
                imageDescription = "Postre cítrico con frutos rojos y caramelo crujiente.",
                priceMinorUnits = 820,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Milk, EuAllergen.Nuts),
                ratingLabel = "4,8",
                reviewCount = 88,
            ),
            previewDish(
                id = "flan-brujula",
                name = "Flan brújula",
                description = "Flan de vainilla de Madagascar, caramelo salado y galleta de cacao.",
                imagePath = "baratie_ocean_panna_cotta.png",
                imageDescription = "Flan de vainilla decorado como una ola marina.",
                priceMinorUnits = 690,
                category = DishCategory.Desserts,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Milk),
            ),
            previewDish(
                id = "sorbete-lima",
                name = "Sorbete Calm Belt",
                description = "Lima, hierbabuena y un toque de jengibre para refrescar la travesía.",
                imagePath = "baratie_ocean_panna_cotta.png",
                imageDescription = "Sorbete cítrico azul con frutas frescas.",
                priceMinorUnits = 620,
                category = DishCategory.Desserts,
            ),
            previewDish(
                id = "bocados-pescado",
                name = "Bocados de pescado crujiente",
                description = "Dados de pescado rebozado con limón y mayonesa de hierbas.",
                imagePath = "baratie_fish_and_chips.png",
                imageDescription = "Pescado dorado y crujiente con salsa de hierbas.",
                priceMinorUnits = 790,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Fish),
            ),
            previewDish(
                id = "patatas-cubierta",
                name = "Patatas de cubierta",
                description = "Patatas especiadas, salsa tártara y sal marina.",
                imagePath = "baratie_fish_and_chips.png",
                imageDescription = "Patatas doradas y especiadas con salsa de hierbas.",
                priceMinorUnits = 590,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.Eggs, EuAllergen.Mustard),
            ),
            previewDish(
                id = "brochetas-pulpo",
                name = "Brochetas de pulpo",
                description = "Pulpo braseado, patata y pimentón en formato para compartir.",
                imagePath = "baratie_grilled_octopus.png",
                imageDescription = "Bocados de pulpo braseado con patatas pequeñas.",
                priceMinorUnits = 990,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.Molluscs),
                ratingLabel = "4,7",
                reviewCount = 64,
            ),
            previewDish(
                id = "mini-arroz-marino",
                name = "Cuenco de arroz marino",
                description = "Versión pequeña de nuestro arroz frito con marisco del día.",
                imagePath = "baratie_seafood_fried_rice.png",
                imageDescription = "Cuenco pequeño de arroz frito con marisco.",
                priceMinorUnits = 750,
                category = DishCategory.SmallBites,
                allergens = setOf(EuAllergen.Crustaceans, EuAllergen.Eggs, EuAllergen.Molluscs, EuAllergen.Soybeans),
            ),
            previewDish(
                id = "captura-del-dia",
                name = "Captura del día",
                description = "El mejor pescado llegado esta mañana, preparado según decida la cocina.",
                imagePath = "baratie_tuna_steak.png",
                imageDescription = "Pescado fresco del día servido con patatas y mantequilla de hierbas.",
                priceMinorUnits = 2490,
                category = null,
            ),
            previewDish(
                id = "menu-emergencia",
                name = "Menú de emergencia",
                description = "Una receta secreta reservada para navegantes que llegan con el estómago vacío.",
                imagePath = "baratie_bouillabaisse.png",
                imageDescription = "Guiso marinero especial de la cocina del Baratie.",
                priceMinorUnits = 1990,
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
                price = "12,50",
                allergens = restaurant.dishes.first().allergens,
                imageUrl = restaurant.dishes.first().imageUrl,
                isPublished = true,
            ),
        ),
    )
}
