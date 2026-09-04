package org.shareat.feature.profile.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.shared.designsystem.theme.ShareatTheme

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigator: SettingsNavigation = koinInject(),
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.LogoutSuccess -> navigator.onLogoutSuccess()
                SettingsEvent.NavigateToEditProfile -> navigator.openEditProfile()
            }
        }
    }
    SettingsScreenStateless(
        uiState = uiState,
        modifier = modifier,
        callbacks = SettingsCallbacks(
            onBackClick = navigator::goBack,
            onUserAction = viewModel::onUserAction,
            onRestaurantAction = viewModel::onRestaurantAction,
        ),
    )
}

private data class SettingsCallbacks(
    val onBackClick: () -> Unit = {},
    val onUserAction: (SettingsUserAction) -> Unit = {},
    val onRestaurantAction: (SettingsRestaurantAction) -> Unit = {},
)

@Composable
private fun SettingsScreenStateless(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    callbacks: SettingsCallbacks = SettingsCallbacks(),
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(
                title = when (uiState) {
                    is SettingsUiState.User -> "Settings"
                    is SettingsUiState.Restaurant -> "Restaurant settings"
                },
                onBackClick = callbacks.onBackClick,
                actionText = if (uiState is SettingsUiState.Restaurant) "Save" else null,
                actionEnabled = uiState is SettingsUiState.Restaurant &&
                    !uiState.isLoading && !uiState.isSaving,
                onActionClick = {
                    callbacks.onRestaurantAction(SettingsRestaurantAction.SaveChanges)
                },
            )

            when (uiState) {
                is SettingsUiState.User -> UserSettings(
                    uiState = uiState,
                    callbacks = callbacks,
                )

                is SettingsUiState.Restaurant -> RestaurantSettings(
                    uiState = uiState,
                    callbacks = callbacks,
                )
            }
        }
    }
}

@Composable
private fun SettingsTopBar(
    title: String,
    onBackClick: () -> Unit,
    actionText: String?,
    actionEnabled: Boolean,
    onActionClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Go back",
            )
        }
        Text(
            text = title,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
        )
        if (actionText != null) {
            TextButton(onClick = onActionClick, enabled = actionEnabled) {
                Text(actionText)
            }
        }
    }
}

