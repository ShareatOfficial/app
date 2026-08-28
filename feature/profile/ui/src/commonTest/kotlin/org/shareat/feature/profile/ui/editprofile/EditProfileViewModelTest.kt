package org.shareat.feature.profile.ui.editprofile

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import org.shareat.feature.profile.domain.LoadProfileSettingsUseCase
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.UpdateCustomerProfileParams
import org.shareat.feature.profile.domain.UpdateCustomerProfileUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EditProfileViewModelTest {
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
    fun loadsEditableCustomerProfile() = runTest(dispatcher) {
        val fixture = profileFixture()
        val viewModel = editProfileViewModel(fixture)

        advanceUntilIdle()

        assertEquals("Ana Rivera", viewModel.uiState.value.fullName)
        assertEquals("AnaR", viewModel.uiState.value.displayName)
        assertEquals("ana@example.com", viewModel.uiState.value.email)
        assertEquals(ProfileLanguage.Spanish, viewModel.uiState.value.preferredLanguage)
    }

    @Test
    fun savesEditedProfile() = runTest(dispatcher) {
        val fixture = profileFixture()
        var received: UpdateCustomerProfileParams? = null
        val viewModel = editProfileViewModel(
            fixture = fixture,
            update = UpdateCustomerProfileUseCase { params ->
                received = params
                RepositoryResult.Success(
                    fixture.second.copy(
                        fullName = params.fullName.trim(),
                        displayName = params.displayName.trim(),
                        phoneNumber = params.phoneNumber,
                        preferredLanguage = params.preferredLanguage,
                    ),
                )
            },
        )
        advanceUntilIdle()

        viewModel.onAction(EditProfileAction.DisplayNameChanged("AnaFood"))
        viewModel.onAction(EditProfileAction.Save)
        advanceUntilIdle()

        assertEquals("AnaFood", received?.displayName)
        assertTrue(viewModel.uiState.value.saveSucceeded)
    }
}

private fun profileFixture(): Pair<Account, CustomerProfile> {
    val id = AccountId("customer-id")
    return Account(
        id = id,
        loginEmail = EmailAddress("ana@example.com"),
        role = AccountRole.Customer,
        status = AccountStatus.Active,
    ) to CustomerProfile(
        accountId = id,
        displayName = "AnaR",
        fullName = "Ana Rivera",
        phoneNumber = "+34 600 000 000",
        preferredLanguage = "es-ES",
    )
}

private fun editProfileViewModel(
    fixture: Pair<Account, CustomerProfile>,
    update: UpdateCustomerProfileUseCase = UpdateCustomerProfileUseCase {
        RepositoryResult.Success(fixture.second)
    },
) = EditProfileViewModel(
    loadProfileSettingsUseCase = LoadProfileSettingsUseCase {
        RepositoryResult.Success(ProfileSettings.User(fixture.first, fixture.second))
    },
    updateCustomerProfileUseCase = update,
)
