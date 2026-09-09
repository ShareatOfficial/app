package org.shareat.app.data.fake

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuItem
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.WeeklyOpeningHours

/** How many restaurants [mockShareatData] and [mockCatalogue] build unless told otherwise. */
const val DefaultMockRestaurantCount: Int = 5

/**
 * Mock data ready to hand to any repository: [restaurantCount] published restaurants, each with its
 * published menu, dishes and reviews.
 *
 * ```kotlin
 * val data = mockShareatData()                       // 5 restaurants
 * val restaurants = FakeRestaurantRepository(data)
 * val menus = FakeMenuRepository(data)               // same instance: writes are visible to reads
 * ```
 *
 * The first restaurant is always the fully detailed anchor (a published carta of eight dishes plus a
 * draft menu). The rest deliberately cover the states a screen has to survive: unrated, no published
 * menu, dishes with and without photos, dishes with and without an allergen declaration.
 */
fun mockShareatData(restaurantCount: Int = DefaultMockRestaurantCount): FakeShareatData =
    mockShareatDataWithAnchor(restaurantCount)

/**
 * The mocked entities on their own, for when you want to build the fixtures into something other
 * than a [FakeShareatData] — a preview, an assertion, or a repository of your own.
 */
fun mockCatalogue(
    restaurantCount: Int = DefaultMockRestaurantCount,
    openingHours: WeeklyOpeningHours = mockOpeningHours(),
): MockCatalogue = catalogueFixtures(openingHours, (restaurantCount - 1).coerceIn(0, catalogue.size))

data class MockCatalogue(
    val accounts: List<Account>,
    val restaurants: List<Restaurant>,
    val menus: List<Menu>,
    val dishes: List<Dish>,
    val menuItems: List<MenuItem>,
    val reviews: List<Review>,
)

internal fun catalogueFixtures(
    openingHours: WeeklyOpeningHours,
    entries: Int = catalogue.size,
): MockCatalogue {
    val selected = catalogue.take(entries)
    val reviewerAccounts = reviewers.map { reviewer ->
        Account(
            id = reviewer.accountId,
            loginEmail = EmailAddress("${reviewer.slug}@example.com"),
            role = AccountRole.Customer,
            status = AccountStatus.Active,
        )
    }
    val ownerAccounts = selected.map { entry ->
        Account(
            id = entry.ownerAccountId,
            loginEmail = EmailAddress("owner@${entry.slug}.example"),
            role = AccountRole.Restaurant,
            status = AccountStatus.Active,
        )
    }

    return MockCatalogue(
        accounts = reviewerAccounts + ownerAccounts,
        restaurants = selected.map { it.toRestaurant(openingHours) },
        menus = selected.flatMap(CatalogueEntry::menus),
        dishes = selected.flatMap(CatalogueEntry::dishes),
        menuItems = selected.flatMap(CatalogueEntry::menuItems),
        reviews = selected.flatMap(CatalogueEntry::reviews),
    )
}

private data class Reviewer(val slug: String, val comment: String) {
    val accountId = AccountId("account-customer-$slug")
}

private val reviewers = listOf(
    Reviewer("lucia", "Volveremos seguro, el trato fue inmejorable."),
    Reviewer("marc", "Buena relación calidad-precio y raciones generosas."),
    Reviewer("nerea", "El local es acogedor y se come muy bien."),
)

private data class DishTemplate(
    val slug: String,
    val name: String,
    val description: String,
    val category: DishCategory,
    val allergens: Set<EuAllergen>?,
    val priceMinorUnits: Long,
    val hasImage: Boolean = false,
    val highlight: String? = null,
)

