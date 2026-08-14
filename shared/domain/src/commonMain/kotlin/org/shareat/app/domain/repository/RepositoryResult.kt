package org.shareat.app.domain.repository

sealed interface RepositoryError {
    data object Offline : RepositoryError
    data object Unavailable : RepositoryError
    data class NotFound(val entity: String, val id: String) : RepositoryError
    data class Conflict(val reason: String) : RepositoryError
}

sealed interface RepositoryResult<out T> {
    data class Success<T>(val value: T) : RepositoryResult<T>
    data class Failure(val error: RepositoryError) : RepositoryResult<Nothing>
}
