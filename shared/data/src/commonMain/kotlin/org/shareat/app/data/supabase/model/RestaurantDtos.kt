package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class RestaurantDto(
    val id: String,
    @SerialName("owner_account_id") val ownerAccountId: String,
    val name: String,
    val description: String? = null,
    @SerialName("hero_image_path") val heroImagePath: String? = null,
    @SerialName("hero_image_alt_text") val heroImageAltText: String? = null,
    @SerialName("public_email") val publicEmail: String? = null,
    @SerialName("public_phone") val publicPhone: String? = null,
    @SerialName("street_line") val streetLine: String,
    val locality: String,
    @SerialName("postal_code") val postalCode: String,
    val region: String? = null,
    @SerialName("country_code") val countryCode: String,
    @SerialName("currency_code") val currencyCode: String = "EUR",
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("publication_state") val publicationState: String,
    @SerialName("restaurant_opening_periods") val openingPeriods: List<EmbeddedOpeningPeriodDto> = emptyList(),
)

/** The schedule as it arrives nested inside its restaurant, without repeating the restaurant id. */
@Serializable
internal data class EmbeddedOpeningPeriodDto(
    val weekday: Int,
    val position: Int,
    @SerialName("opens_at") val opensAt: String,
    @SerialName("closes_at") val closesAt: String,
)

@Serializable
internal data class OpeningPeriodDto(
    @SerialName("restaurant_id") val restaurantId: String,
    val weekday: Int,
    val position: Int,
    @SerialName("opens_at") val opensAt: String,
    @SerialName("closes_at") val closesAt: String,
)

@Serializable
internal data class UpdateRestaurantSettingsRpc(
    @SerialName("p_restaurant_id") val restaurantId: String,
    @SerialName("p_name") val name: String,
    @SerialName("p_description") val description: String?,
    @SerialName("p_public_email") val publicEmail: String?,
    @SerialName("p_public_phone") val publicPhone: String?,
    @SerialName("p_street_line") val streetLine: String,
    @SerialName("p_locality") val locality: String,
    @SerialName("p_postal_code") val postalCode: String,
    @SerialName("p_publication_state") val publicationState: String,
    @SerialName("p_opening_periods") val openingPeriods: List<OpeningPeriodUpdateDto>,
)

@Serializable
internal data class CreateRestaurantProfileRpc(
    @SerialName("p_name") val name: String,
    @SerialName("p_description") val description: String,
    @SerialName("p_public_email") val publicEmail: String,
    @SerialName("p_public_phone") val publicPhone: String,
    @SerialName("p_street_line") val streetLine: String,
    @SerialName("p_locality") val locality: String,
    @SerialName("p_postal_code") val postalCode: String,
    @SerialName("p_region") val region: String,
    @SerialName("p_opening_periods") val openingPeriods: List<CreateOpeningPeriodDto>,
)

@Serializable
internal data class CreateOpeningPeriodDto(
    val weekday: Int,
    @SerialName("opens_at") val opensAt: String,
    @SerialName("closes_at") val closesAt: String,
)

@Serializable
internal data class OpeningPeriodUpdateDto(
    val weekday: Int,
    val position: Int,
    @SerialName("opens_at") val opensAt: String,
    @SerialName("closes_at") val closesAt: String,
)

@Serializable
internal data class RestaurantCurrencyDto(
    @SerialName("currency_code") val currencyCode: String,
)
