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
) {
    init { require(displayName.isNotBlank()) }
}
