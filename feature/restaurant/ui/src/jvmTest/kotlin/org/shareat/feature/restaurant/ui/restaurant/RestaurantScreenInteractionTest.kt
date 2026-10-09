package org.shareat.feature.restaurant.ui.restaurant

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.ImageComposeScene
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import org.shareat.app.domain.model.ReviewReportReason
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restaurant.ui.model.DishCardUiState
import org.shareat.feature.restaurant.ui.model.DishReviewUiState
import org.shareat.feature.restaurant.ui.model.RestaurantHeaderUiState
import org.shareat.feature.restaurant.ui.model.RestaurantUiState
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.category_desserts
import shareat.feature.restaurant.ui.generated.resources.restaurant_close
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_allergens_unknown
import shareat.feature.restaurant.ui.generated.resources.restaurant_dish_rate_star
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_report_spam
import shareat.feature.restaurant.ui.generated.resources.restaurant_review_block
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalComposeUiApi::class)
class RestaurantScreenInteractionTest {
    @Test
    fun categoryClickScrollsToItsHeadingWithoutFilteringTheMenu() {
        val state = menuState()
        withScene(state) { scene ->
            val desserts = runBlocking { getString(Res.string.category_desserts) }
            scene.click { node -> node.hasText(desserts) && node.config.getOrNull(SemanticsActions.OnClick) != null }
            val heading = scene.nodes().first {
                it.hasText(desserts) && it.config.getOrNull(SemanticsActions.OnClick) == null
            }
            assertTrue(heading.boundsInRoot.top in 48f..130f, "Category heading should appear below the pinned bars: ${heading.boundsInRoot}")
            assertTrue(scene.nodes().any { it.hasText(desserts) && it.config.getOrNull(SemanticsProperties.Selected) == true })
            assertEquals(13, state.dishes.size)
        }
    }

    @Test
    fun openDishUsesUpdatedReviewsAndClosesWhenTheDishLeavesTheMenu() {
        val state = mutableStateOf(menuState())
        val scene = ImageComposeScene(411, 891) { ShareatTheme { RestaurantScreenStateless(state.value) } }
        try {
            scene.advance()
            scene.click { it.hasText("Starter 0") && it.config.getOrNull(SemanticsActions.OnClick) != null }
            val unknownAllergens = runBlocking { getString(Res.string.restaurant_dish_allergens_unknown) }
            assertTrue(scene.nodes().any { it.hasText(unknownAllergens) })
            state.value = state.value.copy(dishes = state.value.dishes.map {
                if (it.id == "starter-0") it.copy(reviews = listOf(DishReviewUiState("new-review", 5, "Fresh review"))) else it
            })
            scene.advance()
            assertTrue(scene.nodes().any { it.hasText("Fresh review") })
            state.value = state.value.copy(dishes = state.value.dishes.filterNot { it.id == "starter-0" })
            scene.advance()
            val close = runBlocking { getString(Res.string.restaurant_close) }
            assertFalse(scene.nodes().any { it.hasDescription(close) })
        } finally { scene.close() }
    }

    @Test
    fun ratingDismissesTheDetailBeforeRequestingTheReviewFlow() {
        var requested: Pair<String, Int>? = null
        var panelStillOpenAtRequest = true
        lateinit var scene: ImageComposeScene
        scene = ImageComposeScene(411, 891) {
            ShareatTheme {
                RestaurantScreenStateless(menuState(), onDishRatingClick = { id, rating ->
                    requested = id to rating
                    // The sheet must already be hidden before the parent opens the review sheet.
                    panelStillOpenAtRequest = scene.nodes().any {
                        it.hasDescription(closeLabel) && it.boundsInRoot.height > 0f && it.boundsInRoot.top < 891f
                    }
                })
            }
        }
        try {
            closeLabel = runBlocking { getString(Res.string.restaurant_close) }
            scene.advance()
            scene.click { it.hasText("Starter 0") && it.config.getOrNull(SemanticsActions.OnClick) != null }
            val rate = runBlocking { getString(Res.string.restaurant_dish_rate_star, 4) }
            scene.click { it.hasDescription(rate) }
            scene.advance()
            assertEquals("starter-0" to 4, requested)
            assertFalse(panelStillOpenAtRequest)
            assertFalse(scene.nodes().any { it.hasDescription(closeLabel) })
        } finally { scene.close() }
    }

