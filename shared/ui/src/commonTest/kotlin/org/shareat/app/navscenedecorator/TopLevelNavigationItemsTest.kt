package org.shareat.app.navscenedecorator

import org.shareat.feature.home.ui.navigation.HomeKey
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.restauranthome.ui.navigation.RestaurantHomeKey
import shareat.shared.ui.generated.resources.Res
import shareat.shared.ui.generated.resources.nav_edit_menu
import shareat.shared.ui.generated.resources.nav_profile
import kotlin.test.Test
import kotlin.test.assertEquals

class TopLevelNavigationItemsTest {
    @Test
    fun restaurantOwnersOnlySeeEditMenuAndProfile() {
        val items = topLevelNavigationItems(RestaurantHomeKey)

        assertEquals(listOf(RestaurantHomeKey, SettingsKey), items.map(TopLevelNavigationItem::route))
        assertEquals(
            listOf(Res.string.nav_edit_menu, Res.string.nav_profile),
            items.map(TopLevelNavigationItem::label),
        )
    }

    @Test
    fun customersKeepHomeLastActivityAndSettings() {
        val items = topLevelNavigationItems(HomeKey)

        assertEquals(listOf(HomeKey, LastActivityKey, SettingsKey), items.map(TopLevelNavigationItem::route))
    }
}
