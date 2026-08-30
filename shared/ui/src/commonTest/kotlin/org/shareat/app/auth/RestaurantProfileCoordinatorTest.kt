package org.shareat.app.auth

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.TestScope
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.navigation.NavigationState
import org.shareat.app.navigation.Navigator
import org.shareat.app.navigation.login.LoginNavigationImpl
import org.shareat.feature.home.ui.navigation.HomeKey
import org.shareat.feature.login.ui.LoginKey
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingKey
import org.shareat.feature.profile.ui.profile.ProfileKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantProfileCoordinatorTest {
    @Test
    fun restaurantWithoutProfileRequiresOnboardingUntilItCompletes() = runTest {
        val fixture = fixture(restaurantResult = RepositoryResult.Failure(RepositoryError.NotFound("restaurant", "owner")))
        runCurrent()

        assertIs<RestaurantProfileGateState.OnboardingRequired>(fixture.gate.state.value)
        fixture.gate.completeOnboarding()
        assertIs<RestaurantProfileGateState.Allowed>(fixture.gate.state.value)
    }

    @Test
    fun serverFailureIsRetryableAndNeverTreatedAsMissingProfile() = runTest {
        val restaurants = GateRestaurantRepository(RepositoryResult.Failure(RepositoryError.Offline))
        val fixture = fixture(restaurants = restaurants)
        runCurrent()

        assertIs<RestaurantProfileGateState.Failure>(fixture.gate.state.value)
        restaurants.result = RepositoryResult.Failure(RepositoryError.NotFound("restaurant", "owner"))
        fixture.gate.retry()
        runCurrent()

        assertIs<RestaurantProfileGateState.OnboardingRequired>(fixture.gate.state.value)
    }

    @Test
    fun customerAndRestaurantWithProfileBypassOnboarding() = runTest {
        val customer = fixture(role = AccountRole.Customer)
        runCurrent()
        assertIs<RestaurantProfileGateState.Allowed>(customer.gate.state.value)

        val owner = fixture(restaurantResult = RepositoryResult.Success(restaurant()))
        runCurrent()
        assertIs<RestaurantProfileGateState.Allowed>(owner.gate.state.value)
    }

    @Test
    fun logoutExitsTheGate() = runTest {
        val fixture = fixture(restaurantResult = RepositoryResult.Failure(RepositoryError.NotFound("restaurant", "owner")))
        runCurrent()
        fixture.gate.signOut()
        runCurrent()

        assertIs<RestaurantProfileGateState.Allowed>(fixture.gate.state.value)
    }

    @Test
    fun restaurantRegistrationNavigatesDirectlyToOnboarding() = runTest {
        val fixture = fixture(role = AccountRole.Customer)
        runCurrent()
        fixture.navigationState.backStacks.getValue(HomeKey).add(LoginKey())

        LoginNavigationImpl(fixture.navigator).onRestaurantRegistrationSuccess()

        assertEquals(
            RestaurantOnboardingKey,
            fixture.navigationState.backStacks.getValue(HomeKey).last(),
        )
    }

    private fun TestScope.fixture(
        role: AccountRole = AccountRole.Restaurant,
        restaurantResult: RepositoryResult<Restaurant> = RepositoryResult.Failure(
            RepositoryError.NotFound("restaurant", "owner"),
        ),
        restaurants: GateRestaurantRepository = GateRestaurantRepository(restaurantResult),
    ): GateFixture {
        val account = Account(
            id = AccountId("owner"),
            loginEmail = EmailAddress("owner@example.test"),
            role = role,
            status = AccountStatus.Active,
        )
        val auth = GateAuthRepository(account)
        val sessions = SessionCoordinator(auth, backgroundScope)
        val gate = RestaurantProfileCoordinator(
            sessions = sessions,
            auth = auth,
            accounts = GateAccountRepository(account),
            restaurants = restaurants,
            scope = backgroundScope,
        )
        val navigationState = NavigationState(
            startRoute = HomeKey,
            topLevelRoute = mutableStateOf(HomeKey),
            backStacks = mapOf(
                HomeKey to NavBackStack(HomeKey),
                ProfileKey to NavBackStack(ProfileKey),
            ),
        )
        return GateFixture(
            gate,
            navigationState,
            Navigator(navigationState, sessions),
        )
    }
}

private data class GateFixture(
    val gate: RestaurantProfileCoordinator,
    val navigationState: NavigationState,
    val navigator: Navigator,
)

private class GateAuthRepository(account: Account) : AuthRepository {
    private val session = AuthSession(account.id, account.loginEmail)
    private val states = MutableStateFlow<AuthSessionState>(AuthSessionState.Authenticated(session))

    override fun observeSession() = states
    override suspend fun currentSession() = (states.value as? AuthSessionState.Authenticated)
        ?.session?.let { RepositoryResult.Success(it) }
        ?: RepositoryResult.Failure(RepositoryError.Unauthenticated)
    override suspend fun register(credentials: RegistrationCredentials) = unavailable<AuthSession>()
    override suspend fun signIn(email: EmailAddress, password: String) = unavailable<AuthSession>()
    override suspend fun signOut(): RepositoryResult<Unit> {
        states.value = AuthSessionState.Unauthenticated
        return RepositoryResult.Success(Unit)
    }
    override suspend fun requestPasswordReset(email: EmailAddress) = unavailable<Unit>()
    override suspend fun updatePassword(password: String) = unavailable<Unit>()
}

private class GateAccountRepository(private val account: Account) : AccountRepository {
    override suspend fun getAccount(id: AccountId) = RepositoryResult.Success(account)
    override suspend fun getCustomerProfile(accountId: AccountId) = unavailable<CustomerProfile>()
    override suspend fun updateCustomerProfile(profile: CustomerProfile) = unavailable<CustomerProfile>()
}

private class GateRestaurantRepository(
    var result: RepositoryResult<Restaurant>,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants() = RepositoryResult.Success(emptyList<Restaurant>())
    override suspend fun getRestaurant(id: RestaurantId) = result
    override suspend fun getRestaurantForOwner(accountId: AccountId) = result
    override suspend fun createRestaurantProfile(ownerAccountId: AccountId, draft: RestaurantProfileDraft) = result
    override suspend fun updateRestaurant(restaurant: Restaurant) = RepositoryResult.Success(restaurant)
}

private fun restaurant(): Restaurant = Restaurant(
    id = RestaurantId("restaurant"),
    ownerAccountId = AccountId("owner"),
    name = "Restaurante",
    address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
    openingHours = WeeklyOpeningHours(emptyList()),
    publicationState = RestaurantPublicationState.Draft,
)

private fun <T> unavailable(): RepositoryResult<T> =
    RepositoryResult.Failure(RepositoryError.Unavailable())
