package org.shareat.feature.restaurant.ui.restaurant.composables

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restaurant.ui.model.RestaurantError
import shareat.feature.restaurant.ui.generated.resources.Res
import shareat.feature.restaurant.ui.generated.resources.allergen_celery
import shareat.feature.restaurant.ui.generated.resources.allergen_crustaceans
import shareat.feature.restaurant.ui.generated.resources.allergen_eggs
import shareat.feature.restaurant.ui.generated.resources.allergen_fish
import shareat.feature.restaurant.ui.generated.resources.allergen_gluten
import shareat.feature.restaurant.ui.generated.resources.allergen_lupin
import shareat.feature.restaurant.ui.generated.resources.allergen_milk
import shareat.feature.restaurant.ui.generated.resources.allergen_molluscs
import shareat.feature.restaurant.ui.generated.resources.allergen_mustard
import shareat.feature.restaurant.ui.generated.resources.allergen_nuts
import shareat.feature.restaurant.ui.generated.resources.allergen_peanuts
import shareat.feature.restaurant.ui.generated.resources.allergen_sesame
import shareat.feature.restaurant.ui.generated.resources.allergen_soybeans
import shareat.feature.restaurant.ui.generated.resources.allergen_sulphites
import shareat.feature.restaurant.ui.generated.resources.category_desserts
import shareat.feature.restaurant.ui.generated.resources.category_main_courses
import shareat.feature.restaurant.ui.generated.resources.category_small_bites
import shareat.feature.restaurant.ui.generated.resources.category_starters
import shareat.feature.restaurant.ui.generated.resources.restaurant_all_categories
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_already_exists
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_credentials
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_forbidden
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_generic
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_not_found
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_offline
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_session
import shareat.feature.restaurant.ui.generated.resources.restaurant_error_unavailable

@Composable
internal fun DishCategory?.label(): String = when (this) {
    null -> stringResource(Res.string.restaurant_all_categories)
    DishCategory.Starters -> stringResource(Res.string.category_starters)
    DishCategory.MainCourses -> stringResource(Res.string.category_main_courses)
    DishCategory.Desserts -> stringResource(Res.string.category_desserts)
    DishCategory.SmallBites -> stringResource(Res.string.category_small_bites)
}

@Composable
internal fun EuAllergen.label(): String = when (this) {
    EuAllergen.Celery -> stringResource(Res.string.allergen_celery)
    EuAllergen.CerealsContainingGluten -> stringResource(Res.string.allergen_gluten)
    EuAllergen.Crustaceans -> stringResource(Res.string.allergen_crustaceans)
    EuAllergen.Eggs -> stringResource(Res.string.allergen_eggs)
    EuAllergen.Fish -> stringResource(Res.string.allergen_fish)
    EuAllergen.Lupin -> stringResource(Res.string.allergen_lupin)
    EuAllergen.Milk -> stringResource(Res.string.allergen_milk)
    EuAllergen.Molluscs -> stringResource(Res.string.allergen_molluscs)
    EuAllergen.Mustard -> stringResource(Res.string.allergen_mustard)
    EuAllergen.Nuts -> stringResource(Res.string.allergen_nuts)
    EuAllergen.Peanuts -> stringResource(Res.string.allergen_peanuts)
    EuAllergen.Sesame -> stringResource(Res.string.allergen_sesame)
    EuAllergen.Soybeans -> stringResource(Res.string.allergen_soybeans)
    EuAllergen.SulphurDioxideAndSulphites -> stringResource(Res.string.allergen_sulphites)
}

@Composable
internal fun RestaurantError.label(): String = stringResource(
    when (this) {
        RestaurantError.INVALID_CREDENTIALS -> Res.string.restaurant_error_credentials
        RestaurantError.OFFLINE -> Res.string.restaurant_error_offline
        RestaurantError.UNAUTHENTICATED -> Res.string.restaurant_error_session
        RestaurantError.FORBIDDEN -> Res.string.restaurant_error_forbidden
        RestaurantError.TEMPORARILY_UNAVAILABLE -> Res.string.restaurant_error_unavailable
        RestaurantError.ALREADY_EXISTS -> Res.string.restaurant_error_already_exists
        RestaurantError.NOT_FOUND -> Res.string.restaurant_error_not_found
        RestaurantError.UNKNOWN -> Res.string.restaurant_error_generic
    },
)
