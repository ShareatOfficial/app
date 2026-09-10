package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_all_categories
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_filter
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_hidden
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_published
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_publish_restaurant

@Composable
internal fun FiltersSection(
    categories: List<DishCategory>,
    selectedCategory: DishCategory?,
    allergens: List<EuAllergen>,
    excludedAllergens: Set<EuAllergen>,
    showPublicationSwitch: Boolean,
    isRestaurantPublished: Boolean,
    onCategoryClick: (DishCategory?) -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    onRestaurantPublicationChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (categories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { onCategoryClick(null) },
                        label = { Text(stringResource(Res.string.restaurant_home_all_categories)) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
                items(categories, key = { it.name }) { category ->
                    FilterChip(
                        selected = category == selectedCategory,
                        onClick = { onCategoryClick(category) },
                        label = { Text(category.label()) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
        if (allergens.isNotEmpty()) {
            Text(stringResource(Res.string.restaurant_home_allergen_filter), style = MaterialTheme.typography.labelLarge)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(allergens, key = { it.name }) { allergen ->
                    FilterChip(
                        selected = allergen in excludedAllergens,
                        onClick = { onAllergenClick(allergen) },
                        label = { Text(allergen.label()) },
                        modifier = Modifier.heightIn(min = 48.dp),
                    )
                }
            }
        }
        if (showPublicationSwitch) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(Res.string.restaurant_home_publish_restaurant), style = MaterialTheme.typography.titleSmall)
                    Text(
                        stringResource(
                            if (isRestaurantPublished) Res.string.restaurant_home_published
                            else Res.string.restaurant_home_hidden,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(checked = isRestaurantPublished, onCheckedChange = onRestaurantPublicationChange)
            }
        }
    }
}