private val dishTemplates = listOf(
    DishTemplate(
        slug = "croquetas",
        name = "Croquetas caseras",
        description = "Bechamel suave y rebozado fino, hechas cada mañana.",
        category = DishCategory.Starters,
        allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk, EuAllergen.Eggs),
        priceMinorUnits = 1_100,
        hasImage = true,
        highlight = "Las mejores croquetas que he probado este año.",
    ),
    DishTemplate(
        slug = "tomate",
        name = "Ensalada de tomate de temporada",
        description = "Tomate de huerta, cebolleta y aceite de oliva virgen extra.",
        category = DishCategory.Starters,
        allergens = null,
        priceMinorUnits = 950,
    ),
    DishTemplate(
        slug = "boquerones",
        name = "Boquerones en vinagre",
        description = "Marinados en casa y servidos con pan de cristal.",
        category = DishCategory.Starters,
        allergens = setOf(EuAllergen.Fish),
        priceMinorUnits = 1_050,
    ),
    DishTemplate(
        slug = "arroz",
        name = "Arroz del senyoret",
        description = "Arroz seco de marisco pelado y caldo de pescado de roca.",
        category = DishCategory.MainCourses,
        allergens = setOf(EuAllergen.Crustaceans, EuAllergen.Fish, EuAllergen.Molluscs),
        priceMinorUnits = 2_100,
        hasImage = true,
        highlight = "El sofrito se nota, arroz en su punto exacto.",
    ),
    DishTemplate(
        slug = "carrilleras",
        name = "Carrilleras al vino tinto",
        description = "Cocinadas a baja temperatura con parmentier de patata.",
        category = DishCategory.MainCourses,
        allergens = setOf(EuAllergen.SulphurDioxideAndSulphites, EuAllergen.Milk),
        priceMinorUnits = 1_950,
    ),
    DishTemplate(
        slug = "berenjena",
        name = "Berenjena asada con miso",
        description = "Berenjena a la brasa, miso de soja y sésamo tostado.",
        category = DishCategory.MainCourses,
        allergens = setOf(EuAllergen.Soybeans, EuAllergen.Sesame),
        priceMinorUnits = 1_600,
    ),
    DishTemplate(
        slug = "tarta-queso",
        name = "Tarta de queso al horno",
        description = "Cremosa por dentro y tostada por fuera.",
        category = DishCategory.Desserts,
        allergens = setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Milk, EuAllergen.Eggs),
        priceMinorUnits = 650,
        hasImage = true,
        highlight = "Cremosa de verdad, no la típica cuajada.",
    ),
    DishTemplate(
        slug = "sorbete",
        name = "Sorbete de limón",
        description = "Con ralladura de lima y hierbabuena fresca.",
        category = DishCategory.Desserts,
        allergens = null,
        priceMinorUnits = 500,
    ),
    DishTemplate(
        slug = "bravas",
        name = "Patatas bravas",
        description = "Patata confitada, salsa picante y alioli suave.",
        category = DishCategory.SmallBites,
        allergens = setOf(EuAllergen.Eggs, EuAllergen.Soybeans),
        priceMinorUnits = 850,
    ),
    DishTemplate(
        slug = "quesos",
        name = "Tabla de quesos del norte",
        description = "Cinco quesos con membrillo y pan de nueces.",
        category = DishCategory.SmallBites,
        allergens = setOf(EuAllergen.Milk, EuAllergen.Nuts, EuAllergen.CerealsContainingGluten),
        priceMinorUnits = 1_400,
    ),
)

private data class CatalogueEntry(
    val slug: String,
    val name: String,
    val description: String,
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val ratings: List<Int>,
    val dishSlugs: List<String>,
    val publishesItsMenus: Boolean = true,
) {
    val restaurantId = RestaurantId("restaurant-$slug")
    val ownerAccountId = AccountId("account-restaurant-$slug")

    private val cartaMenuId = MenuId("menu-$slug-carta")

    private val templates: List<DishTemplate>
        get() = dishSlugs.mapNotNull { wanted -> dishTemplates.firstOrNull { it.slug == wanted } }

    private val publicationState: MenuPublicationState
        get() = if (publishesItsMenus) MenuPublicationState.Published else MenuPublicationState.Draft

    fun toRestaurant(openingHours: WeeklyOpeningHours): Restaurant = Restaurant(
        id = restaurantId,
        ownerAccountId = ownerAccountId,
        name = name,
        description = description,
        heroImage = ImageRef(
            url = "https://t3.ftcdn.net/jpg/00/27/57/96/360_F_27579652_tM7V4fZBBw8RLmZo0Bi8WhtO2EosTRFD.jpg",
            alternativeText = "Comedor de $name",
        ),
        publicEmail = EmailAddress("hola@$slug.example"),
        publicPhone = "+34 910 000 0${(slug.length % 9) + 1}",
        address = PostalAddress(
            streetLine = streetLine,
            locality = locality,
            postalCode = postalCode,
            region = locality,
        ),
        openingHours = openingHours,
        publicationState = RestaurantPublicationState.Published,
    )

    fun menus(): List<Menu> = listOf(
        Menu(
            id = cartaMenuId,
            restaurantId = restaurantId,
            name = "Carta",
            description = "Disponible todo el servicio",
            publicationState = publicationState,
        ),
    )

    fun dishes(): List<Dish> = templates.map { template ->
        Dish(
            id = dishId(template),
            restaurantId = restaurantId,
            name = template.name,
            description = template.description,
            image = if (template.hasImage) {
                ImageRef(
                    url = "https://images.example.com/dishes/${template.slug}.jpg",
                    alternativeText = template.name,
                )
            } else {
                null
            },
            allergenDeclaration = template.allergens?.let { allergens ->
                AllergenDeclaration(
                    allergens = allergens,
                    note = "La información ha sido facilitada por el restaurante.",
                )
            },
            isEnabled = true,
        )
    }

    fun menuItems(): List<MenuItem> = templates.mapIndexed { position, template ->
        MenuItem(
            menuId = cartaMenuId,
            dishId = dishId(template),
            price = Money(template.priceMinorUnits),
            position = position,
            isEnabled = true,
            category = template.category,
        )
    }

    fun reviews(): List<Review> = restaurantReviews() + dishReviews()

    private fun restaurantReviews(): List<Review> = ratings.mapIndexed { index, rating ->
        val reviewer = reviewers[index % reviewers.size]
        Review(
            id = ReviewId("review-$slug-restaurant-${reviewer.slug}"),
            authorAccountId = reviewer.accountId,
            target = ReviewTarget.Restaurant(restaurantId),
            rating = Rating(rating),
            comment = reviewer.comment,
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            visitedAt = IsoTimestamp("2026-08-${(index + 10).twoDigits()}T21:00:00Z"),
            createdAt = IsoTimestamp("2026-08-${(index + 11).twoDigits()}T09:15:00Z"),
            updatedAt = IsoTimestamp("2026-08-${(index + 11).twoDigits()}T09:15:00Z"),
        )
    }

    private fun dishReviews(): List<Review> = templates
        .filter { it.highlight != null }
        .mapIndexed { index, template ->
            val reviewer = reviewers[(index + 1) % reviewers.size]
            Review(
                id = ReviewId("review-$slug-${template.slug}-${reviewer.slug}"),
                authorAccountId = reviewer.accountId,
                target = ReviewTarget.Dish(dishId(template)),
                rating = Rating(if (index % 2 == 0) 5 else 4),
                comment = template.highlight,
                visibility = ReviewVisibility.Public,
                moderationStatus = ReviewModerationStatus.Visible,
                createdAt = IsoTimestamp("2026-08-${(index + 12).twoDigits()}T22:30:00Z"),
                updatedAt = IsoTimestamp("2026-08-${(index + 12).twoDigits()}T22:30:00Z"),
            )
        }

    private fun dishId(template: DishTemplate): DishId = DishId("dish-$slug-${template.slug}")
}

