package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import org.shareat.app.domain.repository.AccountDeletionRepository
import org.shareat.app.domain.repository.RepositoryResult

internal class SupabaseAccountDeletionRepository(
    private val client: SupabaseClient,
) : AccountDeletionRepository {
    override suspend fun requestAccountDeletion(): RepositoryResult<Unit> = supabaseResult {
        val requestedAt = client.postgrest.rpc("request_account_deletion").decodeAs<String>()
        check(requestedAt.isNotBlank()) { "Account deletion request was not recorded" }
    }
}
