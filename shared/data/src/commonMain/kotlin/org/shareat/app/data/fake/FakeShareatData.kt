package org.shareat.app.data.fake

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.GeoCoordinates
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuItem
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.OpeningPeriod
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
import org.shareat.app.domain.model.Weekday
import org.shareat.app.domain.model.WeeklyOpeningHours

class FakeShareatData internal constructor(
    internal val accounts: List<Account>,
    customerProfiles: List<CustomerProfile>,
    restaurants: List<Restaurant>,
    menus: List<Menu>,
    dishes: List<Dish>,
    menuItems: List<MenuItem>,
    reviews: List<Review>,
) {
    internal val customerProfiles: MutableList<CustomerProfile> = customerProfiles.toMutableList()
    internal val restaurants: MutableList<Restaurant> = restaurants.toMutableList()
    internal val menus: MutableList<Menu> = menus.toMutableList()
    internal val dishes: MutableList<Dish> = dishes.toMutableList()
    internal val menuItems: MutableList<MenuItem> = menuItems.toMutableList()
    internal val reviews: MutableList<Review> = reviews.toMutableList()

    companion object {
        fun preview(): FakeShareatData = mockShareatData()
        fun empty(): FakeShareatData = FakeShareatData(
            accounts = emptyList(),
            customerProfiles = emptyList(),
            restaurants = emptyList(),
            menus = emptyList(),
            dishes = emptyList(),
            menuItems = emptyList(),
            reviews = emptyList(),
        )
    }
}

object FakeIds {
    val customerAccount = AccountId("account-customer-ana")
    val secondCustomerAccount = AccountId("account-customer-diego")
    val restaurantAccount = AccountId("account-restaurant-casa-naranja")
    val restaurant = RestaurantId("restaurant-casa-naranja")
    val carta = MenuId("menu-casa-naranja")
    val seasonalMenu = MenuId("menu-casa-naranja-temporada")
    val octopus = DishId("dish-charred-octopus")
    val croquettes = DishId("dish-iberian-croquettes")
    val russianSalad = DishId("dish-russian-salad")
    val bravas = DishId("dish-bravas")
    val mushroomRice = DishId("dish-mushroom-rice")
    val seaBass = DishId("dish-sea-bass")
    val sirloin = DishId("dish-sirloin")
    val frenchToast = DishId("dish-french-toast")
}

/** Lunch and dinner every day except Monday — the same schedule for every mocked restaurant. */
fun mockOpeningHours(): WeeklyOpeningHours {
    val lunch = OpeningPeriod(LocalTime(13, 0), LocalTime(16, 0))
    val dinner = OpeningPeriod(LocalTime(20, 0), LocalTime(23, 30))
    return WeeklyOpeningHours(
        days = Weekday.entries.map { day ->
            DailyOpeningHours(
                day = day,
                periods = if (day == Weekday.Monday) emptyList() else listOf(lunch, dinner),
            )
        },
    )
}