private fun Int.twoDigits(): String = toString().padStart(2, '0')

private val catalogue = listOf(
    CatalogueEntry(
        slug = "bodega-del-puerto",
        name = "Bodega del Puerto",
        description = "Producto de lonja y vinos del Penedès frente al mar.",
        streetLine = "Passeig Marítim, 4",
        locality = "Barcelona",
        postalCode = "08003",
        ratings = listOf(5, 4, 5),
        dishSlugs = listOf("boquerones", "arroz", "sorbete", "bravas"),
    ),
    CatalogueEntry(
        slug = "horno-de-san-blas",
        name = "Horno de San Blas",
        description = "Panadería y asador con horno de leña centenario.",
        streetLine = "Calle San Blas, 9",
        locality = "Zaragoza",
        postalCode = "50003",
        ratings = emptyList(),
        dishSlugs = listOf("tomate", "carrilleras", "tarta-queso"),
    ),
    CatalogueEntry(
        slug = "la-salina",
        name = "La Salina",
        description = "Espetos y pescaíto en primera línea de playa.",
        streetLine = "Paseo Marítimo, 44",
        locality = "Málaga",
        postalCode = "29016",
        ratings = listOf(4),
        dishSlugs = listOf("boquerones", "arroz", "bravas"),
        publishesItsMenus = false,
    ),
    CatalogueEntry(
        slug = "marea-baja",
        name = "Marea Baja",
        description = "Arroces a leña y pescado de la Albufera.",
        streetLine = "Carrer de la Mar, 31",
        locality = "València",
        postalCode = "46003",
        ratings = listOf(5, 5, 4, 5),
        dishSlugs = listOf("boquerones", "arroz", "berenjena", "sorbete"),
    ),
    CatalogueEntry(
        slug = "la-taberna-azul",
        name = "La Taberna Azul",
        description = "Tapas clásicas sevillanas en un patio del siglo XIX.",
        streetLine = "Calle Sierpes, 22",
        locality = "Sevilla",
        postalCode = "41004",
        ratings = listOf(4, 4),
        dishSlugs = listOf("croquetas", "tomate", "carrilleras", "tarta-queso", "quesos"),
    ),
    CatalogueEntry(
        slug = "el-rincon-manchego",
        name = "El Rincón Manchego",
        description = "Cocina de cuchara y quesos con denominación de origen.",
        streetLine = "Plaza Zocodover, 7",
        locality = "Toledo",
        postalCode = "45001",
        ratings = listOf(3, 4, 4),
        dishSlugs = listOf("croquetas", "carrilleras", "tarta-queso", "quesos"),
    ),
    CatalogueEntry(
        slug = "casa-petra",
        name = "Casa Petra",
        description = "Pintxos de temporada y sidra natural en el Casco Viejo.",
        streetLine = "Barrenkale, 12",
        locality = "Bilbao",
        postalCode = "48005",
        ratings = listOf(4, 5),
        dishSlugs = listOf("quesos", "bravas", "berenjena", "sorbete"),
    ),
    CatalogueEntry(
        slug = "verde-laurel",
        name = "Verde Laurel",
        description = "Cocina vegetal de mercado junto al Realejo.",
        streetLine = "Cuesta del Realejo, 5",
        locality = "Granada",
        postalCode = "18009",
        ratings = listOf(5, 4, 4),
        dishSlugs = listOf("tomate", "berenjena", "sorbete", "bravas"),
    ),
)
