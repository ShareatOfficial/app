package org.shareat.feature.profile.ui.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.SignOutUseCase
import org.shareat.feature.profile.domain.RequestAccountDeletionUseCase
import org.shareat.feature.profile.domain.UpdateRestaurantInfoParams
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadsGuestSettingsWithoutAccountData() = runTest(dispatcher) {
        val selectedLanguage = MutableStateFlow(AppLanguage.Spanish)
        val viewModel = SettingsViewModel(
            authRepository = TestSettingsAuthRepository(
                MutableStateFlow(AuthSessionState.Unauthenticated),
            ),
            loadProfileSettingsUseCase = {
                RepositoryResult.Success(ProfileSettings.Guest)
            },
            updateRestaurantInfoUseCase = {
                error("Restaurant update must not run for guest settings")
            },
            signOutUseCase = {
                error("Sign out must not run for guest settings")
            },
            observeAppLanguageUseCase = { selectedLanguage },
            getAppLanguageSupportUseCase = { AppLanguageSelectionSupport.IMMEDIATE },
            selectAppLanguageUseCase = { selectedLanguage.value = it },
            requestAccountDeletionUseCase = { error("Deletion must not run for guest settings") },
        )

        advanceUntilIdle()

        val state = assertIs<SettingsUiState.Guest>(viewModel.uiState.value)
        assertFalse(state.isLoading)
        assertEquals(AppLanguage.Spanish, state.language.selected)
    }

    @Test
    fun loadsUserSettingsFromDomainModel() = runTest(dispatcher) {
        val accountId = AccountId("customer-id")
        val account = Account(
            accountId,
            EmailAddress("ana@example.com"),
            AccountRole.Customer,
            AccountStatus.Active,
        )
        val viewModel = SettingsViewModel(
            authRepository = authenticatedAuth(account),
            loadProfileSettingsUseCase = {
                RepositoryResult.Success(
                    ProfileSettings.User(account, CustomerProfile(accountId, "Ana Rivera")),
                )
            },
            updateRestaurantInfoUseCase = {
                error("Restaurant update must not run for user settings")
            },
            signOutUseCase = { RepositoryResult.Success(Unit) },
            observeAppLanguageUseCase = { MutableStateFlow(AppLanguage.System) },
            getAppLanguageSupportUseCase = { AppLanguageSelectionSupport.IMMEDIATE },
            selectAppLanguageUseCase = {},
            requestAccountDeletionUseCase = { RepositoryResult.Success(Unit) },
        )

        advanceUntilIdle()

        val state = assertIs<SettingsUiState.User>(viewModel.uiState.value)
        assertEquals("Ana Rivera", state.name)
        assertEquals("AR", state.initials)
    }

    @Test
    fun loadsRestaurantAndDispatchesFieldActions() = runTest(dispatcher) {
        val restaurant = restaurantFixture()
        val viewModel = viewModelFor(restaurant)
        advanceUntilIdle()

        viewModel.onRestaurantAction(SettingsRestaurantAction.NameChanged("New name"))

        val state = assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value)
        assertEquals("New name", state.name)
    }

    @Test
    fun reloadsAccountSettingsWhenAGuestLogsIn() = runTest(dispatcher) {
        val accountId = AccountId("customer-id")
        val account = Account(
            accountId,
            EmailAddress("ana@example.com"),
            AccountRole.Customer,
            AccountStatus.Active,
        )
        val sessionStates = MutableStateFlow<AuthSessionState>(AuthSessionState.Unauthenticated)
        val viewModel = SettingsViewModel(
            authRepository = TestSettingsAuthRepository(sessionStates),
            loadProfileSettingsUseCase = {
                RepositoryResult.Success(
                    ProfileSettings.User(account, CustomerProfile(accountId, "Ana Rivera")),
                )
            },
            updateRestaurantInfoUseCase = {
                error("Restaurant update must not run for user settings")
            },
            signOutUseCase = { RepositoryResult.Success(Unit) },
            observeAppLanguageUseCase = { MutableStateFlow(AppLanguage.System) },
            getAppLanguageSupportUseCase = { AppLanguageSelectionSupport.IMMEDIATE },
            selectAppLanguageUseCase = {},
            requestAccountDeletionUseCase = { error("Deletion must not run during login") },
        )
        advanceUntilIdle()
        assertIs<SettingsUiState.Guest>(viewModel.uiState.value)

        sessionStates.value = AuthSessionState.Authenticated(
            AuthSession(account.id, account.loginEmail),
        )
        advanceUntilIdle()

        assertEquals(
            "Ana Rivera",
            assertIs<SettingsUiState.User>(viewModel.uiState.value).name,
        )
    }

    @Test
    fun appliesOpeningHoursOnlyWhenTheSheetCommitsItsDraft() = runTest(dispatcher) {
        val viewModel = viewModelFor(restaurantFixture())
        advanceUntilIdle()
        val original = assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value).openingHours
        val changed = original.map { hours ->
            if (hours.day == OpeningDay.Monday) hours.copy(isOpen = false) else hours
        }

        viewModel.onRestaurantAction(SettingsRestaurantAction.OpeningHoursChanged(changed))

        val state = assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value)
        assertEquals(false, state.openingHours.first { it.day == OpeningDay.Monday }.isOpen)
    }

    @Test
    fun saveMapsStateAndInvokesUpdateUseCase() = runTest(dispatcher) {
        val restaurant = restaurantFixture()
        var received: UpdateRestaurantInfoParams? = null
        val viewModel = viewModelFor(
            restaurant = restaurant,
            update = { params ->
                received = params
                RepositoryResult.Success(restaurant.copy(name = params.name))
            },
        )
        advanceUntilIdle()
        viewModel.onRestaurantAction(SettingsRestaurantAction.NameChanged("Saved name"))

        viewModel.onRestaurantAction(SettingsRestaurantAction.SaveChanges)
        advanceUntilIdle()

        assertEquals("Saved name", received?.name)
        assertTrue(assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value).saveSucceeded)
    }

    @Test
    fun invalidFormDoesNotInvokeUpdateUseCase() = runTest(dispatcher) {
        val restaurant = restaurantFixture()
        var updates = 0
        val viewModel = viewModelFor(
            restaurant = restaurant,
            update = {
                updates += 1
                RepositoryResult.Success(restaurant)
            },
        )
        advanceUntilIdle()
        viewModel.onRestaurantAction(SettingsRestaurantAction.EmailChanged("invalid"))

        viewModel.onRestaurantAction(SettingsRestaurantAction.SaveChanges)
        advanceUntilIdle()

        assertEquals(0, updates)
        assertTrue(assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value).error != null)
    }

    @Test
    fun logOutInvokesSignOutUseCase() = runTest(dispatcher) {
        val restaurant = restaurantFixture()
        var signOutCalls = 0
        val viewModel = viewModelFor(
            restaurant = restaurant,
            signOut = SignOutUseCase {
                signOutCalls += 1
                RepositoryResult.Success(Unit)
            },
        )
        advanceUntilIdle()

        viewModel.onRestaurantAction(SettingsRestaurantAction.LogOut)
        advanceUntilIdle()

        assertEquals(1, signOutCalls)
        assertEquals(SettingsEvent.LogoutSuccess, viewModel.events.first())
    }

    @Test
    fun deletionRequestIsSentOnceAndConfirmed() = runTest(dispatcher) {
        var requests = 0
        val viewModel = viewModelFor(
            restaurant = restaurantFixture(),
            requestDeletion = RequestAccountDeletionUseCase {
                requests += 1
                RepositoryResult.Success(Unit)
            },
        )
        advanceUntilIdle()

        viewModel.onRestaurantAction(SettingsRestaurantAction.RequestDeletion)
        viewModel.onRestaurantAction(SettingsRestaurantAction.RequestDeletion)
        advanceUntilIdle()

        assertEquals(1, requests)
        assertEquals(SettingsEvent.DeletionRequested, viewModel.events.first())
    }

    @Test
    fun editProfileEmitsNavigationEvent() = runTest(dispatcher) {
        val viewModel = viewModelFor(restaurantFixture())
        advanceUntilIdle()

        viewModel.onUserAction(SettingsUserAction.EditProfile)

        assertEquals(SettingsEvent.NavigateToEditProfile, viewModel.events.first())
    }

    @Test
    fun theSelectedLanguageAndItsPlatformSupportReachTheUiState() = runTest(dispatcher) {
        val viewModel = viewModelFor(
            restaurantFixture(),
            selectedLanguage = MutableStateFlow(AppLanguage.Spanish),
        )

        advanceUntilIdle()

        val state = assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value)
        assertEquals(AppLanguage.Spanish, state.language.selected)
        assertEquals(AppLanguageSelectionSupport.IMMEDIATE, state.language.support)
    }

    @Test
    fun choosingALanguagePublishesItWithoutDroppingTheLoadedRestaurant() = runTest(dispatcher) {
        val viewModel = viewModelFor(restaurantFixture())
        advanceUntilIdle()

        viewModel.onLanguageAction(SettingsLanguageAction(AppLanguage.English))
        advanceUntilIdle()

        val state = assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value)
        assertEquals(AppLanguage.English, state.language.selected)
        assertEquals("Casa Naranja", state.name)
    }
}

