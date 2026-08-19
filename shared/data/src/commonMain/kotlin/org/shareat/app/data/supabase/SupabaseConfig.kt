package org.shareat.app.data.supabase

import org.shareat.app.data.config.SupabaseBuildConfig

data class SupabaseConfig(
    val url: String,
    val publishableKey: String,
    val callbackScheme: String = platformAuthCallbackScheme(),
    val callbackHost: String = platformAuthCallbackHost(),
) {
    init {
        require(url.startsWith("https://") || url.startsWith("http://127.0.0.1"))
        require(publishableKey.isNotBlank())
        require(!publishableKey.contains("service_role", ignoreCase = true))
        require(callbackScheme.isNotBlank())
        require(callbackHost.isNotBlank())
    }

    companion object {
        fun fromBuildConfig(): SupabaseConfig = SupabaseConfig(
            url = SupabaseBuildConfig.SUPABASE_URL,
            publishableKey = SupabaseBuildConfig.SUPABASE_PUBLISHABLE_KEY,
        )
    }
}

internal expect fun platformAuthCallbackScheme(): String

internal expect fun platformAuthCallbackHost(): String
