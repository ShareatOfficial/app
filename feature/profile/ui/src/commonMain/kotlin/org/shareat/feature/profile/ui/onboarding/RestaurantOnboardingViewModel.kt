package org.shareat.feature.profile.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.shareat.app.domain.model.DailyOpeningHours
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.LocalTime
import org.shareat.app.domain.model.OpeningPeriod
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.CreateRestaurantProfileParams
import org.shareat.feature.profile.domain.CreateRestaurantProfileUseCase
import org.shareat.feature.profile.domain.SignOutUseCase

class RestaurantOnboardingViewModel(
    private val createRestaurantProfile: CreateRestaurantProfileUseCase,
    private val images: ImageRepository,
    private val imageProcessor: RestaurantImageProcessor,
    private val signOut: SignOutUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RestaurantOnboardingUiState())
    val uiState: StateFlow<RestaurantOnboardingUiState> = _uiState.asStateFlow()

    private val eventChannel = Channel<RestaurantOnboardingEvent>(Channel.BUFFERED)
    val events: Flow<RestaurantOnboardingEvent> = eventChannel.receiveAsFlow()

    fun onAction(action: RestaurantOnboardingAction) {
        when (action) {
            is RestaurantOnboardingAction.NameChanged -> edit { copy(name = action.value) }
            is RestaurantOnboardingAction.DescriptionChanged -> edit { copy(description = action.value) }
            is RestaurantOnboardingAction.EmailChanged -> edit { copy(publicEmail = action.value) }
            is RestaurantOnboardingAction.PhoneChanged -> edit { copy(publicPhone = action.value) }
            is RestaurantOnboardingAction.StreetChanged -> edit { copy(street = action.value) }
            is RestaurantOnboardingAction.CityChanged -> edit { copy(city = action.value) }
            is RestaurantOnboardingAction.PostcodeChanged -> edit { copy(postcode = action.value) }
            is RestaurantOnboardingAction.ProvinceChanged -> edit { copy(province = action.value) }
            is RestaurantOnboardingAction.ImageAltTextChanged -> edit { copy(imageAlternativeText = action.value) }
            is RestaurantOnboardingAction.DayEnabledChanged -> updateHours(action.day) {
                copy(enabled = action.enabled, opensAt = "11:00", closesAt = "22:00", error = null)
            }
            is RestaurantOnboardingAction.OpensAtChanged -> updateHours(action.day) {
                copy(opensAt = action.value, error = null)
            }
            is RestaurantOnboardingAction.ClosesAtChanged -> updateHours(action.day) {
                copy(closesAt = action.value, error = null)
            }
            RestaurantOnboardingAction.RemoveImage -> edit {
                copy(image = null, imageAlternativeText = "")
            }
            RestaurantOnboardingAction.Submit -> submit()
            RestaurantOnboardingAction.RetryImageUpload -> retryImageUpload()
            RestaurantOnboardingAction.ContinueWithoutImage -> complete()
            RestaurantOnboardingAction.Logout -> logout()
        }
    }

    fun onImageSelected(displayName: String, bytes: ByteArray) {
        if (_uiState.value.isProcessingImage || _uiState.value.isSubmitting) return
        _uiState.update { it.copy(isProcessingImage = true, errorMessage = null) }
        viewModelScope.launch {
            imageProcessor(displayName, bytes).fold(
                onSuccess = { image ->
                    _uiState.update {
                        it.copy(
                            image = image,
                            isProcessingImage = false,
                            errors = it.errors.copy(image = null),
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            image = null,
                            isProcessingImage = false,
                            errors = it.errors.copy(
                                image = error.message ?: "No se pudo procesar la imagen.",
                            ),
                        )
                    }
                },
            )
        }
    }

    fun onImagePickerError(message: String?) {
        _uiState.update {
            it.copy(errors = it.errors.copy(image = message ?: "No se pudo abrir el selector de imágenes."))
        }
    }

    private fun submit() {
        val state = _uiState.value
        if (state.isSubmitting || state.isProcessingImage || state.createdRestaurantId != null) return
        val validation = validate(state)
        _uiState.value = validation.state
        val params = validation.params ?: return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = createRestaurantProfile(params)) {
                is RepositoryResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            createdRestaurantId = result.value.id.value,
                        )
                    }
                    if (_uiState.value.image == null) complete() else uploadImage(result.value.id)
                }
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.toSpanishMessage())
                }
            }
        }
    }

    private fun retryImageUpload() {
        val restaurantId = _uiState.value.createdRestaurantId?.let(::RestaurantId) ?: return
        if (_uiState.value.isSubmitting) return
        viewModelScope.launch { uploadImage(restaurantId) }
    }

    private suspend fun uploadImage(restaurantId: RestaurantId) {
        val state = _uiState.value
        val image = state.image ?: return complete()
        _uiState.update { it.copy(isSubmitting = true, imageUploadFailed = false, errorMessage = null) }
        val alt = state.imageAlternativeText.trim().ifEmpty { null }
        when (val result = images.replaceImage(
            ImageTarget.RestaurantHero(restaurantId),
            ImageUpload(image.bytes, image.mimeType, alt),
        )) {
            is RepositoryResult.Success -> complete()
            is RepositoryResult.Failure -> _uiState.update {
                it.copy(
                    isSubmitting = false,
                    imageUploadFailed = true,
                    errorMessage = result.error.toSpanishMessage(),
                )
            }
        }
    }

    private fun complete() {
        viewModelScope.launch { eventChannel.send(RestaurantOnboardingEvent.Completed) }
    }

    private fun logout() {
        if (_uiState.value.isSubmitting) return
        _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = signOut()) {
                is RepositoryResult.Success -> eventChannel.send(RestaurantOnboardingEvent.LogoutSuccess)
                is RepositoryResult.Failure -> _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = result.error.toSpanishMessage())
                }
            }
        }
    }

    private fun updateHours(
        day: org.shareat.app.domain.model.Weekday,
        transform: OnboardingOpeningHours.() -> OnboardingOpeningHours,
    ) = edit { copy(hours = hours.map { if (it.day == day) it.transform() else it }) }

    private fun edit(transform: RestaurantOnboardingUiState.() -> RestaurantOnboardingUiState) {
        _uiState.update { it.transform().copy(errorMessage = null, errors = OnboardingFieldErrors()) }
    }
}

