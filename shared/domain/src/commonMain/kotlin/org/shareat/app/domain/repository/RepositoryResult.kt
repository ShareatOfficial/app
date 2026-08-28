package org.shareat.app.domain.repository

sealed interface RepositoryError {
    data object Offline : RepositoryError
    data class Unavailable(val details: String? = null) : RepositoryError
    data object Unauthenticated : RepositoryError
    data object Forbidden : RepositoryError
    data object InvalidCredentials : RepositoryError
    data class Validation(val reason: String) : RepositoryError
    data class AlreadyExists(val entity: String) : RepositoryError
    data class NotFound(val entity: String, val id: String) : RepositoryError
    data class Conflict(val reason: String) : RepositoryError
}

sealed interface RepositoryResult<out T> {
    data class Success<T>(val value: T) : RepositoryResult<T>
    data class Failure(val error: RepositoryError) : RepositoryResult<Nothing>
}
