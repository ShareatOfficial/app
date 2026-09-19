package org.shareat.feature.restauranthome.ui.restauranthome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.shareat.feature.restauranthome.ui.restauranthome.composables.label
import org.shareat.feature.restauranthome.ui.model.RestaurantDish
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeContent
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeData
import org.shareat.shared.designsystem.preview.FormFactorPreviews
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_placeholder_hero
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_other

@Composable
internal fun RestaurantHomeStatelessV2ByTone() {
    //Three parts. Top Bar with the options that the restaurant have. Switch to customer preview. change layout type.
    /*
    What things I want to make the restaurant do?
    - View the menu as a customer
    - Create QR code to let the user navigate to the restaurant.
    - Switch between restaurant publish state. Only if it achieved the values. Name, description and more.
    - Change layout type. Out of MVP scope.
     */

    val uiState = RestaurantHomePreviewData.loaded
    val restaurant = (uiState.content as RestaurantHomeContent.Loaded).restaurant
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        // Infomarción de restaurante, se compacta hasta dejar solo la imagen.
        RestaurantInfo(
            restaurant,
            onOpenDirectionsClick = {},
            onRateClick = {}
        )
        // Sección de filtros y platos.
        RestaurantDishes(
            dishes = restaurant.dishes,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun HomeRestaurantTopBar(modifier: Modifier = Modifier) {
    // TODO Queda pasar el scrollState para que cuando se deslize hacia abajo se oculte.
    Row(
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .height(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Mi Restaurante",
                style = MaterialTheme.typography.titleLargeEmphasized,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Customer view or edit view
        val isCustomerView =
            remember { mutableStateOf(false) } // this state should be hoisted by the viewModel
        Row(
            modifier = Modifier
                .height(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCustomerView.value) "Editar" else "Cliente",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isCustomerView.value,
                onCheckedChange = { isCustomerView.value = it },
            )
        }
    }
}

@Composable
private fun RestaurantInfo(
    restaurant: RestaurantHomeData,
    onOpenDirectionsClick: () -> Unit,
    onRateClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth().background(color = MaterialTheme.colorScheme.background)
    ) {
        // Image + main info box with the image the restaurant choose behind.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 297.dp), // ~1/3 of a 891dp tall screen like Pixel 9

        ) {
            AsyncImage(
                model = restaurant.imageUrl?.ifEmpty { null }
                    ?: Res.drawable.restaurant_placeholder_hero,
                contentDescription = restaurant.imageDescription,
                modifier = Modifier.fillMaxWidth().matchParentSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = 0.7f))
            )
            HomeRestaurantTopBar(modifier = Modifier.align(Alignment.TopCenter))
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = restaurant.description.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }

        // Secondary info.  Street, rating, if it is open, etc...
        Spacer(modifier = Modifier.height(16.dp))
        // TODO  el onclick cambiara dependiendo del state. si es customer vview o no. Pero eso lo gestionará el viewModel

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HomeRestaurantActionRow(
                icon = Icons.Outlined.LocationOn,
                text = restaurant.address.streetLine,
                onClick = { /* open google maps directions */ },
                trailingContent = {}
            )
            HomeRestaurantActionRow(
                icon = Icons.Filled.Star,
                text = restaurant.ratingLabel ?: "Sin calificación",
                onClick = { /* rate the restaurant */ },
                trailingContent = {}
            )
        }

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
    }
}

@Composable
private fun HomeRestaurantActionRow(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    trailingContent: @Composable () -> Unit
) {
    Row(modifier = Modifier.padding(16.dp).clickable(onClick = onClick)) {
        Icon(imageVector = icon, contentDescription = null)
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
        trailingContent()
    }
}


