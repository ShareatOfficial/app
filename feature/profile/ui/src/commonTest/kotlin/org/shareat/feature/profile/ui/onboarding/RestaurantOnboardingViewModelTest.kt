package org.shareat.feature.profile.ui.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.feature.profile.domain.CreateRestaurantProfileUseCase
import org.shareat.feature.profile.domain.SignOutUseCase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantOnboardingViewModelTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest fun setUp() = Dispatchers.setMain(dispatcher)
    @AfterTest fun tearDown() = Dispatchers.resetMain()

    @Test
    fun mandatoryValidationPreventsSubmission() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            RepositoryResult.Success(restaurantFixture())
        })

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals(0, creates)
        assertNotNull(viewModel.uiState.value.errors.name)
        assertNotNull(viewModel.uiState.value.errors.street)
        assertNotNull(viewModel.uiState.value.errors.city)
        assertNotNull(viewModel.uiState.value.errors.postcode)
    }

    @Test
    fun optionalFieldsAndHoursAreMapped() = runTest(dispatcher) {
        var received: RestaurantProfileDraft? = null
        val viewModel = viewModel(create = {
            received = it
            RepositoryResult.Success(restaurantFixture().copy(name = it.name))
        })
        fillRequired(viewModel)
        viewModel.onAction(RestaurantOnboardingAction.DescriptionChanged("  Cocina local  "))
        viewModel.onAction(RestaurantOnboardingAction.EmailChanged("hola@example.com"))
        viewModel.onAction(RestaurantOnboardingAction.DayEnabledChanged(org.shareat.app.domain.model.Weekday.Monday, true))

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals("Cocina local", received?.description)
        assertEquals("hola@example.com", received?.publicEmail?.value)
        assertEquals(1, received?.openingHours?.days?.size)
        assertEquals(11, received?.openingHours?.days?.single()?.periods?.single()?.opensAt?.hour)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }

    @Test
    fun invalidEmailAndHoursStayOnForm() = runTest(dispatcher) {
        val viewModel = viewModel()
        fillRequired(viewModel)
        viewModel.onAction(RestaurantOnboardingAction.EmailChanged("invalid"))
        viewModel.onAction(RestaurantOnboardingAction.DayEnabledChanged(org.shareat.app.domain.model.Weekday.Friday, true))
        viewModel.onAction(RestaurantOnboardingAction.ClosesAtChanged(org.shareat.app.domain.model.Weekday.Friday, "11:00"))

        viewModel.onAction(RestaurantOnboardingAction.Submit)

        assertNotNull(viewModel.uiState.value.errors.email)
        assertNotNull(viewModel.uiState.value.hours.first { it.day == org.shareat.app.domain.model.Weekday.Friday }.error)
    }

    @Test
    fun imageFailureCanRetryWithoutCreatingAgain() = runTest(dispatcher) {
        var creates = 0
        val imageRepository = RecordingImageRepository(failuresBeforeSuccess = 1)
        val viewModel = viewModel(
            create = {
                creates += 1
                RepositoryResult.Success(restaurantFixture())
            },
            imageRepository = imageRepository,
        )
        fillRequired(viewModel)
        viewModel.onImageSelected("hero.jpg", byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 1))
        advanceUntilIdle()

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.imageUploadFailed)

        viewModel.onAction(RestaurantOnboardingAction.RetryImageUpload)
        advanceUntilIdle()

        assertEquals(1, creates)
        assertEquals(2, imageRepository.replaceCalls)
        assertFalse(viewModel.uiState.value.imageUploadFailed)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }

    @Test
    fun doubleSubmitIsIgnored() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            RepositoryResult.Success(restaurantFixture())
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertEquals(1, creates)
    }

    @Test
    fun invalidImageIsRejectedBeforeSubmission() = runTest(dispatcher) {
        val viewModel = viewModel(
            imageProcessor = RestaurantImageProcessor { _, _ ->
                Result.failure(IllegalArgumentException("Selecciona una imagen JPEG, PNG o WebP."))
            },
        )

        viewModel.onImageSelected("hero.gif", byteArrayOf(1, 2, 3))
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errors.image)
        assertEquals(null, viewModel.uiState.value.image)
    }

    @Test
    fun offlineCreationCanBeRetried() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(create = {
            creates += 1
            if (creates == 1) RepositoryResult.Failure(RepositoryError.Offline)
            else RepositoryResult.Success(restaurantFixture())
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.errorMessage?.contains("Sin conexión") == true)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()
        assertEquals(2, creates)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }

    @Test
    fun expiredSessionKeepsTheFormAndShowsRecoveryMessage() = runTest(dispatcher) {
        val viewModel = viewModel(create = {
            RepositoryResult.Failure(RepositoryError.Unauthenticated)
        })
        fillRequired(viewModel)

        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.errorMessage?.contains("sesión ha caducado") == true)
        assertEquals("Casa Nueva", viewModel.uiState.value.name)
    }

    @Test
    fun imageFailureCanContinueWithoutPhotoWithoutRecreating() = runTest(dispatcher) {
        var creates = 0
        val viewModel = viewModel(
            create = {
                creates += 1
                RepositoryResult.Success(restaurantFixture())
            },
            imageRepository = RecordingImageRepository(failuresBeforeSuccess = 1),
        )
        fillRequired(viewModel)
        viewModel.onImageSelected("hero.jpg", byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte()))
        advanceUntilIdle()
        viewModel.onAction(RestaurantOnboardingAction.Submit)
        advanceUntilIdle()

        viewModel.onAction(RestaurantOnboardingAction.ContinueWithoutImage)
        advanceUntilIdle()

        assertEquals(1, creates)
        assertEquals(RestaurantOnboardingEvent.Completed, viewModel.events.first())
    }
}

