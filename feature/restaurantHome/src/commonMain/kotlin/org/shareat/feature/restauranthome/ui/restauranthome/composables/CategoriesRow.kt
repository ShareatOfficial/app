package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_other
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_edit_categories

@Composable
internal fun CategoriesRow(
    categories: List<DishCategory?>,
    selectedCategoryIndex: Int,
    categoryListState: LazyListState,
    onHeightChanged: (Int) -> Unit,
    onCategoryClick: (Int) -> Unit,
    modifier: Modifier = Modifier,
    isEditMode: Boolean = false,
    onEditCategoryClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(it.height) },
    ) {
        LazyRow(
            state = categoryListState,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                bottom = 4.dp,
                top = 4.dp
            ),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background)
        ) {
            item(key = "edit_button_container") {
                AnimatedVisibility(
                    visible = isEditMode,
                    enter = slideInHorizontally(
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = FastOutSlowInEasing,
                        ),
                        initialOffsetX = { -it },
                    ) + expandHorizontally(
                        animationSpec = tween(
                            durationMillis = 280,
                            easing = FastOutSlowInEasing,
                        ),
                        expandFrom = Alignment.Start,
                    ) + fadeIn(
                        animationSpec = tween(
                            durationMillis = 160,
                            delayMillis = 40,
                        ),
                    ),
                    exit = slideOutHorizontally(
                        animationSpec = tween(
                            durationMillis = 180,
                            easing = FastOutLinearInEasing,
                        ),
                        targetOffsetX = { -it },
                    ) + shrinkHorizontally(
                        animationSpec = tween(
                            durationMillis = 180,
                            easing = FastOutLinearInEasing,
                        ),
                        shrinkTowards = Alignment.Start,
                    ) + fadeOut(
                        animationSpec = tween(durationMillis = 120),
                    ),
                    label = "edit_categories_animation",
                ) {
                    AssistChip(
                        modifier = Modifier.heightIn(min = 48.dp),
                        onClick = onEditCategoryClick,
                        label = {
                            Text(
                                text = stringResource(Res.string.restaurant_home_edit_categories),
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = stringResource(Res.string.restaurant_home_edit_categories),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        },
                        colors = AssistChipDefaults.assistChipColors()
                            .copy(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    )
                }
            }

            items(
                count = categories.size,
                key = { index -> categories[index]?.name ?: "other" },
            ) { index ->
                val category = categories[index]
                FilterChip(
                    selected = index == selectedCategoryIndex,
                    onClick = { onCategoryClick(index) },
                    label = {
                        Text(
                            text = category?.label()
                                ?: stringResource(Res.string.restaurant_home_category_other),
                        )
                    },
                    modifier = Modifier.heightIn(min = 48.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            }
        }
        HorizontalDivider()
    }
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun CategoriesRowPreview() {
    CategoriesRowPreviewContent(isEditMode = false)
}

@Preview(showBackground = true, widthDp = 412)
@Composable
private fun CategoriesRowEditModePreview() {
    CategoriesRowPreviewContent(isEditMode = true)
}

@Composable
private fun CategoriesRowPreviewContent(isEditMode: Boolean) {
    ShareatTheme {
        val selectedCategoryIndex = remember { mutableIntStateOf(0) }

        CategoriesRow(
            categories = DishCategory.entries.toList() + null,
            selectedCategoryIndex = selectedCategoryIndex.intValue,
            categoryListState = rememberLazyListState(),
            onHeightChanged = {},
            onCategoryClick = { selectedCategoryIndex.intValue = it },
            isEditMode = isEditMode,
        )
    }
}
