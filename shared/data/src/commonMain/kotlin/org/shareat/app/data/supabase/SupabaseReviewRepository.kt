package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.mapper.toSaveRpc
import org.shareat.app.data.supabase.model.RatingSummaryDto
import org.shareat.app.data.supabase.model.ReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

private const val RestaurantReviews = "restaurant_review_details"
private const val DishReviews = "dish_review_details"

internal class SupabaseReviewRepository(
    private val client: SupabaseClient,
) : ReviewRepository {
    override suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>> = supabaseResult {
        client.from(target.detailsView()).select {
            filter {
                when (target) {
                    is ReviewTarget.Restaurant -> eq("restaurant_id", target.restaurantId.value)
                    is ReviewTarget.Dish -> eq("dish_id", target.dishId.value)
                }
                eq("visibility", "public")
                eq("moderation_status", "visible")
            }
        }.decodeList<ReviewDto>().sortedByDescending(ReviewDto::createdAt).map(ReviewDto::toDomain)
    }

    override suspend fun getPublicDishReviews(
        dishIds: Set<DishId>,
    ): RepositoryResult<Map<DishId, List<Review>>> = supabaseResult {
        if (dishIds.isEmpty()) {
            emptyMap()
        } else {
            selectInBatches(dishIds.map(DishId::value)) { batch ->
                client.from(DishReviews).select {
                    filter {
                        isIn("dish_id", batch)
                        eq("visibility", "public")
                        eq("moderation_status", "visible")
                    }
                }.decodeList<ReviewDto>()
            }
                .sortedByDescending(ReviewDto::createdAt)
                .groupBy({ DishId(requireNotNull(it.dishId)) }, ReviewDto::toDomain)
        }
    }

    override suspend fun getReviewsByAuthor(accountId: AccountId): RepositoryResult<List<Review>> = supabaseResult {
        listOf(RestaurantReviews, DishReviews)
            .flatMap { view ->
                client.from(view).select {
                    filter { eq("author_account_id", accountId.value) }
                }.decodeList<ReviewDto>()
            }
            .sortedByDescending(ReviewDto::updatedAt)
            .map(ReviewDto::toDomain)
    }

    override suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary> = supabaseResult {
        val table = when (target) {
            is ReviewTarget.Restaurant -> "restaurant_rating_summaries"
            is ReviewTarget.Dish -> "dish_rating_summaries"
        }
        val rows = client.from(table).select {
            filter {
                when (target) {
                    is ReviewTarget.Restaurant -> eq("restaurant_id", target.restaurantId.value)
                    is ReviewTarget.Dish -> eq("dish_id", target.dishId.value)
                }
            }
        }.decodeList<RatingSummaryDto>()
        rows.singleOrNull()?.toDomain() ?: RatingSummary(averageTenths = null, ratingCount = 0)
    }

    override suspend fun getRestaurantRatingSummaries(
        restaurantIds: Set<RestaurantId>,
    ): RepositoryResult<Map<RestaurantId, RatingSummary>> = supabaseResult {
        if (restaurantIds.isEmpty()) {
            emptyMap()
        } else {
            selectInBatches(restaurantIds.map(RestaurantId::value)) { batch ->
                client.from("restaurant_rating_summaries").select {
                    filter { isIn("restaurant_id", batch) }
                }.decodeList<RatingSummaryDto>()
            }
                .associateBy({ RestaurantId(requireNotNull(it.restaurantId)) }, RatingSummaryDto::toDomain)
        }
    }

    override suspend fun saveReview(draft: ReviewDraft): RepositoryResult<Review> = supabaseResult {
        val id = client.postgrest.rpc(
            function = "save_review",
            parameters = draft.toSaveRpc(),
        ).decodeAs<String>()
        client.from(draft.target.detailsView()).select {
            filter { eq("id", id) }
        }.decodeList<ReviewDto>().singleOrNull()?.toDomain()
            ?: throw DomainNotFound("review", id)
    }

    override suspend fun deleteReview(
        id: ReviewId,
        authorAccountId: AccountId,
    ): RepositoryResult<Unit> = supabaseResult {
        client.from("reviews").delete {
            filter {
                eq("id", id.value)
                eq("author_account_id", authorAccountId.value)
            }
        }
    }

    override suspend fun reportReview(id: ReviewId, reason: ReviewReportReason): RepositoryResult<Unit> = supabaseResult {
        client.postgrest.rpc(
            function = "report_review",
            parameters = mapOf("p_review_id" to id.value, "p_reason" to reason.name.lowercase()),
        )
    }

    override suspend fun blockReviewAuthor(id: ReviewId): RepositoryResult<AccountId> = supabaseResult {
        AccountId(client.postgrest.rpc(
            function = "block_review_author",
            parameters = mapOf("p_review_id" to id.value),
        ).decodeAs<String>())
    }
}

private fun ReviewTarget.detailsView(): String = when (this) {
    is ReviewTarget.Restaurant -> RestaurantReviews
    is ReviewTarget.Dish -> DishReviews
}
