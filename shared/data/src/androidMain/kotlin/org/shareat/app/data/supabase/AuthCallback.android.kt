package org.shareat.app.data.supabase

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform

// The fake-data graph registers no SupabaseClient, so there is no deep link to hand over there.
fun handleSupabaseAuthDeeplink(intent: Intent) {
    KoinPlatform.getKoin().getOrNull<SupabaseClient>()?.handleDeeplinks(intent)
}
