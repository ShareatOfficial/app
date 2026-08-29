package org.shareat.app.data.supabase

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AccountDto(
    val id: String,
    val role: String,
    val status: String,
)

@Serializable
internal data class CustomerProfileDto(
    @SerialName("account_id") val accountId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("avatar_alt_text") val avatarAltText: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String = "en-US",
)

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
    val latitude: Double? = null,
    val longitude: Double? = null,
    @SerialName("publication_state") val publicationState: String,
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
internal data class MenuDto(
    val id: String,
    @SerialName("restaurant_id") val restaurantId: String,
    val name: String,
    val description: String? = null,
    @SerialName("publication_state") val publicationState: String,
)

@Serializable
internal data class DishDto(
    val id: String,
    @SerialName("restaurant_id") val restaurantId: String,
    val name: String,
    val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("image_alt_text") val imageAltText: String? = null,
    @SerialName("allergen_note") val allergenNote: String? = null,
    @SerialName("allergen_source") val allergenSource: String? = null,
    @SerialName("is_enabled") val isEnabled: Boolean,
)

@Serializable
internal data class DishAllergenDto(
    @SerialName("dish_id") val dishId: String,
    @SerialName("allergen_id") val allergenId: String,
)

@Serializable
internal data class MenuItemDto(
    @SerialName("menu_id") val menuId: String,
    @SerialName("dish_id") val dishId: String,
    @SerialName("restaurant_id") val restaurantId: String,
    @SerialName("price_minor_units") val priceMinorUnits: Long,
    val currency: String,
    val position: Int,
    @SerialName("is_enabled") val isEnabled: Boolean,
)

@Serializable
internal data class ReviewDto(
    val id: String,
    @SerialName("author_account_id") val authorAccountId: String,
    @SerialName("restaurant_id") val restaurantId: String? = null,
    @SerialName("dish_id") val dishId: String? = null,
    val rating: Int,
    val comment: String? = null,
    val visibility: String,
    @SerialName("moderation_status") val moderationStatus: String,
    @SerialName("visited_at") val visitedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
internal data class ReviewInsertDto(
    @SerialName("author_account_id") val authorAccountId: String,
    @SerialName("restaurant_id") val restaurantId: String? = null,
    @SerialName("dish_id") val dishId: String? = null,
    val rating: Int,
    val comment: String? = null,
    val visibility: String,
    @SerialName("visited_at") val visitedAt: String? = null,
)

@Serializable
internal data class ReviewUpdateDto(
    val rating: Int,
    val comment: String? = null,
    val visibility: String,
    @SerialName("visited_at") val visitedAt: String? = null,
)

@Serializable
internal data class RatingSummaryDto(
    @SerialName("restaurant_id") val restaurantId: String? = null,
    @SerialName("dish_id") val dishId: String? = null,
    @SerialName("average_tenths") val averageTenths: Int,
    @SerialName("rating_count") val ratingCount: Long,
)
