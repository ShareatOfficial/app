package org.shareat.app.navigation.lastactivity

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module
import org.koin.dsl.navigation3.navigation
import org.shareat.app.navigation.Navigator
import org.shareat.feature.lastactivity.di.lastActivityModule
import org.shareat.feature.lastactivity.navigation.LastActivityKey
import org.shareat.feature.lastactivity.ui.LastActivityNavigation
import org.shareat.feature.lastactivity.ui.LastActivityScreen
import org.shareat.feature.review.UnlistedDishReviewScreen

@OptIn(KoinExperimentalAPI::class)
val lastActivityNavigationModule = module {
    includes(lastActivityModule)
    factory<LastActivityNavigation> { parameters ->
        LastActivityNavigationImpl(parameters.getOrNull<Navigator>() ?: get<Navigator>())
    }
    navigation<LastActivityKey> {
        var reviewSubmissionCount by remember { mutableStateOf(0) }
        var reviewOpeningToken by remember { mutableStateOf<Any?>(null) }

        LastActivityScreen(
            refreshKey = reviewSubmissionCount,
            onAddReviewClick = {
                reviewOpeningToken = Any()
            },
        )

        reviewOpeningToken?.let { openingToken ->
            UnlistedDishReviewScreen(
                onDismissRequest = { reviewOpeningToken = null },
                openingToken = openingToken,
                onReviewSubmitted = {
                    reviewOpeningToken = null
                    reviewSubmissionCount += 1
                },
            )
        }
    }
}
