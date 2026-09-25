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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.PublicPages
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.shared.designsystem.layout.safeDrawingTopPadding
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.settings.ui.generated.resources.Res
import shareat.feature.settings.ui.generated.resources.settings_language_title
import shareat.feature.settings.ui.generated.resources.settings_language
import shareat.feature.settings.ui.generated.resources.settings_apply
import shareat.feature.settings.ui.generated.resources.settings_back
import shareat.feature.settings.ui.generated.resources.settings_cancel
import shareat.feature.settings.ui.generated.resources.settings_change_hours
import shareat.feature.settings.ui.generated.resources.settings_city
import shareat.feature.settings.ui.generated.resources.settings_closes
import shareat.feature.settings.ui.generated.resources.settings_contact_email
import shareat.feature.settings.ui.generated.resources.settings_contact_phone
import shareat.feature.settings.ui.generated.resources.settings_day_closed
import shareat.feature.settings.ui.generated.resources.settings_day_open
import shareat.feature.settings.ui.generated.resources.settings_delete_account
import shareat.feature.settings.ui.generated.resources.settings_delete_cancel
import shareat.feature.settings.ui.generated.resources.settings_delete_close
import shareat.feature.settings.ui.generated.resources.settings_delete_confirm
import shareat.feature.settings.ui.generated.resources.settings_delete_description
import shareat.feature.settings.ui.generated.resources.settings_delete_success_description
import shareat.feature.settings.ui.generated.resources.settings_delete_success_title
import shareat.feature.settings.ui.generated.resources.settings_delete_title
import shareat.feature.settings.ui.generated.resources.settings_edit_profile
import shareat.feature.settings.ui.generated.resources.settings_hidden
import shareat.feature.settings.ui.generated.resources.settings_hours_dialog_title
import shareat.feature.settings.ui.generated.resources.settings_log_in
import shareat.feature.settings.ui.generated.resources.settings_log_out
import shareat.feature.settings.ui.generated.resources.settings_opens
import shareat.feature.settings.ui.generated.resources.settings_postcode
import shareat.feature.settings.ui.generated.resources.settings_privacy_policy
import shareat.feature.settings.ui.generated.resources.settings_published
import shareat.feature.settings.ui.generated.resources.settings_restaurant_name
import shareat.feature.settings.ui.generated.resources.settings_restaurant_title
import shareat.feature.settings.ui.generated.resources.settings_save
import shareat.feature.settings.ui.generated.resources.settings_save_changes
import shareat.feature.settings.ui.generated.resources.settings_saved
import shareat.feature.settings.ui.generated.resources.settings_section_account
import shareat.feature.settings.ui.generated.resources.settings_section_address
import shareat.feature.settings.ui.generated.resources.settings_section_basic
import shareat.feature.settings.ui.generated.resources.settings_section_hours
import shareat.feature.settings.ui.generated.resources.settings_short_description
import shareat.feature.settings.ui.generated.resources.settings_street
import shareat.feature.settings.ui.generated.resources.settings_terms
import shareat.feature.settings.ui.generated.resources.settings_title
import shareat.feature.settings.ui.generated.resources.settings_visibility_status

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navigator: SettingsNavigation = koinInject(),
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    var showDeletionConfirmation by remember { mutableStateOf(false) }
    var showDeletionSuccess by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                SettingsEvent.LogoutSuccess -> navigator.onLogoutSuccess()
                SettingsEvent.DeletionRequested -> showDeletionSuccess = true
                SettingsEvent.NavigateToEditProfile -> navigator.openEditProfile()
                SettingsEvent.NavigateToSubscription -> navigator.openSubscription()
            }
        }
    }
    SettingsScreenStateless(
        uiState = uiState,
        modifier = modifier,
        callbacks = SettingsCallbacks(
            onBackClick = navigator::goBack,
            onLoginClick = navigator::openLogin,
            onTermsAndConditionsClick = navigator::openTermsAndConditions,
            onPrivacyPolicyClick = { uriHandler.openUri(PublicPages.PRIVACY_POLICY) },
            onRequestDeletionClick = { showDeletionConfirmation = true },
            onLanguageSelected = { viewModel.onLanguageAction(SettingsLanguageAction(it)) },
            onUserAction = viewModel::onUserAction,
            onRestaurantAction = viewModel::onRestaurantAction,
        ),
    )

    if (showDeletionConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeletionConfirmation = false },
            title = { Text(stringResource(Res.string.settings_delete_title)) },
            text = { Text(stringResource(Res.string.settings_delete_description)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeletionConfirmation = false
                    when (uiState) {
                        is SettingsUiState.User -> viewModel.onUserAction(SettingsUserAction.RequestDeletion)
                        is SettingsUiState.Restaurant ->
                            viewModel.onRestaurantAction(SettingsRestaurantAction.RequestDeletion)
                        is SettingsUiState.Guest -> Unit
                    }
                }) { Text(stringResource(Res.string.settings_delete_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeletionConfirmation = false }) {
                    Text(stringResource(Res.string.settings_delete_cancel))
                }
            },
        )
    }
    if (showDeletionSuccess) {
        AlertDialog(
            onDismissRequest = { showDeletionSuccess = false },
            title = { Text(stringResource(Res.string.settings_delete_success_title)) },
            text = { Text(stringResource(Res.string.settings_delete_success_description)) },
            confirmButton = {
                TextButton(onClick = { showDeletionSuccess = false }) {
                    Text(stringResource(Res.string.settings_delete_close))
                }
            },
        )
    }
}

