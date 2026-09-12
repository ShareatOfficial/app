package org.shareat.feature.restauranthome.ui.model

import kotlin.test.Test
import kotlin.test.assertEquals

class RestaurantAddressUiStateTest {
    @Test
    fun `display address keeps the street city and region in a readable order`() {
        val address = RestaurantAddressUiState(
            streetLine = "Calle del Olmo, 18",
            locality = "Madrid",
            postalCode = "28012",
            region = "Comunidad de Madrid",
            countryCode = "ES",
        )

        assertEquals(
            "Calle del Olmo, 18, 28012 Madrid, Comunidad de Madrid",
            address.toDisplayAddress(),
        )
    }

    @Test
    fun `display address omits blank sections without leaving separators`() {
        val address = RestaurantAddressUiState(
            streetLine = " Calle Mayor 1 ",
            locality = " ",
            postalCode = "28013",
            region = " ",
            countryCode = "ES",
        )

        assertEquals("Calle Mayor 1, 28013", address.toDisplayAddress())
    }
}
