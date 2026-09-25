package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.DishEditFormUiState
import org.shareat.feature.restauranthome.ui.model.ImageUploadValidationResult
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.preparedImageUpload
import org.shareat.feature.restauranthome.ui.model.toEditablePrice
import org.shareat.feature.restauranthome.ui.restauranthome.composables.sheetContent.RestaurantHomeSheetContent
import org.shareat.feature.restauranthome.ui.restauranthome.composables.message
import org.jetbrains.compose.resources.stringResource
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_empty_management
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_retry
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme

//Three parts. Top Bar with the options that the restaurant have. Switch to customer preview. change layout type.
/*
What things I want to make the restaurant do?
- View the menu as a customer
- Create QR code to let the user navigate to the restaurant.
- Switch between restaurant publish state. Only if it achieved the values. Name, description and more.
- Change layout type. Out of MVP scope.
 */
// TODO I forgot to add fields as restaurant number. Do not implement it for now.
@Composable
fun RestaurantHomeScreen(
    modifier: Modifier = Modifier,
    onDishReviewClick: ((String) -> Unit)? = null,
    viewModel: RestaurantHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    // picker image things should be manage in viewModel if possible.
    val pickerScope = rememberCoroutineScope()
    var imageTarget by remember { mutableStateOf<ImagePickerTarget?>(null) }
    val imagePicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        val target = imageTarget
        imageTarget = null
        if (file == null || target == null) return@rememberFilePickerLauncher

        pickerScope.launch {
            when (val result = file.toImageUploadValidationResult()) {
                is ImageUploadValidationResult.Success -> when (target) {
                    ImagePickerTarget.Restaurant -> viewModel.onRestaurantImageSelected(result.upload)
                    ImagePickerTarget.Dish -> viewModel.onDishImageSelected(result.upload)
                }

                else -> when (target) {
                    ImagePickerTarget.Restaurant -> viewModel.onRestaurantImagePickerFailure(result)
                    ImagePickerTarget.Dish -> viewModel.onDishImagePickerFailure(result)
                }
            }
        }
    }

    RestaurantHomeStateless(
        uiState = uiState,
        modifier = modifier,
        onRetryClick = viewModel::onRetryClick,
        onEditModeChange = viewModel::onEditModeChange,
        onPublicationStateChange = viewModel::onPublicationStateChange,
        onMainInfoClick = viewModel::onMainInfoClick,
        onAddressClick = viewModel::onAddressClick,
        onRatingClick = viewModel::onRatingClick,
        onCategoriesEditClick = viewModel::onCategoriesEditClick,
        onDishClick = viewModel::onDishClick,
        onAddDishClick = viewModel::onAddDishClick,
        onDismissBottomSheet = viewModel::onDismissBottomSheet,
        onRestaurantNameChange = viewModel::onRestaurantNameChange,
        onRestaurantDescriptionChange = viewModel::onRestaurantDescriptionChange,
        onRestaurantImageChange = {
            imageTarget = ImagePickerTarget.Restaurant
            imagePicker.launch()
        },
        onSaveMainInfo = viewModel::onSaveMainInfo,
        onDishImageChange = {
            imageTarget = ImagePickerTarget.Dish
            imagePicker.launch()
        },
        onDishNameChange = viewModel::onDishNameChange,
        onDishDescriptionChange = viewModel::onDishDescriptionChange,
        onDishPriceChange = viewModel::onDishPriceChange,
        onDishAllergenClick = viewModel::onDishAllergenClick,
        onSaveDish = viewModel::onSaveDish,
        onAddressStreetLineChange = viewModel::onAddressStreetLineChange,
        onAddressLocalityChange = viewModel::onAddressLocalityChange,
        onAddressPostalCodeChange = viewModel::onAddressPostalCodeChange,
        onAddressRegionChange = viewModel::onAddressRegionChange,
        onCategoryClick = viewModel::onCategoryClick,
        onDishReviewClick = onDishReviewClick,
    )
}

private enum class ImagePickerTarget { Restaurant, Dish }

