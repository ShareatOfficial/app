package org.shareat.feature.login.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class LoginKey(
    @Polymorphic val redirectRoute: NavKey? = null,
) : NavKey
