package org.shareat.app

import org.shareat.app.data.supabase.handleSupabaseAuthDeeplink
import platform.Foundation.NSURL

fun handleAuthCallback(url: String) {
    handleSupabaseAuthDeeplink(NSURL(string = url))
}
