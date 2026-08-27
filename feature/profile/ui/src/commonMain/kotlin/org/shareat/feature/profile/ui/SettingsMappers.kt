package org.shareat.feature.profile.ui

import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.OpeningPeriod
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Weekday
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.feature.profile.domain.ProfileSettings
import org.shareat.feature.profile.domain.UpdateRestaurantInfoParams

internal fun ProfileSettings.User.toUiState(): SettingsUiState.User = SettingsUiState.User(
    name = profile.displayName,
    email = account.loginEmail.value,
    initials = profile.displayName.toInitials(),
)

internal fun ProfileSettings.RestaurantOwner.toUiState(): SettingsUiState.Restaurant =
    restaurant.toUiState()

internal fun Restaurant.toUiState(): SettingsUiState.Restaurant = SettingsUiState.Restaurant(
    name = name,
    description = description.orEmpty(),
    phone = publicPhone.orEmpty(),
    email = publicEmail?.value.orEmpty(),
    streetAddress = address.streetLine,
    city = address.locality,
    postcode = address.postalCode,
    isPublished = publicationState == RestaurantPublicationState.Published,
    openingHours = OpeningDay.entries.map { uiDay ->
        val domainDay = uiDay.toDomain()
        val firstPeriod = openingHours.days
            .firstOrNull { it.day == domainDay }
            ?.periods
            ?.firstOrNull()
        OpeningHoursUiState(
            day = uiDay,
            isOpen = firstPeriod != null,
            openingTime = firstPeriod?.opensAt?.toUiTime() ?: "11:00",
            closingTime = firstPeriod?.closesAt?.toUiTime() ?: "22:00",
        )
    },
)

internal sealed interface RestaurantSettingsMappingResult {
    data class Success(val params: UpdateRestaurantInfoParams) : RestaurantSettingsMappingResult
    data class Failure(val message: String) : RestaurantSettingsMappingResult
}

internal fun SettingsUiState.Restaurant.toUpdateParams(
    original: Restaurant,
): RestaurantSettingsMappingResult {
    val trimmedName = name.trim()
    if (trimmedName.isEmpty()) return mappingFailure("Restaurant name cannot be empty.")
    if (streetAddress.isBlank() || city.isBlank() || postcode.isBlank()) {
        return mappingFailure("Street address, city and postcode are required.")
    }

    val mappedEmail = email.trim().takeIf(String::isNotEmpty)?.let { value ->
        runCatching { EmailAddress(value) }.getOrElse {
            return mappingFailure("Enter a valid contact email.")
        }
    }

    val mappedDays = mutableListOf<DailyOpeningHours>()
    for (hours in openingHours) {
        val day = hours.day.toDomain()
        val originalPeriods = original.openingHours.days
            .firstOrNull { it.day == day }
            ?.periods
            .orEmpty()
        val periods = if (!hours.isOpen) {
            emptyList()
        } else {
            val opensAt = hours.openingTime.toDomainTime()
                ?: return mappingFailure("Use HH:mm for ${hours.day.label} opening time.")
            val closesAt = hours.closingTime.toDomainTime()
                ?: return mappingFailure("Use HH:mm for ${hours.day.label} closing time.")
            if (opensAt == closesAt) {
                return mappingFailure("Opening and closing time must differ for ${hours.day.label}.")
            }
            listOf(OpeningPeriod(opensAt, closesAt)) + originalPeriods.drop(1)
        }
        mappedDays += DailyOpeningHours(day, periods)
    }

    return RestaurantSettingsMappingResult.Success(
        UpdateRestaurantInfoParams(
            restaurantId = original.id,
            name = trimmedName,
            description = description.trim().ifBlank { null },
            publicEmail = mappedEmail,
            publicPhone = phone.trim().ifBlank { null },
            address = original.address.copy(
                streetLine = streetAddress.trim(),
                locality = city.trim(),
                postalCode = postcode.trim(),
            ),
            openingHours = WeeklyOpeningHours(mappedDays),
            publicationState = when {
                isPublished -> RestaurantPublicationState.Published
                original.publicationState == RestaurantPublicationState.Disabled ->
                    RestaurantPublicationState.Disabled
                else -> RestaurantPublicationState.Draft
            },
        ),
    )
}

private fun mappingFailure(message: String) = RestaurantSettingsMappingResult.Failure(message)

private fun String.toInitials(): String = trim()
    .split(Regex("\\s+"))
    .filter(String::isNotBlank)
    .take(2)
    .mapNotNull(String::firstOrNull)
    .joinToString("")
    .uppercase()
    .ifBlank { "?" }

private fun OpeningDay.toDomain(): Weekday = Weekday.entries[ordinal]

private fun LocalTime.toUiTime(): String =
    "${hour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')}"

private fun String.toDomainTime(): LocalTime? {
    val parts = trim().split(':')
    if (parts.size != 2 || parts.any { it.length != 2 }) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime(hour, minute) }.getOrNull()
}
