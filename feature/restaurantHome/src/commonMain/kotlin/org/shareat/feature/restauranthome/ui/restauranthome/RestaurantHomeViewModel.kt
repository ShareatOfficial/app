package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.RestaurantPublicationState
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
import org.shareat.feature.restauranthome.domain.model.RestaurantHome
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.DishFormValidation
import org.shareat.feature.restauranthome.ui.model.ImageUploadValidationResult
import org.shareat.feature.restauranthome.ui.model.RestaurantAddressUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.toAllergenDeclaration
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.model.toMoneyOrNull
import org.shareat.feature.restauranthome.ui.model.toRestaurantHomeData

enum class RestaurantHomeBottomSheet {
    VIEW_DISH,
    EDIT_DISH,
    VIEW_RATING,
    VIEW_ADDRESS,
    EDIT_ADDRESS,
    EDIT_MAIN_INFO,
    EDIT_CATEGORIES,
}

data class RestaurantMainInfoDraft(
    val name: String,
    val description: String,
    val imageUrl: String,
    val pendingImageUpload: ImageUpload? = null,
    val isSaving: Boolean = false,
    val nameInvalid: Boolean = false,
    val error: RestaurantHomeError? = null,
)

data class RestaurantAddressDraft(
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val region: String,
    val countryCode: String,
)

data class RestaurantHomeUiStateByTone(
    val content: RestaurantHomeContent = RestaurantHomeContent.Loading,
    val isEditMode: Boolean = true,
    val activeBottomSheet: RestaurantHomeBottomSheet? = null,
    val selectedDishId: String? = null,
    val mainInfoDraft: RestaurantMainInfoDraft? = null,
    val dishEditForm: DishEditFormUiState? = null,
    val addressDraft: RestaurantAddressDraft? = null,
    val categoriesDraft: Set<DishCategory>? = null,
    val isPublicationUpdating: Boolean = false,
    val publicationError: RestaurantHomeError? = null,
    val newRestaurantName: String = "",
    val isCreatingRestaurant: Boolean = false,
    val newRestaurantNameInvalid: Boolean = false,
    val createRestaurantError: RestaurantHomeError? = null,
) {
    val selectedDish: RestaurantDish?
        get() = (content as? RestaurantHomeContent.Loaded)
            ?.restaurant
            ?.dishes
            ?.firstOrNull { it.id == selectedDishId }
}

