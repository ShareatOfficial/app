package org.shareat.app.navigation.profile

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.shareat.app.navigation.RequiresLogin

@Serializable
data object ProfileKey : NavKey

@Serializable
data object EditProfileKey : RequiresLogin
