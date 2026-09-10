package org.shareat.app.data.supabase.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class AccountDto(
    val id: String,
    val role: String,
    val status: String,
)

@Serializable
internal data class CustomerProfileDto(
    @SerialName("account_id") val accountId: String,
    @SerialName("display_name") val displayName: String,
    @SerialName("avatar_path") val avatarPath: String? = null,
    @SerialName("avatar_alt_text") val avatarAltText: String? = null,
    @SerialName("full_name") val fullName: String,
    @SerialName("phone_number") val phoneNumber: String? = null,
    @SerialName("preferred_language") val preferredLanguage: String = "en-US",
)
