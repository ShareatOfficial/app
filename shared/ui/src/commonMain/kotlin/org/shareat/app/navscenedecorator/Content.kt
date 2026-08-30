package org.shareat.app.navscenedecorator

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import org.shareat.app.navigation.Navigator
import org.shareat.feature.home.ui.navigation.HomeKey
import org.shareat.feature.profile.ui.profile.ProfileKey

data class TopLevelNavigationItem(
    val route: NavKey,
    val label: String,
    val icon: ImageVector,
)

val TOP_LEVEL_NAV_ITEMS = listOf(
    TopLevelNavigationItem(HomeKey, "Home", Icons.Default.Home),
    TopLevelNavigationItem(ProfileKey, "Profile", Icons.Default.Person),
)

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
            NavigationBarItem(
                selected = item.route == navigator.state.topLevelRoute,
                onClick = { navigator.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}

@Composable
fun TopLevelNavigationRail(items: List<TopLevelNavigationItem>, navigator: Navigator) {
    NavigationRail {
        items.forEach { item ->
            NavigationRailItem(
                selected = item.route == navigator.state.topLevelRoute,
                onClick = { navigator.navigate(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
