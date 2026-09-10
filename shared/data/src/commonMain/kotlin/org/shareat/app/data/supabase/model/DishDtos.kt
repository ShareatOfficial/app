package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DishDto(
    val id: String,
    val name: String,
    val description: String? = null,
    @SerialName("image_path") val imagePath: String? = null,
    @SerialName("image_alt_text") val imageAltText: String? = null,
    @SerialName("allergen_note") val allergenNote: String? = null,
    @SerialName("is_enabled") val isEnabled: Boolean,
)

@Serializable
internal data class RestaurantDishDto(
    @SerialName("dish_id") val dishId: String,
    @SerialName("restaurant_id") val restaurantId: String,
)

@Serializable
internal data class DishAllergenDto(
    @SerialName("dish_id") val dishId: String,
    @SerialName("allergen_id") val allergenId: String,
)

@Serializable
internal data class SaveRestaurantDishRpc(
    @SerialName("p_restaurant_id") val restaurantId: String,
    @SerialName("p_dish_id") val dishId: String? = null,
    @SerialName("p_name") val name: String,
    @SerialName("p_description") val description: String? = null,
    @SerialName("p_is_enabled") val isEnabled: Boolean,
    @SerialName("p_allergen_ids") val allergenIds: List<String>,
    @SerialName("p_allergen_note") val allergenNote: String? = null,
)
