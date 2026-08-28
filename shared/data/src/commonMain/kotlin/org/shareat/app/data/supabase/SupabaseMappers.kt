package org.shareat.app.data.supabase

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.AllergenInformationSource
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.GeoCoordinates
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.OpeningPeriod
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.Weekday
import org.shareat.app.domain.model.WeeklyOpeningHours

internal fun AccountDto.toDomain(email: String): Account = Account(
    id = AccountId(id),
    loginEmail = EmailAddress(email),
    role = when (role) {
        "customer" -> AccountRole.Customer
        "restaurant" -> AccountRole.Restaurant
        else -> error("Unsupported account role: $role")
    },
    status = when (status) {
        "active" -> AccountStatus.Active
        "disabled" -> AccountStatus.Disabled
        "deletion_pending" -> AccountStatus.DeletionPending
        else -> error("Unsupported account status: $status")
    },
)

internal fun RestaurantDto.toDomain(
    periods: List<OpeningPeriodDto>,
    publicImageUrl: (String) -> String,
): Restaurant = Restaurant(
    id = RestaurantId(id),
    ownerAccountId = AccountId(ownerAccountId),
    name = name,
    description = description,
    heroImage = heroImagePath?.let { ImageRef(publicImageUrl(it), heroImageAltText) },
    publicEmail = publicEmail?.let(::EmailAddress),
    publicPhone = publicPhone,
    address = PostalAddress(
        streetLine = streetLine,
        locality = locality,
        postalCode = postalCode,
        region = region,
        countryCode = countryCode,
        coordinates = latitude?.let { GeoCoordinates(it, requireNotNull(longitude)) },
    ),
    openingHours = WeeklyOpeningHours(
        periods.groupBy { it.weekday }.entries.sortedBy { it.key }.map { (weekday, rows) ->
            DailyOpeningHours(
                day = Weekday.entries[weekday - 1],
                periods = rows.sortedBy(OpeningPeriodDto::position).map {
                    OpeningPeriod(it.opensAt.toLocalTime(), it.closesAt.toLocalTime())
                },
            )
        },
    ),
    publicationState = when (publicationState) {
        "draft" -> RestaurantPublicationState.Draft
        "published" -> RestaurantPublicationState.Published
        "disabled" -> RestaurantPublicationState.Disabled
        else -> error("Unsupported restaurant state: $publicationState")
    },
)

internal fun Restaurant.toUpdateSettingsRpc(): UpdateRestaurantSettingsRpc =
    UpdateRestaurantSettingsRpc(
        restaurantId = id.value,
        name = name,
        description = description,
        publicEmail = publicEmail?.value,
        publicPhone = publicPhone,
        streetLine = address.streetLine,
        locality = address.locality,
        postalCode = address.postalCode,
        publicationState = when (publicationState) {
            RestaurantPublicationState.Draft -> "draft"
            RestaurantPublicationState.Published -> "published"
            RestaurantPublicationState.Disabled -> "disabled"
        },
        openingPeriods = openingHours.days.flatMap { hours ->
            hours.periods.mapIndexed { position, period ->
                OpeningPeriodUpdateDto(
                    weekday = hours.day.ordinal + 1,
                    position = position,
                    opensAt = period.opensAt.toDatabaseTime(),
                    closesAt = period.closesAt.toDatabaseTime(),
                )
            }
        },
    )

private fun LocalTime.toDatabaseTime(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"

internal fun MenuDto.toDomain(): Menu = Menu(
    id = MenuId(id),
    restaurantId = RestaurantId(restaurantId),
    name = name,
    description = description,
    publicationState = when (publicationState) {
        "draft" -> MenuPublicationState.Draft
        "published" -> MenuPublicationState.Published
        "unpublished" -> MenuPublicationState.Unpublished
        "disabled" -> MenuPublicationState.Disabled
        else -> error("Unsupported menu state: $publicationState")
    },
)

internal fun DishDto.toDomain(
    allergens: Set<String>,
    publicImageUrl: (String) -> String,
): Dish = Dish(
    id = DishId(id),
    restaurantId = RestaurantId(restaurantId),
    name = name,
    description = description,
    image = imagePath?.let { ImageRef(publicImageUrl(it), imageAltText) },
    allergenDeclaration = if (allergens.isEmpty() && allergenNote == null) null else AllergenDeclaration(
        allergens = allergens.mapTo(mutableSetOf(), String::toEuAllergen),
        note = allergenNote,
        source = AllergenInformationSource.Restaurant,
    ),
    isEnabled = isEnabled,
)

internal fun MenuItemDto.toDomain(dish: Dish): MenuDish = MenuDish(
    dish = dish,
    price = Money(priceMinorUnits, Currency.Euro),
    position = position,
)

internal fun ReviewDto.toDomain(): Review = Review(
    id = ReviewId(id),
    authorAccountId = AccountId(authorAccountId),
    target = restaurantId?.let { ReviewTarget.Restaurant(RestaurantId(it)) }
        ?: ReviewTarget.Dish(DishId(requireNotNull(dishId))),
    rating = Rating(rating),
    comment = comment,
    visibility = if (visibility == "public") ReviewVisibility.Public else ReviewVisibility.Private,
    moderationStatus = when (moderationStatus) {
        "visible" -> ReviewModerationStatus.Visible
        "hidden" -> ReviewModerationStatus.Hidden
        "removed" -> ReviewModerationStatus.Removed
        else -> error("Unsupported moderation status: $moderationStatus")
    },
    visitedAt = visitedAt?.let(::IsoTimestamp),
    createdAt = IsoTimestamp(createdAt),
    updatedAt = IsoTimestamp(updatedAt),
)

internal fun RatingSummaryDto.toDomain(): RatingSummary = RatingSummary(
    averageTenths = averageTenths,
    ratingCount = ratingCount.toInt(),
)

private fun String.toLocalTime(): LocalTime {
    val parts = split(':')
    return LocalTime(parts[0].toInt(), parts[1].toInt())
}

private fun String.toEuAllergen(): EuAllergen = when (this) {
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
    else -> error("Unsupported allergen: $this")
}
