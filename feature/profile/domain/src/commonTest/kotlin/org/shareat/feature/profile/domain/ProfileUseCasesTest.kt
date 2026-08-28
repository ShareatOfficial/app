package org.shareat.feature.profile.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ProfileUseCasesTest {
    @Test
    fun loadsCustomerSettingsFromCurrentSession() = runTest {
        val dependencies = testDependencies(AccountRole.Customer)

        val result = LoadProfileSettingsUseCaseImpl(
            dependencies.auth,
            dependencies.accounts,
            dependencies.restaurants,
        )()

        val settings = assertIs<RepositoryResult.Success<ProfileSettings>>(result).value
        assertEquals("Ana Rivera", assertIs<ProfileSettings.User>(settings).profile.displayName)
    }

    @Test
    fun loadsRestaurantSettingsForRestaurantOwner() = runTest {
        val dependencies = testDependencies(AccountRole.Restaurant)

        val result = LoadProfileSettingsUseCaseImpl(
            dependencies.auth,
            dependencies.accounts,
            dependencies.restaurants,
        )()

        val settings = assertIs<RepositoryResult.Success<ProfileSettings>>(result).value
        assertEquals("Casa Naranja", assertIs<ProfileSettings.RestaurantOwner>(settings).restaurant.name)
    }

    @Test
    fun returnsUnauthenticatedWhenThereIsNoSession() = runTest {
        val dependencies = testDependencies(AccountRole.Customer)
        dependencies.auth.session = null

        val result = LoadProfileSettingsUseCaseImpl(
            dependencies.auth,
            dependencies.accounts,
            dependencies.restaurants,
        )()

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Unauthenticated),
            result,
        )
    }

    @Test
    fun updatesEditableRestaurantInfoAndPreservesNonEditableFields() = runTest {
        val dependencies = testDependencies(AccountRole.Restaurant)
        val original = dependencies.restaurant
        val result = UpdateRestaurantInfoUseCaseImpl(dependencies.restaurants)(
            UpdateRestaurantInfoParams(
                restaurantId = original.id,
                name = "Updated name",
                description = null,
                publicEmail = null,
                publicPhone = null,
                address = original.address.copy(locality = "Madrid"),
                openingHours = original.openingHours,
                publicationState = RestaurantPublicationState.Published,
            ),
        )

        val updated = assertIs<RepositoryResult.Success<Restaurant>>(result).value
        assertEquals("Updated name", updated.name)
        assertEquals("Madrid", updated.address.locality)
        assertEquals(original.ownerAccountId, updated.ownerAccountId)
        assertEquals(original.heroImage, updated.heroImage)
    }

    @Test
    fun signOutDelegatesToAuthRepository() = runTest {
        val auth = testDependencies(AccountRole.Customer).auth

        val result = SignOutUseCaseImpl(auth)()

        assertIs<RepositoryResult.Success<Unit>>(result)
        assertEquals(1, auth.signOutCalls)
    }
}

private data class TestDependencies(
    val auth: TestAuthRepository,
    val accounts: TestAccountRepository,
    val restaurants: TestRestaurantRepository,
    val restaurant: Restaurant,
)

private fun testDependencies(role: AccountRole): TestDependencies {
    val accountId = AccountId("account-id")
    val account = Account(
        id = accountId,
        loginEmail = EmailAddress("owner@example.com"),
        role = role,
        status = AccountStatus.Active,
    )
    val restaurant = Restaurant(
        id = RestaurantId("restaurant-id"),
        ownerAccountId = accountId,
        name = "Casa Naranja",
        heroImage = ImageRef("https://example.com/restaurant.jpg"),
        address = PostalAddress("Calle Mayor 1", "Valencia", "46001"),
        openingHours = WeeklyOpeningHours(emptyList()),
        publicationState = RestaurantPublicationState.Draft,
    )
    return TestDependencies(
        auth = TestAuthRepository(AuthSession(accountId, account.loginEmail)),
        accounts = TestAccountRepository(
            account,
            CustomerProfile(accountId, "Ana Rivera"),
        ),
        restaurants = TestRestaurantRepository(restaurant),
        restaurant = restaurant,
    )
}

private class TestAuthRepository(
    var session: AuthSession?,
) : AuthRepository {
    var signOutCalls = 0

    override fun observeSession(): Flow<AuthSessionState> = flowOf(
        session?.let(AuthSessionState::Authenticated) ?: AuthSessionState.Unauthenticated,
    )
    override suspend fun currentSession() = RepositoryResult.Success(session)
    override suspend fun register(
        credentials: RegistrationCredentials,
    ): RepositoryResult<AuthSession> = unavailable()
    override suspend fun signIn(
        email: EmailAddress,
        password: String,
    ): RepositoryResult<AuthSession> = unavailable()
    override suspend fun signOut(): RepositoryResult<Unit> {
        signOutCalls += 1
        return RepositoryResult.Success(Unit)
    }
    override suspend fun requestPasswordReset(
        email: EmailAddress,
    ): RepositoryResult<Unit> = unavailable()
    override suspend fun updatePassword(password: String): RepositoryResult<Unit> = unavailable()
}

private class TestAccountRepository(
    private val account: Account,
    private var profile: CustomerProfile,
) : AccountRepository {
    override suspend fun getAccount(id: AccountId) = RepositoryResult.Success(account)
    override suspend fun getCustomerProfile(accountId: AccountId) = RepositoryResult.Success(profile)
    override suspend fun updateCustomerProfile(
        profile: CustomerProfile,
    ): RepositoryResult<CustomerProfile> {
        this.profile = profile
        return RepositoryResult.Success(profile)
    }
}

private class TestRestaurantRepository(
    private var restaurant: Restaurant,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants() = RepositoryResult.Success(listOf(restaurant))
    override suspend fun getRestaurant(id: RestaurantId) = RepositoryResult.Success(restaurant)
    override suspend fun getRestaurantForOwner(accountId: AccountId) = RepositoryResult.Success(restaurant)
    override suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant> {
        this.restaurant = restaurant
        return RepositoryResult.Success(restaurant)
    }
}

private fun <T> unavailable(): RepositoryResult<T> =
    RepositoryResult.Failure(RepositoryError.Unavailable())
