package org.shareat.feature.subscription.ui

import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val subscriptionUiModule = module {
    viewModel { SubscriptionViewModel(get()) }
}