@Composable
private fun UserSettings(
    uiState: SettingsUiState.User,
    callbacks: SettingsCallbacks,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                start = 20.dp,
                top = 16.dp,
                end = 20.dp,
                bottom = 32.dp
            )
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp),

        ) {
        UserIdentityHeader(uiState)

        if (uiState.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        }
        uiState.errorMessage?.let { message ->
            SettingsStatusText(message = message, isError = true)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            SettingsItem(
                leadingIcon = Icons.Outlined.ManageAccounts,
                text = "Edit profile",
                onClick = { callbacks.onUserAction(SettingsUserAction.EditProfile) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Lock,
                text = "Password & security",
                onClick = { callbacks.onUserAction(SettingsUserAction.PasswordAndSecurity) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.NotificationsNone,
                text = "Notifications",
                onClick = { callbacks.onUserAction(SettingsUserAction.Notifications) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Security,
                text = "Privacy",
                onClick = { callbacks.onUserAction(SettingsUserAction.Privacy) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Link,
                text = "Connected accounts",
                onClick = { callbacks.onUserAction(SettingsUserAction.ConnectedAccounts) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.History,
                text = "Review history",
                onClick = { callbacks.onUserAction(SettingsUserAction.ReviewHistory) },
            )
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            SettingsItem(
                leadingIcon = Icons.Outlined.Download,
                text = "Download my data",
                onClick = { callbacks.onUserAction(SettingsUserAction.DownloadData) },
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Delete,
                text = "Delete account",
                onClick = { callbacks.onUserAction(SettingsUserAction.DeleteAccount) },
                isDestructive = true,
                showChevron = false,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { callbacks.onUserAction(SettingsUserAction.LogOut) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !uiState.isLoading,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = null,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text("Log out")
        }
    }

}

@Composable
private fun UserIdentityHeader(uiState: SettingsUiState.User) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = uiState.initials,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
        Text(
            text = uiState.name,
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = uiState.email,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun RestaurantSettings(
    uiState: SettingsUiState.Restaurant,
    callbacks: SettingsCallbacks,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            start = 20.dp,
            top = 8.dp,
            end = 20.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            RestaurantIdentityHeader(uiState)
        }
        if (uiState.isLoading) {
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        uiState.errorMessage?.let { message ->
            item { SettingsStatusText(message = message, isError = true) }
        }
        if (uiState.saveSucceeded) {
            item { SettingsStatusText(message = "Changes saved.", isError = false) }
        }
        item {
            RestaurantSectionCard(
                title = "Basic info",
                icon = Icons.Outlined.Storefront,
            ) {
                RestaurantTextField(
                    value = uiState.name,
                    label = "Restaurant name",
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.NameChanged(it))
                    },
                )
                RestaurantTextField(
                    value = uiState.description,
                    label = "Short description",
                    onValueChange = {
                        callbacks.onRestaurantAction(
                            SettingsRestaurantAction.DescriptionChanged(it),
                        )
                    },
                    minLines = 3,
                )
                RestaurantTextField(
                    value = uiState.phone,
                    label = "Contact phone",
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.PhoneChanged(it))
                    },
                )
                RestaurantTextField(
                    value = uiState.email,
                    label = "Contact email",
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.EmailChanged(it))
                    },
                )
            }
        }
        item {
            RestaurantSectionCard(
                title = "Location",
                icon = Icons.Outlined.LocationOn,
            ) {
                RestaurantTextField(
                    value = uiState.streetAddress,
                    label = "Street address",
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.StreetChanged(it))
                    },
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = {
                            callbacks.onRestaurantAction(SettingsRestaurantAction.CityChanged(it))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("City") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.postcode,
                        onValueChange = {
                            callbacks.onRestaurantAction(SettingsRestaurantAction.PostcodeChanged(it))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text("Postcode") },
                        singleLine = true,
                    )
                }
                TextButton(
                    onClick = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.AdjustMapPin)
                    },
                ) {
                    Icon(imageVector = Icons.Outlined.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Adjust map pin")
                }
            }
        }
        item {
            RestaurantSectionCard(
                title = "Opening hours",
                icon = Icons.Outlined.Schedule,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = {
                            callbacks.onRestaurantAction(
                                SettingsRestaurantAction.SpecialDatesAndHolidays,
                            )
                        },
                    ) {
                        Text("Special dates & holidays")
                    }
                }
                uiState.openingHours.forEach { hours ->
                    OpeningHoursRow(
                        hours = hours,
                        onOpenChange = {
                            callbacks.onRestaurantAction(
                                SettingsRestaurantAction.OpeningDayChanged(hours.day, it),
                            )
                        },
                        onOpeningTimeChange = {
                            callbacks.onRestaurantAction(
                                SettingsRestaurantAction.OpeningTimeChanged(hours.day, it),
                            )
                        },
                        onClosingTimeChange = {
                            callbacks.onRestaurantAction(
                                SettingsRestaurantAction.ClosingTimeChanged(hours.day, it),
                            )
                        },
                    )
                    if (hours != uiState.openingHours.last()) {
                        SettingsDivider()
                    }
                }
                TextButton(
                    onClick = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.AddSplitHours)
                    },
                ) {
                    Text("+ Add split hours")
                }
            }
        }
        item {
            RestaurantSectionCard(
                title = "Management",
                icon = Icons.Outlined.Tune,
            ) {
                SettingsItem(
                    Icons.Outlined.EventAvailable,
                    "Reservations & order links",
                    { callbacks.onRestaurantAction(SettingsRestaurantAction.ReservationsAndOrderLinks) },
                )
                SettingsDivider()
                SettingsItem(
                    Icons.Outlined.PhotoLibrary,
                    "Photos & media",
                    { callbacks.onRestaurantAction(SettingsRestaurantAction.PhotosAndMedia) },
                )
                SettingsDivider()
                SettingsItem(
                    Icons.Outlined.NotificationsNone,
                    "Notifications",
                    { callbacks.onRestaurantAction(SettingsRestaurantAction.Notifications) },
                )
                SettingsDivider()
                SettingsItem(
                    Icons.Outlined.Group,
                    "Team & permissions",
                    { callbacks.onRestaurantAction(SettingsRestaurantAction.TeamAndPermissions) },
                )
            }
        }
        item {
            RestaurantSectionCard(
                title = "Account",
                icon = Icons.Outlined.ManageAccounts,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Visibility status",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = if (uiState.isPublished) "Published" else "Hidden",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = uiState.isPublished,
                        onCheckedChange = {
                            callbacks.onRestaurantAction(
                                SettingsRestaurantAction.VisibilityChanged(it),
                            )
                        },
                    )
                }
                SettingsDivider()
                SettingsItem(
                    leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                    text = "Log out",
                    onClick = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.LogOut)
                    },
                    isDestructive = true,
                    showChevron = false,
                )
            }
        }
        item {
            Button(
                onClick = {
                    callbacks.onRestaurantAction(SettingsRestaurantAction.SaveChanges)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading && !uiState.isSaving,
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Save changes")
                }
            }
        }
    }
}

