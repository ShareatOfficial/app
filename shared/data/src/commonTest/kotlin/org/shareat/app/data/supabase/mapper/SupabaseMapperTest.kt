package org.shareat.app.data.supabase.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.shareat.app.data.supabase.model.DishDto
import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.MenuItemDto
import org.shareat.app.data.supabase.model.OpeningPeriodDto
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Weekday

class SupabaseMapperTest {
    @Test
    fun restaurantDtoMapsCoordinatesHoursAndPublicImage() {
        val restaurant = RestaurantDto(
            id = "restaurant-id",
            ownerAccountId = "owner-id",
            name = "Shareat Test",
            heroImagePath = "restaurant-id/hero.jpg",
            publicEmail = "hello@example.com",
            streetLine = "Street 1",
            locality = "Madrid",
            postalCode = "28001",
            countryCode = "ES",
            latitude = 40.4,
            longitude = -3.7,
            publicationState = "published",
        ).toDomain(
            periods = listOf(OpeningPeriodDto("restaurant-id", 1, 0, "13:30:00", "16:00:00")),
            publicImageUrl = { "https://images.example/$it" },
        )

        assertEquals(RestaurantPublicationState.Published, restaurant.publicationState)
        assertEquals(Weekday.Monday, restaurant.openingHours.days.single().day)
        assertEquals(13, restaurant.openingHours.days.single().periods.single().opensAt.hour)
        assertEquals(-3.7, restaurant.address.coordinates?.longitude)
        assertEquals("https://images.example/restaurant-id/hero.jpg", restaurant.heroImage?.url)
        assertEquals(Currency.Euro, restaurant.currency)
    }

    @Test
    fun dishDtoMapsFixedEuAllergens() {
        val dish = DishDto(
            id = "dish-id",
            name = "Dish",
            allergenNote = "Ask the restaurant",
            isEnabled = true,
        ).toDomain(
            restaurantId = RestaurantId("restaurant-id"),
            allergens = setOf("milk", "cereals_containing_gluten"),
            publicImageUrl = { it },
        )

        val declaration = assertNotNull(dish.allergenDeclaration)
        assertEquals(setOf(EuAllergen.Milk, EuAllergen.CerealsContainingGluten), declaration.allergens)
    }

    @Test
    fun anUnknownAllergenIsDroppedInsteadOfFailingTheDish() {
        val dish = dishWithAllergens(setOf("milk", "unobtainium"))

        assertEquals(setOf(EuAllergen.Milk), assertNotNull(dish.allergenDeclaration).allergens)
    }

    @Test
    fun aSetMenuCarriesItsFixedPriceAndAnALaCarteMenuDoesNot() {
        val setMenu = menuDto(priceMinorUnits = 2_950).toDomain(Currency.Euro)
        val aLaCarte = menuDto(priceMinorUnits = null).toDomain(Currency.Euro)

        assertEquals(2_950, setMenu.price?.minorUnits)
        assertEquals(Currency.Euro, setMenu.price?.currency)
        assertNull(aLaCarte.price)
    }

    @Test
    fun aMenuItemIsPricedInTheRestaurantsCurrency() {
        val dish = dishWithAllergens(emptySet())
        val menuDish = MenuItemDto(
            menuId = "menu-id",
            dishId = dish.id.value,
            priceMinorUnits = 1_800,
            position = 0,
            isEnabled = true,
        ).toDomain(dish, Currency.Euro)

        assertEquals(1_800, menuDish.price.minorUnits)
        assertEquals(Currency.Euro, menuDish.price.currency)
    }

    @Test
    fun restaurantUpdateMapsAllOpeningPeriodsForTransactionalRpc() {
        val restaurant = RestaurantDto(
            id = "restaurant-id",
            ownerAccountId = "owner-id",
            name = "Shareat Test",
            streetLine = "Street 1",
            locality = "Madrid",
            postalCode = "28001",
            countryCode = "ES",
            publicationState = "draft",
        ).toDomain(
            periods = listOf(
                OpeningPeriodDto("restaurant-id", 1, 0, "09:00:00", "13:00:00"),
                OpeningPeriodDto("restaurant-id", 1, 1, "17:00:00", "22:30:00"),
            ),
            publicImageUrl = { it },
        )

        val rpc = restaurant.toUpdateSettingsRpc()

        assertEquals("restaurant-id", rpc.restaurantId)
        assertEquals(2, rpc.openingPeriods.size)
        assertEquals("09:00:00", rpc.openingPeriods.first().opensAt)
        assertEquals("22:30:00", rpc.openingPeriods.last().closesAt)
    }

    @Test
    fun restaurantProfileRpcAlwaysIncludesOptionalTextParameters() {
        val rpc = RestaurantProfileDraft(
            name = "Shareat Test",
            address = PostalAddress("Street 1", "Madrid", "28001"),
        ).toCreateProfileRpc()

        assertEquals("", rpc.description)
        assertEquals("", rpc.publicEmail)
        assertEquals("", rpc.publicPhone)
        assertEquals("", rpc.region)
    }

    private fun dishWithAllergens(allergens: Set<String>) = DishDto(
        id = "dish-id",
        name = "Dish",
        isEnabled = true,
    ).toDomain(
        restaurantId = RestaurantId("restaurant-id"),
        allergens = allergens,
        publicImageUrl = { it },
    )

    private fun menuDto(priceMinorUnits: Long?) = MenuDto(
        id = "menu-id",
        restaurantId = "restaurant-id",
        name = "Menu",
        publicationState = "published",
        priceMinorUnits = priceMinorUnits,
    )
}
