package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MenuDto(
    val id: String,
    @SerialName("restaurant_id") val restaurantId: String,
    val name: String,
    val description: String? = null,
    @SerialName("publication_state") val publicationState: String,
    @SerialName("price_minor_units") val priceMinorUnits: Long? = null,
    @SerialName("restaurants") val restaurant: MenuRestaurantDto? = null,
    @SerialName("menu_items") val items: List<MenuItemDto> = emptyList(),
)

/** Only the part of the owning restaurant a menu needs: the currency its prices are in. */
@Serializable
internal data class MenuRestaurantDto(
    @SerialName("currency_code") val currencyCode: String,
)

@Serializable
internal data class MenuItemDto(
    @SerialName("menu_id") val menuId: String = "",
    @SerialName("dish_id") val dishId: String,
    @SerialName("price_minor_units") val priceMinorUnits: Long,
    val position: Int,
    @SerialName("is_enabled") val isEnabled: Boolean,
    @SerialName("dishes") val dish: DishDto? = null,
)

@Serializable
internal data class SaveRestaurantMenuRpc(
    @SerialName("p_restaurant_id") val restaurantId: String,
    @SerialName("p_menu_id") val menuId: String?,
    @SerialName("p_name") val name: String,
    @SerialName("p_description") val description: String,
    @SerialName("p_publication_state") val publicationState: String,
    @SerialName("p_price_minor_units") val priceMinorUnits: Long?,
    @SerialName("p_items") val items: List<MenuItemUpdateDto>,
)

@Serializable
internal data class MenuItemUpdateDto(
    @SerialName("dish_id") val dishId: String,
    @SerialName("price_minor_units") val priceMinorUnits: Long,
    val position: Int,
    @SerialName("is_enabled") val isEnabled: Boolean,
)
