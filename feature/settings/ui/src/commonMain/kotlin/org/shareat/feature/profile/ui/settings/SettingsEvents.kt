package org.shareat.feature.profile.ui.settings

internal sealed interface SettingsEvent {
    data object LogoutSuccess : SettingsEvent
    data object DeletionRequested : SettingsEvent
    data object NavigateToEditProfile : SettingsEvent
    data object NavigateToSubscription : SettingsEvent
}
