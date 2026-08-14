package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.storage.Storage

internal fun createShareatSupabaseClient(
    config: SupabaseConfig,
    secureSessionStorage: SecureSessionStorage?,
): SupabaseClient =
    createSupabaseClient(config.url, config.publishableKey) {
        install(Auth) {
            flowType = FlowType.PKCE
            scheme = config.callbackScheme
            host = config.callbackHost
            secureSessionStorage?.let { sessionManager = SecureSessionManager(it) }
        }
        install(Postgrest)
        install(Storage)
    }
