package org.shareat.app.data.supabase.mapper

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.shareat.app.data.supabase.model.UnlistedDishReviewDto
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import kotlin.test.Test
import kotlin.test.assertEquals

class UnlistedDishReviewMapperTest {
    @Test fun decodesPrivateHiddenReviewWithSignedImageAndVisitDate() {
        val dto = Json.decodeFromString<UnlistedDishReviewDto>("""
            {"id":"review","author_account_id":"author","restaurant_name":"Casa","dish_name":"Tortilla",
             "image_path":"author/photo.jpg","rating":4,"comment":"Muy buena","visibility":"private",
             "moderation_status":"hidden","visited_at":"2026-01-01T12:00:00Z",
             "created_at":"2026-01-02T12:00:00Z","updated_at":"2026-01-03T12:00:00Z"}
        """.trimIndent())
        val review = dto.toDomain("https://storage.example/signed-photo")
        assertEquals("https://storage.example/signed-photo", review.image?.url)
        assertEquals("Tortilla", review.image?.alternativeText)
        assertEquals(ReviewVisibility.Private, review.visibility)
        assertEquals(ReviewModerationStatus.Hidden, review.moderationStatus)
        assertEquals("2026-01-01T12:00:00Z", review.visitedAt?.value)
        assertEquals("2026-01-03T12:00:00Z", review.updatedAt.value)
    }

    @Test fun rpcUsesTheDatabaseParameterNamesAndDoesNotSendAnAuthorIdentity() {
        val draft = UnlistedDishReviewDraft(AccountId("author"), "Casa", "Tortilla", ImageUpload(byteArrayOf(1), "image/jpeg"), Rating(4), "Muy buena")
        val json = Json.parseToJsonElement(Json.encodeToString(draft.toSaveRpc("author/photo.jpg"))).jsonObject
        assertEquals(setOf("p_restaurant_name", "p_dish_name", "p_image_path", "p_rating", "p_comment", "p_visibility", "p_visited_at"), json.keys)
        assertEquals("author/photo.jpg", json.getValue("p_image_path").jsonPrimitive.content)
        assertEquals("public", json.getValue("p_visibility").jsonPrimitive.content)
    }
}
