package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.restauranthome.domain.GetRestaurantHomeUseCase
import org.shareat.feature.restauranthome.domain.CreateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerDishImageUseCase
import org.shareat.feature.restauranthome.domain.ReplaceOwnerRestaurantImageUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerDishUseCase
import org.shareat.feature.restauranthome.domain.UpdateOwnerRestaurantInfoUseCase
import org.shareat.feature.restauranthome.domain.UpdateRestaurantPublicationStateUseCase
import org.shareat.feature.restauranthome.domain.model.OwnerDishDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate
import org.shareat.feature.restauranthome.domain.model.OwnerRatedMenuDish
import org.shareat.feature.restauranthome.domain.model.RestaurantHome
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.DishFormValidation
import org.shareat.feature.restauranthome.ui.model.ImageUploadValidationResult
import org.shareat.feature.restauranthome.ui.model.RestaurantEditFormUiState
import org.shareat.feature.restauranthome.ui.model.RestaurantFormValidation
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeEditor
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeUiState
import org.shareat.feature.restauranthome.ui.model.toAllergenDeclaration
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.model.toMoneyOrNull
import org.shareat.feature.restauranthome.ui.model.toRestaurantHomeData

@Stable
@KoinViewModel
class RestaurantHomeViewModel(
    private val loadOwnerRestaurantHome: GetRestaurantHomeUseCase,
    private val createOwnerDish: CreateOwnerDishUseCase,
    private val updateOwnerRestaurantInfo: UpdateOwnerRestaurantInfoUseCase,
    private val updateRestaurantPublicationState: UpdateRestaurantPublicationStateUseCase,
    private val replaceOwnerRestaurantImage: ReplaceOwnerRestaurantImageUseCase,
    private val updateOwnerDish: UpdateOwnerDishUseCase,
    private val replaceOwnerDishImage: ReplaceOwnerDishImageUseCase,
) : ViewModel() {
    private var ownerHome: RestaurantHome? = null

    private val _uiState = MutableStateFlow(RestaurantHomeUiState())
    val uiState: StateFlow<RestaurantHomeUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetryClick() = load()

    fun onModeToggle() {
        _uiState.value = _uiState.value.copy(
            visonMode = if (_uiState.value.visonMode == RestaurantHomeMode.MANAGEMENT) {
                RestaurantHomeMode.CUSTOMER_PREVIEW
            } else {
                RestaurantHomeMode.MANAGEMENT
            },
            editor = null,
        )
    }

    fun onCategoryClick(category: org.shareat.app.domain.model.DishCategory?) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun onAllergenClick(allergen: org.shareat.app.domain.model.EuAllergen) {
        val exclusions = _uiState.value.excludedAllergens
        _uiState.value = _uiState.value.copy(
            excludedAllergens = if (allergen in exclusions) exclusions - allergen else exclusions + allergen,
        )
    }

    fun onRestaurantPublicationChange(isPublished: Boolean) {
        if (_uiState.value.content !is RestaurantHomeContent.Loaded) return
        val state = if (isPublished) RestaurantPublicationState.Published else RestaurantPublicationState.Draft
        viewModelScope.launch {
            when (val result = updateRestaurantPublicationState(state)) {
                is RepositoryResult.Success -> refreshOwnerHome()
                is RepositoryResult.Failure -> publishError(result.error.toUiError())
            }
        }
    }

    fun onEditRestaurantClick() {
        val current = ownerHome ?: return
        val address = current.restaurant.address
        _uiState.value = _uiState.value.copy(
            editor = RestaurantHomeEditor.Restaurant(
                RestaurantEditFormUiState(
                    name = current.restaurant.name,
                    streetLine = address?.streetLine.orEmpty(),
                    locality = address?.locality.orEmpty(),
                    postalCode = address?.postalCode.orEmpty(),
                    region = address?.region.orEmpty(),
                    description = current.restaurant.description.orEmpty(),
                    imageUrl = current.restaurant.heroImage?.url,
                ),
            ),
        )
    }

    fun onRestaurantNameChanged(value: String) = updateRestaurantForm { copy(name = value, validation = validation.copy(nameInvalid = false), error = null) }

    fun onRestaurantStreetChanged(value: String) = updateRestaurantForm { copy(streetLine = value, validation = validation.copy(streetInvalid = false), error = null) }

    fun onRestaurantLocalityChanged(value: String) = updateRestaurantForm { copy(locality = value, validation = validation.copy(localityInvalid = false), error = null) }

    fun onRestaurantPostalCodeChanged(value: String) = updateRestaurantForm { copy(postalCode = value, validation = validation.copy(postalCodeInvalid = false), error = null) }

    fun onRestaurantRegionChanged(value: String) = updateRestaurantForm { copy(region = value, error = null) }

    fun onRestaurantDescriptionChanged(value: String) = updateRestaurantForm { copy(description = value, error = null) }

    fun onRestaurantImageSelected(upload: ImageUpload) = updateRestaurantForm { copy(pendingImageUpload = upload, error = null) }

    internal fun onRestaurantImagePickerFailure(result: ImageUploadValidationResult) = updateRestaurantForm {
        copy(error = result.toImageError())
    }

    fun onEditDishClick(dishId: String) {
        val dish = ownerHome?.menu?.dishes?.firstOrNull { it.menuDish.dish.id.value == dishId } ?: return
        _uiState.value = _uiState.value.copy(
            editor = RestaurantHomeEditor.Dish(
                DishEditFormUiState(
                    dishId = dishId,
                    name = dish.menuDish.dish.name,
                    description = dish.menuDish.dish.description.orEmpty(),
                    price = dish.menuDish.price.minorUnits.toEditablePrice(),
                    allergens = dish.menuDish.dish.allergenDeclaration?.allergens.orEmpty(),
                    imageUrl = dish.menuDish.dish.image?.url,
                    isPublished = dish.menuDish.dish.isEnabled && dish.menuDish.isEnabled,
                ),
            ),
        )
    }

    fun onAddDishClick() {
        _uiState.value = _uiState.value.copy(
            editor = RestaurantHomeEditor.Dish(
                DishEditFormUiState(
                    name = "",
                    description = "",
                    price = "",
                    allergens = emptySet(),
                    imageUrl = null,
                    isPublished = false,
                ),
            ),
        )
    }

    fun onDishNameChanged(value: String) = updateDishForm { copy(name = value, validation = validation.copy(nameInvalid = false), error = null) }

    fun onDishDescriptionChanged(value: String) = updateDishForm { copy(description = value, error = null) }

    fun onDishPriceChanged(value: String) = updateDishForm { copy(price = value, validation = validation.copy(priceInvalid = false), error = null) }

    fun onDishAllergenClick(allergen: org.shareat.app.domain.model.EuAllergen) = updateDishForm {
        copy(allergens = if (allergen in allergens) allergens - allergen else allergens + allergen, error = null)
    }

    fun onDishPublicationChange(isPublished: Boolean) = updateDishForm { copy(isPublished = isPublished, error = null) }

    fun onDishImageSelected(upload: ImageUpload) = updateDishForm { copy(pendingImageUpload = upload, error = null) }

    internal fun onDishImagePickerFailure(result: ImageUploadValidationResult) = updateDishForm {
        copy(error = result.toImageError())
    }

    fun onDismissEditor() {
        if (currentEditorSaving()) return
        _uiState.value = _uiState.value.copy(editor = null)
    }

    fun onSaveRestaurant() {
        val form = (_uiState.value.editor as? RestaurantHomeEditor.Restaurant)?.form ?: return
        if (form.isSaving) return
        val home = ownerHome ?: return
        val addressStarted = form.streetLine.isNotBlank() ||
            form.locality.isNotBlank() || form.postalCode.isNotBlank()
        val addressRequired = home.restaurant.publicationState == RestaurantPublicationState.Published
        val validation = RestaurantFormValidation(
            nameInvalid = form.name.isBlank(),
            streetInvalid = (addressStarted || addressRequired) && form.streetLine.isBlank(),
            localityInvalid = (addressStarted || addressRequired) && form.locality.isBlank(),
            postalCodeInvalid = (addressStarted || addressRequired) && form.postalCode.isBlank(),
        )
        if (validation.nameInvalid || validation.streetInvalid || validation.localityInvalid || validation.postalCodeInvalid) {
            updateRestaurantForm { copy(validation = validation) }
            return
        }
        updateRestaurantForm { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            when (val result = updateOwnerRestaurantInfo(
                OwnerRestaurantInfoDraft(
                    name = form.name,
                    description = form.description.trim().ifEmpty { null },
                    address = if (addressStarted) PostalAddress(
                        streetLine = form.streetLine.trim(),
                        locality = form.locality.trim(),
                        postalCode = form.postalCode.trim(),
                        region = form.region.trim().ifEmpty { null },
                        countryCode = home.restaurant.address?.countryCode ?: "ES",
                    ) else null,
                    publicEmail = home.restaurant.publicEmail,
                    publicPhone = home.restaurant.publicPhone,
                    openingHours = home.restaurant.openingHours,
                ),
            )) {
                is RepositoryResult.Success -> {
                    ownerHome = ownerHome?.copy(restaurant = result.value)
                    val imageResult = form.pendingImageUpload?.let { replaceOwnerRestaurantImage(it) }
                    if (imageResult is RepositoryResult.Failure) {
                        updateRestaurantForm { copy(isSaving = false, error = imageResult.error.toUiError()) }
                        publishLoadedContent()
                        return@launch
                    }
                    if (imageResult is RepositoryResult.Success) {
                        ownerHome = ownerHome?.copy(restaurant = ownerHome!!.restaurant.copy(heroImage = imageResult.value))
                    }
                    _uiState.value = _uiState.value.copy(editor = null)
                    publishLoadedContent()
                }
                is RepositoryResult.Failure -> updateRestaurantForm { copy(isSaving = false, error = result.error.toUiError()) }
            }
        }
    }

    fun onSaveDish() {
        val form = (_uiState.value.editor as? RestaurantHomeEditor.Dish)?.form ?: return
        if (form.isSaving) return
        val price = form.price.toMoneyOrNull()
        val validation = DishFormValidation(nameInvalid = form.name.isBlank(), priceInvalid = price == null)
        if (validation.nameInvalid || validation.priceInvalid) {
            updateDishForm { copy(validation = validation) }
            return
        }
        updateDishForm { copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val draft = OwnerDishDraft(
                name = form.name,
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
                    // A failed image upload must retry this persisted dish, not create another one.
                    if (form.dishId == null) updateDishForm { copy(dishId = savedDishId.value) }
                    val imageResult = form.pendingImageUpload?.let { replaceOwnerDishImage(savedDishId, it) }
                    if (imageResult is RepositoryResult.Failure) {
                        updateDishForm { copy(isSaving = false, error = imageResult.error.toUiError()) }
                        publishLoadedContent()
                        return@launch
                    }
                    if (imageResult is RepositoryResult.Success) applyDishImage(savedDishId.value, imageResult.value)
                    _uiState.value = _uiState.value.copy(editor = null)
                    publishLoadedContent()
                }
                is RepositoryResult.Failure -> updateDishForm { copy(isSaving = false, error = result.error.toUiError()) }
            }
        }
    }

    private fun load() {
        _uiState.value = _uiState.value.copy(content = RestaurantHomeContent.Loading, editor = null)
        viewModelScope.launch {
            refreshOwnerHome()
        }
    }

    private suspend fun refreshOwnerHome() {
        when (val result = loadOwnerRestaurantHome()) {
            is RepositoryResult.Success -> {
                ownerHome = result.value
                publishLoadedContent()
            }
            is RepositoryResult.Failure -> publishError(result.error.toUiError())
        }
    }

    private fun publishLoadedContent() {
        val home = ownerHome ?: return
        _uiState.value = _uiState.value.copy(
            content = RestaurantHomeContent.Loaded(home.toRestaurantHomeData()),
        )
    }

    private fun publishError(error: RestaurantHomeError) {
        _uiState.value = _uiState.value.copy(content = RestaurantHomeContent.Error(error), editor = null)
    }

    private fun updateRestaurantForm(change: RestaurantEditFormUiState.() -> RestaurantEditFormUiState) {
        val editor = _uiState.value.editor as? RestaurantHomeEditor.Restaurant ?: return
        _uiState.value = _uiState.value.copy(editor = RestaurantHomeEditor.Restaurant(editor.form.change()))
    }

    private fun updateDishForm(change: DishEditFormUiState.() -> DishEditFormUiState) {
        val editor = _uiState.value.editor as? RestaurantHomeEditor.Dish ?: return
        _uiState.value = _uiState.value.copy(editor = RestaurantHomeEditor.Dish(editor.form.change()))
    }

    private fun currentEditorSaving(): Boolean = when (val editor = _uiState.value.editor) {
        is RestaurantHomeEditor.Restaurant -> editor.form.isSaving
        is RestaurantHomeEditor.Dish -> editor.form.isSaving
        null -> false
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

    private fun applyDishImage(dishId: String, image: org.shareat.app.domain.model.ImageRef) {
        val home = ownerHome ?: return
        val menu = home.menu ?: return
        ownerHome = home.copy(
            menu = menu.copy(dishes = menu.dishes.map { ratedDish ->
                if (ratedDish.menuDish.dish.id.value == dishId) {
                    ratedDish.copy(menuDish = ratedDish.menuDish.copy(dish = ratedDish.menuDish.dish.copy(image = image)))
                } else ratedDish
            }),
        )
    }
}

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
