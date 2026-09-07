package org.shareat.feature.review.di

import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.shareat.feature.review.DishReviewViewModel
import org.shareat.feature.review.domain.SubmitDishReviewsUseCase
import org.shareat.feature.review.domain.SubmitDishReviewsUseCaseImpl

public val reviewUiModule: Module = module {
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
