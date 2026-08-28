package org.shareat.feature.profile.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.outlined.AddPhotoAlternate
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.name
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.shareat.app.domain.model.Weekday
import shareat.feature.profile.ui.generated.resources.Res
import shareat.feature.profile.ui.generated.resources.*

@Composable
fun RestaurantOnboardingScreen(
    navigation: RestaurantOnboardingNavigation = koinInject(),
    viewModel: RestaurantOnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val picker = rememberFilePickerLauncher(
        type = FileKitType.Image,
        onResult = { file ->
            if (file != null) scope.launch {
                viewModel.onImageSelected(file.name, file.readBytes())
            }
        },
    )

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RestaurantOnboardingEvent.Completed -> navigation.onCompleted()
                RestaurantOnboardingEvent.LogoutSuccess -> navigation.onLogoutSuccess()
            }
        }
    }

    RestaurantOnboardingContent(
        state = state,
        onAction = viewModel::onAction,
        onPickImage = picker::launch,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RestaurantOnboardingContent(
    state: RestaurantOnboardingUiState,
    onAction: (RestaurantOnboardingAction) -> Unit,
    onPickImage: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Shareat", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(
                        enabled = !state.isSubmitting,
                        onClick = { onAction(RestaurantOnboardingAction.Logout) },
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.ExitToApp, contentDescription = null)
                        Spacer(Modifier.size(6.dp))
                        Text(stringResource(Res.string.onboarding_logout))
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    if (state.imageUploadFailed) {
                        Row(
                            modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSubmitting,
                                onClick = { onAction(RestaurantOnboardingAction.ContinueWithoutImage) },
                            ) { Text(stringResource(Res.string.onboarding_continue_without_image)) }
                            Button(
                                modifier = Modifier.weight(1f),
                                enabled = !state.isSubmitting,
                                onClick = { onAction(RestaurantOnboardingAction.RetryImageUpload) },
                            ) { Text(stringResource(Res.string.onboarding_retry_image)) }
                        }
                    } else {
                        Button(
                            modifier = Modifier.widthIn(max = 720.dp).fillMaxWidth().height(52.dp),
                            enabled = !state.isSubmitting && !state.isProcessingImage,
                            onClick = { onAction(RestaurantOnboardingAction.Submit) },
                        ) {
                            if (state.isSubmitting) {
                                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.size(8.dp))
                                Text(stringResource(Res.string.onboarding_saving))
                            } else Text(stringResource(Res.string.onboarding_create))
                        }
                    }
                }
            }
        },
    ) { padding ->
        BoxWithConstraints(Modifier.fillMaxSize().padding(padding)) {
            val wide = maxWidth >= 720.dp
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = if (wide) 40.dp else 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item {
                    Column(Modifier.widthIn(max = 1040.dp).fillMaxWidth()) {
                        Text(stringResource(Res.string.onboarding_title), style = MaterialTheme.typography.headlineLarge)
                        Spacer(Modifier.height(8.dp))
                        Text(stringResource(Res.string.onboarding_subtitle), style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item {
                    OnboardingSection(stringResource(Res.string.onboarding_image)) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Icon(
                                        if (state.image == null) Icons.Outlined.AddPhotoAlternate else Icons.Outlined.CheckCircle,
                                        contentDescription = null,
                                    )
                                    Column(Modifier.weight(1f)) {
                                        Text(state.image?.displayName ?: stringResource(Res.string.onboarding_optional))
                                        Text(stringResource(Res.string.onboarding_image_help), style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = onPickImage, enabled = !state.isProcessingImage) {
                                        Text(stringResource(if (state.image == null) Res.string.onboarding_select_image else Res.string.onboarding_change_image))
                                    }
                                    if (state.image != null) TextButton(onClick = { onAction(RestaurantOnboardingAction.RemoveImage) }) {
                                        Text(stringResource(Res.string.onboarding_remove_image))
                                    }
                                }
                                if (state.isProcessingImage) Text(stringResource(Res.string.onboarding_image_processing))
                                state.errors.image?.let { ErrorText(it) }
                                OutlinedTextField(
                                    value = state.imageAlternativeText,
                                    onValueChange = { onAction(RestaurantOnboardingAction.ImageAltTextChanged(it)) },
                                    label = { Text(stringResource(Res.string.onboarding_image_alt)) },
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = state.image != null,
                                )
                            }
                        }
                    }
                }
                item {
                    OnboardingSection(stringResource(Res.string.onboarding_basic)) {
                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { onAction(RestaurantOnboardingAction.NameChanged(it)) },
                            label = { Text(stringResource(Res.string.onboarding_name)) },
                            isError = state.errors.name != null,
                            supportingText = state.errors.name?.let { { ErrorText(it) } },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        OutlinedTextField(
                            value = state.description,
                            onValueChange = { onAction(RestaurantOnboardingAction.DescriptionChanged(it)) },
                            label = { Text(stringResource(Res.string.onboarding_description)) },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
                item {
                    OnboardingSection(stringResource(Res.string.onboarding_contact)) {
                        ResponsivePair(wide,
                            first = {
                                OnboardingField(state.publicEmail, { onAction(RestaurantOnboardingAction.EmailChanged(it)) }, stringResource(Res.string.onboarding_email), state.errors.email, KeyboardType.Email)
                            },
                            second = {
                                OnboardingField(state.publicPhone, { onAction(RestaurantOnboardingAction.PhoneChanged(it)) }, stringResource(Res.string.onboarding_phone), null, KeyboardType.Phone)
                            },
                        )
                    }
                }
                item {
                    OnboardingSection(stringResource(Res.string.onboarding_location)) {
                        OnboardingField(state.street, { onAction(RestaurantOnboardingAction.StreetChanged(it)) }, stringResource(Res.string.onboarding_street), state.errors.street)
                        ResponsivePair(wide,
                            first = { OnboardingField(state.city, { onAction(RestaurantOnboardingAction.CityChanged(it)) }, stringResource(Res.string.onboarding_city), state.errors.city) },
                            second = { OnboardingField(state.postcode, { onAction(RestaurantOnboardingAction.PostcodeChanged(it)) }, stringResource(Res.string.onboarding_postcode), state.errors.postcode, KeyboardType.Number) },
                        )
                        ResponsivePair(wide,
                            first = { OnboardingField(state.province, { onAction(RestaurantOnboardingAction.ProvinceChanged(it)) }, stringResource(Res.string.onboarding_province), null) },
                            second = {
                                OutlinedTextField(value = stringResource(Res.string.onboarding_spain), onValueChange = {}, enabled = false, label = { Text(stringResource(Res.string.onboarding_country)) }, modifier = Modifier.fillMaxWidth())
                            },
                        )
                    }
                }
                item {
                    OnboardingSection(stringResource(Res.string.onboarding_hours)) {
                        Text(stringResource(Res.string.onboarding_hours_help), style = MaterialTheme.typography.bodyMedium)
                        state.hours.forEach { hours -> OpeningHoursRow(hours, onAction) }
                    }
                }
                item {
                    Column(Modifier.widthIn(max = 1040.dp).fillMaxWidth()) {
                        state.errorMessage?.let { ErrorText(it) }
                        if (state.imageUploadFailed) {
                            Text(stringResource(Res.string.onboarding_image_error_title), fontWeight = FontWeight.Bold)
                            Text(stringResource(Res.string.onboarding_image_error_body))
                        } else {
                            Text(stringResource(Res.string.onboarding_draft_notice), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RestaurantOnboardingGateErrorScreen(onRetry: () -> Unit, onLogout: () -> Unit) {
    Surface(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(Icons.Outlined.CloudOff, contentDescription = null, modifier = Modifier.size(56.dp))
            Spacer(Modifier.height(20.dp))
            Text(stringResource(Res.string.onboarding_gate_error_title), style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(Res.string.onboarding_gate_error_body), style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry) { Text(stringResource(Res.string.onboarding_retry)) }
            TextButton(onClick = onLogout) { Text(stringResource(Res.string.onboarding_logout)) }
        }
    }
}

@Composable
private fun OnboardingSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.widthIn(max = 1040.dp).fillMaxWidth()) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            content()
        }
    }
}

@Composable
private fun ResponsivePair(wide: Boolean, first: @Composable () -> Unit, second: @Composable () -> Unit) {
    if (wide) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.weight(1f)) { first() }
        Box(Modifier.weight(1f)) { second() }
    } else Column(verticalArrangement = Arrangement.spacedBy(12.dp)) { first(); second() }
}

@Composable
private fun OnboardingField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = error?.let { { ErrorText(it) } },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun OpeningHoursRow(hours: OnboardingOpeningHours, onAction: (RestaurantOnboardingAction) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(weekdayLabel(hours.day), modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            if (!hours.enabled) Text(stringResource(Res.string.onboarding_not_configured), style = MaterialTheme.typography.bodySmall)
            Switch(checked = hours.enabled, onCheckedChange = { onAction(RestaurantOnboardingAction.DayEnabledChanged(hours.day, it)) })
        }
        if (hours.enabled) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) {
                    OnboardingField(hours.opensAt, { onAction(RestaurantOnboardingAction.OpensAtChanged(hours.day, it)) }, stringResource(Res.string.onboarding_opens), hours.error)
                }
                Box(Modifier.weight(1f)) {
                    OnboardingField(hours.closesAt, { onAction(RestaurantOnboardingAction.ClosesAtChanged(hours.day, it)) }, stringResource(Res.string.onboarding_closes), hours.error)
                }
            }
        }
    }
}

