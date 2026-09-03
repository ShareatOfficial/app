package org.shareat.app.data

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.data.fake.FakeAccountRepository
import org.shareat.app.data.fake.FakeDishRepository
import org.shareat.app.data.fake.FakeAuthRepository
import org.shareat.app.data.fake.FakeImageRepository
import org.shareat.app.data.fake.FakeRestaurantRepository
import org.shareat.app.data.fake.FakeReviewRepository
import org.shareat.app.data.fake.FakeShareatData
import org.shareat.app.data.supabase.SupabaseAccountRepository
import org.shareat.app.data.supabase.SupabaseAuthRepository
import org.shareat.app.data.supabase.SupabaseConfig
import org.shareat.app.data.supabase.SupabaseDishRepository
import org.shareat.app.data.supabase.SupabaseImageRepository
import org.shareat.app.data.supabase.SupabaseRestaurantRepository
import org.shareat.app.data.supabase.SupabaseReviewRepository
import org.shareat.app.data.supabase.SecureSessionStorage
import org.shareat.app.data.supabase.createShareatSupabaseClient
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

/**
 * Deterministic bindings for previews, unit tests and explicit demo scenarios.
 */
val fakeDataModule: Module = module {
    single { FakeShareatData.preview() }
    single<AccountRepository> { FakeAccountRepository(get()) }
    single<RestaurantRepository> { FakeRestaurantRepository(get()) }
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
    single { SupabaseDishRepository(get()) }
    single<DishRepository> { get<SupabaseDishRepository>() }
    single<RestaurantRepository> { SupabaseRestaurantRepository(get()) }
    single<ReviewRepository> { SupabaseReviewRepository(get()) }
    single<ImageRepository> { SupabaseImageRepository(get()) }
}
