package org.shareat.app.di

import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext
import org.shareat.app.data.language.AndroidAppLanguageApplier
import org.shareat.app.data.language.AndroidAppLanguageStorage
import org.shareat.app.data.language.AppLanguageApplier
import org.shareat.app.data.language.AppLanguageStorage
import org.shareat.app.data.supabase.AndroidKeystoreSessionStorage
import org.shareat.app.data.supabase.SecureSessionStorage

actual val platformModule: Module = module {
    single<SecureSessionStorage> { AndroidKeystoreSessionStorage(androidContext()) }
    single<AppLanguageStorage> { AndroidAppLanguageStorage(androidContext()) }
    single<AppLanguageApplier> { AndroidAppLanguageApplier() }
}