private data class SettingsCallbacks(
    val onBackClick: () -> Unit = {},
    val onLoginClick: () -> Unit = {},
    val onTermsAndConditionsClick: () -> Unit = {},
    val onPrivacyPolicyClick: () -> Unit = {},
    val onRequestDeletionClick: () -> Unit = {},
    val onLanguageSelected: (AppLanguage) -> Unit = {},
    val onUserAction: (SettingsUserAction) -> Unit = {},
    val onRestaurantAction: (SettingsRestaurantAction) -> Unit = {},
)

@Composable
private fun SettingsScreenStateless(
    uiState: SettingsUiState,
    modifier: Modifier = Modifier,
    callbacks: SettingsCallbacks = SettingsCallbacks(),
) {
    var showLanguageSheet by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingTopPadding()) {
            SettingsTopBar(
                title = when (uiState) {
                    is SettingsUiState.Guest -> stringResource(Res.string.settings_title)
                    is SettingsUiState.User -> stringResource(Res.string.settings_title)
                    is SettingsUiState.Restaurant -> stringResource(Res.string.settings_restaurant_title)
                },
                onBackClick = callbacks.onBackClick,
                actionText = if (uiState is SettingsUiState.Restaurant) {
                    stringResource(Res.string.settings_save)
                } else {
                    null
                },
                actionEnabled = uiState is SettingsUiState.Restaurant &&
                    !uiState.isLoading && !uiState.isSaving,
                onActionClick = {
                    callbacks.onRestaurantAction(SettingsRestaurantAction.SaveChanges)
                },
            )

            when (uiState) {
                is SettingsUiState.Guest -> GuestSettings(
                    uiState = uiState,
                    callbacks = callbacks,
                    onLanguageClick = { showLanguageSheet = true },
                )

                is SettingsUiState.User -> UserSettings(
                    uiState = uiState,
                    callbacks = callbacks,
                    onLanguageClick = { showLanguageSheet = true },
                )

                is SettingsUiState.Restaurant -> RestaurantSettings(
                    uiState = uiState,
                    callbacks = callbacks,
                    onLanguageClick = { showLanguageSheet = true },
                )
            }
        }
    }

    if (showLanguageSheet) {
        AppLanguageSheet(
            language = uiState.language,
            onSelect = {
                callbacks.onLanguageSelected(it)
                showLanguageSheet = false
            },
            onDismiss = { showLanguageSheet = false },
        )
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
                contentDescription = stringResource(Res.string.settings_back),
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
private fun GuestSettings(
    uiState: SettingsUiState.Guest,
    callbacks: SettingsCallbacks,
    onLanguageClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 20.dp, top = 16.dp, end = 20.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        uiState.error?.let { error ->
            SettingsStatusText(message = error.label(), isError = true)
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            ),
        ) {
            AppLanguageSettingsItem(uiState.language, onLanguageClick)
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Description,
                text = stringResource(Res.string.settings_terms),
                onClick = callbacks.onTermsAndConditionsClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Description,
                text = stringResource(Res.string.settings_privacy_policy),
                onClick = callbacks.onPrivacyPolicyClick,
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        if (uiState.error == null) {
            Button(
                onClick = callbacks.onLoginClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
            ) {
                Text(stringResource(Res.string.settings_log_in))
            }
        }
    }
}

