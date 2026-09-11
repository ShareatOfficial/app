package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage
import org.shareat.app.data.supabase.mapper.toDomain
import org.shareat.app.data.supabase.model.AccountDto
import org.shareat.app.data.supabase.model.CustomerProfileDto
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.RepositoryResult
import kotlin.time.Duration.Companion.hours

internal class SupabaseAccountRepository(
    private val client: SupabaseClient,
) : AccountRepository {
    override suspend fun getAccount(id: AccountId): RepositoryResult<Account> = supabaseResult {
        val user = client.auth.currentUserOrNull()
        val dto = client.from("accounts").select {
            filter { eq("id", id.value) }
        }.decodeList<AccountDto>().singleOrNull()
            ?: throw DomainNotFound("account", id.value)
        dto.toDomain(requireNotNull(user?.email))
    }

    override suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile> = supabaseResult {
        val dto = client.from("customer_profiles").select {
            filter { eq("account_id", accountId.value) }
        }.decodeList<CustomerProfileDto>().singleOrNull()
            ?: throw DomainNotFound("customer profile", accountId.value)
        dto.toDomain()
    }

    override suspend fun updateCustomerProfile(
        profile: CustomerProfile,
    ): RepositoryResult<CustomerProfile> = supabaseResult {
        val dto = client.from("customer_profiles").update({
            set("full_name", profile.fullName)
            set("display_name", profile.displayName)
            set("phone_number", profile.phoneNumber)
            set("preferred_language", profile.preferredLanguage)
        }) {
            select()
            filter { eq("account_id", profile.accountId.value) }
        }.decodeList<CustomerProfileDto>().singleOrNull()
            ?: throw DomainForbidden()
        dto.toDomain()
    }

    private suspend fun CustomerProfileDto.toDomain() = CustomerProfile(
        accountId = AccountId(accountId),
        displayName = displayName,
        avatar = avatarPath?.let { path ->
            ImageRef(
                url = client.storage.from("avatars").createSignedUrl(path, 1.hours),
                alternativeText = avatarAltText,
            )
        },
        fullName = fullName,
        phoneNumber = phoneNumber,
        preferredLanguage = preferredLanguage,
    )
}
