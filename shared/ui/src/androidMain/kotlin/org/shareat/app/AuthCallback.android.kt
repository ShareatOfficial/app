package org.shareat.app

import android.content.Intent
import org.shareat.app.data.supabase.handleSupabaseAuthDeeplink

fun handleAuthCallback(intent: Intent) = handleSupabaseAuthDeeplink(intent)
