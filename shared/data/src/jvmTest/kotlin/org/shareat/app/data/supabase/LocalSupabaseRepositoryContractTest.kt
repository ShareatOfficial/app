package org.shareat.app.data.supabase

import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.repository.RepositoryResult

class LocalSupabaseRepositoryContractTest {
    @Test
    fun anonymousCatalogueAndAuthUseTheRealLocalStack() = runBlocking {
        val url = System.getenv("SHAREAT_SUPABASE_URL") ?: return@runBlocking
        val key = System.getenv("SHAREAT_SUPABASE_PUBLISHABLE_KEY") ?: return@runBlocking
        val client = createShareatSupabaseClient(SupabaseConfig(url, key), secureSessionStorage = null)
        val restaurants = SupabaseRestaurantRepository(client).getPublishedRestaurants()
        val restaurant = (assertIs<RepositoryResult.Success<*>>(restaurants).value as List<*>)
            .single() as Restaurant
        assertEquals("Local Shareat Kitchen", restaurant.name)

        val summaryResult = SupabaseReviewRepository(client)
            .getRatingSummary(ReviewTarget.Restaurant(restaurant.id))
        val summary = assertIs<RepositoryResult.Success<*>>(summaryResult).value as RatingSummary
        assertEquals(50, summary.averageTenths)
        assertEquals(1, summary.ratingCount)

        val auth = SupabaseAuthRepository(client)
        val email = EmailAddress("integration-${System.currentTimeMillis()}@shareat.test")
        val registered = auth.register(
            RegistrationCredentials(email, "integration-password", AccountRole.Customer),
        )
        val session = assertIs<RepositoryResult.Success<*>>(registered).value as AuthSession
        assertEquals(email, session.email)

        val accountResult = SupabaseAccountRepository(client).getAccount(session.accountId)
        val account = assertIs<RepositoryResult.Success<*>>(accountResult).value as Account
        assertEquals(AccountRole.Customer, account.role)
        assertIs<RepositoryResult.Success<*>>(auth.signOut())
    }
}
