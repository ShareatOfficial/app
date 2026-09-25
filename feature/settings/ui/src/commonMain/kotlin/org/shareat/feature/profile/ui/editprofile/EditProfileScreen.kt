package org.shareat.feature.profile.ui.editprofile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.shared.designsystem.layout.safeDrawingTopPadding
import org.jetbrains.compose.resources.stringResource
import org.shareat.shared.designsystem.theme.ShareatTheme
import shareat.feature.settings.ui.generated.resources.Res
import shareat.feature.settings.ui.generated.resources.edit_profile_back
import shareat.feature.settings.ui.generated.resources.edit_profile_clear_display_name
import shareat.feature.settings.ui.generated.resources.edit_profile_display_name
import shareat.feature.settings.ui.generated.resources.edit_profile_display_name_hint
import shareat.feature.settings.ui.generated.resources.edit_profile_email
import shareat.feature.settings.ui.generated.resources.edit_profile_full_name
import shareat.feature.settings.ui.generated.resources.edit_profile_personal_data
import shareat.feature.settings.ui.generated.resources.edit_profile_phone
import shareat.feature.settings.ui.generated.resources.edit_profile_save
import shareat.feature.settings.ui.generated.resources.edit_profile_title
import shareat.feature.settings.ui.generated.resources.edit_profile_verified

@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    navigator: EditProfileNavigation = koinInject(),
    viewModel: EditProfileViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) navigator.goBack()
    }

    EditProfileScreenStateless(
        uiState = uiState,
        modifier = modifier,
        onBackClick = navigator::goBack,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun EditProfileScreenStateless(
    uiState: EditProfileUiState,
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit = {},
    onAction: (EditProfileAction) -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(modifier = Modifier.fillMaxSize().safeDrawingTopPadding()) {
            EditProfileTopBar(
                isSaving = uiState.isSaving,
                canSave = uiState.canSave,
                onBackClick = onBackClick,
                onSaveClick = { onAction(EditProfileAction.Save) },
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 20.dp, end = 20.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                ProfileAvatar(
                    initials = uiState.initials,
                )
                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = stringResource(Res.string.edit_profile_personal_data),
                    modifier = Modifier.fillMaxWidth(),
                    style = MaterialTheme.typography.headlineSmall,
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (uiState.isLoading) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(20.dp))
                }

                uiState.error?.let { error ->
                    Text(
                        text = error.label(),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                OutlinedTextField(
                    value = uiState.fullName,
                    onValueChange = { onAction(EditProfileAction.FullNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.edit_profile_full_name)) },
                    singleLine = true,
                    enabled = !uiState.isLoading && !uiState.isSaving,
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.displayName,
                    onValueChange = { onAction(EditProfileAction.DisplayNameChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.edit_profile_display_name)) },
                    supportingText = {
                        Text(stringResource(Res.string.edit_profile_display_name_hint))
                    },
                    trailingIcon = {
                        if (uiState.displayName.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    onAction(EditProfileAction.DisplayNameChanged(""))
                                },
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription =
                                        stringResource(Res.string.edit_profile_clear_display_name),
                                )
                            }
                        }
                    },
                    singleLine = true,
                    enabled = !uiState.isLoading && !uiState.isSaving,
                )
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.edit_profile_email)) },
                    leadingIcon = { Icon(Icons.Outlined.Mail, contentDescription = null) },
                    trailingIcon = {
                        if (uiState.isEmailVerified) {
                            Row(
                                modifier = Modifier.padding(end = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                                Text(
                                    text = stringResource(Res.string.edit_profile_verified),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    },
                    singleLine = true,
                    readOnly = true,
                    enabled = !uiState.isLoading,
                )
                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = uiState.phoneNumber,
                    onValueChange = { onAction(EditProfileAction.PhoneNumberChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(Res.string.edit_profile_phone)) },
                    leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null) },
                    singleLine = true,
                    enabled = !uiState.isLoading && !uiState.isSaving,
                )
            }
        }
    }
}

@Composable
private fun EditProfileTopBar(
    isSaving: Boolean,
    canSave: Boolean,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBackClick, enabled = !isSaving) {
            Icon(
                Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.edit_profile_back),
            )
        }
        Text(
            text = stringResource(Res.string.edit_profile_title),
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.headlineSmall,
        )
        TextButton(onClick = onSaveClick, enabled = canSave) {
            if (isSaving) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(Res.string.edit_profile_save))
            }
        }
    }
}

@Composable
private fun ProfileAvatar(
    initials: String,
) {
    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Preview
@Composable
private fun EditProfileScreenPreview() {
    ShareatTheme {
        EditProfileScreenStateless(
            uiState = EditProfileUiState(
                fullName = "Alex Rivera",
                displayName = "AlexR",
                email = "alex.rivera@example.com",
                phoneNumber = "+1 555-0123",
                isLoading = false,
            ),
        )
    }
}
