package org.shareat.app.navscenedecorator

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.navigation.Navigator
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.restauranthome.ui.navigation.RestaurantHomeKey
import shareat.shared.ui.generated.resources.Res
import shareat.shared.ui.generated.resources.nav_activity
import shareat.shared.ui.generated.resources.nav_edit_menu
import shareat.shared.ui.generated.resources.nav_home
import shareat.shared.ui.generated.resources.nav_profile
import shareat.shared.ui.generated.resources.nav_settings

data class TopLevelNavigationItem(
    val route: NavKey,
    val label: StringResource,
    val icon: ImageVector,
)

fun topLevelNavigationItems(homeRoute: NavKey): List<TopLevelNavigationItem> =
    if (homeRoute == RestaurantHomeKey) {
        listOf(
            TopLevelNavigationItem(RestaurantHomeKey, Res.string.nav_edit_menu, Icons.Default.Home),
            TopLevelNavigationItem(SettingsKey, Res.string.nav_profile, Icons.Default.Settings),
        )
    } else {
        listOf(
            TopLevelNavigationItem(homeRoute, Res.string.nav_home, Icons.Default.Home),
            TopLevelNavigationItem(LastActivityKey, Res.string.nav_activity, Icons.Default.History),
            TopLevelNavigationItem(SettingsKey, Res.string.nav_settings, Icons.Default.Settings),
        )
    }

@Composable
fun TopLevelNavigationBar(items: List<TopLevelNavigationItem>, navigator: Navigator) {
    NavigationBar(
        modifier = Modifier.consumeWindowInsets(
            WindowInsets.safeDrawing.only(
                WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom,
            ),
        ),
    ) {
        items.forEach { item ->
            val label = stringResource(item.label)
            NavigationBarItem(
                selected = item.route == navigator.state.topLevelRoute,
                onClick = { navigator.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
fun TopLevelNavigationRail(items: List<TopLevelNavigationItem>, navigator: Navigator) {
    NavigationRail {
        items.forEach { item ->
            val label = stringResource(item.label)
            NavigationRailItem(
                selected = item.route == navigator.state.topLevelRoute,
                onClick = { navigator.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = label) },
                label = { Text(label) },
            )
        }
    }
}
