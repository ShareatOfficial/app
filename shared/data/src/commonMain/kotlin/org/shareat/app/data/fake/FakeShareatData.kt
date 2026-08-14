package org.shareat.app.data.fake

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.DailyOpeningHours
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
    internal val customerProfiles: List<CustomerProfile>,
    internal val restaurants: List<Restaurant>,
    internal val menus: List<Menu>,
    internal val dishes: List<Dish>,
    internal val menuItems: List<MenuItem>,
    reviews: List<Review>,
) {
    internal val reviews: MutableList<Review> = reviews.toMutableList()

    companion object {
        fun preview(): FakeShareatData = previewData()
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
    val menu = MenuId("menu-casa-naranja")
    val octopus = DishId("dish-charred-octopus")
    val croquettes = DishId("dish-iberian-croquettes")
}

private fun previewData(): FakeShareatData {
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
        ),
        CustomerProfile(
            accountId = FakeIds.secondCustomerAccount,
            displayName = "Diego",
        ),
    )
    val service = OpeningPeriod(LocalTime(13, 0), LocalTime(16, 0))
    val dinner = OpeningPeriod(LocalTime(20, 0), LocalTime(23, 30))
    val weeklyHours = WeeklyOpeningHours(
        days = Weekday.entries.map { day ->
            DailyOpeningHours(
                day = day,
                periods = if (day == Weekday.Monday) emptyList() else listOf(service, dinner),
            )
        },
    )
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
            id = FakeIds.menu,
            restaurantId = FakeIds.restaurant,
            name = "Carta principal",
            description = "Platos pensados para compartir.",
            publicationState = MenuPublicationState.Published,
        ),
    )
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
                note = "La información ha sido facilitada por el restaurante.",
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
    )
    val menuItems = listOf(
        MenuItem(FakeIds.menu, FakeIds.octopus, Money(1_800), position = 0, isEnabled = true),
        MenuItem(FakeIds.menu, FakeIds.croquettes, Money(1_200), position = 1, isEnabled = true),
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
    return FakeShareatData(accounts, profiles, restaurants, menus, dishes, menuItems, reviews)
}
