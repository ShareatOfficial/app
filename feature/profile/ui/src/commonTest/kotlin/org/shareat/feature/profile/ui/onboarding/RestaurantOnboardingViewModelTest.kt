package org.shareat.feature.profile.ui.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.CreateRestaurantProfileUseCase
import org.shareat.feature.profile.domain.SignOutUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantOnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun allDaysAreOpenByDefault() {
        assertTrue(RestaurantOnboardingUiState().hours.all { it.enabled })
    }

    @Test
    fun mandatoryValidationPreventsSubmission() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            RepositoryResult.Success(restaurantFixture())
        })

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals(0, creates)
        assertNotNull(viewModel.uiState.value.errors.name)
        assertNotNull(viewModel.uiState.value.errors.street)
        assertNotNull(viewModel.uiState.value.errors.city)
        assertNotNull(viewModel.uiState.value.errors.postcode)
    }

    @Test
    fun optionalFieldsAndHoursAreMapped() = runTest(dispatcher) {
        var received: RestaurantProfileDraft? = null
        val viewModel = viewModel(create = {
            received = it
            RepositoryResult.Success(restaurantFixture().copy(name = it.name))
        })
        fillRequired(viewModel)
        viewModel.onAction(RestaurantOnboardingAction.DescriptionChanged("  Cocina local  "))
        viewModel.onAction(RestaurantOnboardingAction.EmailChanged("hola@example.com"))

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals("Cocina local", received?.description)
        assertEquals("hola@example.com", received?.publicEmail?.value)
        assertEquals(7, received?.openingHours?.days?.size)
        assertEquals(11, received?.openingHours?.days?.first()?.periods?.single()?.opensAt?.hour)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }

    @Test
    fun invalidEmailAndHoursStayOnForm() = runTest(dispatcher) {
        val viewModel = viewModel()
        fillRequired(viewModel)
        viewModel.onAction(RestaurantOnboardingAction.EmailChanged("invalid"))
        viewModel.onAction(RestaurantOnboardingAction.DayEnabledChanged(org.shareat.app.domain.model.Weekday.Friday, true))
        viewModel.onAction(RestaurantOnboardingAction.ClosesAtChanged(org.shareat.app.domain.model.Weekday.Friday, "11:00"))

        viewModel.onAction(RestaurantOnboardingAction.Submit)

        assertNotNull(viewModel.uiState.value.errors.email)
        assertNotNull(viewModel.uiState.value.hours.first { it.day == org.shareat.app.domain.model.Weekday.Friday }.error)
    }

    @Test
    fun doubleSubmitIsIgnored() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            RepositoryResult.Success(restaurantFixture())
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals(1, creates)
    }

    @Test
    fun offlineCreationCanBeRetried() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            if (creates == 1) RepositoryResult.Failure(RepositoryError.Offline)
            else RepositoryResult.Success(restaurantFixture())
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage?.contains("Sin conexión") == true)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()
        assertEquals(2, creates)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }

    @Test
    fun expiredSessionKeepsTheFormAndShowsRecoveryMessage() = runTest(dispatcher) {
        val viewModel = viewModel(create = {
            RepositoryResult.Failure(RepositoryError.Unauthenticated)
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("sesión ha caducado") == true)
        assertEquals("Casa Nueva", viewModel.uiState.value.name)
    }

}

private fun viewModel(
    create: CreateRestaurantProfileUseCase = CreateRestaurantProfileUseCase {
        RepositoryResult.Success(restaurantFixture().copy(name = it.name))
    },
) = RestaurantOnboardingViewModel(
    createRestaurantProfileUseCase = create,
    signOutUseCase = SignOutUseCase { RepositoryResult.Success(Unit) },
)

private fun fillRequired(viewModel: RestaurantOnboardingViewModel) {
    viewModel.onAction(RestaurantOnboardingAction.NameChanged("Casa Nueva"))
    viewModel.onAction(RestaurantOnboardingAction.StreetChanged("Calle Mayor 1"))
    viewModel.onAction(RestaurantOnboardingAction.CityChanged("Madrid"))
    viewModel.onAction(RestaurantOnboardingAction.PostcodeChanged("28001"))
}

private fun restaurantFixture() = Restaurant(
    id = RestaurantId("restaurant-id"),
    ownerAccountId = AccountId("owner-id"),
    name = "Casa Nueva",
    address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
    openingHours = WeeklyOpeningHours(emptyList()),
    publicationState = RestaurantPublicationState.Draft,
)
