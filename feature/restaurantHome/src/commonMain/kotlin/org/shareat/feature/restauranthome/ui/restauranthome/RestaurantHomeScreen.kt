package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ImageUpload
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeEditor
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeMode
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeUiState
import org.shareat.feature.restauranthome.ui.model.ImageUploadValidationResult
import org.shareat.feature.restauranthome.ui.model.imageUploadFrom
import org.shareat.feature.restauranthome.ui.restauranthome.composables.CustomerModeTopBar
import org.shareat.feature.restauranthome.ui.restauranthome.composables.DishEditBottomSheet
import org.shareat.feature.restauranthome.ui.restauranthome.composables.ErrorContent
import org.shareat.feature.restauranthome.ui.restauranthome.composables.FiltersSection
import org.shareat.feature.restauranthome.ui.restauranthome.composables.ManagementDishCard
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantEditBottomSheet
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantHeaderCard
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantHomeEmptyContent
import org.shareat.feature.restauranthome.ui.restauranthome.composables.RestaurantHomeSkeleton
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme
import org.jetbrains.compose.resources.stringResource
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_add_dish

private val ScreenPadding = 16.dp

/** Owner landing screen with an in-app FileKit image picker and validated uploads. */
@Composable
fun RestaurantHomeScreen(
    modifier: Modifier = Modifier,
    viewModel: RestaurantHomeViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    var imageTarget by remember { mutableStateOf<ImagePickerTarget?>(null) }
    val imagePicker = rememberFilePickerLauncher(type = FileKitType.Image) { file ->
        val target = imageTarget
        imageTarget = null
        if (file == null || target == null) return@rememberFilePickerLauncher
        scope.launch {
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
    RestaurantHomeScreenStateless(
        uiState = uiState,
        modifier = modifier,
        onRetryClick = viewModel::onRetryClick,
        onModeToggle = viewModel::onModeToggle,
        onCategoryClick = viewModel::onCategoryClick,
        onAllergenClick = viewModel::onAllergenClick,
        onRestaurantPublicationChange = viewModel::onRestaurantPublicationChange,
        onEditRestaurantClick = viewModel::onEditRestaurantClick,
        onAddDishClick = viewModel::onAddDishClick,
        onEditDishClick = viewModel::onEditDishClick,
        onDismissEditor = viewModel::onDismissEditor,
        onRestaurantNameChanged = viewModel::onRestaurantNameChanged,
        onRestaurantStreetChanged = viewModel::onRestaurantStreetChanged,
        onRestaurantLocalityChanged = viewModel::onRestaurantLocalityChanged,
        onRestaurantPostalCodeChanged = viewModel::onRestaurantPostalCodeChanged,
        onRestaurantRegionChanged = viewModel::onRestaurantRegionChanged,
        onRestaurantDescriptionChanged = viewModel::onRestaurantDescriptionChanged,
        onRestaurantImageRequested = {
            imageTarget = ImagePickerTarget.Restaurant
            imagePicker.launch()
        },
        onSaveRestaurant = viewModel::onSaveRestaurant,
        onDishNameChanged = viewModel::onDishNameChanged,
        onDishDescriptionChanged = viewModel::onDishDescriptionChanged,
        onDishPriceChanged = viewModel::onDishPriceChanged,
        onDishAllergenClick = viewModel::onDishAllergenClick,
        onDishPublicationChange = viewModel::onDishPublicationChange,
        onDishImageRequested = {
            imageTarget = ImagePickerTarget.Dish
            imagePicker.launch()
        },
        onSaveDish = viewModel::onSaveDish,
    )
}

private enum class ImagePickerTarget { Restaurant, Dish }

private suspend fun PlatformFile.toImageUploadValidationResult(): ImageUploadValidationResult = runCatching {
    imageUploadFrom(
        bytes = readBytes(),
        mimeType = mimeType()?.toString()?.substringBefore(';'),
        fileName = name,
    )
}.getOrElse { ImageUploadValidationResult.InvalidFile }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantHomeScreenStateless(
    uiState: RestaurantHomeUiState,
    modifier: Modifier = Modifier,
    onRetryClick: () -> Unit = {},
    onModeToggle: () -> Unit = {},
    onCategoryClick: (DishCategory?) -> Unit = {},
    onAllergenClick: (EuAllergen) -> Unit = {},
    onRestaurantPublicationChange: (Boolean) -> Unit = {},
    onEditRestaurantClick: () -> Unit = {},
    onAddDishClick: () -> Unit = {},
    onEditDishClick: (String) -> Unit = {},
    onDismissEditor: () -> Unit = {},
    onRestaurantNameChanged: (String) -> Unit = {},
    onRestaurantStreetChanged: (String) -> Unit = {},
    onRestaurantLocalityChanged: (String) -> Unit = {},
    onRestaurantPostalCodeChanged: (String) -> Unit = {},
    onRestaurantRegionChanged: (String) -> Unit = {},
    onRestaurantDescriptionChanged: (String) -> Unit = {},
    onRestaurantImageRequested: (() -> Unit)? = null,
    onSaveRestaurant: () -> Unit = {},
    onDishNameChanged: (String) -> Unit = {},
    onDishDescriptionChanged: (String) -> Unit = {},
    onDishPriceChanged: (String) -> Unit = {},
    onDishAllergenClick: (EuAllergen) -> Unit = {},
    onDishPublicationChange: (Boolean) -> Unit = {},
    onDishImageRequested: (() -> Unit)? = null,
    onSaveDish: () -> Unit = {},
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CustomerModeTopBar(
                mode = uiState.mode,
                onModeToggle = onModeToggle,
            )
        },
    ) { padding ->
        when (val content = uiState.content) {
            RestaurantHomeContent.Loading -> RestaurantHomeSkeleton(
                modifier = Modifier.padding(padding),
            )
            is RestaurantHomeContent.Error -> ErrorContent(
                error = content.reason,
                onRetryClick = onRetryClick,
                modifier = Modifier.padding(padding),
            )
            RestaurantHomeContent.Empty -> RestaurantHomeEmptyContent(
                modifier = Modifier.padding(padding),
            )
            is RestaurantHomeContent.Loaded -> RestaurantHomeLoadedContent(
                uiState = uiState,
                restaurant = content.restaurant,
                onCategoryClick = onCategoryClick,
                onAllergenClick = onAllergenClick,
                onRestaurantPublicationChange = onRestaurantPublicationChange,
                onEditRestaurantClick = onEditRestaurantClick,
                onAddDishClick = onAddDishClick,
                onEditDishClick = onEditDishClick,
                modifier = Modifier.padding(padding),
            )
        }
    }

    when (val editor = uiState.editor) {
        is RestaurantHomeEditor.Restaurant -> RestaurantEditBottomSheet(
            form = editor.form,
            onDismiss = onDismissEditor,
            onNameChange = onRestaurantNameChanged,
            onStreetChange = onRestaurantStreetChanged,
            onLocalityChange = onRestaurantLocalityChanged,
            onPostalCodeChange = onRestaurantPostalCodeChanged,
            onRegionChange = onRestaurantRegionChanged,
            onDescriptionChange = onRestaurantDescriptionChanged,
            onRequestImage = onRestaurantImageRequested,
            onSave = onSaveRestaurant,
        )
        is RestaurantHomeEditor.Dish -> DishEditBottomSheet(
            form = editor.form,
            onDismiss = onDismissEditor,
            onNameChange = onDishNameChanged,
            onDescriptionChange = onDishDescriptionChanged,
            onPriceChange = onDishPriceChanged,
            onAllergenClick = onDishAllergenClick,
            onPublishedChange = onDishPublicationChange,
            onRequestImage = onDishImageRequested,
            onSave = onSaveDish,
        )
        null -> Unit
    }
}