@Stable
class RestaurantHomeViewModel(
    private val loadRestaurantHome: GetRestaurantHomeUseCase,
    private val createOwnerRestaurant: CreateOwnerRestaurantUseCase,
    private val createOwnerDish: CreateOwnerDishUseCase,
    private val updateOwnerDish: UpdateOwnerDishUseCase,
    private val updateOwnerRestaurantInfo: UpdateOwnerRestaurantInfoUseCase,
    private val updateRestaurantPublicationState: UpdateRestaurantPublicationStateUseCase,
    private val replaceOwnerRestaurantImage: ReplaceOwnerRestaurantImageUseCase,
    private val replaceOwnerDishImage: ReplaceOwnerDishImageUseCase,
) : ViewModel() {
    private var ownerHome: RestaurantHome? = null

    private val _uiState = MutableStateFlow(RestaurantHomeUiStateByTone())
    val uiState: StateFlow<RestaurantHomeUiStateByTone> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onNewRestaurantNameChange(name: String) {
        _uiState.value = _uiState.value.copy(
            newRestaurantName = name,
            newRestaurantNameInvalid = false,
            createRestaurantError = null,
        )
    }

    fun onCreateRestaurantClick() {
        val state = _uiState.value
        if (state.content != RestaurantHomeContent.Empty || state.isCreatingRestaurant) return
        if (state.newRestaurantName.isBlank()) {
            _uiState.value = state.copy(newRestaurantNameInvalid = true)
            return
        }
        _uiState.value = state.copy(isCreatingRestaurant = true, createRestaurantError = null)
        viewModelScope.launch {
            when (val result = createOwnerRestaurant(state.newRestaurantName.trim())) {
                is RepositoryResult.Success -> load()
                is RepositoryResult.Failure -> _uiState.value = _uiState.value.copy(
                    isCreatingRestaurant = false,
                    createRestaurantError = result.error.toUiError(),
                )
            }
        }
    }

    fun onEditModeChange(isEditMode: Boolean) {
        _uiState.value = _uiState.value.copy(
            isEditMode = isEditMode,
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
            publicationError = null,
        )
    }

    fun onPublicationStateChange(isPublished: Boolean) {
        val state = _uiState.value
        val restaurant = (state.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        if (!state.isEditMode || state.isPublicationUpdating) return
        if (isPublished == restaurant.isPublished) return
        if (isPublished && (restaurant.dishes.none { it.isPublished } ||
                restaurant.address.streetLine.isBlank() ||
                restaurant.address.locality.isBlank() ||
                restaurant.address.postalCode.isBlank())) return

        _uiState.value = state.copy(
            isPublicationUpdating = true,
            publicationError = null,
        )
        viewModelScope.launch {
            val targetState = if (isPublished) {
                RestaurantPublicationState.Published
            } else {
                RestaurantPublicationState.Draft
            }
            when (val result = updateRestaurantPublicationState(targetState)) {
                is RepositoryResult.Success -> {
                    ownerHome = ownerHome?.copy(restaurant = result.value)
                    _uiState.value = _uiState.value.copy(
                        isPublicationUpdating = false,
                        publicationError = null,
                    )
                    publishLoadedContent()
                }

                is RepositoryResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isPublicationUpdating = false,
                        publicationError = result.error.toUiError(),
                    )
                }
            }
        }
    }

    fun onDishClick(dishId: String) {
        val dish = findDish(dishId) ?: return
        val isEditMode = _uiState.value.isEditMode
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = if (isEditMode) {
                RestaurantHomeBottomSheet.EDIT_DISH
            } else {
                RestaurantHomeBottomSheet.VIEW_DISH
            },
            selectedDishId = dishId,
            mainInfoDraft = null,
            dishEditForm = if (isEditMode) dish.toEditForm() else null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onAddDishClick() {
        if (!_uiState.value.isEditMode || _uiState.value.content !is RestaurantHomeContent.Loaded) return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_DISH,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = DishEditFormUiState(
                dishId = null,
                name = "",
                description = "",
                price = "",
                allergens = emptySet(),
                imageUrl = null,
                isPublished = false,
            ),
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onMainInfoClick() {
        if (!_uiState.value.isEditMode) return
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_MAIN_INFO,
            selectedDishId = null,
            mainInfoDraft = RestaurantMainInfoDraft(
                name = restaurant.name,
                description = restaurant.description.orEmpty(),
                imageUrl = restaurant.imageUrl.orEmpty(),
            ),
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    fun onAddressClick() {
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        if (!_uiState.value.isEditMode) {
            openSheet(RestaurantHomeBottomSheet.VIEW_ADDRESS)
            return
        }

        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_ADDRESS,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = restaurant.address.toDraft(),
            categoriesDraft = null,
        )
    }

    fun onRatingClick() {
        if (_uiState.value.content !is RestaurantHomeContent.Loaded) return
        openSheet(RestaurantHomeBottomSheet.VIEW_RATING)
    }

    fun onCategoriesEditClick() {
        if (!_uiState.value.isEditMode) return
        val restaurant = (_uiState.value.content as? RestaurantHomeContent.Loaded)?.restaurant ?: return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = RestaurantHomeBottomSheet.EDIT_CATEGORIES,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = restaurant.categories.toSet(),
        )
    }

    fun onRestaurantNameChange(value: String) = updateMainInfoDraft {
        copy(name = value, nameInvalid = false, error = null)
    }

    fun onRestaurantDescriptionChange(value: String) = updateMainInfoDraft {
        copy(description = value, error = null)
    }

    fun onRestaurantImageSelected(upload: ImageUpload) = updateMainInfoDraft {
        copy(pendingImageUpload = upload, error = null)
    }

    internal fun onRestaurantImagePickerFailure(result: ImageUploadValidationResult) =
        updateMainInfoDraft { copy(error = result.toImageError()) }

    fun onSaveMainInfo() {
        val form = _uiState.value.mainInfoDraft ?: return
        if (form.isSaving) return
        if (form.name.isBlank()) {
            updateMainInfoDraft { copy(nameInvalid = true) }
            return
        }
        val home = ownerHome ?: return

        updateMainInfoDraft { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (
                val result = updateOwnerRestaurantInfo(
                    OwnerRestaurantInfoDraft(
                        name = form.name.trim(),
                        description = form.description.trim().ifEmpty { null },
                        address = home.restaurant.address,
                        publicEmail = home.restaurant.publicEmail,
                        publicPhone = home.restaurant.publicPhone,
                        openingHours = home.restaurant.openingHours,
                    ),
                )
            ) {
                is RepositoryResult.Success -> {
                    ownerHome = home.copy(restaurant = result.value)
                    val imageResult = form.pendingImageUpload?.let { replaceOwnerRestaurantImage(it) }
                    if (imageResult is RepositoryResult.Failure) {
                        updateMainInfoDraft {
                            copy(
                                isSaving = false,
                                error = RestaurantHomeError.IMAGE_UPLOAD_FAILED_AFTER_DETAILS_SAVED,
                            )
                        }
                        publishLoadedContent()
                        return@launch
                    }
                    if (imageResult is RepositoryResult.Success) {
                        ownerHome = ownerHome?.let { currentHome ->
                            currentHome.copy(
                                restaurant = currentHome.restaurant.copy(heroImage = imageResult.value),
                            )
                        }
                    }
                    _uiState.value = _uiState.value.copy(
                        activeBottomSheet = null,
                        mainInfoDraft = null,
                    )
                    publishLoadedContent()
                }

                is RepositoryResult.Failure -> updateMainInfoDraft {
                    copy(isSaving = false, error = result.error.toUiError())
                }
            }
        }
    }

    fun onDishNameChange(value: String) = updateDishEditForm {
        copy(
            name = value,
            validation = validation.copy(nameInvalid = false),
            error = null,
        )
    }

    fun onDishDescriptionChange(value: String) = updateDishEditForm {
        copy(description = value, error = null)
    }

    fun onDishPriceChange(value: String) = updateDishEditForm {
        copy(
            price = value,
            validation = validation.copy(priceInvalid = false),
            error = null,
        )
    }

    fun onDishAllergenClick(allergen: EuAllergen) = updateDishEditForm {
        copy(
            allergens = if (allergen in allergens) allergens - allergen else allergens + allergen,
            error = null,
        )
    }

    fun onDishImageSelected(upload: ImageUpload) = updateDishEditForm {
        copy(pendingImageUpload = upload, error = null)
    }

    internal fun onDishImagePickerFailure(result: ImageUploadValidationResult) =
        updateDishEditForm { copy(error = result.toImageError()) }

    fun onSaveDish() {
        val form = _uiState.value.dishEditForm ?: return
        if (form.isSaving) return

        val price = form.price.toMoneyOrNull()
        val validation = DishFormValidation(
            nameInvalid = form.name.isBlank(),
            priceInvalid = price == null,
        )
        if (validation.nameInvalid || validation.priceInvalid) {
            updateDishEditForm { copy(validation = validation) }
            return
        }

        updateDishEditForm { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val draft = OwnerDishDraft(
                name = form.name.trim(),
                description = form.description.trim().ifEmpty { null },
                allergenDeclaration = form.allergens.toAllergenDeclaration(),
                isEnabled = form.isPublished,
                price = requireNotNull(price),
            )
            val result = if (form.dishId == null) {
                createOwnerDish(
                    OwnerDishCreateDraft(
                        name = draft.name,
                        description = draft.description,
                        allergenDeclaration = draft.allergenDeclaration,
                        isEnabled = draft.isEnabled,
                        price = draft.price,
                    ),
                )
            } else {
                updateOwnerDish(DishId(form.dishId), draft)
            }

            when (result) {
                is RepositoryResult.Success -> {
                    applyDishUpdate(result.value)
                    val savedDishId = result.value.dish.id
                    if (form.dishId == null) {
                        updateDishEditForm { copy(dishId = savedDishId.value) }
                    }
                    val imageResult = form.pendingImageUpload?.let {
                        replaceOwnerDishImage(savedDishId, it)
                    }
                    if (imageResult is RepositoryResult.Failure) {
                        updateDishEditForm {
                            copy(
                                isSaving = false,
                                error = RestaurantHomeError.IMAGE_UPLOAD_FAILED_AFTER_DETAILS_SAVED,
                            )
                        }
                        publishLoadedContent()
                        return@launch
                    }
                    if (imageResult is RepositoryResult.Success) {
                        applyDishImage(savedDishId.value, imageResult.value)
                    }
                    _uiState.value = _uiState.value.copy(
                        activeBottomSheet = null,
                        selectedDishId = null,
                        mainInfoDraft = null,
                        dishEditForm = null,
                        addressDraft = null,
                        categoriesDraft = null,
                    )
                    publishLoadedContent()
                }

                is RepositoryResult.Failure -> updateDishEditForm {
                    copy(isSaving = false, error = result.error.toUiError())
                }
            }
        }
    }

    fun onAddressStreetLineChange(value: String) = updateAddressDraft { copy(streetLine = value) }

    fun onAddressLocalityChange(value: String) = updateAddressDraft { copy(locality = value) }

    fun onAddressPostalCodeChange(value: String) = updateAddressDraft { copy(postalCode = value) }

    fun onAddressRegionChange(value: String) = updateAddressDraft { copy(region = value) }

    fun onCategoryClick(category: DishCategory) {
        val categories = _uiState.value.categoriesDraft ?: return
        _uiState.value = _uiState.value.copy(
            categoriesDraft = if (category in categories) {
                categories - category
            } else {
                categories + category
            },
        )
    }

    fun onDismissBottomSheet() {
        if (
            _uiState.value.dishEditForm?.isSaving == true ||
            _uiState.value.mainInfoDraft?.isSaving == true
        ) return
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(
            content = RestaurantHomeContent.Loading,
            activeBottomSheet = null,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
            isPublicationUpdating = false,
            publicationError = null,
        )
        viewModelScope.launch {
            when (val result = loadRestaurantHome()) {
                is RepositoryResult.Success -> {
                    ownerHome = result.value
                    publishLoadedContent()
                }

                is RepositoryResult.Failure -> {
                    ownerHome = null
                    _uiState.value = _uiState.value.copy(
                        content = if (result.error is RepositoryError.NotFound) {
                            RestaurantHomeContent.Empty
                        } else {
                            RestaurantHomeContent.Error(result.error.toUiError())
                        },
                        isCreatingRestaurant = false,
                    )
                }
            }
        }
    }

    private fun openSheet(sheet: RestaurantHomeBottomSheet) {
        _uiState.value = _uiState.value.copy(
            activeBottomSheet = sheet,
            selectedDishId = null,
            mainInfoDraft = null,
            dishEditForm = null,
            addressDraft = null,
            categoriesDraft = null,
        )
    }

    private fun findDish(dishId: String): RestaurantDish? =
        (_uiState.value.content as? RestaurantHomeContent.Loaded)
            ?.restaurant
            ?.dishes
            ?.firstOrNull { it.id == dishId }

    private fun updateMainInfoDraft(change: RestaurantMainInfoDraft.() -> RestaurantMainInfoDraft) {
        val draft = _uiState.value.mainInfoDraft ?: return
        _uiState.value = _uiState.value.copy(mainInfoDraft = draft.change())
    }

    private fun updateDishEditForm(change: DishEditFormUiState.() -> DishEditFormUiState) {
        val form = _uiState.value.dishEditForm ?: return
        _uiState.value = _uiState.value.copy(dishEditForm = form.change())
    }

    private fun updateAddressDraft(change: RestaurantAddressDraft.() -> RestaurantAddressDraft) {
        val draft = _uiState.value.addressDraft ?: return
        _uiState.value = _uiState.value.copy(addressDraft = draft.change())
    }

    private fun publishLoadedContent() {
        val home = ownerHome ?: return
        _uiState.value = _uiState.value.copy(
            content = RestaurantHomeContent.Loaded(home.toRestaurantHomeData()),
        )
    }

    private fun applyDishUpdate(update: OwnerDishUpdate) {
        val home = ownerHome ?: return
        val menu = home.menu ?: return
        val oldDishes = menu.dishes.associateBy { it.menuDish.dish.id }
        ownerHome = home.copy(
            menu = menu.copy(
                menu = update.menu.menu,
                dishes = update.menu.items.map { item ->
                    oldDishes[item.dish.id]?.copy(menuDish = item)
                        ?: OwnerRatedMenuDish(menuDish = item, reviews = emptyList())
                },
            ),
        )
    }

    private fun applyDishImage(dishId: String, image: ImageRef) {
        val home = ownerHome ?: return
        val menu = home.menu ?: return
        ownerHome = home.copy(
            menu = menu.copy(
                dishes = menu.dishes.map { ratedDish ->
                    if (ratedDish.menuDish.dish.id.value == dishId) {
                        ratedDish.copy(
                            menuDish = ratedDish.menuDish.copy(
                                dish = ratedDish.menuDish.dish.copy(image = image),
                            ),
                        )
                    } else {
                        ratedDish
                    }
                },
            ),
        )
    }
}

