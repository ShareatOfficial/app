package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.shareat.app.domain.model.DishCategory
import org.shareat.feature.restaurant.ui.model.CategoryChipUiState
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
internal fun CategoryChipsRow(
    categories: List<CategoryChipUiState>,
    onCategoryClick: (DishCategory?) -> Unit,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = contentPadding,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(categories) { chip ->
            FilterChip(
                selected = chip.isSelected,
                onClick = { onCategoryClick(chip.category) },
                label = { Text(text = chip.category.label()) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun CategoryChipsRowPreview() {
    ShareatTheme {
        CategoryChipsRow(
            categories = listOf(
                CategoryChipUiState(category = null, isSelected = false),
                CategoryChipUiState(category = DishCategory.Starters, isSelected = true),
                CategoryChipUiState(category = DishCategory.MainCourses, isSelected = false),
                CategoryChipUiState(category = DishCategory.Desserts, isSelected = false),
                CategoryChipUiState(category = DishCategory.SmallBites, isSelected = false),
            ),
            onCategoryClick = {},
        )
    }
}
