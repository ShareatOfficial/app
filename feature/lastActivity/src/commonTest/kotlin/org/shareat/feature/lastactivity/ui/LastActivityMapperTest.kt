package org.shareat.feature.lastactivity.ui

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.feature.lastactivity.domain.LastActivityItem
import kotlin.test.Test
import kotlin.test.assertEquals

class LastActivityMapperTest {
    @Test fun manualReviewUsesDishTitleRestaurantDescriptionAndItsOwnPhoto() {
        val review = UnlistedDishReview(
            ReviewId("manual"), AccountId("author"), "Bar del barrio", "Tortilla",
            ImageRef("https://example.com/signed.jpg", "Mi tortilla"), Rating(4), "Muy buena",
            ReviewVisibility.Private, ReviewModerationStatus.Hidden,
            createdAt = IsoTimestamp("2026-01-01T12:00:00Z"), updatedAt = IsoTimestamp("2026-01-01T12:00:00Z"),
        )
        assertEquals(
            LastActivityReviewUiState(review.id, LastActivityTargetType.DISH, review.image?.url, "Mi tortilla", "Tortilla", "Bar del barrio", 4, "Muy buena"),
            LastActivityItem.Unlisted(review).toUiState(),
        )
    }
}
