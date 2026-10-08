package org.shareat.feature.review.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.review.DishReviewViewModel
import org.shareat.feature.review.UnlistedDishReviewViewModel
import org.shareat.feature.review.domain.SubmitDishReviewsUseCase
import org.shareat.feature.review.domain.SubmitDishReviewsUseCaseImpl
import org.shareat.feature.review.domain.SubmitUnlistedDishReviewUseCase
import org.shareat.feature.review.domain.SubmitUnlistedDishReviewUseCaseImpl

public val reviewUiModule: Module = module {
    factory<SubmitUnlistedDishReviewUseCase> { SubmitUnlistedDishReviewUseCaseImpl(get(), get(), get()) }
    viewModel { UnlistedDishReviewViewModel(get()) }
    factory<SubmitDishReviewsUseCase> {
        SubmitDishReviewsUseCaseImpl(get(), get(), get(), get())
    }
    viewModel { parameters ->
        DishReviewViewModel(
            dishId = parameters.get(),
            submitDishReviews = get(),
        )
    }
}
