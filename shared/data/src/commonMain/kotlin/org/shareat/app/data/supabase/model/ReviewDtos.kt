package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** One row of restaurant_review_details or dish_review_details; only its own target id is set. */
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

/** Writes go through the RPC so the parent row and its target row land in one transaction. */
@Serializable
internal data class SaveReviewRpc(
    @SerialName("p_target_type") val targetType: String,
    @SerialName("p_restaurant_id") val restaurantId: String?,
    @SerialName("p_dish_id") val dishId: String?,
    @SerialName("p_rating") val rating: Int,
    @SerialName("p_comment") val comment: String?,
    @SerialName("p_visibility") val visibility: String,
    @SerialName("p_visited_at") val visitedAt: String?,
)

@Serializable
internal data class RatingSummaryDto(
    @SerialName("restaurant_id") val restaurantId: String? = null,
    @SerialName("dish_id") val dishId: String? = null,
    @SerialName("average_tenths") val averageTenths: Int,
    @SerialName("rating_count") val ratingCount: Long,
)
