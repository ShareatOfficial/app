package org.shareat.feature.restauranthome.ui.restauranthome

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.feature.restauranthome.domain.LoadOwnerRestaurantHomeUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerDishImageUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerRestaurantImageUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerRestaurantInfoUseCase
import org.shareat.feature.restauranthome.domain.UpdateRestaurantPublicationStateUseCase
import org.shareat.feature.restauranthome.domain.model.OwnerRatedMenuDish
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantHome
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantMenu
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeEditor
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.toRestaurantHomeData
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantHomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsThePrivateMenuAndTogglesCustomerPreview() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        val loaded = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals(listOf("Arroz", "Tarta"), loaded.restaurant.dishes.map { it.name })

        viewModel.onModeToggle()
        assertEquals(RestaurantHomeMode.CUSTOMER_PREVIEW, viewModel.uiState.value.mode)
    }

    @Test
    fun keepsRestaurantEditorOpenAndMarksInvalidFieldsWithoutSubmitting() = runTest(dispatcher) {
        var updateCalls = 0
        val viewModel = viewModelFor(onUpdateRestaurant = { updateCalls++ })
        advanceUntilIdle()
        viewModel.onEditRestaurantClick()
        viewModel.onRestaurantNameChanged(" ")
        viewModel.onRestaurantStreetChanged(" ")

        viewModel.onSaveRestaurant()

        val form = assertIs<RestaurantHomeEditor.Restaurant>(viewModel.uiState.value.editor).form
        assertTrue(form.validation.nameInvalid)
        assertTrue(form.validation.streetInvalid)
        assertEquals(0, updateCalls)
    }

    @Test
    fun despublishingUsesDraftAndDishRatingsAreMapped() = runTest(dispatcher) {
        var publicationState: RestaurantPublicationState? = null
        val home = ownerHome().let { original ->
            original.copy(menu = original.menu!!.copy(dishes = original.menu.dishes.mapIndexed { index, dish ->
                if (index == 0) dish.copy(reviews = listOf(dishReview(dish.menuDish.dish.id))) else dish
            }))
        }
        val viewModel = viewModelFor(home = home, onPublicationChange = { publicationState = it })
        advanceUntilIdle()

        val mappedDish = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content).restaurant.dishes.first()
        assertEquals("5,0", mappedDish.ratingLabel)
        assertEquals(1, mappedDish.reviewCount)

        viewModel.onRestaurantPublicationChange(false)
        advanceUntilIdle()
        assertEquals(RestaurantPublicationState.Draft, publicationState)
    }

    @Test
    fun publicationSuccessReloadsRestaurantAndMenuState() = runTest(dispatcher) {
        var loadCalls = 0
        val initial = ownerHome()
        val unpublished = initial.copy(
            restaurant = initial.restaurant.copy(publicationState = RestaurantPublicationState.Draft),
            menu = initial.menu!!.copy(
                menu = initial.menu.menu.copy(publicationState = MenuPublicationState.Unpublished),
            ),
        )
        val viewModel = viewModelFor(
            home = initial,
            onLoadHome = {
                loadCalls++
                RepositoryResult.Success(if (loadCalls == 1) initial else unpublished)
            },
        )
        advanceUntilIdle()

        viewModel.onRestaurantPublicationChange(false)
        advanceUntilIdle()

        val loaded = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals(2, loadCalls)
        assertTrue(!loaded.restaurant.isPublished)
    }

    @Test
    fun opensAnEmptyDishEditorForTheAddDishCallToAction() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        viewModel.onAddDishClick()

        val form = assertIs<RestaurantHomeEditor.Dish>(viewModel.uiState.value.editor).form
        assertEquals(null, form.dishId)
        assertEquals("", form.name)
        assertEquals("", form.price)
        assertTrue(!form.isPublished)
    }

    @Test
    fun savesTheCompleteEditableAddressWithoutReplacingUneditedFields() = runTest(dispatcher) {
        var submitted: OwnerRestaurantInfoDraft? = null
        val viewModel = viewModelFor(onRestaurantDraft = { submitted = it })
        advanceUntilIdle()
        viewModel.onEditRestaurantClick()
        viewModel.onRestaurantStreetChanged("Calle Nueva 5")
        viewModel.onRestaurantLocalityChanged("Alcalá de Henares")
        viewModel.onRestaurantPostalCodeChanged("28801")
        viewModel.onRestaurantRegionChanged("Madrid")
        viewModel.onSaveRestaurant()
        advanceUntilIdle()

        assertEquals("Calle Nueva 5", submitted?.address?.streetLine)
        assertEquals("Alcalá de Henares", submitted?.address?.locality)
        assertEquals("28801", submitted?.address?.postalCode)
        assertEquals("Madrid", submitted?.address?.region)
        assertEquals("ES", submitted?.address?.countryCode)
    }

    @Test
    fun imageRetryAfterDishCreationUpdatesThePersistedDishInsteadOfCreatingAnother() = runTest(dispatcher) {
        var createCalls = 0
        var updateCalls = 0
        var imageCalls = 0
        val update = newDishUpdate(ownerHome())
        val viewModel = viewModelFor(
            onCreateDish = {
                createCalls++
                RepositoryResult.Success(update)
            },
            onUpdateDish = { _, _ ->
                updateCalls++
                RepositoryResult.Success(update)
            },
            onReplaceDishImage = { _, _ ->
                imageCalls++
                RepositoryResult.Failure(RepositoryError.Offline)
            },
        )
        advanceUntilIdle()
        viewModel.onAddDishClick()
        viewModel.onDishNameChanged("Croquetas")
        viewModel.onDishPriceChanged("9,50")
        viewModel.onDishImageSelected(ImageUpload(byteArrayOf(1), "image/jpeg", "Croquetas"))

        viewModel.onSaveDish()
        advanceUntilIdle()

        assertEquals(1, createCalls)
        assertEquals("dish-new", assertIs<RestaurantHomeEditor.Dish>(viewModel.uiState.value.editor).form.dishId)

        viewModel.onSaveDish()
        advanceUntilIdle()

        assertEquals(1, createCalls)
        assertEquals(1, updateCalls)
        assertEquals(2, imageCalls)
    }

    private fun viewModelFor(
        home: OwnerRestaurantHome = ownerHome(),
        onLoadHome: (() -> RepositoryResult<OwnerRestaurantHome>)? = null,
        onUpdateRestaurant: () -> Unit = {},
        onRestaurantDraft: (OwnerRestaurantInfoDraft) -> Unit = {},
        onPublicationChange: (RestaurantPublicationState) -> Unit = {},
        onCreateDish: (OwnerDishCreateDraft) -> RepositoryResult<OwnerDishUpdate> = { error("unused") },
        onUpdateDish: (DishId, org.shareat.feature.restauranthome.domain.model.OwnerDishDraft) -> RepositoryResult<OwnerDishUpdate> = { _, _ -> error("unused") },
        onReplaceDishImage: (DishId, ImageUpload) -> RepositoryResult<org.shareat.app.domain.model.ImageRef> = { _, _ -> error("unused") },
    ): RestaurantHomeViewModel {
        return RestaurantHomeViewModel(
            loadOwnerRestaurantHome = LoadOwnerRestaurantHomeUseCase {
                onLoadHome?.invoke() ?: RepositoryResult.Success(home)
            },
            createOwnerDish = CreateOwnerDishUseCase(onCreateDish),
            updateOwnerRestaurantInfo = UpdateOwnerRestaurantInfoUseCase { draft ->
                onUpdateRestaurant()
                onRestaurantDraft(draft)
                RepositoryResult.Success(home.restaurant.copy(name = draft.name, address = draft.address))
            },
            updateRestaurantPublicationState = UpdateRestaurantPublicationStateUseCase { state ->
                onPublicationChange(state)
                RepositoryResult.Success(home.restaurant.copy(publicationState = state))
            },
            replaceOwnerRestaurantImage = ReplaceOwnerRestaurantImageUseCase { error("unused") },
            updateOwnerDish = UpdateOwnerDishUseCase(onUpdateDish),
            replaceOwnerDishImage = ReplaceOwnerDishImageUseCase(onReplaceDishImage),
        )
    }
}

