package org.shareat.feature.profile.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.shareat.shared.navigation.RequiresLogin

@Serializable
data object ProfileKey : NavKey

@Serializable
data object EditProfileKey : RequiresLogin
