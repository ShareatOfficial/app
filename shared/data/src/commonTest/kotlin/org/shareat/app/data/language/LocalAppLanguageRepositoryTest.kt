package org.shareat.app.data.language

import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.AppLanguage
import org.shareat.app.domain.model.AppLanguageSelectionSupport
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class RecordingStorage(initial: String? = null) : AppLanguageStorage {
    var stored: String? = initial
    override fun load(): String? = stored
    override fun save(languageTag: String?) {
        stored = languageTag
    }
}

private class RecordingApplier(
    override val selectionSupport: AppLanguageSelectionSupport = AppLanguageSelectionSupport.IMMEDIATE,
) : AppLanguageApplier {
    val applied = mutableListOf<AppLanguage>()
    override fun apply(language: AppLanguage) {
        applied += language
    }
}

class LocalAppLanguageRepositoryTest {
    @Test
    fun theStoredLanguageIsAppliedWhenTheRepositoryIsCreated() {
        val applier = RecordingApplier()

        val repository = LocalAppLanguageRepository(RecordingStorage("es"), applier)

        assertEquals(AppLanguage.Spanish, repository.observeSelected().value)
        assertEquals(listOf(AppLanguage.Spanish), applier.applied)
    }

    @Test
    fun noStoredChoiceFollowsTheSystem() {
        val repository = LocalAppLanguageRepository(RecordingStorage(), RecordingApplier())

        assertEquals(AppLanguage.System, repository.observeSelected().value)
    }

    @Test
    fun anUnknownStoredTagFallsBackToTheSystemInsteadOfFailing() {
        val repository = LocalAppLanguageRepository(RecordingStorage("kl"), RecordingApplier())

        assertEquals(AppLanguage.System, repository.observeSelected().value)
    }

    @Test
    fun selectingPersistsAppliesAndPublishesTheChoice() = runTest {
        val storage = RecordingStorage()
        val applier = RecordingApplier()
        val repository = LocalAppLanguageRepository(storage, applier)

        repository.select(AppLanguage.Spanish)

        assertEquals("es", storage.stored)
        assertEquals(AppLanguage.Spanish, repository.observeSelected().value)
        assertTrue(applier.applied.last() == AppLanguage.Spanish)
    }

    @Test
    fun selectingTheSystemLanguageClearsTheStoredTag() = runTest {
        val storage = RecordingStorage("es")
        val repository = LocalAppLanguageRepository(storage, RecordingApplier())

        repository.select(AppLanguage.System)

        assertEquals(null, storage.stored)
        assertEquals(AppLanguage.System, repository.observeSelected().value)
    }

    @Test
    fun aPlatformThatCannotOverrideItsLocaleIgnoresASelection() = runTest {
        val storage = RecordingStorage()
        val applier = RecordingApplier(AppLanguageSelectionSupport.UNSUPPORTED)
        val repository = LocalAppLanguageRepository(storage, applier)

        repository.select(AppLanguage.Spanish)

        assertEquals(null, storage.stored)
        assertEquals(AppLanguage.System, repository.observeSelected().value)
    }
}
