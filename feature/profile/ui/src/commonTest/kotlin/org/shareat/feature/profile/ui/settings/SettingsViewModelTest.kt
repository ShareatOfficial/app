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
import org.shareat.feature.profile.domain.UpdateRestaurantInfoParams
import org.shareat.feature.profile.domain.UpdateRestaurantInfoUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

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
    fun loadsUserSettingsFromDomainModel() = runTest(dispatcher) {
        val accountId = AccountId("customer-id")
        val account = Account(
            accountId,
            EmailAddress("ana@example.com"),
            AccountRole.Customer,
            AccountStatus.Active,
        )
        val viewModel = SettingsViewModel(
            loadProfileSettingsUseCase = {
                RepositoryResult.Success(
                    ProfileSettings.User(account, CustomerProfile(accountId, "Ana Rivera")),
                )
            },
            updateRestaurantInfoUseCase = {
                error("Restaurant update must not run for user settings")
            },
            signOutUseCase = { RepositoryResult.Success(Unit) },
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
        assertTrue(assertIs<SettingsUiState.Restaurant>(viewModel.uiState.value).errorMessage != null)
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
    fun editProfileEmitsNavigationEvent() = runTest(dispatcher) {
        val viewModel = viewModelFor(restaurantFixture())
        advanceUntilIdle()

        viewModel.onUserAction(SettingsUserAction.EditProfile)

        assertEquals(SettingsEvent.NavigateToEditProfile, viewModel.events.first())
    }
}

private fun viewModelFor(
    restaurant: org.shareat.app.domain.model.Restaurant,
    update: UpdateRestaurantInfoUseCase = UpdateRestaurantInfoUseCase {
        RepositoryResult.Success(restaurant)
    },
    signOut: SignOutUseCase = SignOutUseCase { RepositoryResult.Success(Unit) },
): SettingsViewModel {
    val account = Account(
        AccountId("owner-id"),
        EmailAddress("owner@example.com"),
        AccountRole.Restaurant,
        AccountStatus.Active,
    )
    return SettingsViewModel(
        loadProfileSettingsUseCase = {
            RepositoryResult.Success(ProfileSettings.RestaurantOwner(account, restaurant))
        },
        updateRestaurantInfoUseCase = update,
        signOutUseCase = signOut,
    )
}
