package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
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

@OptIn(ExperimentalLayoutApi::class)
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategoryClick(null) },
                    label = { Text(stringResource(Res.string.restaurant_home_all_categories)) },
                    modifier = Modifier.heightIn(min = 48.dp),
                )
                categories.forEach { category ->
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
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                allergens.forEach { allergen ->
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
            androidx.compose.material3.ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .padding(top = 2.dp)
                    .toggleable(
                        value = isRestaurantPublished,
                        role = Role.Switch,
                        onValueChange = onRestaurantPublicationChange,
                    )
                    .semantics(mergeDescendants = true) {},
                headlineContent = {
                    Text(
                        stringResource(Res.string.restaurant_home_publish_restaurant),
                        style = MaterialTheme.typography.titleSmall,
                    )
                },
                supportingContent = {
                    Text(
                        stringResource(
                            if (isRestaurantPublished) Res.string.restaurant_home_published
                            else Res.string.restaurant_home_hidden,
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
                trailingContent = {
                    Switch(checked = isRestaurantPublished, onCheckedChange = null)
                },
            )
        }
    }
}
