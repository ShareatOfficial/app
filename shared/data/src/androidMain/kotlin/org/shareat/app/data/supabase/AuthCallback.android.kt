package org.shareat.app.data.supabase

import android.content.Intent
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform

fun handleSupabaseAuthDeeplink(intent: Intent) {
    KoinPlatform.getKoin().get<SupabaseClient>().handleDeeplinks(intent)
}
