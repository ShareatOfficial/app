package org.shareat.app.data

import org.koin.core.module.Module
import org.koin.dsl.module
import org.shareat.app.data.fake.FakeAccountRepository
import org.shareat.app.data.fake.FakeDishRepository
import org.shareat.app.data.fake.FakeMenuRepository
import org.shareat.app.data.fake.FakeRestaurantRepository
import org.shareat.app.data.fake.FakeReviewRepository
import org.shareat.app.data.fake.FakeShareatData
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

/**
 * Fake bindings used by :shared:ui until a remote data module is introduced.
 * Replace only these bindings when a remote data source is introduced.
 */
val fakeDataModule: Module = module {
    single { FakeShareatData.preview() }
    single<AccountRepository> { FakeAccountRepository(get()) }
    single<RestaurantRepository> { FakeRestaurantRepository(get()) }
    single<MenuRepository> { FakeMenuRepository(get()) }
    single<DishRepository> { FakeDishRepository(get()) }
    single<ReviewRepository> { FakeReviewRepository(get()) }
}
