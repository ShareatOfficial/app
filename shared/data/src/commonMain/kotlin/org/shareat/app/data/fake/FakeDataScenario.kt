package org.shareat.app.data.fake

import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

enum class FakeDataScenario {
    Populated,
    Empty,
    Offline,
    Unavailable,
}

internal inline fun <T> FakeDataScenario.result(
    populated: () -> T,
    empty: () -> T,
): RepositoryResult<T> = when (this) {
    FakeDataScenario.Populated -> RepositoryResult.Success(populated())
    FakeDataScenario.Empty -> RepositoryResult.Success(empty())
    FakeDataScenario.Offline -> RepositoryResult.Failure(RepositoryError.Offline)
    FakeDataScenario.Unavailable -> RepositoryResult.Failure(RepositoryError.Unavailable)
}
