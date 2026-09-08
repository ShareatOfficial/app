package org.shareat.feature.subscription.data

import org.koin.dsl.module
import org.shareat.feature.subscription.domain.SubscriptionRepository

actual val subscriptionDataModule = module {
    single<SubscriptionRepository>(createdAtStart = true) { RevenueCatSubscriptionRepository() }
}