    @Test
    fun reviewModerationActionsRemainAvailableInsideTheDishPanel() {
        var report: Pair<String, ReviewReportReason>? = null
        var blockedReview: String? = null
        val state = menuState().copy(dishes = menuState().dishes.map {
            if (it.id == "starter-0") it.copy(reviews = listOf(DishReviewUiState("review-1", 5, "Public review"))) else it
        })
        val scene = ImageComposeScene(411, 891) {
            ShareatTheme {
                RestaurantScreenStateless(
                    state,
                    onReportReview = { id, reason -> report = id to reason },
                    onBlockReviewer = { blockedReview = it },
                )
            }
        }
        try {
            scene.advance()
            scene.click { it.hasText("Starter 0") && it.config.getOrNull(SemanticsActions.OnClick) != null }
            val reportLabel = runBlocking { getString(Res.string.restaurant_review_report) }
            val spamLabel = runBlocking { getString(Res.string.restaurant_review_report_spam) }
            scene.click { it.hasText(reportLabel) && it.config.getOrNull(SemanticsActions.OnClick) != null }
            scene.click { it.hasText(spamLabel) && it.config.getOrNull(SemanticsActions.OnClick) != null }
            assertEquals("review-1" to ReviewReportReason.Spam, report)
            val blockLabel = runBlocking { getString(Res.string.restaurant_review_block) }
            scene.click { it.hasText(blockLabel) && it.config.getOrNull(SemanticsActions.OnClick) != null }
            // The second click confirms the existing moderation dialog.
            val confirmation = scene.nodes().last {
                it.hasText(blockLabel) && it.config.getOrNull(SemanticsActions.OnClick) != null
            }
            assertTrue(confirmation.config.getOrNull(SemanticsActions.OnClick)?.action?.invoke() == true)
            scene.advance()
            assertEquals("review-1", blockedReview)
        } finally { scene.close() }
    }

    private var closeLabel = ""

    private fun menuState() = RestaurantUiState(
        header = RestaurantHeaderUiState("Casa Naranja", "Calle del Olmo, 18, Madrid", description = "Cocina de temporada"),
        hasPublishedMenu = true,
        dishes = List(5) { index ->
            DishCardUiState("starter-$index", "Starter $index", "12€", category = DishCategory.Starters)
        } + List(8) { index ->
            DishCardUiState("dessert-$index", "Dessert $index", "6€", category = DishCategory.Desserts)
        },
    )

    private fun withScene(state: RestaurantUiState, block: (ImageComposeScene) -> Unit) {
        val scene = ImageComposeScene(411, 891) { ShareatTheme { RestaurantScreenStateless(state) } }
        try { scene.advance(); block(scene) } finally { scene.close() }
    }

    private var frameTime = 0L

    private fun ImageComposeScene.advance() {
        repeat(30) { frameTime += 100_000_000L; render(frameTime).close() }
    }

    private fun ImageComposeScene.nodes(): List<SemanticsNode> =
        semanticsOwners.flatMap { owner -> collect(owner.rootSemanticsNode) }

    private fun collect(node: SemanticsNode): List<SemanticsNode> = listOf(node) + node.children.flatMap(::collect)
    private fun SemanticsNode.hasText(text: String) = config.getOrNull(SemanticsProperties.Text)?.any { it.text == text } == true
    private fun SemanticsNode.hasDescription(text: String) = config.getOrNull(SemanticsProperties.ContentDescription)?.contains(text) == true

    private fun ImageComposeScene.click(predicate: (SemanticsNode) -> Boolean) {
        val target = nodes().firstOrNull(predicate)
        assertNotNull(target, "Expected clickable UI element")
        assertTrue(target.config.getOrNull(SemanticsActions.OnClick)?.action?.invoke() == true)
        advance()
    }
}
