package org.shareat.app.domain.model

enum class AccountRole {
    Customer,
    Restaurant,
}

enum class AccountStatus {
    Active,
    Disabled,
    DeletionPending,
}

data class Account(
    val id: AccountId,
    val loginEmail: EmailAddress,
    val role: AccountRole,
    val status: AccountStatus,
)

data class CustomerProfile(
    val accountId: AccountId,
    val displayName: String,
    val avatar: ImageRef? = null,
    val fullName: String = displayName,
    val phoneNumber: String? = null,
    val preferredLanguage: String = "en-US",
) {
    init {
        require(displayName.isNotBlank())
        require(fullName.isNotBlank())
        require(phoneNumber == null || phoneNumber.isNotBlank())
        require(preferredLanguage.isNotBlank())
    }
}
