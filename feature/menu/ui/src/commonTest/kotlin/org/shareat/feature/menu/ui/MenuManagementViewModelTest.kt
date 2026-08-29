package org.shareat.feature.menu.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.feature.menu.domain.CreateDraftMenuUseCase
import org.shareat.feature.menu.domain.LoadOwnedRestaurantMenuUseCase
import kotlin.test.Test
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

@OptIn(ExperimentalCoroutinesApi::class)
class MenuManagementViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun blankNameIsRejectedWithoutCreating() = runTest(dispatcher) {
        var creates = 0
        val viewModel = MenuManagementViewModel(
            loadOwnedRestaurantMenu = LoadOwnedRestaurantMenuUseCase {
                RepositoryResult.Failure(RepositoryError.Unavailable())
            },
            createDraftMenu = CreateDraftMenuUseCase {
                creates += 1
                RepositoryResult.Success(menu())
            },
        )
        advanceUntilIdle()

        viewModel.create()

        assertEquals("Introduce el nombre de la carta.", viewModel.uiState.value.nameError)
        assertEquals(0, creates)
    }

    @Test
    fun successfulCreationShowsTheDraftMenu() = runTest(dispatcher) {
        val viewModel = MenuManagementViewModel(
            loadOwnedRestaurantMenu = LoadOwnedRestaurantMenuUseCase {
                RepositoryResult.Failure(RepositoryError.Unavailable())
            },
            createDraftMenu = CreateDraftMenuUseCase { RepositoryResult.Success(menu()) },
        )
        advanceUntilIdle()

        viewModel.onNameChanged("Carta principal")
        viewModel.create()
        advanceUntilIdle()

        assertEquals("Carta principal", viewModel.uiState.value.existingMenu?.name)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun repeatedCreateTapOnlySubmitsOnce() = runTest(dispatcher) {
        var creates = 0
        val viewModel = MenuManagementViewModel(
            loadOwnedRestaurantMenu = LoadOwnedRestaurantMenuUseCase {
                RepositoryResult.Failure(RepositoryError.Unavailable())
            },
            createDraftMenu = CreateDraftMenuUseCase {
                creates += 1
                RepositoryResult.Success(menu())
            },
        )
        advanceUntilIdle()

        viewModel.onNameChanged("Carta principal")
        viewModel.create()
        viewModel.create()
        advanceUntilIdle()

        assertEquals(1, creates)
    }

    private fun menu() = Menu(
        MenuId("menu"), RestaurantId("restaurant"), "Carta principal",
        publicationState = MenuPublicationState.Draft,
    )
}
