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
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.CreateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerRestaurantUseCase
import org.shareat.feature.restauranthome.domain.GetRestaurantHomeUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerDishImageUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerRestaurantImageUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerRestaurantInfoUseCase
import org.shareat.feature.restauranthome.domain.UpdateRestaurantPublicationStateUseCase
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate
import org.shareat.feature.restauranthome.domain.model.OwnerRatedMenuDish
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantMenu
import org.shareat.feature.restauranthome.domain.model.RestaurantHome
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.ImageUploadValidationResult
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantHomeViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(dispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun loadsTheRestaurantAndChangesViewMode() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        val loaded = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals(listOf("Arroz", "Tarta"), loaded.restaurant.dishes.map { it.name })
        assertTrue(viewModel.uiState.value.isEditMode)

        viewModel.onEditModeChange(false)

        assertFalse(viewModel.uiState.value.isEditMode)
        assertNull(viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun mapsLoadFailureToTheScreenError() = runTest(dispatcher) {
        val viewModel = viewModelFor(
            onLoad = { RepositoryResult.Failure(RepositoryError.Offline) },
        )
        advanceUntilIdle()

        val content = assertIs<RestaurantHomeContent.Error>(viewModel.uiState.value.content)
        assertEquals(RestaurantHomeError.OFFLINE, content.error)
    }

    @Test
    fun newOwnerCreatesRestaurantFromHome() = runTest(dispatcher) {
        val home = ownerHome()
        var created = false
        var submittedName: String? = null
        val viewModel = viewModelFor(
            onLoad = {
                if (created) RepositoryResult.Success(home)
                else RepositoryResult.Failure(RepositoryError.NotFound("restaurant", "owner"))
            },
            onCreateRestaurant = { name ->
                submittedName = name
                created = true
                RepositoryResult.Success(home.restaurant)
            },
        )
        advanceUntilIdle()
        assertIs<RestaurantHomeContent.Empty>(viewModel.uiState.value.content)

        viewModel.onCreateRestaurantClick()
        assertTrue(viewModel.uiState.value.newRestaurantNameInvalid)
        viewModel.onNewRestaurantNameChange("  Casa Nueva  ")
        viewModel.onCreateRestaurantClick()
        advanceUntilIdle()

        assertEquals("Casa Nueva", submittedName)
        assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
    }

    @Test
    fun publicationSwitchUpdatesTheRestaurantState() = runTest(dispatcher) {
        val home = ownerHome()
        var submittedState: RestaurantPublicationState? = null
        val viewModel = viewModelFor(
            home = home,
            onUpdatePublication = { state ->
                submittedState = state
                RepositoryResult.Success(home.restaurant.copy(publicationState = state))
            },
        )
        advanceUntilIdle()

        viewModel.onPublicationStateChange(false)
        assertTrue(viewModel.uiState.value.isPublicationUpdating)
        advanceUntilIdle()

        assertEquals(RestaurantPublicationState.Draft, submittedState)
        assertFalse(viewModel.uiState.value.isPublicationUpdating)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertFalse(content.restaurant.isPublished)
    }

    @Test
    fun publicationFailureKeepsTheCurrentStateAndExposesTheError() = runTest(dispatcher) {
        val viewModel = viewModelFor(
            onUpdatePublication = {
                RepositoryResult.Failure(RepositoryError.Offline)
            },
        )
        advanceUntilIdle()

        viewModel.onPublicationStateChange(false)
        advanceUntilIdle()

        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertTrue(content.restaurant.isPublished)
        assertEquals(RestaurantHomeError.OFFLINE, viewModel.uiState.value.publicationError)
        assertFalse(viewModel.uiState.value.isPublicationUpdating)
    }

    @Test
    fun restaurantWithoutDishesCannotBePublished() = runTest(dispatcher) {
        val publishedHome = ownerHome()
        val draftHome = publishedHome.copy(
            restaurant = publishedHome.restaurant.copy(
                publicationState = RestaurantPublicationState.Draft,
            ),
            menu = publishedHome.menu?.copy(dishes = emptyList()),
        )
        var updateCalls = 0
        val viewModel = viewModelFor(
            home = draftHome,
            onUpdatePublication = {
                updateCalls++
                error("A restaurant without dishes must not be published")
            },
        )
        advanceUntilIdle()

        viewModel.onPublicationStateChange(true)
        advanceUntilIdle()

        assertEquals(0, updateCalls)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertFalse(content.restaurant.isPublished)
    }

    @Test
    fun dishClickOpensTheCorrectSheetForEachMode() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        viewModel.onDishClick("dish-1")

        assertEquals(RestaurantHomeBottomSheet.EDIT_DISH, viewModel.uiState.value.activeBottomSheet)
        assertEquals("dish-1", viewModel.uiState.value.dishEditForm?.dishId)

        viewModel.onEditModeChange(false)
        viewModel.onDishClick("dish-1")

        assertEquals(RestaurantHomeBottomSheet.VIEW_DISH, viewModel.uiState.value.activeBottomSheet)
        assertEquals("dish-1", viewModel.uiState.value.selectedDishId)
        assertNull(viewModel.uiState.value.dishEditForm)
    }

    @Test
    fun addDishOpensAnEmptyEditableForm() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        viewModel.onAddDishClick()

        assertEquals(RestaurantHomeBottomSheet.EDIT_DISH, viewModel.uiState.value.activeBottomSheet)
        assertNull(viewModel.uiState.value.selectedDishId)
        val form = requireNotNull(viewModel.uiState.value.dishEditForm)
        assertNull(form.dishId)
        assertEquals("", form.name)
        assertEquals("", form.description)
        assertEquals("", form.price)
        assertTrue(form.allergens.isEmpty())
    }

    @Test
    fun invalidDishDoesNotReachTheRepository() = runTest(dispatcher) {
        var createCalls = 0
        val viewModel = viewModelFor(
            onCreateDish = {
                createCalls++
                error("Invalid forms must not be submitted")
            },
        )
        advanceUntilIdle()
        viewModel.onAddDishClick()

        viewModel.onSaveDish()

        val form = requireNotNull(viewModel.uiState.value.dishEditForm)
        assertTrue(form.validation.nameInvalid)
        assertTrue(form.validation.priceInvalid)
        assertEquals(0, createCalls)
        assertEquals(RestaurantHomeBottomSheet.EDIT_DISH, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun saveNewDishPersistsRefreshesTheMenuAndClosesTheSheet() = runTest(dispatcher) {
        val home = ownerHome()
        var submittedDraft: OwnerDishCreateDraft? = null
        val viewModel = viewModelFor(
            home = home,
            onCreateDish = { draft ->
                submittedDraft = draft
                RepositoryResult.Success(newDishUpdate(home))
            },
        )
        advanceUntilIdle()

        viewModel.onAddDishClick()
        viewModel.onDishNameChange("Paella del Baratie")
        viewModel.onDishPriceChange("9,50")
        viewModel.onSaveDish()
        advanceUntilIdle()

        assertEquals("Paella del Baratie", submittedDraft?.name)
        assertEquals(Money(950), submittedDraft?.price)
        assertNull(viewModel.uiState.value.activeBottomSheet)
        assertNull(viewModel.uiState.value.dishEditForm)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertTrue(content.restaurant.dishes.any { it.id == "dish-new" })
    }

    @Test
    fun saveExistingDishUsesUpdateAndRefreshesTheMenu() = runTest(dispatcher) {
        val home = ownerHome()
        var submittedId: DishId? = null
        var submittedDraft: OwnerDishDraft? = null
        var createCalls = 0
        val viewModel = viewModelFor(
            home = home,
            onCreateDish = {
                createCalls++
                error("An existing dish must not be created again")
            },
            onUpdateDish = { dishId, draft ->
                submittedId = dishId
                submittedDraft = draft
                RepositoryResult.Success(updatedDishUpdate(home, dishId, draft))
            },
        )
        advanceUntilIdle()

        viewModel.onDishClick("dish-1")
        viewModel.onDishNameChange("Arroz All Blue")
        viewModel.onDishPriceChange("14,25")
        viewModel.onSaveDish()
        advanceUntilIdle()

        assertEquals(0, createCalls)
        assertEquals(DishId("dish-1"), submittedId)
        assertEquals("Arroz All Blue", submittedDraft?.name)
        assertEquals(Money(1425), submittedDraft?.price)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        val dish = content.restaurant.dishes.single { it.id == "dish-1" }
        assertEquals("Arroz All Blue", dish.name)
        assertEquals(1425, dish.priceMinorUnits)
    }

    @Test
    fun dishSaveFailureKeepsTheEditorOpenAndShowsTheError() = runTest(dispatcher) {
        val viewModel = viewModelFor(
            onCreateDish = { RepositoryResult.Failure(RepositoryError.Offline) },
        )
        advanceUntilIdle()
        viewModel.onAddDishClick()
        viewModel.onDishNameChange("Sopa del East Blue")
        viewModel.onDishPriceChange("8")

        viewModel.onSaveDish()
        advanceUntilIdle()

        val form = requireNotNull(viewModel.uiState.value.dishEditForm)
        assertFalse(form.isSaving)
        assertEquals(RestaurantHomeError.OFFLINE, form.error)
        assertEquals(RestaurantHomeBottomSheet.EDIT_DISH, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun blankRestaurantNameIsRejectedWithoutSubmitting() = runTest(dispatcher) {
        var updateCalls = 0
        val viewModel = viewModelFor(
            onUpdateRestaurant = {
                updateCalls++
                error("Invalid forms must not be submitted")
            },
        )
        advanceUntilIdle()
        viewModel.onMainInfoClick()
        viewModel.onRestaurantNameChange(" ")

        viewModel.onSaveMainInfo()

        assertTrue(requireNotNull(viewModel.uiState.value.mainInfoDraft).nameInvalid)
        assertEquals(0, updateCalls)
        assertEquals(RestaurantHomeBottomSheet.EDIT_MAIN_INFO, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun saveMainInfoPersistsRefreshesTheRestaurantAndClosesTheSheet() = runTest(dispatcher) {
        val home = ownerHome()
        var submittedDraft: OwnerRestaurantInfoDraft? = null
        val viewModel = viewModelFor(
            home = home,
            onUpdateRestaurant = { draft ->
                submittedDraft = draft
                RepositoryResult.Success(
                    home.restaurant.copy(name = draft.name, description = draft.description),
                )
            },
        )
        advanceUntilIdle()

        viewModel.onMainInfoClick()
        viewModel.onRestaurantNameChange("Baratie renovado")
        viewModel.onRestaurantDescriptionChange("Restaurante flotante del East Blue")
        viewModel.onSaveMainInfo()
        advanceUntilIdle()

        assertEquals("Baratie renovado", submittedDraft?.name)
        assertEquals("Restaurante flotante del East Blue", submittedDraft?.description)
        assertEquals(home.restaurant.address, submittedDraft?.address)
        assertNull(viewModel.uiState.value.activeBottomSheet)
        assertNull(viewModel.uiState.value.mainInfoDraft)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals("Baratie renovado", content.restaurant.name)
    }

    @Test
    fun mainInfoSaveFailureKeepsTheEditorOpenAndShowsTheError() = runTest(dispatcher) {
        val viewModel = viewModelFor(
            onUpdateRestaurant = {
                RepositoryResult.Failure(RepositoryError.Unavailable("maintenance"))
            },
        )
        advanceUntilIdle()
        viewModel.onMainInfoClick()
        viewModel.onRestaurantNameChange("Baratie")

        viewModel.onSaveMainInfo()
        advanceUntilIdle()

        val form = requireNotNull(viewModel.uiState.value.mainInfoDraft)
        assertFalse(form.isSaving)
        assertEquals(RestaurantHomeError.TEMPORARILY_UNAVAILABLE, form.error)
        assertEquals(RestaurantHomeBottomSheet.EDIT_MAIN_INFO, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun selectedRestaurantImageIsUploadedAndPublishedAfterSavingDetails() = runTest(dispatcher) {
        val home = ownerHome()
        val upload = ImageUpload(byteArrayOf(1, 2, 3), "image/jpeg", "Baratie")
        val uploadedImage = ImageRef("https://images.test/restaurants/baratie.jpg", "Baratie")
        var submittedUpload: ImageUpload? = null
        val viewModel = viewModelFor(
            home = home,
            onUpdateRestaurant = { RepositoryResult.Success(home.restaurant) },
            onReplaceRestaurantImage = {
                submittedUpload = it
                RepositoryResult.Success(uploadedImage)
            },
        )
        advanceUntilIdle()

        viewModel.onMainInfoClick()
        viewModel.onRestaurantImageSelected(upload)
        assertEquals(upload, viewModel.uiState.value.mainInfoDraft?.pendingImageUpload)

        viewModel.onSaveMainInfo()
        advanceUntilIdle()

        assertEquals(upload, submittedUpload)
        assertNull(viewModel.uiState.value.activeBottomSheet)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals(uploadedImage.url, content.restaurant.imageUrl)
    }

    @Test
    fun selectedDishImageIsUploadedForThePersistedDish() = runTest(dispatcher) {
        val home = ownerHome()
        val upload = ImageUpload(byteArrayOf(4, 5, 6), "image/jpeg", "Paella")
        val uploadedImage = ImageRef("https://images.test/dishes/paella.jpg", "Paella")
        var uploadedDishId: DishId? = null
        val viewModel = viewModelFor(
            home = home,
            onCreateDish = { RepositoryResult.Success(newDishUpdate(home)) },
            onReplaceDishImage = { dishId, selectedUpload ->
                uploadedDishId = dishId
                assertEquals(upload, selectedUpload)
                RepositoryResult.Success(uploadedImage)
            },
        )
        advanceUntilIdle()

        viewModel.onAddDishClick()
        viewModel.onDishNameChange("Paella del Baratie")
        viewModel.onDishPriceChange("9,50")
        viewModel.onDishImageSelected(upload)
        viewModel.onSaveDish()
        advanceUntilIdle()

        assertEquals(DishId("dish-new"), uploadedDishId)
        assertNull(viewModel.uiState.value.activeBottomSheet)
        val content = assertIs<RestaurantHomeContent.Loaded>(viewModel.uiState.value.content)
        assertEquals(
            uploadedImage.url,
            content.restaurant.dishes.single { it.id == "dish-new" }.imageUrl,
        )
    }

    @Test
    fun failedDishImageUploadKeepsThePersistedDishOpenForRetry() = runTest(dispatcher) {
        val home = ownerHome()
        val viewModel = viewModelFor(
            home = home,
            onCreateDish = { RepositoryResult.Success(newDishUpdate(home)) },
            onReplaceDishImage = { _, _ -> RepositoryResult.Failure(RepositoryError.Offline) },
        )
        advanceUntilIdle()

        viewModel.onAddDishClick()
        viewModel.onDishNameChange("Paella del Baratie")
        viewModel.onDishPriceChange("9,50")
        viewModel.onDishImageSelected(ImageUpload(byteArrayOf(1), "image/jpeg", "Paella"))
        viewModel.onSaveDish()
        advanceUntilIdle()

        val form = requireNotNull(viewModel.uiState.value.dishEditForm)
        assertEquals("dish-new", form.dishId)
        assertFalse(form.isSaving)
        assertEquals(RestaurantHomeError.IMAGE_UPLOAD_FAILED_AFTER_DETAILS_SAVED, form.error)
        assertEquals(RestaurantHomeBottomSheet.EDIT_DISH, viewModel.uiState.value.activeBottomSheet)
    }

    @Test
    fun imagePickerValidationFailureIsShownInTheActiveEditor() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()
        viewModel.onMainInfoClick()

        viewModel.onRestaurantImagePickerFailure(ImageUploadValidationResult.TooLarge)

        assertEquals(
            RestaurantHomeError.IMAGE_TOO_LARGE,
            viewModel.uiState.value.mainInfoDraft?.error,
        )
    }

    @Test
    fun addressAndCategoryDraftsAreUpdatedInEditMode() = runTest(dispatcher) {
        val viewModel = viewModelFor()
        advanceUntilIdle()

        viewModel.onAddressClick()
        viewModel.onAddressStreetLineChange("Muelle 3")

        assertEquals(RestaurantHomeBottomSheet.EDIT_ADDRESS, viewModel.uiState.value.activeBottomSheet)
        assertEquals("Muelle 3", viewModel.uiState.value.addressDraft?.streetLine)

        viewModel.onCategoriesEditClick()
        viewModel.onCategoryClick(DishCategory.Desserts)

        assertEquals(RestaurantHomeBottomSheet.EDIT_CATEGORIES, viewModel.uiState.value.activeBottomSheet)
        assertFalse(DishCategory.Desserts in requireNotNull(viewModel.uiState.value.categoriesDraft))
    }

    private fun viewModelFor(
        home: RestaurantHome = ownerHome(),
        onLoad: suspend () -> RepositoryResult<RestaurantHome> = {
            RepositoryResult.Success(home)
        },
        onCreateRestaurant: suspend (String) -> RepositoryResult<Restaurant> = {
            error("Create restaurant was not expected")
        },
        onCreateDish: suspend (OwnerDishCreateDraft) -> RepositoryResult<OwnerDishUpdate> = {
            error("Create dish was not expected")
        },
        onUpdateDish: suspend (DishId, OwnerDishDraft) -> RepositoryResult<OwnerDishUpdate> = { _, _ ->
            error("Update dish was not expected")
        },
        onUpdateRestaurant: suspend (OwnerRestaurantInfoDraft) -> RepositoryResult<Restaurant> = {
            error("Update restaurant was not expected")
        },
        onUpdatePublication: suspend (RestaurantPublicationState) -> RepositoryResult<Restaurant> = {
            error("Update publication was not expected")
        },
        onReplaceRestaurantImage: suspend (ImageUpload) -> RepositoryResult<ImageRef> = {
            error("Replace restaurant image was not expected")
        },
        onReplaceDishImage: suspend (DishId, ImageUpload) -> RepositoryResult<ImageRef> = { _, _ ->
            error("Replace dish image was not expected")
        },
    ) = RestaurantHomeViewModel(
        loadRestaurantHome = GetRestaurantHomeUseCase(onLoad),
        createOwnerRestaurant = CreateOwnerRestaurantUseCase(onCreateRestaurant),
        createOwnerDish = CreateOwnerDishUseCase(onCreateDish),
        updateOwnerDish = UpdateOwnerDishUseCase(onUpdateDish),
        updateOwnerRestaurantInfo = UpdateOwnerRestaurantInfoUseCase(onUpdateRestaurant),
        updateRestaurantPublicationState = UpdateRestaurantPublicationStateUseCase(onUpdatePublication),
        replaceOwnerRestaurantImage = ReplaceOwnerRestaurantImageUseCase(onReplaceRestaurantImage),
        replaceOwnerDishImage = ReplaceOwnerDishImageUseCase(onReplaceDishImage),
    )
}

private fun newDishUpdate(home: RestaurantHome): OwnerDishUpdate {
    val dish = Dish(DishId("dish-new"), home.restaurant.id, "Paella del Baratie", isEnabled = false)
    val menu = requireNotNull(home.menu)
    return OwnerDishUpdate(
        dish = dish,
        menu = MenuDetails(
            menu.menu,
            menu.dishes.map(OwnerRatedMenuDish::menuDish) + MenuDish(
                dish = dish,
                price = Money(950),
                position = menu.dishes.size,
                isEnabled = false,
            ),
        ),
    )
}

private fun updatedDishUpdate(
    home: RestaurantHome,
    dishId: DishId,
    draft: OwnerDishDraft,
): OwnerDishUpdate {
    val menu = requireNotNull(home.menu)
    val currentItem = menu.dishes.single { it.menuDish.dish.id == dishId }.menuDish
    val updatedDish = currentItem.dish.copy(
        name = draft.name,
        description = draft.description,
        allergenDeclaration = draft.allergenDeclaration,
        isEnabled = draft.isEnabled,
    )
    val updatedItem = currentItem.copy(
        dish = updatedDish,
        price = draft.price,
        isEnabled = draft.isEnabled,
    )
    return OwnerDishUpdate(
        dish = updatedDish,
        menu = MenuDetails(
            menu = menu.menu,
            items = menu.dishes.map { ratedDish ->
                if (ratedDish.menuDish.dish.id == dishId) updatedItem else ratedDish.menuDish
            },
        ),
    )
}

private fun ownerHome(): RestaurantHome {
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
    return RestaurantHome(
        restaurant = restaurant,
        restaurantRatingSummary = RatingSummary.Unrated,
        menu = OwnerRestaurantMenu(
            menu = Menu(
                id = MenuId("menu"),
                restaurantId = restaurant.id,
                name = "Carta",
                publicationState = MenuPublicationState.Published,
            ),
            dishes = listOf(
                OwnerRatedMenuDish(
                    menuDish = MenuDish(
                        dish = enabled,
                        price = Money(1200),
                        position = 0,
                        category = DishCategory.MainCourses,
                    ),
                    reviews = emptyList(),
                ),
                OwnerRatedMenuDish(
                    menuDish = MenuDish(
                        dish = disabled,
                        price = Money(800),
                        position = 1,
                        isEnabled = false,
                        category = DishCategory.Desserts,
                    ),
                    reviews = emptyList(),
                ),
            ),
        ),
    )
}