private suspend fun PlatformFile.toImageUploadValidationResult(): ImageUploadValidationResult =
    preparedImageUpload(fileName = name) { compression -> compressedAsJpeg(compression) }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantHomeStateless(
    uiState: RestaurantHomeUiStateByTone,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit,
    onEditModeChange: (Boolean) -> Unit,
    onPublicationStateChange: (Boolean) -> Unit,
    onMainInfoClick: () -> Unit,
    onAddressClick: () -> Unit,
    onRatingClick: () -> Unit,
    onCategoriesEditClick: () -> Unit,
    onDishClick: (String) -> Unit,
    onAddDishClick: () -> Unit,
    onDismissBottomSheet: () -> Unit,
    onRestaurantNameChange: (String) -> Unit,
    onRestaurantDescriptionChange: (String) -> Unit,
    onRestaurantImageChange: () -> Unit,
    onSaveMainInfo: () -> Unit,
    onDishImageChange: () -> Unit,
    onDishNameChange: (String) -> Unit,
    onDishDescriptionChange: (String) -> Unit,
    onDishPriceChange: (String) -> Unit,
    onDishAllergenClick: (EuAllergen) -> Unit,
    onSaveDish: () -> Unit,
    onAddressStreetLineChange: (String) -> Unit,
    onAddressLocalityChange: (String) -> Unit,
    onAddressPostalCodeChange: (String) -> Unit,
    onAddressRegionChange: (String) -> Unit,
    onCategoryClick: (DishCategory) -> Unit,
    onDishReviewClick: ((String) -> Unit)? = null,
) {
    when (val content = uiState.content) {
        RestaurantHomeContent.Loading -> Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        is RestaurantHomeContent.Error -> RestaurantHomeStatus(
            message = content.error.message(),
            onRetryClick = onRetryClick,
            modifier = modifier,
        )
        RestaurantHomeContent.Empty -> RestaurantHomeStatus(
            message = stringResource(Res.string.restaurant_home_empty_management),
            onRetryClick = onRetryClick,
            modifier = modifier,
        )

        is RestaurantHomeContent.Loaded -> RestaurantHomeCompact(
            restaurant = content.restaurant,
            isEditMode = uiState.isEditMode,
            onEditModeChange = onEditModeChange,
            isPublicationUpdating = uiState.isPublicationUpdating,
            publicationError = uiState.publicationError,
            onPublicationStateChange = onPublicationStateChange,
            onOpenDirectionsClick = onAddressClick,
            onRateClick = onRatingClick,
            onEditMainInfoClick = onMainInfoClick,
            onEditCategoryClick = onCategoriesEditClick,
            onDishClick = { onDishClick(it.id) },
            modifier = modifier.fillMaxSize(),
            onAddDishClick = onAddDishClick,
        )
    }

    val activeBottomSheet = uiState.activeBottomSheet
    val restaurant = (uiState.content as? RestaurantHomeContent.Loaded)?.restaurant
    // TODO for web ideally we have here a check and if the screen is Desktop size we use a Side Sheet. Do not implement for the moment
    if (activeBottomSheet != null && restaurant != null) {
        ModalBottomSheet(
            onDismissRequest = onDismissBottomSheet,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
        ) {
            RestaurantHomeSheetContent(
                sheet = activeBottomSheet,
                uiState = uiState,
                restaurant = restaurant,
                onRestaurantNameChange = onRestaurantNameChange,
                onRestaurantDescriptionChange = onRestaurantDescriptionChange,
                onRestaurantImageChange = onRestaurantImageChange,
                onSaveMainInfo = onSaveMainInfo,
                onDishImageChange = onDishImageChange,
                onDishNameChange = onDishNameChange,
                onDishDescriptionChange = onDishDescriptionChange,
                onDishPriceChange = onDishPriceChange,
                onDishAllergenClick = onDishAllergenClick,
                onSaveDish = onSaveDish,
                onAddressStreetLineChange = onAddressStreetLineChange,
                onAddressLocalityChange = onAddressLocalityChange,
                onAddressPostalCodeChange = onAddressPostalCodeChange,
                onAddressRegionChange = onAddressRegionChange,
                onCategoryClick = onCategoryClick,
                onDishReviewClick = onDishReviewClick,
            )
        }
    }
}