@Composable
private fun UserSettings(
    uiState: SettingsUiState.User,
    callbacks: SettingsCallbacks,
    onLanguageClick: () -> Unit,
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
        uiState.error?.let { error ->
            SettingsStatusText(message = error.label(), isError = true)
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
                text = stringResource(Res.string.settings_edit_profile),
                onClick = { callbacks.onUserAction(SettingsUserAction.EditProfile) },
            )
            SettingsDivider()
            AppLanguageSettingsItem(uiState.language, onLanguageClick)
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Description,
                text = stringResource(Res.string.settings_terms),
                onClick = callbacks.onTermsAndConditionsClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.Description,
                text = stringResource(Res.string.settings_privacy_policy),
                onClick = callbacks.onPrivacyPolicyClick,
            )
            SettingsDivider()
            SettingsItem(
                leadingIcon = Icons.Outlined.ManageAccounts,
                text = stringResource(Res.string.settings_delete_account),
                onClick = callbacks.onRequestDeletionClick,
                isDestructive = true,
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
            Text(stringResource(Res.string.settings_log_out))
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
    onLanguageClick: () -> Unit,
) {
    val listState = rememberLazyListState()
    var showOpeningHoursSheet by remember { mutableStateOf(false) }
    // The status text is the first item, while "Guardar cambios" sits at the very bottom: a rejected
    // save would otherwise report itself off-screen.
    LaunchedEffect(uiState.error, uiState.saveSucceeded) {
        if (uiState.error != null || uiState.saveSucceeded) listState.animateScrollToItem(0)
    }

    LazyColumn(
        state = listState,
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
        uiState.error?.let { error ->
            item { SettingsStatusText(message = error.label(), isError = true) }
        }
        if (uiState.saveSucceeded) {
            item { SettingsStatusText(message = stringResource(Res.string.settings_saved), isError = false) }
        }
        item {
            RestaurantSectionCard(
                title = stringResource(Res.string.settings_section_basic),
                icon = Icons.Outlined.Storefront,
            ) {
                RestaurantTextField(
                    value = uiState.name,
                    label = stringResource(Res.string.settings_restaurant_name),
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.NameChanged(it))
                    },
                )
                RestaurantTextField(
                    value = uiState.description,
                    label = stringResource(Res.string.settings_short_description),
                    onValueChange = {
                        callbacks.onRestaurantAction(
                            SettingsRestaurantAction.DescriptionChanged(it),
                        )
                    },
                    minLines = 3,
                )
                RestaurantTextField(
                    value = uiState.phone,
                    label = stringResource(Res.string.settings_contact_phone),
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.PhoneChanged(it))
                    },
                )
                RestaurantTextField(
                    value = uiState.email,
                    label = stringResource(Res.string.settings_contact_email),
                    onValueChange = {
                        callbacks.onRestaurantAction(SettingsRestaurantAction.EmailChanged(it))
                    },
                )
            }
        }
        item {
            RestaurantSectionCard(
                title = stringResource(Res.string.settings_section_address),
                icon = Icons.Outlined.LocationOn,
            ) {
                RestaurantTextField(
                    value = uiState.streetAddress,
                    label = stringResource(Res.string.settings_street),
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
                        label = { Text(stringResource(Res.string.settings_city)) },
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = uiState.postcode,
                        onValueChange = {
                            callbacks.onRestaurantAction(SettingsRestaurantAction.PostcodeChanged(it))
                        },
                        modifier = Modifier.weight(1f),
                        label = { Text(stringResource(Res.string.settings_postcode)) },
                        singleLine = true,
                    )
                }
            }
        }
        item {
            RestaurantSectionCard(
                title = stringResource(Res.string.settings_section_hours),
                icon = Icons.Outlined.Schedule,
            ) {
                SettingsItem(
                    leadingIcon = Icons.Outlined.Schedule,
                    text = stringResource(Res.string.settings_change_hours),
                    onClick = { showOpeningHoursSheet = true },
                )
            }
        }
        item {
            RestaurantSectionCard(
                title = stringResource(Res.string.settings_section_account),
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
                            text = stringResource(Res.string.settings_visibility_status),
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Text(
                            text = stringResource(
                                if (uiState.isPublished) Res.string.settings_published else Res.string.settings_hidden,
                            ),
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
                AppLanguageSettingsItem(uiState.language, onLanguageClick)
                SettingsDivider()
                SettingsItem(
                    Icons.Outlined.Description,
                    stringResource(Res.string.settings_terms),
                    callbacks.onTermsAndConditionsClick,
                )
                SettingsDivider()
                SettingsItem(
                    Icons.Outlined.Description,
                    stringResource(Res.string.settings_privacy_policy),
                    callbacks.onPrivacyPolicyClick,
                )
                SettingsDivider()
                SettingsItem(
                    leadingIcon = Icons.Outlined.ManageAccounts,
                    text = stringResource(Res.string.settings_delete_account),
                    onClick = callbacks.onRequestDeletionClick,
                    isDestructive = true,
                )
                SettingsDivider()
                SettingsItem(
                    leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                    text = stringResource(Res.string.settings_log_out),
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
                    Text(stringResource(Res.string.settings_save_changes))
                }
            }
        }
    }

    if (showOpeningHoursSheet) {
        OpeningHoursBottomSheet(
            openingHours = uiState.openingHours,
            onDismiss = { showOpeningHoursSheet = false },
            onApply = { openingHours ->
                callbacks.onRestaurantAction(SettingsRestaurantAction.OpeningHoursChanged(openingHours))
                showOpeningHoursSheet = false
            },
        )
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
                    text = stringResource(
                        if (uiState.isPublished) Res.string.settings_published else Res.string.settings_hidden,
                    ),
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
                text = hours.day.label(),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
            )
            Text(
                text = stringResource(
                    if (hours.isOpen) Res.string.settings_day_open else Res.string.settings_day_closed,
                ),
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
                    label = { Text(stringResource(Res.string.settings_opens)) },
                    singleLine = true,
                )
                Text("–")
                OutlinedTextField(
                    value = hours.closingTime,
                    onValueChange = onClosingTimeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text(stringResource(Res.string.settings_closes)) },
                    singleLine = true,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OpeningHoursBottomSheet(
    openingHours: List<OpeningHoursUiState>,
    onDismiss: () -> Unit,
    onApply: (List<OpeningHoursUiState>) -> Unit,
) {
    // This state intentionally lives only for the sheet's lifetime. Canceling or dismissing the
    // sheet leaves SettingsUiState untouched; Apply is the single commit into the screen draft.
    var draft by remember(openingHours) { mutableStateOf(openingHours) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 20.dp),
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    stringResource(Res.string.settings_hours_dialog_title),
                    style = MaterialTheme.typography.headlineSmall,
                )
                draft.forEach { hours ->
                    OpeningHoursRow(
                        hours = hours,
                        onOpenChange = { isOpen ->
                            draft = draft.replaceHours(hours.day) { copy(isOpen = isOpen) }
                        },
                        onOpeningTimeChange = { value ->
                            draft = draft.replaceHours(hours.day) { copy(openingTime = value) }
                        },
                        onClosingTimeChange = { value ->
                            draft = draft.replaceHours(hours.day) { copy(closingTime = value) }
                        },
                    )
                    if (hours != draft.last()) SettingsDivider()
                }
            }
            Row(
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(stringResource(Res.string.settings_cancel))
                }
                Button(
                    onClick = { onApply(draft) },
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) {
                    Text(stringResource(Res.string.settings_apply))
                }
            }
        }
    }
}

