package org.shareat.feature.profile.domain

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours

sealed interface ProfileSettings {
    data class User(
        val account: Account,
        val profile: CustomerProfile,
    ) : ProfileSettings

    data class RestaurantOwner(
        val account: Account,
        val restaurant: Restaurant,
    ) : ProfileSettings
}

data class UpdateRestaurantInfoParams(
    val restaurantId: RestaurantId,
    val name: String,
    val description: String?,
    val publicEmail: EmailAddress?,
    val publicPhone: String?,
    val address: PostalAddress,
    val openingHours: WeeklyOpeningHours,
    val publicationState: RestaurantPublicationState,
)
