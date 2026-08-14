package org.shareat.app.data.supabase

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import org.koin.mp.KoinPlatform
import platform.Foundation.NSURL

fun handleSupabaseAuthDeeplink(url: NSURL) {
    KoinPlatform.getKoin().get<SupabaseClient>().handleDeeplinks(url)
}