private fun viewModelFor(
    restaurant: org.shareat.app.domain.model.Restaurant,
    update: UpdateRestaurantInfoUseCase = UpdateRestaurantInfoUseCase {
        RepositoryResult.Success(restaurant)
    },
    signOut: SignOutUseCase = SignOutUseCase { RepositoryResult.Success(Unit) },
    requestDeletion: RequestAccountDeletionUseCase = RequestAccountDeletionUseCase {
        RepositoryResult.Success(Unit)
    },
    selectedLanguage: MutableStateFlow<AppLanguage> = MutableStateFlow(AppLanguage.System),
): SettingsViewModel {
    val account = Account(
        AccountId("owner-id"),
        EmailAddress("owner@example.com"),
        AccountRole.Restaurant,
        AccountStatus.Active,
    )
    return SettingsViewModel(
        authRepository = authenticatedAuth(account),
        loadProfileSettingsUseCase = {
            RepositoryResult.Success(ProfileSettings.RestaurantOwner(account, restaurant))
        },
        updateRestaurantInfoUseCase = update,
        signOutUseCase = signOut,
        observeAppLanguageUseCase = { selectedLanguage },
        getAppLanguageSupportUseCase = { AppLanguageSelectionSupport.IMMEDIATE },
        selectAppLanguageUseCase = { selectedLanguage.value = it },
        requestAccountDeletionUseCase = requestDeletion,
    )
}

private fun authenticatedAuth(account: Account) = TestSettingsAuthRepository(
    MutableStateFlow(
        AuthSessionState.Authenticated(AuthSession(account.id, account.loginEmail)),
    ),
)

private class TestSettingsAuthRepository(
    private val sessionStates: MutableStateFlow<AuthSessionState>,
) : AuthRepository {
    override fun observeSession() = sessionStates

    override suspend fun currentSession(): RepositoryResult<AuthSession?> =
        RepositoryResult.Success(
            (sessionStates.value as? AuthSessionState.Authenticated)?.session,
        )

    override suspend fun register(credentials: RegistrationCredentials): RepositoryResult<AuthSession> =
        error("Registration is not used by settings tests")

    override suspend fun signIn(
        email: EmailAddress,
        password: String,
    ): RepositoryResult<AuthSession> = error("Sign in is not used by settings tests")

    override suspend fun signOut(): RepositoryResult<Unit> =
        error("Repository sign out is not used by settings tests")

    override suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit> =
        error("Password reset is not used by settings tests")

    override suspend fun updatePassword(password: String): RepositoryResult<Unit> =
        error("Password update is not used by settings tests")
}