private fun newDishUpdate(home: OwnerRestaurantHome): OwnerDishUpdate {
    val dish = Dish(DishId("dish-new"), home.restaurant.id, "Croquetas", isEnabled = false)
    val menu = requireNotNull(home.menu).menu
    val details = MenuDetails(
        menu,
        home.menu.dishes.map(OwnerRatedMenuDish::menuDish) + MenuDish(dish, Money(950), position = 2, isEnabled = false),
    )
    return OwnerDishUpdate(dish, details)
}

private fun dishReview(dishId: DishId): Review = Review(
    id = ReviewId("review-$dishId"),
    authorAccountId = AccountId("customer"),
    target = ReviewTarget.Dish(dishId),
    rating = Rating(5),
    visibility = ReviewVisibility.Public,
    moderationStatus = ReviewModerationStatus.Visible,
    createdAt = IsoTimestamp("2026-01-01T10:00:00Z"),
    updatedAt = IsoTimestamp("2026-01-01T10:00:00Z"),
)

private fun ownerHome(): OwnerRestaurantHome {
    val restaurant = Restaurant(
        id = RestaurantId("restaurant"),
        ownerAccountId = AccountId("owner"),
        name = "Casa Naranja",
        address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
        openingHours = WeeklyOpeningHours(emptyList()),
        publicationState = RestaurantPublicationState.Published,
    )
    val enabled = Dish(DishId("dish-1"), restaurant.id, "Arroz", isEnabled = true)
    val disabled = Dish(DishId("dish-2"), restaurant.id, "Tarta", isEnabled = false)
    return OwnerRestaurantHome(
        restaurant = restaurant,
        restaurantRatingSummary = RatingSummary.Unrated,
        menu = OwnerRestaurantMenu(
            menu = Menu(MenuId("menu"), restaurant.id, "Carta", publicationState = MenuPublicationState.Published),
            dishes = listOf(
                OwnerRatedMenuDish(MenuDish(enabled, Money(1200), 0, category = DishCategory.MainCourses), emptyList()),
                OwnerRatedMenuDish(MenuDish(disabled, Money(800), 1, isEnabled = false, category = DishCategory.Desserts), emptyList()),
            ),
        ),
    )
}
