package org.shareat.app.navigation.login

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.Serializable

@Serializable
data class LoginKey(
    @Polymorphic val redirectRoute: NavKey,
) : NavKey
