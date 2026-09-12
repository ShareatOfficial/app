package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.DishDto
import org.shareat.app.data.supabase.model.SaveRestaurantDishRpc
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.AllergenInformationSource
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.RestaurantId

internal fun DishDto.toDomain(
    publicImageUrl: (String) -> String,
): Dish = Dish(
    id = DishId(id),
    restaurantId = RestaurantId(restaurantId),
    name = name,
    description = description,
    image = imagePath?.let { ImageRef(publicImageUrl(it), imageAltText) },
    allergenDeclaration = if (allergens.isEmpty() && allergenNote == null) null else AllergenDeclaration(
        allergens = allergens.mapNotNullTo(mutableSetOf()) { it.allergenId.toEuAllergenOrNull() },
        note = allergenNote,
        source = AllergenInformationSource.Restaurant,
    ),
    isEnabled = isEnabled,
)

internal fun DishDraft.toSaveRpc(): SaveRestaurantDishRpc = SaveRestaurantDishRpc(
    restaurantId = restaurantId.value,
    dishId = id?.value,
    name = name.trim(),
    description = description?.trim().orEmpty(),
    isEnabled = isEnabled,
    allergenIds = allergenDeclaration?.allergens.orEmpty().map(EuAllergen::toDatabaseValue),
    allergenNote = allergenDeclaration?.note?.trim().orEmpty(),
)

internal fun EuAllergen.toDatabaseValue(): String = when (this) {
    EuAllergen.Celery -> "celery"
    EuAllergen.CerealsContainingGluten -> "cereals_containing_gluten"
    EuAllergen.Crustaceans -> "crustaceans"
    EuAllergen.Eggs -> "eggs"
    EuAllergen.Fish -> "fish"
    EuAllergen.Lupin -> "lupin"
    EuAllergen.Milk -> "milk"
    EuAllergen.Molluscs -> "molluscs"
    EuAllergen.Mustard -> "mustard"
    EuAllergen.Nuts -> "nuts"
    EuAllergen.Peanuts -> "peanuts"
    EuAllergen.Sesame -> "sesame"
    EuAllergen.Soybeans -> "soybeans"
    EuAllergen.SulphurDioxideAndSulphites -> "sulphur_dioxide_and_sulphites"
}

// An id we don't recognise is dropped rather than thrown on: one unknown allergen must never fail
// the whole menu it belongs to.
internal fun String.toEuAllergenOrNull(): EuAllergen? = when (this) {
    "celery" -> EuAllergen.Celery
    "cereals_containing_gluten" -> EuAllergen.CerealsContainingGluten
    "crustaceans" -> EuAllergen.Crustaceans
    "eggs" -> EuAllergen.Eggs
    "fish" -> EuAllergen.Fish
    "lupin" -> EuAllergen.Lupin
    "milk" -> EuAllergen.Milk
    "molluscs" -> EuAllergen.Molluscs
    "mustard" -> EuAllergen.Mustard
    "nuts" -> EuAllergen.Nuts
    "peanuts" -> EuAllergen.Peanuts
    "sesame" -> EuAllergen.Sesame
    "soybeans" -> EuAllergen.Soybeans
    "sulphur_dioxide_and_sulphites" -> EuAllergen.SulphurDioxideAndSulphites
    else -> null
}
