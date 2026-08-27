package org.shareat.feature.profile

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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.feature.profile.navigation.EditProfileNavigation

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navigator: EditProfileNavigation = koinInject(),
    viewModel: EditProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreenStateless(
        uiState = uiState,
        modifier = modifier,
        callbacks = SettingsCallbacks(
            onBackClick = navigator::goBack,
            onEditProfileClick = {},
            onPasswordAndSecurityClick = {},
            onNotificationsClick = {},
            onPrivacyClick = {},
            onDownloadDataClick = {},
            onDeleteAccountClick = {},
            onLogOutClick = viewModel::onCloseSession,
            onRestaurantNameChange = viewModel::onRestaurantNameChange,
            onRestaurantCuisineChange = viewModel::onRestaurantCuisineChange,
            onRestaurantDescriptionChange = viewModel::onRestaurantDescriptionChange,
            onRestaurantPhoneChange = viewModel::onRestaurantPhoneChange,
            onRestaurantEmailChange = viewModel::onRestaurantEmailChange,
            onRestaurantWebsiteChange = viewModel::onRestaurantWebsiteChange,
            onRestaurantStreetChange = viewModel::onRestaurantStreetChange,
            onRestaurantCityChange = viewModel::onRestaurantCityChange,
            onRestaurantPostcodeChange = viewModel::onRestaurantPostcodeChange,
            onRestaurantVisibilityChange = viewModel::onRestaurantVisibilityChange,
            onOpeningDayChange = viewModel::onOpeningDayChange,
            onOpeningTimeChange = viewModel::onOpeningTimeChange,
            onClosingTimeChange = viewModel::onClosingTimeChange,
            onSaveClick = viewModel::onSaveClick,
        ),
    )
}

private data class SettingsCallbacks(
    val onBackClick: () -> Unit = {},
    val onPasswordAndSecurityClick: () -> Unit = {},
    val onNotificationsClick: () -> Unit = {},
    val onPrivacyClick: () -> Unit = {},
    val onEditProfileClick: () -> Unit = {},
    val onDownloadDataClick: () -> Unit = {},
    val onDeleteAccountClick: () -> Unit = {},
    val onLogOutClick: () -> Unit = {},
    val onRestaurantNameChange: (String) -> Unit = {},
    val onRestaurantCuisineChange: (String) -> Unit = {},
    val onRestaurantDescriptionChange: (String) -> Unit = {},
    val onRestaurantPhoneChange: (String) -> Unit = {},
    val onRestaurantEmailChange: (String) -> Unit = {},
    val onRestaurantWebsiteChange: (String) -> Unit = {},
    val onRestaurantStreetChange: (String) -> Unit = {},
    val onRestaurantCityChange: (String) -> Unit = {},
    val onRestaurantPostcodeChange: (String) -> Unit = {},
    val onRestaurantVisibilityChange: (Boolean) -> Unit = {},
    val onOpeningDayChange: (String, Boolean) -> Unit = { _, _ -> },
    val onOpeningTimeChange: (String, String) -> Unit = { _, _ -> },
    val onClosingTimeChange: (String, String) -> Unit = { _, _ -> },
    val onSaveClick: () -> Unit = {},
)

