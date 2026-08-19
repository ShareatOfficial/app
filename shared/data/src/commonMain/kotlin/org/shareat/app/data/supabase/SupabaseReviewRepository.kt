package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.CancellationException
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ReviewRepository

internal class SupabaseReviewRepository(
    private val client: SupabaseClient,
) : ReviewRepository {
    override suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>> = supabaseResult {
        client.from("reviews").select {
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

    override suspend fun getReviewsByAuthor(accountId: AccountId): RepositoryResult<List<Review>> = supabaseResult {
        client.from("reviews").select {
            filter { eq("author_account_id", accountId.value) }
        }.decodeList<ReviewDto>().sortedByDescending(ReviewDto::updatedAt).map(ReviewDto::toDomain)
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

    override suspend fun saveReview(draft: ReviewDraft): RepositoryResult<Review> = supabaseResult {
        val existing = client.from("reviews").select {
            filter {
                eq("author_account_id", draft.authorAccountId.value)
                when (val target = draft.target) {
                    is ReviewTarget.Restaurant -> eq("restaurant_id", target.restaurantId.value)
                    is ReviewTarget.Dish -> eq("dish_id", target.dishId.value)
                }
            }
        }.decodeList<ReviewDto>().singleOrNull()

        if (existing == null) {
            try {
                client.from("reviews").insert(draft.toInsertDto()) {
                    select()
                }.decodeSingle<ReviewDto>().toDomain()
            } catch (insertError: CancellationException) {
                throw insertError
            } catch (insertError: Throwable) {
                val concurrentlyCreated = client.from("reviews").select {
                    filter {
                        eq("author_account_id", draft.authorAccountId.value)
                        when (val target = draft.target) {
                            is ReviewTarget.Restaurant -> eq("restaurant_id", target.restaurantId.value)
                            is ReviewTarget.Dish -> eq("dish_id", target.dishId.value)
                        }
                    }
                }.decodeList<ReviewDto>().singleOrNull() ?: throw insertError
                client.from("reviews").update(draft.toUpdateDto()) {
                    select()
                    filter { eq("id", concurrentlyCreated.id) }
                }.decodeSingle<ReviewDto>().toDomain()
            }
        } else {
            client.from("reviews").update(draft.toUpdateDto()) {
                select()
                filter { eq("id", existing.id) }
            }.decodeSingle<ReviewDto>().toDomain()
        }
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
}

private fun ReviewDraft.toInsertDto(): ReviewInsertDto = ReviewInsertDto(
    authorAccountId = authorAccountId.value,
    restaurantId = (target as? ReviewTarget.Restaurant)?.restaurantId?.value,
    dishId = (target as? ReviewTarget.Dish)?.dishId?.value,
    rating = rating.value,
    comment = comment,
    visibility = visibility.toDatabaseValue(),
    visitedAt = visitedAt?.value,
)

private fun ReviewDraft.toUpdateDto(): ReviewUpdateDto = ReviewUpdateDto(
    rating = rating.value,
    comment = comment,
    visibility = visibility.toDatabaseValue(),
    visitedAt = visitedAt?.value,
)

private fun ReviewVisibility.toDatabaseValue(): String = when (this) {
    ReviewVisibility.Public -> "public"
    ReviewVisibility.Private -> "private"
}
