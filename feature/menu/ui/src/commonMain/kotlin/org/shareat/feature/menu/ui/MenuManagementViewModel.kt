package org.shareat.feature.menu.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.MenuItemDraft
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.feature.menu.domain.ArchiveDishUseCase
import org.shareat.feature.menu.domain.DeleteDishUseCase
import org.shareat.feature.menu.domain.DeleteMenuUseCase
import org.shareat.feature.menu.domain.LoadMenuManagementUseCase
import org.shareat.feature.menu.domain.SaveDishUseCase
import org.shareat.feature.menu.domain.SaveRestaurantMenuUseCase

sealed interface MenuManagementUiState {
    data object Loading : MenuManagementUiState
    data class Failure(val message: String) : MenuManagementUiState
    data class Editor(
        val restaurantId: org.shareat.app.domain.model.RestaurantId,
        val restaurantName: String,
        val menuId: org.shareat.app.domain.model.MenuId? = null,
        val name: String = "",
        val description: String = "",
        val publicationState: MenuPublicationState = MenuPublicationState.Draft,
        val items: List<MenuItemEditor> = emptyList(),
        val dishes: List<Dish> = emptyList(),
        val isSaving: Boolean = false,
        val message: String? = null,
        val dishEditor: DishEditorState? = null,
        val confirm: Confirmation? = null,
    ) : MenuManagementUiState
}

data class MenuItemEditor(
    val dish: Dish,
    val price: String,
    val isEnabled: Boolean,
)

data class DishEditorState(
    val id: DishId? = null,
    val name: String = "",
    val description: String = "",
    val enabled: Boolean = true,
    val allergens: Set<EuAllergen> = emptySet(),
    val allergenNote: String = "",
    val image: ImageRef? = null,
    val imageAlternativeText: String = "",
    val pendingImage: ProcessedDishImage? = null,
    val removeImage: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
)

sealed interface Confirmation {
    data class Archive(val id: DishId) : Confirmation
    data class DeleteDish(val id: DishId) : Confirmation
    data object DeleteMenu : Confirmation
}

sealed interface MenuManagementAction {
    data object Retry : MenuManagementAction
    data object Back : MenuManagementAction
    data class NameChanged(val value: String) : MenuManagementAction
    data class DescriptionChanged(val value: String) : MenuManagementAction
    data class PriceChanged(val dishId: DishId, val value: String) : MenuManagementAction
    data class ItemEnabledChanged(val dishId: DishId, val value: Boolean) : MenuManagementAction
    data class AddDish(val dishId: DishId) : MenuManagementAction
    data class RemoveDish(val dishId: DishId) : MenuManagementAction
    data class MoveDish(val dishId: DishId, val offset: Int) : MenuManagementAction
    data object SaveDraft : MenuManagementAction
    data object Publish : MenuManagementAction
    data object Unpublish : MenuManagementAction
    data object RequestDeleteMenu : MenuManagementAction
    data object CreateDish : MenuManagementAction
    data class EditDish(val id: DishId) : MenuManagementAction
    data object DismissDishEditor : MenuManagementAction
    data class DishNameChanged(val value: String) : MenuManagementAction
    data class DishDescriptionChanged(val value: String) : MenuManagementAction
    data class DishEnabledChanged(val value: Boolean) : MenuManagementAction
    data class AllergenChanged(val allergen: EuAllergen, val selected: Boolean) : MenuManagementAction
    data class AllergenNoteChanged(val value: String) : MenuManagementAction
    data class ImageAlternativeTextChanged(val value: String) : MenuManagementAction
    data object RemoveImage : MenuManagementAction
    data object SaveDish : MenuManagementAction
    data class RequestArchiveDish(val id: DishId) : MenuManagementAction
    data class RequestDeleteDish(val id: DishId) : MenuManagementAction
    data object DismissConfirmation : MenuManagementAction
    data object Confirm : MenuManagementAction
}