@Composable
private fun SettingsScreenStateless(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    callbacks: SettingsCallbacks = SettingsCallbacks(),
) {
    Surface(
        modifier = modifier.fillMaxSize().background(color = MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SettingsTopBar(
                title = when (uiState) {
                    is SettingsUiState.User -> "Settings"
                    is SettingsUiState.Restaurant -> "Restaurant settings"
                },
                onBackClick = callbacks.onBackClick,
                actionText = if (uiState is SettingsUiState.Restaurant) "Save" else null,
                onActionClick = callbacks.onSaveClick,
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
            TextButton(onClick = onActionClick) {
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

        Card(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
        ) {
            SettingsItem(
                leadingIcon = Icons.Default.Edit,
                text = "Edit profile",
                onClick = callbacks.onEditProfileClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Lock,
                text = "Password & security",
                onClick = callbacks.onPasswordAndSecurityClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Notifications,
                text = "Notifications",
                onClick = callbacks.onNotificationsClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Person,
                text = "Privacy",
                onClick = callbacks.onPrivacyClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Settings,
                text = "Connected accounts",
                onClick = {},
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.DateRange,
                text = "Review history",
                onClick = {},
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Email,
                text = "Download my data",
                onClick = callbacks.onDownloadDataClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Default.Delete,
                text = "Delete account",
                onClick = callbacks.onDeleteAccountClick,
                isDestructive = true,
                showChevron = false,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = callbacks.onLogOutClick,
            modifier = Modifier.fillMaxWidth(),
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
        item {
            RestaurantSectionCard(
                title = "Basic info",
                icon = Icons.Default.Info,
            ) {
                RestaurantTextField(
                    value = uiState.name,
                    label = "Restaurant name",
                    onValueChange = callbacks.onRestaurantNameChange,
                )
                RestaurantTextField(
                    value = uiState.cuisine,
                    label = "Cuisine / category",
                    onValueChange = callbacks.onRestaurantCuisineChange,
                )
                RestaurantTextField(
                    value = uiState.description,
                    label = "Short description",
                    onValueChange = callbacks.onRestaurantDescriptionChange,
                    minLines = 3,
                )
                RestaurantTextField(
                    value = uiState.phone,
                    label = "Contact phone",
                    onValueChange = callbacks.onRestaurantPhoneChange,
                )
                RestaurantTextField(
                    value = uiState.email,
                    label = "Contact email",
                    onValueChange = callbacks.onRestaurantEmailChange,
                )
                RestaurantTextField(
                    value = uiState.website,
                    label = "Website",
                    onValueChange = callbacks.onRestaurantWebsiteChange,
                )
            }
        }
        item {
            RestaurantSectionCard(
                title = "Location",
                icon = Icons.Default.LocationOn,
            ) {
                RestaurantTextField(
                    value = uiState.streetAddress,
                    label = "Street address",
                    onValueChange = callbacks.onRestaurantStreetChange,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedTextField(
                        value = uiState.city,
                        onValueChange = callbacks.onRestaurantCityChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("City") },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.postcode,
                        onValueChange = callbacks.onRestaurantPostcodeChange,
                        modifier = Modifier.weight(1f),
                        label = { Text("Postcode") },
                        singleLine = true,
                    )
                }
                TextButton(onClick = {}) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text("Adjust map pin")
                }
            }
        }
        item {
            RestaurantSectionCard(
                title = "Opening hours",
                icon = Icons.Default.DateRange,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = {}) {
                        Text("Special dates & holidays")
                    }
                }
                uiState.openingHours.forEach { hours ->
                    OpeningHoursRow(
                        hours = hours,
                        onOpenChange = { callbacks.onOpeningDayChange(hours.day, it) },
                        onOpeningTimeChange = {
                            callbacks.onOpeningTimeChange(hours.day, it)
                        },
                        onClosingTimeChange = {
                            callbacks.onClosingTimeChange(hours.day, it)
                        },
                    )
                    if (hours != uiState.openingHours.last()) {
                        SettingsDivider()
                    }
                }
                TextButton(onClick = {}) {
                    Text("+ Add split hours")
                }
            }
        }
        item {
            RestaurantSectionCard(
                title = "Management",
                icon = Icons.Default.Settings,
            ) {
                SettingsItem(Icons.Default.DateRange, "Reservations & order links", {})
                SettingsDivider()
                SettingsItem(Icons.Default.Settings, "Menu management", {})
                SettingsDivider()
                SettingsItem(Icons.Default.AccountCircle, "Photos & media", {})
                SettingsDivider()
                SettingsItem(Icons.Default.Notifications, "Notifications", {})
                SettingsDivider()
                SettingsItem(Icons.Default.Person, "Team & permissions", {})
            }
        }
        item {
            RestaurantSectionCard(
                title = "Account",
                icon = Icons.Default.AccountCircle,
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
                        onCheckedChange = callbacks.onRestaurantVisibilityChange,
                    )
                }
                SettingsDivider()
                SettingsItem(
                    leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                    text = "Log out",
                    onClick = callbacks.onLogOutClick,
                    isDestructive = true,
                    showChevron = false,
                )
            }
        }
        item {
            Button(
                onClick = callbacks.onSaveClick,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Save changes")
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
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(uiState.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                uiState.cuisine,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
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
                    fontWeight = FontWeight.SemiBold,
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
                text = hours.day,
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
    MaterialTheme {
        SettingsScreenStateless(uiState = SettingsUiState.User())
    }
}

@Preview
@Composable
private fun RestaurantSettingsScreenPreview() {
    MaterialTheme {
        SettingsScreenStateless(uiState = SettingsUiState.Restaurant())
    }
}

@Preview
@Composable
private fun SettingsItemPreview() {
    MaterialTheme {
        SettingsItem(
            leadingIcon = Icons.Default.Edit,
            text = "Edit profile",
            onClick = {},
        )
    }
}