@Composable
private fun RestaurantDishes(
    dishes: List<RestaurantDish>,
    modifier: Modifier = Modifier,
) {
    val categorySections = remember(dishes) {
        dishes.groupBy { it.category }.entries.toList()
    }
    val categoryItemIndices = remember(categorySections) {
        var nextItemIndex = 0
        categorySections.map { section ->
            nextItemIndex.also {
                nextItemIndex += section.value.size + 1
            }
        }
    }
    val categoryListState = rememberLazyListState()
    val dishListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val selectedCategoryIndex = rememberSaveable(dishListState, categoryItemIndices) {
        derivedStateOf {
            /* Basically this calculates the value of the current selected category. If the current is
             empty or totalCount, that means user has not scrolled yet, then 0. If the user cannot scroll more down
             then the index is the last one. Then the selectedCategory is the one corresponding to the first visible item.
             */
            when {
                categoryItemIndices.isEmpty() -> 0
                dishListState.layoutInfo.totalItemsCount == 0 -> 0
                !dishListState.canScrollForward -> categoryItemIndices.lastIndex
                else -> categoryItemIndices
                    .indexOfLast { it <= dishListState.firstVisibleItemIndex }
                    .coerceAtLeast(0)
            }
        }
    }.value

    LaunchedEffect(selectedCategoryIndex) {
        if (categorySections.isNotEmpty()) {
            categoryListState.animateScrollToItem(selectedCategoryIndex)
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        LazyRow(
            state = categoryListState,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(
                count = categorySections.size,
                key = { index -> categorySections[index].key?.name ?: "other" },
            ) { index ->
                val category = categorySections[index].key
                FilterChip(
                    selected = index == selectedCategoryIndex,
                    onClick = {
                        coroutineScope.launch {
                            dishListState.animateScrollToItem(categoryItemIndices[index])
                        }
                    },
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

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            state = dishListState,
            contentPadding = PaddingValues(bottom = 16.dp),
        ) {
            categorySections.forEach { (category, dishesList) ->
                item(key = "header-${category?.name ?: "other"}") {
                    Text(
                        text = category?.label()
                            ?: stringResource(Res.string.restaurant_home_category_other),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
                items(
                    count = dishesList.size,
                    key = { index -> dishesList[index].id },
                ) { index ->
                    DishItem(
                        dish = dishesList[index],
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clickable { /* TODO */ },
                    )
                }
            }
        }
    }
}

@Composable
private fun DishItem(dish: RestaurantDish, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = dish.imageUrl?.ifEmpty { null },
            contentDescription = dish.imageDescription,
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = dish.name,
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                text = dish.description.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
            )
        }
        Text(
            text = "$ ${dish.priceMinorUnits}",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            textAlign = TextAlign.End
        )
    }
}


@FormFactorPreviews
@Composable
private fun RestaurantHomeStatelessV2ByTonePreview() {
    ShareatTheme {
        RestaurantHomeStatelessV2ByTone()
    }
}

@Preview
@Composable
private fun DishItemPreview() {
    ShareatTheme {
        val uiState = RestaurantHomePreviewData.loaded
        val restaurant = (uiState.content as RestaurantHomeContent.Loaded).restaurant
        DishItem(
            dish = restaurant.dishes.first()
                .copy(imageUrl = "https://imgs.search.brave.com/jI7tFJqoulIUMoKkI4w0xw_JiOevDaE-FTkwxJgj61Y/rs:fit:500:0:1:0/g:ce/aHR0cHM6Ly9tZWRp/YS5nZXR0eWltYWdl/cy5jb20vaWQvOTgw/ODcwMjIvZXMvZm90/by9hbi1leGhpYml0/b3Itc2VydmVzLWtl/YmFiLW1lYXQtZnJv/bS1hLW5ldy1kb25l/ci1rZWJhYi1jdXR0/aW5nLXJvYm90LWR1/cmluZy10aGUtZG9n/YS10aGUtZG9uZXIu/anBnP3M9NjEyeDYx/MiZ3PTAmaz0yMCZj/PTNLSnpSMGVlRVhJ/RTcxMmc0WEpYbm5L/b3o1WXpPb1VfZHo3/S05wcXNFV1U9")
        )
    }
}
