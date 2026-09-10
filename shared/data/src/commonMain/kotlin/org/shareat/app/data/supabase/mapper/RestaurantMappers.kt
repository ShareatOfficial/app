package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.CreateOpeningPeriodDto
import org.shareat.app.data.supabase.model.CreateRestaurantProfileRpc
import org.shareat.app.data.supabase.model.EmbeddedOpeningPeriodDto
import org.shareat.app.data.supabase.model.OpeningPeriodUpdateDto
import org.shareat.app.data.supabase.model.RestaurantDto
import org.shareat.app.data.supabase.model.UpdateRestaurantSettingsRpc
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.GeoCoordinates
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.OpeningPeriod
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Weekday
import org.shareat.app.domain.model.WeeklyOpeningHours

internal fun RestaurantDto.toDomain(
    publicImageUrl: (String) -> String,
): Restaurant = Restaurant(
    id = RestaurantId(id),
    ownerAccountId = AccountId(ownerAccountId),
    name = name,
    description = description,
    heroImage = heroImagePath?.let {
        if (it.startsWith("http")) {
            ImageRef(it, heroImageAltText)
        } else {
            ImageRef(publicImageUrl(it), heroImageAltText)
        }
    },
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
        openingPeriods.groupBy { it.weekday }.entries.sortedBy { it.key }.map { (weekday, rows) ->
            DailyOpeningHours(
                day = Weekday.entries[weekday - 1],
                periods = rows.sortedBy(EmbeddedOpeningPeriodDto::position).map {
                    OpeningPeriod(it.opensAt.toLocalTime(), it.closesAt.toLocalTime())
                },
            )
        },
    ),
    currency = currencyCode.toCurrency(),
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

internal fun RestaurantProfileDraft.toCreateProfileRpc(): CreateRestaurantProfileRpc =
    CreateRestaurantProfileRpc(
        name = name,
        // PostgREST resolves RPC overloads from the JSON keys it receives. Send
        // empty values for every optional text parameter so none can be omitted
        // by a serializer configured with explicitNulls = false. The SQL RPC
        // converts these values back to NULL with nullif(btrim(...), '').
        description = description.orEmpty(),
        publicEmail = publicEmail?.value.orEmpty(),
        publicPhone = publicPhone.orEmpty(),
        streetLine = address.streetLine,
        locality = address.locality,
        postalCode = address.postalCode,
        region = address.region.orEmpty(),
        openingPeriods = openingHours.days.flatMap { hours ->
            hours.periods.map { period ->
                CreateOpeningPeriodDto(
                    weekday = hours.day.ordinal + 1,
                    opensAt = period.opensAt.toDatabaseTime(),
                    closesAt = period.closesAt.toDatabaseTime(),
                )
            }
        },
    )

internal fun String.toCurrency(): Currency = when (this) {
    Currency.Euro.code -> Currency.Euro
    else -> error("Unsupported currency: $this")
}

private fun LocalTime.toDatabaseTime(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}:00"

private fun String.toLocalTime(): LocalTime {
    val parts = split(':')
    return LocalTime(parts[0].toInt(), parts[1].toInt())
}
