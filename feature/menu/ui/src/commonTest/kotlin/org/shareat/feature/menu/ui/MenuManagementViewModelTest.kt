package org.shareat.feature.menu.ui

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.menu.domain.ArchiveDishUseCase
import org.shareat.feature.menu.domain.DeleteDishUseCase
import org.shareat.feature.menu.domain.DeleteMenuUseCase
import org.shareat.feature.menu.domain.LoadMenuManagementUseCase
import org.shareat.feature.menu.domain.MenuManagementData
import org.shareat.feature.menu.domain.SaveDishUseCase
import org.shareat.feature.menu.domain.SaveRestaurantMenuUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class MenuManagementViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun publishSavesAnEnabledDishWithItsEurPrice() = runTest(dispatcher) {
        val restaurant = Restaurant(RestaurantId("restaurant"), AccountId("owner"), "Casa", address = PostalAddress("Street 1", "Madrid", "28001"), openingHours = WeeklyOpeningHours(emptyList()), publicationState = RestaurantPublicationState.Draft)
        val dish = Dish(DishId("dish"), restaurant.id, "Soup", isEnabled = true)
        var savedState: MenuPublicationState? = null
        var savedPrice: Long? = null
        val vm = MenuManagementViewModel(
            load = LoadMenuManagementUseCase { RepositoryResult.Success(MenuManagementData(restaurant, null, listOf(dish))) },
            saveMenu = SaveRestaurantMenuUseCase { draft ->
                savedState = draft.publicationState; savedPrice = draft.items.single().price.minorUnits
                RepositoryResult.Success(MenuDetails(Menu(MenuId("menu"), restaurant.id, draft.name, draft.description, draft.publicationState), emptyList()))
            },
            saveDish = SaveDishUseCase { error("not used") },
            archiveDish = ArchiveDishUseCase { error("not used") },
            deleteDish = DeleteDishUseCase { error("not used") },
            deleteMenu = DeleteMenuUseCase { error("not used") },
            images = object : ImageRepository {
                override suspend fun replaceImage(target: org.shareat.app.domain.model.ImageTarget, upload: org.shareat.app.domain.model.ImageUpload) = error("not used")
                override suspend fun deleteImage(target: org.shareat.app.domain.model.ImageTarget) = error("not used")
            },
            imageProcessor = DishImageProcessor { _, _ -> error("not used") },
        )
        advanceUntilIdle()

        vm.onAction(MenuManagementAction.NameChanged("Lunch"))
        vm.onAction(MenuManagementAction.AddDish(dish.id))
        vm.onAction(MenuManagementAction.PriceChanged(dish.id, "12,50"))
        vm.onAction(MenuManagementAction.Publish)
        advanceUntilIdle()

        assertEquals(MenuPublicationState.Published, savedState)
        assertEquals(1_250L, savedPrice)
        assertIs<MenuManagementUiState.Editor>(vm.uiState.value)
    }
}
