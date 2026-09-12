package org.shareat.feature.restauranthome.ui.restauranthome.composables

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.EuAllergen
import org.shareat.feature.restauranthome.ui.model.RestaurantHomeError
import shareat.feature.restauranthome.ui.generated.resources.Res
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_celery
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_crustaceans
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_eggs
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_fish
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_gluten
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_lupin
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_milk
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_molluscs
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_mustard
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_nuts
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_peanuts
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_sesame
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_soy
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_allergen_sulphites
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_desserts
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_main
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_small_bites
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_category_starters
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_forbidden
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_generic
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_format
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_read
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_size
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_image_upload_after_details_saved
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_not_found
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_offline
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_session
import shareat.feature.restauranthome.ui.generated.resources.restaurant_home_error_unavailable

@Composable
internal fun DishCategory.label(): String = stringResource(
    when (this) {
        DishCategory.Starters -> Res.string.restaurant_home_category_starters
        DishCategory.MainCourses -> Res.string.restaurant_home_category_main
        DishCategory.Desserts -> Res.string.restaurant_home_category_desserts
        DishCategory.SmallBites -> Res.string.restaurant_home_category_small_bites
    },
)

@Composable
internal fun EuAllergen.label(): String = stringResource(
    when (this) {
        EuAllergen.Celery -> Res.string.restaurant_home_allergen_celery
        EuAllergen.CerealsContainingGluten -> Res.string.restaurant_home_allergen_gluten
        EuAllergen.Crustaceans -> Res.string.restaurant_home_allergen_crustaceans
        EuAllergen.Eggs -> Res.string.restaurant_home_allergen_eggs
        EuAllergen.Fish -> Res.string.restaurant_home_allergen_fish
        EuAllergen.Lupin -> Res.string.restaurant_home_allergen_lupin
        EuAllergen.Milk -> Res.string.restaurant_home_allergen_milk
        EuAllergen.Molluscs -> Res.string.restaurant_home_allergen_molluscs
        EuAllergen.Mustard -> Res.string.restaurant_home_allergen_mustard
        EuAllergen.Nuts -> Res.string.restaurant_home_allergen_nuts
        EuAllergen.Peanuts -> Res.string.restaurant_home_allergen_peanuts
        EuAllergen.Sesame -> Res.string.restaurant_home_allergen_sesame
        EuAllergen.Soybeans -> Res.string.restaurant_home_allergen_soy
        EuAllergen.SulphurDioxideAndSulphites -> Res.string.restaurant_home_allergen_sulphites
    },
)

@Composable
internal fun RestaurantHomeError.label(): String = stringResource(
    when (this) {
        RestaurantHomeError.OFFLINE -> Res.string.restaurant_home_error_offline
        RestaurantHomeError.UNAUTHENTICATED -> Res.string.restaurant_home_error_session
        RestaurantHomeError.FORBIDDEN -> Res.string.restaurant_home_error_forbidden
        RestaurantHomeError.NOT_FOUND -> Res.string.restaurant_home_error_not_found
        RestaurantHomeError.TEMPORARILY_UNAVAILABLE -> Res.string.restaurant_home_error_unavailable
        RestaurantHomeError.IMAGE_FORMAT_UNSUPPORTED -> Res.string.restaurant_home_error_image_format
        RestaurantHomeError.IMAGE_TOO_LARGE -> Res.string.restaurant_home_error_image_size
        RestaurantHomeError.IMAGE_READ_FAILED -> Res.string.restaurant_home_error_image_read
        RestaurantHomeError.IMAGE_UPLOAD_FAILED_AFTER_DETAILS_SAVED ->
            Res.string.restaurant_home_error_image_upload_after_details_saved
        RestaurantHomeError.VALIDATION, RestaurantHomeError.UNKNOWN -> Res.string.restaurant_home_error_generic
    },
)