private fun RestaurantAddressUiState.toDraft() = RestaurantAddressDraft(
    streetLine = streetLine,
    locality = locality,
    postalCode = postalCode,
    region = region.orEmpty(),
    countryCode = countryCode,
)

private fun RestaurantDish.toEditForm() = DishEditFormUiState(
    dishId = id,
    name = name,
    description = description.orEmpty(),
    price = priceMinorUnits.toEditablePrice(),
    allergens = allergens,
    imageUrl = imageUrl,
    isPublished = isPublished,
)

private fun RepositoryError.toUiError(): RestaurantHomeError = when (this) {
    RepositoryError.Offline -> RestaurantHomeError.OFFLINE
    RepositoryError.Unauthenticated -> RestaurantHomeError.UNAUTHENTICATED
    RepositoryError.Forbidden -> RestaurantHomeError.FORBIDDEN
    is RepositoryError.NotFound -> RestaurantHomeError.NOT_FOUND
    is RepositoryError.Unavailable -> RestaurantHomeError.TEMPORARILY_UNAVAILABLE
    is RepositoryError.Validation -> RestaurantHomeError.VALIDATION
    else -> RestaurantHomeError.UNKNOWN
}

private fun ImageUploadValidationResult.toImageError(): RestaurantHomeError = when (this) {
    ImageUploadValidationResult.UnsupportedFormat -> RestaurantHomeError.IMAGE_FORMAT_UNSUPPORTED
    ImageUploadValidationResult.TooLarge -> RestaurantHomeError.IMAGE_TOO_LARGE
    ImageUploadValidationResult.InvalidFile -> RestaurantHomeError.IMAGE_READ_FAILED
    is ImageUploadValidationResult.Success -> RestaurantHomeError.IMAGE_READ_FAILED
}
