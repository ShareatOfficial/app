package org.shareat.feature.profile.ui.di

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
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
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.feature.profile.ui.profile.ProfileNavigation
import org.shareat.feature.profile.ui.onboarding.RestaurantOnboardingNavigation
import org.shareat.feature.profile.ui.editprofile.EditProfileNavigation
import org.shareat.feature.profile.ui.editprofile.EditProfileUiState
import org.shareat.feature.profile.ui.editprofile.EditProfileViewModel
import org.shareat.feature.profile.ui.settings.SettingsNavigation
import org.shareat.feature.profile.ui.settings.SettingsUiState
import org.shareat.feature.profile.ui.settings.SettingsViewModel
import org.shareat.feature.profile.ui.settings.restaurantFixture
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileUiModuleTest {
    private val dispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    @Test
    fun resolvesProfileViewModelsWithUseCases() {
        val restaurant = restaurantFixture()
        val account = Account(
            AccountId("owner-id"),
            EmailAddress("owner@example.com"),
            AccountRole.Restaurant,
            AccountStatus.Active,
        )
        val app = startKoin {
            modules(
                module {
                    single<AuthRepository> { WiringAuthRepository(account) }
                    single<AccountRepository> { WiringAccountRepository(account) }
                    single<RestaurantRepository> { WiringRestaurantRepository(restaurant) }
                    single<ProfileNavigation> { WiringProfileNavigation }
                    single<EditProfileNavigation> { WiringEditProfileNavigation }
                    single<SettingsNavigation> { WiringSettingsNavigation }
                    single<RestaurantOnboardingNavigation> { WiringRestaurantOnboardingNavigation }
                },
                profileUiModule,
            )
        }

        assertIs<SettingsUiState.Restaurant>(
            app.koin.get<SettingsViewModel>().uiState.value,
        )
        assertIs<EditProfileUiState>(app.koin.get<EditProfileViewModel>().uiState.value)
    }
}

private data object WiringProfileNavigation : ProfileNavigation {
    override fun openSettings() = Unit
}

private data object WiringEditProfileNavigation : EditProfileNavigation {
    override fun goBack() = Unit
}

private data object WiringSettingsNavigation : SettingsNavigation {
    override fun goBack() = Unit
    override fun openEditProfile() = Unit
    override fun openMenuManagement() = Unit
    override fun onLogoutSuccess() = Unit
}

private data object WiringRestaurantOnboardingNavigation : RestaurantOnboardingNavigation {
    override fun onCompleted() = Unit
    override fun onLogoutSuccess() = Unit
}

private class WiringAuthRepository(account: Account) : AuthRepository {
    private val session = AuthSession(account.id, account.loginEmail)
    override fun observeSession() = flowOf(AuthSessionState.Authenticated(session))
    override suspend fun currentSession() = RepositoryResult.Success(session)
    override suspend fun register(credentials: RegistrationCredentials) = unavailable<AuthSession>()
    override suspend fun signIn(email: EmailAddress, password: String) = unavailable<AuthSession>()
    override suspend fun signOut() = RepositoryResult.Success(Unit)
    override suspend fun requestPasswordReset(email: EmailAddress) = unavailable<Unit>()
    override suspend fun updatePassword(password: String) = unavailable<Unit>()
}

private class WiringAccountRepository(
    private val account: Account,
) : AccountRepository {
    override suspend fun getAccount(id: AccountId) = RepositoryResult.Success(account)
    override suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile> =
        unavailable()
    override suspend fun updateCustomerProfile(
        profile: CustomerProfile,
    ): RepositoryResult<CustomerProfile> = unavailable()
}

private class WiringRestaurantRepository(
    private var restaurant: Restaurant,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants() = RepositoryResult.Success(listOf(restaurant))
    override suspend fun getRestaurant(id: RestaurantId) = RepositoryResult.Success(restaurant)
    override suspend fun getRestaurantForOwner(accountId: AccountId) = RepositoryResult.Success(restaurant)
    override suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ) = RepositoryResult.Success(restaurant.copy(name = draft.name))
    override suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant> {
        this.restaurant = restaurant
        return RepositoryResult.Success(restaurant)
    }
}

private fun <T> unavailable(): RepositoryResult<T> =
    RepositoryResult.Failure(RepositoryError.Unavailable())