private data class ValidationResult(
    val state: RestaurantOnboardingUiState,
    val params: CreateRestaurantProfileParams?,
)

private fun validate(state: RestaurantOnboardingUiState): ValidationResult {
    val name = state.name.trim()
    val street = state.street.trim()
    val city = state.city.trim()
    val postcode = state.postcode.trim()
    val email = state.publicEmail.trim()
    var errors = OnboardingFieldErrors(
        name = if (name.isEmpty()) "Introduce el nombre del restaurante." else null,
        street = if (street.isEmpty()) "Introduce la calle y el número." else null,
        city = if (city.isEmpty()) "Introduce la ciudad." else null,
        postcode = when {
            postcode.isEmpty() -> "Introduce el código postal."
            !postcode.matches(Regex("\\d{5}")) -> "Introduce un código postal español de 5 cifras."
            else -> null
        },
        email = if (email.isNotEmpty() && !email.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
            "Introduce un correo válido."
        } else null,
        image = state.errors.image,
    )
    val mappedHours = mutableListOf<DailyOpeningHours>()
    val updatedHours = state.hours.map { hours ->
        if (!hours.enabled) return@map hours.copy(error = null)
        val opens = hours.opensAt.toLocalTimeOrNull()
        val closes = hours.closesAt.toLocalTimeOrNull()
        val error = when {
            opens == null || closes == null -> "Usa el formato HH:mm."
            opens == closes -> "La apertura y el cierre deben ser distintos."
            else -> null
        }
        if (error == null) {
            mappedHours += DailyOpeningHours(hours.day, listOf(OpeningPeriod(opens!!, closes!!)))
        }
        hours.copy(error = error)
    }
    if (updatedHours.any { it.error != null }) errors = errors.copy()
    val updatedState = state.copy(errors = errors, hours = updatedHours, errorMessage = null)
    if (errors.hasErrors || updatedHours.any { it.error != null }) return ValidationResult(updatedState, null)
    return ValidationResult(
        updatedState,
        CreateRestaurantProfileParams(
            name = name,
            description = state.description.trim().ifEmpty { null },
            publicEmail = email.takeIf { it.isNotEmpty() }?.let(::EmailAddress),
            publicPhone = state.publicPhone.trim().ifEmpty { null },
            address = PostalAddress(
                streetLine = street,
                locality = city,
                postalCode = postcode,
                region = state.province.trim().ifEmpty { null },
                countryCode = "ES",
                coordinates = null,
            ),
            openingHours = WeeklyOpeningHours(mappedHours),
        ),
    )
}

private fun String.toLocalTimeOrNull(): LocalTime? {
    val parts = split(':')
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    return runCatching { LocalTime(hour, minute) }.getOrNull()
}

private fun RepositoryError.toSpanishMessage(): String = when (this) {
    RepositoryError.Offline -> "Sin conexión. Comprueba tu red e inténtalo de nuevo."
    RepositoryError.Unauthenticated, RepositoryError.InvalidCredentials ->
        "La sesión ha caducado. Cierra sesión y vuelve a entrar."
    RepositoryError.Forbidden -> "Esta cuenta no puede crear un perfil de restaurante."
    is RepositoryError.Validation -> reason
    is RepositoryError.AlreadyExists -> "El perfil ya existe."
    is RepositoryError.Conflict -> reason
    is RepositoryError.NotFound -> "No se encontró el recurso solicitado."
    is RepositoryError.Unavailable -> "El servicio no está disponible temporalmente."
}
