package org.shareat.feature.restauranthome.ui.restauranthome.composables.sheetContent

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.feature.restauranthome.ui.model.toDisplayAddress
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomeBottomSheet
import org.shareat.feature.restauranthome.ui.restauranthome.RestaurantHomeUiStateByTone
import org.shareat.feature.restauranthome.ui.restauranthome.composables.ViewDishContent
import org.shareat.shared.designsystem.components.RatingBadge
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_close
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_reviews
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_unrated
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_view_ratings
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_view_address

@Composable
internal fun RestaurantHomeSheetContent(
    sheet: RestaurantHomeBottomSheet,
    uiState: RestaurantHomeUiStateByTone,
    restaurant: RestaurantHomeData,
    onRestaurantNameChange: (String) -> Unit,
    onRestaurantDescriptionChange: (String) -> Unit,
    onRestaurantImageChange: () -> Unit,
    onSaveMainInfo: () -> Unit,
    onSaveAddress: () -> Unit,
    onClose: () -> Unit,
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
    onDishReviewClick: ((String) -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            IconButton(
                onClick = onClose,
                enabled = uiState.mainInfoDraft?.isSaving != true &&
                    uiState.dishEditForm?.isSaving != true &&
                    uiState.addressDraft?.isSaving != true,
                modifier = Modifier.size(48.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = stringResource(Res.string.restaurant_home_close),
                )
            }
        }
        when (sheet) {
            RestaurantHomeBottomSheet.VIEW_DISH -> uiState.selectedDish?.let { dish ->
                ViewDishContent(
                    dish = dish,
                    onReviewClick = onDishReviewClick?.let { callback -> { callback(dish.id) } },
                )
            }

            RestaurantHomeBottomSheet.EDIT_DISH -> {
                uiState.dishEditForm?.let { form ->
                    EditDishContent(
                        dish = uiState.selectedDish,
                        form = form,
                        onImageChange = onDishImageChange,
                        onDishNameChange = onDishNameChange,
                        onDescriptionChange = onDishDescriptionChange,
                        onPriceChange = onDishPriceChange,
                        onAllergenClick = onDishAllergenClick,
                        onSaveClick = onSaveDish,
                    )
                }
            }

            RestaurantHomeBottomSheet.EDIT_MAIN_INFO -> uiState.mainInfoDraft?.let { draft ->
                EditMainInfoContent(
                    restaurantName = draft.name,
                    description = draft.description,
                    restaurantImageUrl = draft.imageUrl,
                    onImageChange = onRestaurantImageChange,
                    onRestaurantNameChange = onRestaurantNameChange,
                    onDescriptionChange = onRestaurantDescriptionChange,
                    onSaveClick = onSaveMainInfo,
                    hasPendingImage = draft.pendingImageUpload != null,
                    isSaving = draft.isSaving,
                    nameInvalid = draft.nameInvalid,
                    error = draft.error,
                )
            }

            RestaurantHomeBottomSheet.VIEW_ADDRESS -> {
                Text(
                    text = stringResource(Res.string.restaurant_home_view_address),
                    style = MaterialTheme.typography.titleLarge,
                )
                Text(
                    text = restaurant.address.toDisplayAddress(),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            RestaurantHomeBottomSheet.EDIT_ADDRESS -> {
                uiState.addressDraft?.let { form ->
                    EditAddressContent(
                        form = form,
                        onStreetLineChange = onAddressStreetLineChange,
                        onLocalityChange = onAddressLocalityChange,
                        onPostalCodeChange = onAddressPostalCodeChange,
                        onRegionChange = onAddressRegionChange,
                        onSaveClick = onSaveAddress,
                    )
                }
            }

            RestaurantHomeBottomSheet.EDIT_CATEGORIES -> {
                CategoriesComingSoonContent()
            }

            RestaurantHomeBottomSheet.VIEW_RATING -> {
                Text(
                    text = stringResource(Res.string.restaurant_home_view_ratings),
                    style = MaterialTheme.typography.headlineSmall,
                )
                restaurant.ratingLabel?.let { RatingBadge(ratingLabel = it) }
                    ?: Text(stringResource(Res.string.restaurant_home_unrated))
                Text(
                    pluralStringResource(
                        Res.plurals.restaurant_home_reviews,
                        restaurant.reviewCount,
                        restaurant.reviewCount,
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

        }
    }
}
