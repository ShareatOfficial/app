package org.shareat.app.navigation.restaurant

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.domain.model.DishId
import org.shareat.app.navigation.Navigator
import org.shareat.feature.review.DishReviewScreen
import org.shareat.feature.restaurant.ui.di.restaurantUiModule
import org.shareat.feature.restaurant.ui.navigation.RestaurantKey
import org.shareat.feature.restaurant.ui.navigation.RestaurantNavigation
import org.shareat.feature.restaurant.ui.restaurant.RestaurantScreen
import org.shareat.feature.restaurant.ui.restaurant.RestaurantViewModel

@OptIn(KoinExperimentalAPI::class)
val restaurantNavigationModule = module {
    includes(restaurantUiModule)

    factory<RestaurantNavigation> { parameters ->
        val navigator = parameters.getOrNull<Navigator>() ?: get<Navigator>()
        RestaurantNavigationImpl(navigator = navigator)
    }

    navigation<RestaurantKey> { key ->
        val restaurantViewModel = koinViewModel<RestaurantViewModel>(
            key = key.restaurant.id,
            parameters = { parametersOf(key.restaurant) },
        )
        var reviewRequest by remember { mutableStateOf<DishReviewRequest?>(null) }

        RestaurantScreen(
            args = key.restaurant,
            viewModel = restaurantViewModel,
            onDishReviewRequest = { dishId, rating ->
                reviewRequest = DishReviewRequest(dishId, rating)
            },
        )

        reviewRequest?.let { request ->
            DishReviewScreen(
                dishId = request.dishId,
                initialDishRating = request.initialRating,
                onDismissRequest = { reviewRequest = null },
                onReviewSubmitted = {
                    reviewRequest = null
                    restaurantViewModel.onRefresh()
                },
            )
        }
    }
}

private data class DishReviewRequest(
    val dishId: DishId,
    val initialRating: Int,
)
