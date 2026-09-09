package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform
import platform.Foundation.NSURL

// The fake-data graph registers no SupabaseClient, so there is no deep link to hand over there.
fun handleSupabaseAuthDeeplink(url: NSURL) {
    KoinPlatform.getKoin().getOrNull<SupabaseClient>()?.handleDeeplinks(url)
}
