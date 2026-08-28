package org.shareat.feature.profile.ui.settings

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.GeoCoordinates
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.OpeningPeriod
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Weekday
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.feature.profile.domain.ProfileSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SettingsMappersTest {
    @Test
    fun mapsCustomerDomainDataToUserUiState() {
        val accountId = AccountId("customer-id")
        val settings = ProfileSettings.User(
            account = Account(
                accountId,
                EmailAddress("ana@example.com"),
                AccountRole.Customer,
                AccountStatus.Active,
            ),
            profile = CustomerProfile(accountId, "Ana María Rivera"),
        )

        val state = settings.toUiState()

        assertEquals("Ana María Rivera", state.name)
        assertEquals("ana@example.com", state.email)
        assertEquals("AM", state.initials)
    }

    @Test
    fun mapsFirstOpeningPeriodToUiAndPreservesTheSecondOnUpdate() {
        val original = restaurantFixture()
        val state = original.toUiState()

        val monday = state.openingHours.first { it.day == OpeningDay.Monday }
        assertEquals("09:00", monday.openingTime)
        assertEquals("13:00", monday.closingTime)

        val result = state.copy(
            openingHours = state.openingHours.map {
                if (it.day == OpeningDay.Monday) it.copy(openingTime = "10:00") else it
            },
        ).toUpdateParams(original)

        val params = assertIs<RestaurantSettingsMappingResult.Success>(result).params
        val periods = params.openingHours.days.first { it.day == Weekday.Monday }.periods
        assertEquals(LocalTime(10, 0), periods.first().opensAt)
        assertEquals(original.openingHours.days.first().periods[1], periods[1])
        assertEquals(original.address.coordinates, params.address.coordinates)
    }

    @Test
    fun emptyOptionalContactFieldsMapToNull() {
        val original = restaurantFixture()
        val result = original.toUiState().copy(email = "", phone = "", description = "")
            .toUpdateParams(original)

        val params = assertIs<RestaurantSettingsMappingResult.Success>(result).params
        assertNull(params.publicEmail)
        assertNull(params.publicPhone)
        assertNull(params.description)
    }

    @Test
    fun rejectsInvalidEmailAndTime() {
        val original = restaurantFixture()

        assertIs<RestaurantSettingsMappingResult.Failure>(
            original.toUiState().copy(email = "invalid").toUpdateParams(original),
        )
        assertIs<RestaurantSettingsMappingResult.Failure>(
            original.toUiState().copy(
                openingHours = original.toUiState().openingHours.map {
                    if (it.day == OpeningDay.Monday) it.copy(openingTime = "9am") else it
                },
            ).toUpdateParams(original),
        )
    }

    @Test
    fun hiddenDisabledRestaurantRemainsDisabled() {
        val original = restaurantFixture().copy(
            publicationState = RestaurantPublicationState.Disabled,
        )

        val result = original.toUiState().copy(isPublished = false).toUpdateParams(original)

        val params = assertIs<RestaurantSettingsMappingResult.Success>(result).params
        assertEquals(RestaurantPublicationState.Disabled, params.publicationState)
    }
}

internal fun restaurantFixture(): Restaurant {
    val first = OpeningPeriod(LocalTime(9, 0), LocalTime(13, 0))
    val second = OpeningPeriod(LocalTime(17, 0), LocalTime(22, 0))
    return Restaurant(
        id = RestaurantId("restaurant-id"),
        ownerAccountId = AccountId("owner-id"),
        name = "Casa Naranja",
        description = "Local food",
        publicEmail = EmailAddress("hello@example.com"),
        publicPhone = "+34 900 000 000",
        address = PostalAddress(
            streetLine = "Calle Mayor 1",
            locality = "Valencia",
            postalCode = "46001",
            coordinates = GeoCoordinates(39.4699, -0.3763),
        ),
        openingHours = WeeklyOpeningHours(
            listOf(DailyOpeningHours(Weekday.Monday, listOf(first, second))),
        ),
        publicationState = RestaurantPublicationState.Published,
    )
}