@Composable
private fun weekdayLabel(day: Weekday): String = stringResource(when (day) {
    Weekday.Monday -> Res.string.weekday_monday
    Weekday.Tuesday -> Res.string.weekday_tuesday
    Weekday.Wednesday -> Res.string.weekday_wednesday
    Weekday.Thursday -> Res.string.weekday_thursday
    Weekday.Friday -> Res.string.weekday_friday
    Weekday.Saturday -> Res.string.weekday_saturday
    Weekday.Sunday -> Res.string.weekday_sunday
})

@Composable
private fun ErrorText(message: String) = Text(message, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)

@Preview(name = "Onboarding mobile", widthDp = 390, heightDp = 844)
@Composable private fun MobilePreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(), {}, {})

@Preview(name = "Onboarding foldable", widthDp = 673, heightDp = 900)
@Composable private fun FoldablePreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(), {}, {})

@Preview(name = "Onboarding tablet", widthDp = 900, heightDp = 1000)
@Composable private fun TabletPreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(name = "Casa Naranja"), {}, {})

@Preview(name = "Onboarding desktop", widthDp = 1280, heightDp = 900)
@Composable private fun DesktopPreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(name = "Casa Naranja"), {}, {})

@Preview(name = "Onboarding validation", widthDp = 390, heightDp = 844)
@Composable private fun ValidationPreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(errors = OnboardingFieldErrors(name = "Introduce el nombre.", street = "Introduce la dirección.")), {}, {})

@Preview(name = "Onboarding image error", widthDp = 390, heightDp = 844)
@Composable private fun ImageErrorPreview() = RestaurantOnboardingContent(RestaurantOnboardingUiState(createdRestaurantId = "restaurant", imageUploadFailed = true), {}, {})