@KoinViewModel
class MenuManagementViewModel(
    private val load: LoadMenuManagementUseCase,
    private val saveMenu: SaveRestaurantMenuUseCase,
    private val saveDish: SaveDishUseCase,
    private val archiveDish: ArchiveDishUseCase,
    private val deleteDish: DeleteDishUseCase,
    private val deleteMenu: DeleteMenuUseCase,
    private val images: ImageRepository,
    private val imageProcessor: DishImageProcessor,
) : ViewModel() {
    private val _uiState = MutableStateFlow<MenuManagementUiState>(MenuManagementUiState.Loading)
    val uiState: StateFlow<MenuManagementUiState> = _uiState.asStateFlow()

    init { reload() }

    fun onAction(action: MenuManagementAction) {
        when (action) {
            MenuManagementAction.Retry -> reload()
            is MenuManagementAction.NameChanged -> edit { copy(name = action.value, message = null) }
            is MenuManagementAction.DescriptionChanged -> edit { copy(description = action.value, message = null) }
            is MenuManagementAction.PriceChanged -> edit { copy(items = items.map { if (it.dish.id == action.dishId) it.copy(price = action.value) else it }) }
            is MenuManagementAction.ItemEnabledChanged -> edit { copy(items = items.map { if (it.dish.id == action.dishId) it.copy(isEnabled = action.value) else it }) }
            is MenuManagementAction.AddDish -> edit {
                if (items.any { it.dish.id == action.dishId }) this else {
                    val dish = dishes.first { it.id == action.dishId }
                    copy(items = items + MenuItemEditor(dish, "0.00", dish.isEnabled))
                }
            }
            is MenuManagementAction.RemoveDish -> edit { copy(items = items.filterNot { it.dish.id == action.dishId }) }
            is MenuManagementAction.MoveDish -> move(action.dishId, action.offset)
            MenuManagementAction.SaveDraft -> persist(MenuPublicationState.Draft)
            MenuManagementAction.Publish -> persist(MenuPublicationState.Published)
            MenuManagementAction.Unpublish -> persist(MenuPublicationState.Unpublished)
            MenuManagementAction.RequestDeleteMenu -> edit { copy(confirm = Confirmation.DeleteMenu) }
            MenuManagementAction.CreateDish -> edit { copy(dishEditor = DishEditorState()) }
            is MenuManagementAction.EditDish -> edit {
                val dish = dishes.first { it.id == action.id }
                copy(dishEditor = DishEditorState(dish.id, dish.name, dish.description.orEmpty(), dish.isEnabled, dish.allergenDeclaration?.allergens.orEmpty(), dish.allergenDeclaration?.note.orEmpty(), dish.image, dish.image?.alternativeText.orEmpty()))
            }
            MenuManagementAction.DismissDishEditor -> edit { copy(dishEditor = null) }
            is MenuManagementAction.DishNameChanged -> dishEdit { copy(name = action.value, error = null) }
            is MenuManagementAction.DishDescriptionChanged -> dishEdit { copy(description = action.value, error = null) }
            is MenuManagementAction.DishEnabledChanged -> dishEdit { copy(enabled = action.value) }
            is MenuManagementAction.AllergenChanged -> dishEdit { copy(allergens = if (action.selected) allergens + action.allergen else allergens - action.allergen) }
            is MenuManagementAction.AllergenNoteChanged -> dishEdit { copy(allergenNote = action.value) }
            is MenuManagementAction.ImageAlternativeTextChanged -> dishEdit { copy(imageAlternativeText = action.value) }
            MenuManagementAction.RemoveImage -> dishEdit { copy(image = null, pendingImage = null, removeImage = true) }
            MenuManagementAction.SaveDish -> persistDish()
            is MenuManagementAction.RequestArchiveDish -> edit { copy(confirm = Confirmation.Archive(action.id)) }
            is MenuManagementAction.RequestDeleteDish -> edit { copy(confirm = Confirmation.DeleteDish(action.id)) }
            MenuManagementAction.DismissConfirmation -> edit { copy(confirm = null) }
            MenuManagementAction.Confirm -> confirm()
            MenuManagementAction.Back -> Unit
        }
    }

    fun onDishImageSelected(displayName: String, bytes: ByteArray) {
        val editor = (_uiState.value as? MenuManagementUiState.Editor)?.dishEditor ?: return
        if (editor.isSaving) return
        viewModelScope.launch {
            imageProcessor(displayName, bytes).fold(
                onSuccess = { image -> dishEdit { copy(pendingImage = image, removeImage = false, error = null) } },
                onFailure = { error -> dishEdit { copy(error = error.message ?: "Unable to process the image.") } },
            )
        }
    }

    private fun reload() = viewModelScope.launch {
        _uiState.value = MenuManagementUiState.Loading
        when (val result = load()) {
            is RepositoryResult.Success -> {
                val details = result.value.menu
                _uiState.value = MenuManagementUiState.Editor(
                    restaurantId = result.value.restaurant.id,
                    restaurantName = result.value.restaurant.name,
                    menuId = details?.menu?.id,
                    name = details?.menu?.name.orEmpty(),
                    description = details?.menu?.description.orEmpty(),
                    publicationState = details?.menu?.publicationState ?: MenuPublicationState.Draft,
                    items = details?.items.orEmpty().map { MenuItemEditor(it.dish, it.price.toEditablePrice(), it.isEnabled) },
                    dishes = result.value.dishes,
                )
            }
            is RepositoryResult.Failure -> _uiState.value = MenuManagementUiState.Failure(result.error.message())
        }
    }

    private fun persist(state: MenuPublicationState) {
        val editor = _uiState.value as? MenuManagementUiState.Editor ?: return
        val items = editor.items.mapIndexed { position, item ->
            val cents = item.price.toEuroCents() ?: return edit { copy(message = "Use a valid EUR price, for example 12.50.") }
            MenuItemDraft(item.dish.id, Money(cents), position, item.isEnabled)
        }
        if (editor.name.isBlank()) return edit { copy(message = "Menu name is required.") }
        if (state == MenuPublicationState.Published && items.none { it.isEnabled && editor.items.any { item -> item.dish.id == it.dishId && item.dish.isEnabled } }) {
            return edit { copy(message = "Publishing requires an enabled dish with a price.") }
        }
        viewModelScope.launch {
            edit { copy(isSaving = true, message = null) }
            when (val result = saveMenu(RestaurantMenuDraft(editor.restaurantId, editor.menuId, editor.name.trim(), editor.description.trim().ifEmpty { null }, state, items))) {
                is RepositoryResult.Success -> reload()
                is RepositoryResult.Failure -> edit { copy(isSaving = false, message = result.error.message()) }
            }
        }
    }

    private fun persistDish() {
        val editor = _uiState.value as? MenuManagementUiState.Editor ?: return
        val draft = editor.dishEditor ?: return
        if (draft.name.isBlank()) return dishEdit { copy(error = "Dish name is required.") }
        viewModelScope.launch {
            dishEdit { copy(isSaving = true, error = null) }
            val allergens = AllergenDeclaration(draft.allergens, draft.allergenNote.trim().ifEmpty { null })
                .takeIf { it.allergens.isNotEmpty() || it.note != null }
            when (val result = saveDish(DishDraft(editor.restaurantId, draft.id, draft.name.trim(), draft.description.trim().ifEmpty { null }, allergens, draft.enabled))) {
                is RepositoryResult.Success -> saveDishImage(result.value.id, draft)
                is RepositoryResult.Failure -> dishEdit { copy(isSaving = false, error = result.error.message()) }
            }
        }
    }

    private suspend fun saveDishImage(id: DishId, editor: DishEditorState) {
        val result = when {
            editor.pendingImage != null -> images.replaceImage(
                ImageTarget.DishImage(id),
                ImageUpload(editor.pendingImage.bytes, editor.pendingImage.mimeType, editor.imageAlternativeText.trim().ifEmpty { null }),
            )
            editor.removeImage -> images.deleteImage(ImageTarget.DishImage(id))
            else -> RepositoryResult.Success(Unit)
        }
        when (result) {
            is RepositoryResult.Success -> reload()
            is RepositoryResult.Failure -> dishEdit { copy(isSaving = false, error = "Dish saved, but image update failed: ${result.error.message()}") }
        }
    }

    private fun confirm() {
        val editor = _uiState.value as? MenuManagementUiState.Editor ?: return
        when (val confirmation = editor.confirm ?: return) {
            is Confirmation.Archive -> runMutation { archiveDish(confirmation.id) }
            is Confirmation.DeleteDish -> runMutation { deleteDish(confirmation.id) }
            Confirmation.DeleteMenu -> {
                val id = editor.menuId ?: return edit { copy(confirm = null) }
                runMutation { deleteMenu(id) }
            }
        }
    }

    private fun runMutation(operation: suspend () -> RepositoryResult<Unit>) = viewModelScope.launch {
        edit { copy(isSaving = true, confirm = null, message = null) }
        when (val result = operation()) {
            is RepositoryResult.Success -> reload()
            is RepositoryResult.Failure -> edit { copy(isSaving = false, message = result.error.message()) }
        }
    }

    private fun move(id: DishId, offset: Int) = edit {
        val index = items.indexOfFirst { it.dish.id == id }
        val destination = index + offset
        if (index < 0 || destination !in items.indices) this else copy(items = items.toMutableList().also { list ->
            val item = list.removeAt(index); list.add(destination, item)
        })
    }

    private fun edit(transform: MenuManagementUiState.Editor.() -> MenuManagementUiState.Editor) = _uiState.update {
        (it as? MenuManagementUiState.Editor)?.transform() ?: it
    }
    private fun dishEdit(transform: DishEditorState.() -> DishEditorState) = edit {
        dishEditor?.let { copy(dishEditor = it.transform()) } ?: this
    }
}

private fun Money.toEditablePrice(): String = "${minorUnits / 100}.${(minorUnits % 100).toString().padStart(2, '0')}"
private fun String.toEuroCents(): Long? {
    val match = Regex("^\\d+(?:[.,]\\d{1,2})?$").matchEntire(trim()) ?: return null
    val parts = replace(',', '.').split('.')
    return parts[0].toLongOrNull()?.times(100)?.plus(parts.getOrNull(1)?.padEnd(2, '0')?.toLongOrNull() ?: 0)
}
private fun RepositoryError.message(): String = when (this) {
    RepositoryError.Offline -> "No connection. Try again when online."
    RepositoryError.Unauthenticated -> "Your session has expired."
    RepositoryError.Forbidden -> "You do not have permission to change this menu."
    is RepositoryError.Validation -> reason
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "The requested item no longer exists."
    is RepositoryError.AlreadyExists -> "This item already exists."
    RepositoryError.InvalidCredentials -> "Your session credentials are invalid."
    is RepositoryError.Unavailable -> "The service is temporarily unavailable."
}
