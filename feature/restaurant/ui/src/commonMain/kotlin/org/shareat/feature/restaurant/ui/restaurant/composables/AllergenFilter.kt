package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restaurant.ui.model.AllergenChipUiState
import org.shareat.feature.restaurant.ui.model.AllergenFilterUiState
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.restaurant_allergen_excluded
import shareat.feature.restaurant.ui.generated.resources.restaurant_allergen_filter
import shareat.feature.restaurant.ui.generated.resources.restaurant_allergen_included

private val PanelShape = RoundedCornerShape(16.dp)
private val PanelElevation = 6.dp
private val ChipIconSize = 18.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AllergenFilter(
    state: AllergenFilterUiState,
    onToggleExpanded: () -> Unit,
    onAllergenClick: (EuAllergen) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.allergens.isEmpty()) return
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AssistChip(
            onClick = onToggleExpanded,
            label = { Text(text = stringResource(Res.string.restaurant_allergen_filter)) },
            trailingIcon = {
                Icon(
                    imageVector = if (state.isExpanded) {
                        Icons.Rounded.KeyboardArrowUp
                    } else {
                        Icons.Rounded.KeyboardArrowDown
                    },
                    contentDescription = null,
                    modifier = Modifier.size(ChipIconSize),
                )
            },
        )
        AnimatedVisibility(visible = state.isExpanded) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = PanelShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = PanelElevation,
                shadowElevation = PanelElevation,
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    state.allergens.forEach { chip ->
                        AllergenChip(chip = chip, onClick = { onAllergenClick(chip.allergen) })
                    }
                }
            }
        }
    }
}

/**
 * Reads as a permission rather than a selection: a tick means dishes declaring this allergen are
 * still on the list, a cross means tapping it took them off.
 */
@Composable
private fun AllergenChip(chip: AllergenChipUiState, onClick: () -> Unit) {
    FilterChip(
        selected = chip.isExcluded,
        onClick = onClick,
        label = { Text(text = chip.allergen.label()) },
        leadingIcon = {
            Icon(
                imageVector = if (chip.isExcluded) Icons.Rounded.Close else Icons.Rounded.Check,
                contentDescription = stringResource(
                    if (chip.isExcluded) {
                        Res.string.restaurant_allergen_excluded
                    } else {
                        Res.string.restaurant_allergen_included
                    },
                ),
                modifier = Modifier.size(ChipIconSize),
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onErrorContainer,
            iconColor = MaterialTheme.colorScheme.primary,
        ),
    )
}

@Preview
@Composable
private fun AllergenFilterCollapsedPreview() {
    ShareatTheme {
        AllergenFilter(
            state = AllergenFilterUiState(allergens = allIncluded, isExpanded = false),
            onToggleExpanded = {},
            onAllergenClick = {},
        )
    }
}

@Preview
@Composable
private fun AllergenFilterAllIncludedPreview() {
    ShareatTheme {
        AllergenFilter(
            state = AllergenFilterUiState(allergens = allIncluded, isExpanded = true),
            onToggleExpanded = {},
            onAllergenClick = {},
        )
    }
}

@Preview
@Composable
private fun AllergenFilterWithExclusionsPreview() {
    ShareatTheme {
        AllergenFilter(
            state = AllergenFilterUiState(
                allergens = allIncluded.map { chip ->
                    chip.copy(
                        isExcluded = chip.allergen in setOf(
                            EuAllergen.CerealsContainingGluten,
                            EuAllergen.Milk,
                        ),
                    )
                },
                isExpanded = true,
            ),
            onToggleExpanded = {},
            onAllergenClick = {},
        )
    }
}

private val allIncluded = listOf(
    EuAllergen.CerealsContainingGluten,
    EuAllergen.Fish,
    EuAllergen.Soybeans,
    EuAllergen.Milk,
    EuAllergen.Eggs,
    EuAllergen.Molluscs,
).map { allergen -> AllergenChipUiState(allergen, isExcluded = false) }