@Composable
private fun RestaurantHomeStatus(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(message)
            Button(onClick = onRetryClick) { Text(stringResource(Res.string.restaurant_home_retry)) }
        }
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeStatelessPreview() {
    ShareatTheme {
        val content = RestaurantHomePreviewData.loaded.content
        val restaurant = (content as RestaurantHomeContent.Loaded).restaurant
        var uiState by remember {
            mutableStateOf(RestaurantHomeUiStateByTone(content = content))
        }

        RestaurantHomeStateless(
            uiState = uiState,
            onEditModeChange = { isEditMode ->
                uiState = uiState.copy(
                    isEditMode = isEditMode,
                    activeBottomSheet = null,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onPublicationStateChange = {},
            onMainInfoClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.EDIT_MAIN_INFO,
                    mainInfoDraft = RestaurantMainInfoDraft(
                        name = restaurant.name,
                        description = restaurant.description.orEmpty(),
                        imageUrl = restaurant.imageUrl.orEmpty(),
                    ),
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onAddressClick = {
                val isEditMode = uiState.isEditMode
                uiState = uiState.copy(
                    activeBottomSheet = if (isEditMode) {
                        RestaurantHomeBottomSheet.EDIT_ADDRESS
                    } else {
                        RestaurantHomeBottomSheet.VIEW_ADDRESS
                    },
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = if (isEditMode) {
                        RestaurantAddressDraft(
                            streetLine = restaurant.address.streetLine,
                            locality = restaurant.address.locality,
                            postalCode = restaurant.address.postalCode,
                            region = restaurant.address.region.orEmpty(),
                            countryCode = restaurant.address.countryCode,
                        )
                    } else {
                        null
                    },
                    categoriesDraft = null,
                )
            },
            onRatingClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.VIEW_RATING,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onCategoriesEditClick = {
                uiState = uiState.copy(
                    activeBottomSheet = RestaurantHomeBottomSheet.EDIT_CATEGORIES,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = restaurant.categories.toSet(),
                )
            },
            onDishClick = { dishId ->
                val dish = restaurant.dishes.firstOrNull { it.id == dishId }
                uiState = uiState.copy(
                    activeBottomSheet = if (uiState.isEditMode) {
                        RestaurantHomeBottomSheet.EDIT_DISH
                    } else {
                        RestaurantHomeBottomSheet.VIEW_DISH
                    },
                    selectedDishId = dishId,
                    dishEditForm = if (uiState.isEditMode && dish != null) {
                        DishEditFormUiState(
                            dishId = dish.id,
                            name = dish.name,
                            description = dish.description.orEmpty(),
                            price = dish.priceMinorUnits.toEditablePrice(),
                            allergens = dish.allergens,
                            imageUrl = dish.imageUrl,
                            isPublished = dish.isPublished,
                        )
                    } else {
                        null
                    },
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onAddDishClick = {
                uiState = uiState.copy(
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
            },
            onDismissBottomSheet = {
                uiState = uiState.copy(
                    activeBottomSheet = null,
                    selectedDishId = null,
                    mainInfoDraft = null,
                    dishEditForm = null,
                    addressDraft = null,
                    categoriesDraft = null,
                )
            },
            onRestaurantNameChange = { value ->
                uiState = uiState.copy(mainInfoDraft = uiState.mainInfoDraft?.copy(name = value))
            },
            onRestaurantDescriptionChange = { value ->
                uiState = uiState.copy(
                    mainInfoDraft = uiState.mainInfoDraft?.copy(description = value),
                )
            },
            onDishNameChange = { value ->
                uiState = uiState.copy(dishEditForm = uiState.dishEditForm?.copy(name = value))
            },
            onDishDescriptionChange = { value ->
                uiState = uiState.copy(
                    dishEditForm = uiState.dishEditForm?.copy(description = value),
                )
            },
            onDishPriceChange = { value ->
                uiState = uiState.copy(dishEditForm = uiState.dishEditForm?.copy(price = value))
            },
            onDishAllergenClick = { allergen ->
                val form = uiState.dishEditForm
                if (form != null) {
                    uiState = uiState.copy(
                        dishEditForm = form.copy(
                            allergens = if (allergen in form.allergens) {
                                form.allergens - allergen
                            } else {
                                form.allergens + allergen
                            },
                        ),
                    )
                }
            },
            onSaveDish = {
                val form = uiState.dishEditForm
                if (form != null) {
                    val nameInvalid = form.name.isBlank()
                    val priceInvalid = form.price.isBlank()
                    uiState = if (nameInvalid || priceInvalid) {
                        uiState.copy(
                            dishEditForm = form.copy(
                                validation = form.validation.copy(
                                    nameInvalid = nameInvalid,
                                    priceInvalid = priceInvalid,
                                ),
                            ),
                        )
                    } else {
                        uiState.copy(
                            activeBottomSheet = null,
                            selectedDishId = null,
                            dishEditForm = null,
                        )
                    }
                }
            },
            onAddressStreetLineChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(streetLine = value),
                )
            },
            onAddressLocalityChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(locality = value),
                )
            },
            onAddressPostalCodeChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(postalCode = value),
                )
            },
            onAddressRegionChange = { value ->
                uiState = uiState.copy(
                    addressDraft = uiState.addressDraft?.copy(region = value),
                )
            },
            onCategoryClick = { category ->
                val categories = uiState.categoriesDraft
                if (categories != null) {
                    uiState = uiState.copy(
                        categoriesDraft = if (category in categories) {
                            categories - category
                        } else {
                            categories + category
                        },
                    )
                }
            },
            onRetryClick = {},
            onRestaurantImageChange = {},
            onSaveMainInfo = {},
            onDishImageChange = {},
            onDishReviewClick = {},
        )
    }
}