internal fun mockShareatDataWithAnchor(restaurantCount: Int): FakeShareatData {
    val accounts = listOf(
        Account(
            id = FakeIds.customerAccount,
            loginEmail = EmailAddress("ana@example.com"),
            role = AccountRole.Customer,
            status = AccountStatus.Active,
        ),
        Account(
            id = FakeIds.secondCustomerAccount,
            loginEmail = EmailAddress("diego@example.com"),
            role = AccountRole.Customer,
            status = AccountStatus.Active,
        ),
        Account(
            id = FakeIds.restaurantAccount,
            loginEmail = EmailAddress("owner@casanaranja.example"),
            role = AccountRole.Restaurant,
            status = AccountStatus.Active,
        ),
    )
    val profiles = listOf(
        CustomerProfile(
            accountId = FakeIds.customerAccount,
            displayName = "Ana",
            avatar = ImageRef("https://images.example.com/users/ana.jpg", "Ana"),
            fullName = "Ana Rivera",
            phoneNumber = "+34 600 000 001",
            preferredLanguage = "es-ES",
        ),
        CustomerProfile(
            accountId = FakeIds.secondCustomerAccount,
            displayName = "Diego",
            fullName = "Diego Martín",
            preferredLanguage = "es-ES",
        ),
    )
    val weeklyHours = mockOpeningHours()
    val restaurants = listOf(
        Restaurant(
            id = FakeIds.restaurant,
            ownerAccountId = FakeIds.restaurantAccount,
            name = "Casa Naranja",
            description = "Cocina española moderna elaborada con producto local.",
            heroImage = ImageRef(
                url = "https://images.example.com/restaurants/casa-naranja.jpg",
                alternativeText = "Interior de Casa Naranja",
            ),
            publicEmail = EmailAddress("hola@casanaranja.example"),
            publicPhone = "+34 910 000 001",
            address = PostalAddress(
                streetLine = "Calle del Olmo, 18",
                locality = "Madrid",
                postalCode = "28015",
                region = "Madrid",
                coordinates = GeoCoordinates(40.4327, -3.7044),
            ),
            openingHours = weeklyHours,
            publicationState = RestaurantPublicationState.Published,
        ),
    )
    val menus = listOf(
        Menu(
            id = FakeIds.carta,
            restaurantId = FakeIds.restaurant,
            name = "Carta",
            description = "Platos pensados para compartir.",
            publicationState = MenuPublicationState.Published,
        ),
        Menu(
            id = FakeIds.seasonalMenu,
            restaurantId = FakeIds.restaurant,
            name = "Menú de temporada",
            description = "Todavía en preparación.",
            publicationState = MenuPublicationState.Draft,
        ),
    )
    val restaurantSuppliedAllergens = "La información ha sido facilitada por el restaurante."
    val dishes = listOf(
        Dish(
            id = FakeIds.octopus,
            restaurantId = FakeIds.restaurant,
            name = "Pulpo a la brasa",
            description = "Pulpo, patata y pimentón ahumado.",
            image = ImageRef(
                url = "https://images.example.com/dishes/charred-octopus.jpg",
                alternativeText = "Pulpo a la brasa con patata",
            ),
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Molluscs),
                note = restaurantSuppliedAllergens,
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.croquettes,
            restaurantId = FakeIds.restaurant,
            name = "Croquetas de jamón ibérico",
            description = "Cremosas y recién hechas.",
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(
                    EuAllergen.CerealsContainingGluten,
                    EuAllergen.Eggs,
                    EuAllergen.Milk,
                ),
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.russianSalad,
            restaurantId = FakeIds.restaurant,
            name = "Ensaladilla de la casa",
            description = "Con ventresca de atún y aceite de oliva virgen extra.",
            image = ImageRef(
                url = "https://images.example.com/dishes/russian-salad.jpg",
                alternativeText = "Ensaladilla con ventresca",
            ),
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Fish, EuAllergen.Eggs),
                note = restaurantSuppliedAllergens,
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.bravas,
            restaurantId = FakeIds.restaurant,
            name = "Bravas de la casa",
            description = "Patata confitada, salsa brava y alioli de soja.",
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Soybeans, EuAllergen.Eggs),
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.mushroomRice,
            restaurantId = FakeIds.restaurant,
            name = "Arroz meloso de setas",
            description = "Arroz bomba, setas de temporada y parmesano curado.",
            image = ImageRef(
                url = "https://images.example.com/dishes/mushroom-rice.jpg",
                alternativeText = "Arroz meloso de setas",
            ),
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Milk),
                note = restaurantSuppliedAllergens,
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.seaBass,
            restaurantId = FakeIds.restaurant,
            name = "Lubina a la bilbaína",
            description = "Lubina salvaje con refrito de ajo y guindilla.",
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Fish),
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.sirloin,
            restaurantId = FakeIds.restaurant,
            name = "Solomillo con salsa de mostaza",
            description = "Solomillo de vaca madurado y patata panadera.",
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(EuAllergen.Mustard, EuAllergen.Milk),
            ),
            isEnabled = true,
        ),
        Dish(
            id = FakeIds.frenchToast,
            restaurantId = FakeIds.restaurant,
            name = "Torrija caramelizada",
            description = "Brioche, leche infusionada y helado de vainilla.",
            image = ImageRef(
                url = "https://images.example.com/dishes/french-toast.jpg",
                alternativeText = "Torrija caramelizada con helado",
            ),
            allergenDeclaration = AllergenDeclaration(
                allergens = setOf(
                    EuAllergen.CerealsContainingGluten,
                    EuAllergen.Eggs,
                    EuAllergen.Milk,
                ),
                note = restaurantSuppliedAllergens,
            ),
            isEnabled = true,
        ),
    )
    val menuItems = listOf(
        MenuItem(FakeIds.carta, FakeIds.octopus, Money(1_800), 0, isEnabled = true, category = DishCategory.Starters),
        MenuItem(FakeIds.carta, FakeIds.croquettes, Money(1_200), 1, isEnabled = true, category = DishCategory.Starters),
        MenuItem(FakeIds.carta, FakeIds.russianSalad, Money(1_400), 2, isEnabled = true, category = DishCategory.Starters),
        MenuItem(FakeIds.carta, FakeIds.mushroomRice, Money(1_900), 3, isEnabled = true, category = DishCategory.MainCourses),
        MenuItem(FakeIds.carta, FakeIds.seaBass, Money(2_600), 4, isEnabled = true, category = DishCategory.MainCourses),
        MenuItem(FakeIds.carta, FakeIds.sirloin, Money(2_800), 5, isEnabled = true, category = DishCategory.MainCourses),
        MenuItem(FakeIds.carta, FakeIds.frenchToast, Money(700), 6, isEnabled = true, category = DishCategory.Desserts),
        MenuItem(FakeIds.carta, FakeIds.bravas, Money(900), 7, isEnabled = true, category = DishCategory.SmallBites),

        MenuItem(FakeIds.seasonalMenu, FakeIds.bravas, Money(900), 0, isEnabled = true, category = DishCategory.SmallBites),
    )
    val reviews = listOf(
        Review(
            id = ReviewId("review-restaurant-ana"),
            authorAccountId = FakeIds.customerAccount,
            target = ReviewTarget.Restaurant(FakeIds.restaurant),
            rating = Rating(5),
            comment = "Un sitio cálido y muy cuidado.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            visitedAt = IsoTimestamp("2026-08-09T20:00:00Z"),
            createdAt = IsoTimestamp("2026-08-10T08:30:00Z"),
            updatedAt = IsoTimestamp("2026-08-10T08:30:00Z"),
        ),
        Review(
            id = ReviewId("review-restaurant-diego"),
            authorAccountId = FakeIds.secondCustomerAccount,
            target = ReviewTarget.Restaurant(FakeIds.restaurant),
            rating = Rating(4),
            comment = "Buen producto y servicio atento.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            visitedAt = IsoTimestamp("2026-08-07T13:00:00Z"),
            createdAt = IsoTimestamp("2026-08-08T10:15:00Z"),
            updatedAt = IsoTimestamp("2026-08-08T10:15:00Z"),
        ),
        Review(
            id = ReviewId("review-croquettes-diego"),
            authorAccountId = FakeIds.secondCustomerAccount,
            target = ReviewTarget.Dish(FakeIds.croquettes),
            rating = Rating(4),
            comment = "Cremosas por dentro y muy crujientes.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            createdAt = IsoTimestamp("2026-08-08T10:20:00Z"),
            updatedAt = IsoTimestamp("2026-08-08T10:20:00Z"),
        ),
        Review(
            id = ReviewId("review-sea-bass-ana"),
            authorAccountId = FakeIds.customerAccount,
            target = ReviewTarget.Dish(FakeIds.seaBass),
            rating = Rating(4),
            comment = "El refrito estaba en su punto.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            createdAt = IsoTimestamp("2026-08-11T21:05:00Z"),
            updatedAt = IsoTimestamp("2026-08-11T21:05:00Z"),
        ),
        Review(
            id = ReviewId("review-french-toast-diego"),
            authorAccountId = FakeIds.secondCustomerAccount,
            target = ReviewTarget.Dish(FakeIds.frenchToast),
            rating = Rating(5),
            comment = "El mejor postre de la carta.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            createdAt = IsoTimestamp("2026-08-09T22:40:00Z"),
            updatedAt = IsoTimestamp("2026-08-09T22:40:00Z"),
        ),
        Review(
            id = ReviewId("review-octopus-ana"),
            authorAccountId = FakeIds.customerAccount,
            target = ReviewTarget.Dish(FakeIds.octopus),
            rating = Rating(5),
            comment = "Tiernísimo y con el punto justo de humo.",
            visibility = ReviewVisibility.Public,
            moderationStatus = ReviewModerationStatus.Visible,
            visitedAt = IsoTimestamp("2026-08-09T20:00:00Z"),
            createdAt = IsoTimestamp("2026-08-10T08:35:00Z"),
            updatedAt = IsoTimestamp("2026-08-10T08:35:00Z"),
        ),
    )
    val catalogue = catalogueFixtures(weeklyHours, entries = (restaurantCount - 1).coerceAtLeast(0))
    return FakeShareatData(
        accounts = accounts + catalogue.accounts,
        customerProfiles = profiles,
        restaurants = restaurants + catalogue.restaurants,
        menus = menus + catalogue.menus,
        dishes = dishes + catalogue.dishes,
        menuItems = menuItems + catalogue.menuItems,
        reviews = reviews + catalogue.reviews,
    )
}