private fun viewModel(
    create: CreateRestaurantProfileUseCase = CreateRestaurantProfileUseCase {
        RepositoryResult.Success(restaurantFixture().copy(name = it.name))
    },
    imageRepository: ImageRepository = RecordingImageRepository(),
    imageProcessor: RestaurantImageProcessor = RestaurantImageProcessor { name, bytes ->
        Result.success(ProcessedRestaurantImage(name, bytes, "image/jpeg"))
    },
) = RestaurantOnboardingViewModel(
    createRestaurantProfile = create,
    images = imageRepository,
    imageProcessor = imageProcessor,
    signOut = SignOutUseCase { RepositoryResult.Success(Unit) },
)

private fun fillRequired(viewModel: RestaurantOnboardingViewModel) {
    viewModel.onAction(RestaurantOnboardingAction.NameChanged("Casa Nueva"))
    viewModel.onAction(RestaurantOnboardingAction.StreetChanged("Calle Mayor 1"))
    viewModel.onAction(RestaurantOnboardingAction.CityChanged("Madrid"))
    viewModel.onAction(RestaurantOnboardingAction.PostcodeChanged("28001"))
}

private class RecordingImageRepository(
    private var failuresBeforeSuccess: Int = 0,
) : ImageRepository {
    var replaceCalls = 0

    override suspend fun replaceImage(target: ImageTarget, upload: ImageUpload): RepositoryResult<ImageRef> {
        replaceCalls += 1
        if (failuresBeforeSuccess > 0) {
            failuresBeforeSuccess -= 1
            return RepositoryResult.Failure(RepositoryError.Offline)
        }
        return RepositoryResult.Success(ImageRef("https://example.com/hero.jpg"))
    }

    override suspend fun deleteImage(target: ImageTarget) = RepositoryResult.Success(Unit)
}

private fun restaurantFixture() = Restaurant(
    id = RestaurantId("restaurant-id"),
    ownerAccountId = AccountId("owner-id"),
    name = "Casa Nueva",
    address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
    openingHours = WeeklyOpeningHours(emptyList()),
    publicationState = RestaurantPublicationState.Draft,
)