@Composable
private fun RestaurantIdentityHeader(uiState: SettingsUiState.Restaurant) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Storefront,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(uiState.name, style = MaterialTheme.typography.headlineSmall)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (uiState.isPublished) "Published" else "Hidden",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@Composable
private fun RestaurantSectionCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            content()
        }
    }
}

@Composable
private fun RestaurantTextField(
    value: String,
    label: String,
    onValueChange: (String) -> Unit,
    minLines: Int = 1,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        minLines = minLines,
        singleLine = minLines == 1,
    )
}

@Composable
private fun OpeningHoursRow(
    hours: OpeningHoursUiState,
    onOpenChange: (Boolean) -> Unit,
    onOpeningTimeChange: (String) -> Unit,
    onClosingTimeChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = hours.day.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = if (hours.isOpen) "Open" else "Closed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(8.dp))
            Switch(checked = hours.isOpen, onCheckedChange = onOpenChange)
        }
        if (hours.isOpen) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = hours.openingTime,
                    onValueChange = onOpeningTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Opens") },
                    singleLine = true,
                )
                Text("–")
                OutlinedTextField(
                    value = hours.closingTime,
                    onValueChange = onClosingTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("Closes") },
                    singleLine = true,
                )
            }
        }
    }
}

@Composable
private fun SettingsStatusText(
    message: String,
    isError: Boolean,
) {
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodyMedium,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.tertiary
        },
    )
}

@Composable
private fun SettingsItem(
    leadingIcon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false,
    showChevron: Boolean = true,
) {
    val contentColor = if (isDestructive) {
        MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = contentColor,
        )
        Spacer(modifier = Modifier.size(16.dp))
        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor,
        )
        if (showChevron) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Preview
@Composable
private fun UserSettingsScreenPreview() {
    ShareatTheme {
        SettingsScreenStateless(uiState = SettingsUiState.User())
    }
}

@Preview
@Composable
private fun RestaurantSettingsScreenPreview() {
    ShareatTheme {
        SettingsScreenStateless(uiState = SettingsUiState.Restaurant())
    }
}

@Preview
@Composable
private fun SettingsItemPreview() {
    ShareatTheme {
        SettingsItem(
            leadingIcon = Icons.Outlined.ManageAccounts,
            text = "Edit profile",
            onClick = {},
        )
    }
}
