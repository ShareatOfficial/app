package org.shareat.app.data.supabase

import kotlinx.coroutines.CancellationException
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult

internal suspend inline fun <T> supabaseResult(crossinline operation: suspend () -> T): RepositoryResult<T> =
    try {
        RepositoryResult.Success(operation())
    } catch (error: CancellationException) {
        throw error
    } catch (error: Throwable) {
        RepositoryResult.Failure(error.toRepositoryError())
    }

private fun Throwable.toRepositoryError(): RepositoryError {
    val details = buildString {
        append(message.orEmpty())
        append(' ')
        append(cause?.message.orEmpty())
    }.lowercase()
    return when {
        this is DomainNotFound -> RepositoryError.NotFound(entity, identifier)
        this is DomainForbidden -> RepositoryError.Forbidden
        this is IllegalArgumentException -> RepositoryError.Validation(
            message ?: "The supplied value is invalid",
        )
        "invalid login credentials" in details || "invalid credentials" in details ->
            RepositoryError.InvalidCredentials
        "not authenticated" in details || "jwt" in details && "missing" in details || "401" in details ->
            RepositoryError.Unauthenticated
        "42501" in details || "permission denied" in details || "row-level security" in details || "403" in details ->
            RepositoryError.Forbidden
        "23505" in details || "already registered" in details || "duplicate key" in details ->
            RepositoryError.AlreadyExists("record")
        "constraint" in details || "validation" in details || "invalid input" in details ->
            RepositoryError.Validation(message ?: "The server rejected the supplied value")
        "timeout" in details || "network" in details || "connection" in details || "unresolved" in details ->
            RepositoryError.Offline
        else -> RepositoryError.Unavailable
    }
}

internal class DomainNotFound(
    val entity: String,
    val identifier: String,
) : Throwable()

internal class DomainForbidden : Throwable()
