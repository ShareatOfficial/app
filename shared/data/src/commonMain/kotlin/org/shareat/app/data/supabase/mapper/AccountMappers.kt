package org.shareat.app.data.supabase.mapper

import org.shareat.app.data.supabase.model.AccountDto
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.EmailAddress

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