private fun List<OpeningHoursUiState>.replaceHours(
    day: OpeningDay,
    transform: OpeningHoursUiState.() -> OpeningHoursUiState,
): List<OpeningHoursUiState> = map { hours ->
    if (hours.day == day) hours.transform() else hours
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
    enabled: Boolean = true,
    trailingText: String? = null,
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
            .clickable(enabled = enabled, onClick = onClick)
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
        if (trailingText != null) {
            Text(
                text = trailingText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.size(8.dp))
        }
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
private fun AppLanguageSettingsItem(
    language: AppLanguageUiState,
    onClick: () -> Unit,
) {
    SettingsItem(
        leadingIcon = Icons.Outlined.Language,
        text = stringResource(Res.string.settings_language),
        onClick = onClick,
        showChevron = language.canSelect,
        enabled = language.canSelect,
        trailingText = language.selected.label(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppLanguageSheet(
    language: AppLanguageUiState,
    onSelect: (AppLanguage) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                stringResource(Res.string.settings_language_title),
                style = MaterialTheme.typography.headlineSmall,
            )
            AppLanguage.entries.forEach { option ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(enabled = language.canSelect) { onSelect(option) }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(
                        selected = option == language.selected,
                        onClick = { onSelect(option) },
                        enabled = language.canSelect,
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(option.label(), style = MaterialTheme.typography.bodyLarge)
                }
            }
            language.appliesLaterNoticeOrNull()?.let { notice ->
                Text(
                    text = notice,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }
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
private fun GuestSettingsScreenPreview() {
    ShareatTheme {
        SettingsScreenStateless(uiState = SettingsUiState.Guest())
    }
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
            text = stringResource(Res.string.settings_edit_profile),
            onClick = {},
        )
    }
}
