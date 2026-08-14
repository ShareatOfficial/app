package org.shareat.app.domain.model

import kotlin.jvm.JvmInline

@JvmInline
value class AccountId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class RestaurantId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class MenuId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class DishId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class ReviewId(val value: String) {
    init { require(value.isNotBlank()) }
}

@JvmInline
value class EmailAddress(val value: String) {
    init { require('@' in value && value.indexOf('@') > 0) }
}

@JvmInline
value class IsoTimestamp(val value: String) {
    init { require('T' in value) }
}

data class ImageRef(
    val url: String,
    val alternativeText: String? = null,
) {
    init { require(url.isNotBlank()) }
}

enum class Currency(val code: String) {
    Euro("EUR"),
}

data class Money(
    val minorUnits: Long,
    val currency: Currency = Currency.Euro,
) {
    init { require(minorUnits >= 0) }
}
