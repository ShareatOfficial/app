package org.shareat.feature.login.ui

import androidx.compose.runtime.Stable

@Stable
interface LoginNavigation {
    fun goBack()
    fun onLoginSuccess()
}
