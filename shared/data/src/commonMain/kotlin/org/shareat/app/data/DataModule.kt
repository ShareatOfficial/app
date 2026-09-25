package org.shareat.app.data

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.data.fake.FakeAccountRepository
import org.shareat.app.data.fake.FakeAccountDeletionRepository
import org.shareat.app.data.fake.FakeDishRepository
import org.shareat.app.data.fake.FakeAuthRepository
import org.shareat.app.data.fake.FakeImageRepository
import org.shareat.app.data.fake.FakeMenuRepository
import org.shareat.app.data.fake.FakeRestaurantRepository
import org.shareat.app.data.fake.FakeReviewRepository
import org.shareat.app.data.fake.FakeShareatData
import org.shareat.app.data.language.AppLanguageApplier
import org.shareat.app.data.language.AppLanguageStorage
import org.shareat.app.data.language.LocalAppLanguageRepository
import org.shareat.app.data.language.NoAppLanguageStorage
import org.shareat.app.data.language.UnsupportedAppLanguageApplier
import org.shareat.app.data.supabase.SupabaseAccountRepository
import org.shareat.app.data.supabase.SupabaseAccountDeletionRepository
import org.shareat.app.data.supabase.SupabaseAuthRepository
import org.shareat.app.data.supabase.SupabaseConfig
import org.shareat.app.data.supabase.SupabaseDishRepository
import org.shareat.app.data.supabase.SupabaseImageRepository
import org.shareat.app.data.supabase.SupabaseMenuRepository
import org.shareat.app.data.supabase.SupabaseRestaurantRepository
import org.shareat.app.data.supabase.SupabaseReviewRepository
import org.shareat.app.data.supabase.SecureSessionStorage
import org.shareat.app.data.supabase.createShareatSupabaseClient
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AccountDeletionRepository
import org.shareat.app.domain.repository.AppLanguageRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

/**
 * The selected app language. Bound independently of the data source: it is a device preference,
 * not something either backend owns. Platforms without bindings fall back to "follow the system".
 */
val appLanguageModule: Module = module {
    single<AppLanguageRepository> {
        LocalAppLanguageRepository(
            getOrNull<AppLanguageStorage>() ?: NoAppLanguageStorage,
            getOrNull<AppLanguageApplier>() ?: UnsupportedAppLanguageApplier,
        )
    }
}

/**
 * Deterministic bindings for previews, unit tests and explicit demo scenarios.
 */
val fakeDataModule: Module = module {
    single { FakeShareatData.preview() }
    single<AccountRepository> { FakeAccountRepository(get()) }
    single<AccountDeletionRepository> { FakeAccountDeletionRepository() }
    single<RestaurantRepository> { FakeRestaurantRepository(get()) }
    single<MenuRepository> { FakeMenuRepository(get()) }
    single<DishRepository> { FakeDishRepository(get()) }
    single<ReviewRepository> { FakeReviewRepository(get()) }
    single<AuthRepository> { FakeAuthRepository() }
    single<ImageRepository> { FakeImageRepository() }
}

/** Runtime bindings. This module only accepts a publishable key; secret keys are rejected by [SupabaseConfig]. */
fun supabaseDataModule(config: SupabaseConfig = SupabaseConfig.fromBuildConfig()): Module = module {
    single { createShareatSupabaseClient(config, getOrNull<SecureSessionStorage>()) }
    single<AuthRepository> { SupabaseAuthRepository(get()) }
    single<AccountRepository> { SupabaseAccountRepository(get()) }
    single<AccountDeletionRepository> { SupabaseAccountDeletionRepository(get()) }
    single<DishRepository> { SupabaseDishRepository(get()) }
    single<RestaurantRepository> { SupabaseRestaurantRepository(get()) }
    single<MenuRepository> { SupabaseMenuRepository(get()) }
    single<ReviewRepository> { SupabaseReviewRepository(get()) }
    single<ImageRepository> { SupabaseImageRepository(get()) }
}
