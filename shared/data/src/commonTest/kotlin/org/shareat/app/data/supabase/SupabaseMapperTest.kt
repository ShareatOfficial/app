package org.shareat.app.data.supabase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.PostalAddress
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
    }

    @Test
    fun dishDtoMapsFixedEuAllergens() {
        val dish = DishDto(
            id = "dish-id",
            restaurantId = "restaurant-id",
            name = "Dish",
            allergenNote = "Ask the restaurant",
            allergenSource = "restaurant",
            isEnabled = true,
        ).toDomain(
            allergens = setOf("milk", "cereals_containing_gluten"),
            publicImageUrl = { it },
        )

        val declaration = assertNotNull(dish.allergenDeclaration)
        assertEquals(setOf(EuAllergen.Milk, EuAllergen.CerealsContainingGluten), declaration.allergens)
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
}
