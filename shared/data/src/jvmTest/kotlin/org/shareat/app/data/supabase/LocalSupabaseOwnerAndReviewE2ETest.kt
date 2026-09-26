package org.shareat.app.data.supabase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.repository.RepositoryResult

/** Full auth-to-publication flows against a fresh local Supabase stack. */
class LocalSupabaseOwnerAndReviewE2ETest {
    @Test
    fun restaurantRegistersSignsInCreatesAndPublishesRestaurant() = runBlocking {
        val config = localConfig() ?: return@runBlocking
        val client = createShareatSupabaseClient(config, secureSessionStorage = null)
        val auth = SupabaseAuthRepository(client)
        val restaurants = SupabaseRestaurantRepository(client)
        val dishes = SupabaseDishRepository(client)
        val menus = SupabaseMenuRepository(client)
        val email = EmailAddress("owner-e2e-${System.currentTimeMillis()}@shareat.test")
        val password = "e2e-restaurant-password"

        val registered = (auth.register(
            RegistrationCredentials(email, password, AccountRole.Restaurant),
        ) as RepositoryResult.Success).value
        assertEquals(AccountRole.Restaurant,
            (SupabaseAccountRepository(client).getAccount(registered.accountId) as RepositoryResult.Success).value.role)
        assertTrue(restaurants.getRestaurantForOwner(registered.accountId) is RepositoryResult.Failure)

        val profile = (restaurants.createRestaurantProfile(
            registered.accountId,
            RestaurantProfileDraft(
                name = "E2E Restaurant ${System.currentTimeMillis()}",
                address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
            ),
        ) as RepositoryResult.Success).value
        val dish = (dishes.saveDish(DishDraft(
            restaurantId = profile.id,
            name = "E2E dish",
        )) as RepositoryResult.Success).value
        val starterMenu = (menus.getMenus(profile.id) as RepositoryResult.Success).value.single()
        menus.saveMenu(RestaurantMenuDraft(
            restaurantId = profile.id,
            menuId = starterMenu.id,
            name = starterMenu.name,
            items = listOf(MenuItemDraft(dish.id, Money(1200), 0)),
        )).let { assertTrue(it is RepositoryResult.Success, "Menu save failed: $it") }
        val published = (restaurants.updateRestaurant(profile.copy(
            publicationState = RestaurantPublicationState.Published,
        )) as RepositoryResult.Success).value
        assertEquals(RestaurantPublicationState.Published, published.publicationState)

        assertTrue(auth.signOut() is RepositoryResult.Success)
        val signedIn = (auth.signIn(email, password) as RepositoryResult.Success).value
        assertEquals(registered.accountId, signedIn.accountId)
        assertEquals(profile.id,
            (restaurants.getRestaurantForOwner(signedIn.accountId) as RepositoryResult.Success).value.id)
        assertTrue((restaurants.getPublishedRestaurants() as RepositoryResult.Success).value.any { it.id == profile.id })
        assertTrue((menus.getPublishedMenus(profile.id) as RepositoryResult.Success).value.single()
            .items.any { it.dish.id == dish.id })
    }

    @Test
    fun customerRegistersSignsInAndCreatesReview() = runBlocking {
        val config = localConfig() ?: return@runBlocking
        val client = createShareatSupabaseClient(config, secureSessionStorage = null)
        val auth = SupabaseAuthRepository(client)
        val restaurants = SupabaseRestaurantRepository(client)
        val menus = SupabaseMenuRepository(client)
        val reviews = SupabaseReviewRepository(client)
        val restaurant = (restaurants.getPublishedRestaurants() as RepositoryResult.Success).value
            .single { it.name == "Local Shareat Kitchen" }
        val dish = (menus.getPublishedMenus(restaurant.id) as RepositoryResult.Success).value
            .single().items.single().dish
        val email = EmailAddress("review-e2e-${System.currentTimeMillis()}@shareat.test")
        val password = "e2e-customer-password"

        val registered = (auth.register(
            RegistrationCredentials(email, password, AccountRole.Customer),
        ) as RepositoryResult.Success).value
        assertTrue(auth.signOut() is RepositoryResult.Success)
        val signedIn = (auth.signIn(email, password) as RepositoryResult.Success).value
        assertEquals(registered.accountId, signedIn.accountId)
        val created = (reviews.saveReview(ReviewDraft(
            authorAccountId = signedIn.accountId,
            target = ReviewTarget.Dish(dish.id),
            rating = Rating(5),
            visibility = ReviewVisibility.Public,
        )) as RepositoryResult.Success).value
        assertEquals(signedIn.accountId, created.authorAccountId)
        assertEquals(5, created.rating.value)
        assertTrue((reviews.getReviewsByAuthor(signedIn.accountId) as RepositoryResult.Success).value
            .any { it.id == created.id })
        assertTrue((reviews.getPublicReviews(ReviewTarget.Dish(dish.id)) as RepositoryResult.Success).value
            .any { it.id == created.id })
    }
}

private fun localConfig(): SupabaseConfig? {
    val url = System.getenv("SHAREAT_SUPABASE_URL") ?: return null
    val key = System.getenv("SHAREAT_SUPABASE_PUBLISHABLE_KEY") ?: return null
    require(url.contains("localhost") || url.contains("127.0.0.1")) {
        "E2E tests may only run against local Supabase"
    }
    return SupabaseConfig(url, key)
}
