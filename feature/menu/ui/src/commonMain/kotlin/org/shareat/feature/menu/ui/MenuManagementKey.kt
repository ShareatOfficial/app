package org.shareat.feature.menu.ui

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable
import org.shareat.shared.navigation.RequiresLogin

@Serializable
data object MenuManagementKey : NavKey, RequiresLogin
