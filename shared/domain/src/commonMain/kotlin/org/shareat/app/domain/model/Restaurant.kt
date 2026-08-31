package org.shareat.app.domain.model

data class GeoCoordinates(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0)
        require(longitude in -180.0..180.0)
    }
}

data class PostalAddress(
    val streetLine: String,
    val locality: String,
    val postalCode: String,
    val region: String? = null,
    val countryCode: String = "ES",
    val coordinates: GeoCoordinates? = null,
) {
    init {
        require(streetLine.isNotBlank())
        require(locality.isNotBlank())
        require(postalCode.isNotBlank())
        require(countryCode.length == 2)
    }
}

enum class Weekday {
    Monday,
    Tuesday,
    Wednesday,
    Thursday,
    Friday,
    Saturday,
    Sunday,
}

data class LocalTime(
    val hour: Int,
    val minute: Int,
) : Comparable<LocalTime> {
    init {
        require(hour in 0..23)
        require(minute in 0..59)
    }

    override fun compareTo(other: LocalTime): Int =
        (hour * 60 + minute).compareTo(other.hour * 60 + other.minute)
}

/** An end before the start represents a period that closes after midnight. */
data class OpeningPeriod(
    val opensAt: LocalTime,
    val closesAt: LocalTime,
) {
    init { require(opensAt != closesAt) }
}

data class DailyOpeningHours(
    val day: Weekday,
    val periods: List<OpeningPeriod>,
)

data class WeeklyOpeningHours(
    val days: List<DailyOpeningHours>,
) {
    init { require(days.map(DailyOpeningHours::day).distinct().size == days.size) }
}

enum class RestaurantPublicationState {
    Draft,
    Published,
    Disabled,
}

data class Restaurant(
    val id: RestaurantId,
    val ownerAccountId: AccountId,
    val name: String,
    val description: String? = null,
    val heroImage: ImageRef? = null,
    val publicEmail: EmailAddress? = null,
    val publicPhone: String? = null,
    val address: PostalAddress,
    val openingHours: WeeklyOpeningHours,
    val publicationState: RestaurantPublicationState,
) {
    init {
        require(name.isNotBlank())
        require(publicPhone == null || publicPhone.isNotBlank())
    }
}

/** Information accepted when an authenticated owner creates their first restaurant profile. */
data class RestaurantProfileDraft(
    val name: String,
    val description: String? = null,
    val publicEmail: EmailAddress? = null,
    val publicPhone: String? = null,
    val address: PostalAddress,
    val openingHours: WeeklyOpeningHours = WeeklyOpeningHours(emptyList()),
) {
    init {
        require(name.isNotBlank())
        require(description == null || description.isNotBlank())
        require(publicPhone == null || publicPhone.isNotBlank())
        require(address.countryCode == "ES")
        require(address.coordinates == null)
        require(openingHours.days.all { it.periods.size <= 1 })
    }
}
