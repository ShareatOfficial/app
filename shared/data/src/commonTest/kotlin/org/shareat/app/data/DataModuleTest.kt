package org.shareat.app.data

import kotlin.test.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinApplication
import org.koin.test.check.checkModules
import org.shareat.app.data.supabase.SupabaseConfig

@OptIn(KoinExperimentalAPI::class)
class DataModuleTest {
    @Test
    fun fakeGraphIsComplete() {
        koinApplication { modules(fakeDataModule) }.checkModules()
    }

    @Test
    fun supabaseGraphIsCompleteWithoutAPlatformSessionOverride() {
        koinApplication {
            modules(
                supabaseDataModule(
                    SupabaseConfig(
                        url = "http://127.0.0.1:54321",
                        publishableKey = "local-publishable-test-key",
                    ),
                ),
            )
        }.checkModules()
    }
}
