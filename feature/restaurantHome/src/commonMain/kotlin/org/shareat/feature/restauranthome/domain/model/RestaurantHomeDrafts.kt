package org.shareat.feature.restauranthome.domain.model

import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.WeeklyOpeningHours

/** Editable owner fields. Blank names are reported as a [RepositoryError.Validation] by the use case. */
data class OwnerRestaurantInfoDraft(
    val name: String,
    val description: String?,
    val address: PostalAddress?,
    val publicEmail: EmailAddress? = null,
    val publicPhone: String? = null,
    val openingHours: WeeklyOpeningHours? = null,
)

/** One dish edit updates the dish record and its matching item in the restaurant's sole menu. */
data class OwnerDishDraft(
    val name: String,
    val description: String?,
    val allergenDeclaration: AllergenDeclaration?,
    val isEnabled: Boolean,
    val price: Money,
)

/** Fields used to add a new dish to the owner's single menu. */
data class OwnerDishCreateDraft(
    val name: String,
    val description: String?,
    val allergenDeclaration: AllergenDeclaration?,
    val isEnabled: Boolean,
    val price: Money,
)
