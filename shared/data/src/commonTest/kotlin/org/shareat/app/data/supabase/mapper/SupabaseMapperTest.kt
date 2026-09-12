package org.shareat.app.data.supabase.mapper

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import org.shareat.app.data.supabase.model.DishDto
import org.shareat.app.data.supabase.model.EmbeddedAllergenDto
import org.shareat.app.data.supabase.model.EmbeddedOpeningPeriodDto
import org.shareat.app.data.supabase.model.MenuDto
import org.shareat.app.data.supabase.model.MenuItemDto
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft
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
            region = "Comunidad de Madrid",
            countryCode = "ES",
            latitude = 40.4,
            longitude = -3.7,
            publicationState = "published",
            openingPeriods = listOf(EmbeddedOpeningPeriodDto(1, 0, "13:30:00", "16:00:00")),
        ).toDomain(publicImageUrl = { "https://images.example/$it" })

        assertEquals(RestaurantPublicationState.Published, restaurant.publicationState)
        assertEquals(Weekday.Monday, restaurant.openingHours.days.single().day)
        assertEquals(13, restaurant.openingHours.days.single().periods.single().opensAt.hour)
        assertEquals(-3.7, restaurant.address?.coordinates?.longitude)
        assertEquals("https://images.example/restaurant-id/hero.jpg", restaurant.heroImage?.url)
        assertEquals(Currency.Euro, restaurant.currency)
    }

    @Test
    fun dishDtoMapsFixedEuAllergens() {
        val dish = DishDto(
            id = "dish-id",
            restaurantId = "restaurant-id",
            name = "Dish",
            allergenNote = "Ask the restaurant",
            isEnabled = true,
            allergens = listOf(EmbeddedAllergenDto("milk"), EmbeddedAllergenDto("cereals_containing_gluten")),
        ).toDomain(publicImageUrl = { it })

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
            region = "Comunidad de Madrid",
            countryCode = "ES",
            publicationState = "draft",
            openingPeriods = listOf(
                EmbeddedOpeningPeriodDto(1, 0, "09:00:00", "13:00:00"),
                EmbeddedOpeningPeriodDto(1, 1, "17:00:00", "22:30:00"),
            ),
        ).toDomain(publicImageUrl = { it })

        val rpc = restaurant.toUpdateSettingsRpc()

        assertEquals("restaurant-id", rpc.restaurantId)
        assertEquals(2, rpc.openingPeriods.size)
        assertEquals("09:00:00", rpc.openingPeriods.first().opensAt)
        assertEquals("22:30:00", rpc.openingPeriods.last().closesAt)
        assertEquals("", rpc.description)
        assertEquals("", rpc.publicEmail)
        assertEquals("", rpc.publicPhone)
        assertEquals("Comunidad de Madrid", rpc.region)
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

    @Test
    fun saveMenuRpcIncludesAnEmptyDescriptionWhenTheDraftHasNone() {
        val rpc = RestaurantMenuDraft(
            restaurantId = RestaurantId("restaurant-id"),
            name = "Menu",
            items = emptyList(),
        ).toSaveRpc()

        assertEquals("", rpc.description)
    }

    @Test
    fun saveDishRpcIncludesEmptyOptionalTextWhenTheDraftHasNone() {
        val rpc = DishDraft(
            restaurantId = RestaurantId("restaurant-id"),
            name = "Dish",
        ).toSaveRpc()

        assertEquals("", rpc.description)
        assertEquals("", rpc.allergenNote)
    }

    private fun dishWithAllergens(allergens: Set<String>) = DishDto(
        id = "dish-id",
        restaurantId = "restaurant-id",
        name = "Dish",
        isEnabled = true,
        allergens = allergens.map(::EmbeddedAllergenDto),
    ).toDomain(publicImageUrl = { it })

    private fun menuDto(priceMinorUnits: Long?) = MenuDto(
        id = "menu-id",
        restaurantId = "restaurant-id",
        name = "Menu",
        publicationState = "published",
        priceMinorUnits = priceMinorUnits,
    )
}
