package org.shareat.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.data.language.AppLanguageApplier
import org.shareat.app.data.language.AppLanguageStorage
import org.shareat.app.data.language.IosAppLanguageApplier
import org.shareat.app.data.language.IosAppLanguageStorage
import org.shareat.app.data.supabase.IosKeychainSessionStorage
import org.shareat.app.data.supabase.SecureSessionStorage

actual val platformModule: Module = module {
    single<SecureSessionStorage> { IosKeychainSessionStorage() }
    single<AppLanguageStorage> { IosAppLanguageStorage() }
    single<AppLanguageApplier> { IosAppLanguageApplier() }
}
