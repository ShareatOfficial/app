package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class UnlistedDishReviewDto(
    val id: String,
    @SerialName("author_account_id") val authorAccountId: String,
    @SerialName("restaurant_name") val restaurantName: String,
    @SerialName("dish_name") val dishName: String,
    @SerialName("image_path") val imagePath: String,
    val rating: Int,
    val comment: String,
    val visibility: String,
    @SerialName("moderation_status") val moderationStatus: String,
    @SerialName("visited_at") val visitedAt: String? = null,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
internal data class SaveUnlistedDishReviewRpc(
    @SerialName("p_restaurant_name") val restaurantName: String,
    @SerialName("p_dish_name") val dishName: String,
    @SerialName("p_image_path") val imagePath: String,
    @SerialName("p_rating") val rating: Int,
    @SerialName("p_comment") val comment: String,
    @SerialName("p_visibility") val visibility: String,
    @SerialName("p_visited_at") val visitedAt: String?,
)