@Composable
private fun RestaurantHomeLoadedContent(
    uiState: RestaurantHomeUiState,
    restaurant: org.shareat.feature.restauranthome.ui.model.RestaurantHomeData,
    onCategoryClick: (DishCategory?) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    onRestaurantPublicationChange: (Boolean) -> Unit,
    onEditRestaurantClick: () -> Unit,
    onAddDishClick: () -> Unit,
    onEditDishClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isManagement = uiState.mode == RestaurantHomeMode.MANAGEMENT
    val filteredDishes = restaurant.dishes.filter { dish ->
        (uiState.selectedCategory == null || dish.category == uiState.selectedCategory) &&
            dish.allergens.none { it in uiState.excludedAllergens } &&
            (isManagement || (restaurant.isPublished && dish.isPublished))
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = ScreenPadding, end = ScreenPadding, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RestaurantHeaderCard(
                name = restaurant.name,
                description = restaurant.description,
                imageUrl = restaurant.imageUrl,
                imageDescription = restaurant.imageDescription,
                address = restaurant.address.streetLine,
                ratingLabel = restaurant.ratingLabel,
                reviewCount = restaurant.reviewCount,
                isManagement = isManagement,
                onEditClick = onEditRestaurantClick,
            )
        }
        item {
            FiltersSection(
                categories = restaurant.categories,
                selectedCategory = uiState.selectedCategory,
                allergens = restaurant.allergens,
                excludedAllergens = uiState.excludedAllergens,
                showPublicationSwitch = isManagement,
                isRestaurantPublished = restaurant.isPublished,
                onCategoryClick = onCategoryClick,
                onAllergenClick = onAllergenClick,
                onRestaurantPublicationChange = onRestaurantPublicationChange,
            )
        }
        if (isManagement) {
            item {
                Button(
                    onClick = onAddDishClick,
                    modifier = Modifier.fillMaxWidth().sizeIn(minHeight = 48.dp),
                ) {
                    Text(stringResource(Res.string.restaurant_home_add_dish))
                }
            }
        }
        if (filteredDishes.isEmpty()) {
            item {
                RestaurantHomeEmptyContent(
                    isManagement = isManagement,
                    onAddDishClick = onAddDishClick,
                )
            }
        } else {
            items(filteredDishes, key = { it.id }) { dish ->
                ManagementDishCard(
                    dish = dish,
                    showEdit = isManagement,
                    onEditClick = { onEditDishClick(dish.id) },
                )
            }
        }
    }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeLoadedPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.loaded) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomePreviewModePreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.preview) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeLoadingPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomeUiState()) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeErrorPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.error) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeEmptyPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.empty) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeRestaurantEditorPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.restaurantEditor) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeNewDishEditorPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.newDishEditor) }
}

@FormFactorPreviews
@Composable
private fun RestaurantHomeEditDishEditorPreview() {
    ShareatTheme { RestaurantHomeScreenStateless(uiState = RestaurantHomePreviewData.editDishEditor) }
}
