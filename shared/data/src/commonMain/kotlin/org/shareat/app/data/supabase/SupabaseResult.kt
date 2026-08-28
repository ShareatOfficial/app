package org.shareat.app.data.supabase

import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
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

private fun Throwable.toRepositoryError(): RepositoryError = when (this) {
    is DomainNotFound -> RepositoryError.NotFound(entity, identifier)
    is DomainForbidden -> RepositoryError.Forbidden
    is IllegalArgumentException -> RepositoryError.Validation(message ?: "The supplied value is invalid")
    is AuthRestException -> toRepositoryError()
    is PostgrestRestException -> toRepositoryError()
    is RestException -> statusCodeError()
    is HttpRequestException -> RepositoryError.Offline
    else -> if (looksLikeNetworkFailure()) RepositoryError.Offline else RepositoryError.Unavailable(diagnostics())
}

private fun AuthRestException.toRepositoryError(): RepositoryError = when (errorCode) {
    AuthErrorCode.InvalidCredentials,
    AuthErrorCode.UserNotFound,
    AuthErrorCode.BadCodeVerifier,
    -> RepositoryError.InvalidCredentials

    AuthErrorCode.EmailExists,
    AuthErrorCode.PhoneExists,
    AuthErrorCode.UserAlreadyExists,
    AuthErrorCode.IdentityAlreadyExists,
    -> RepositoryError.AlreadyExists("account")

    AuthErrorCode.WeakPassword,
    AuthErrorCode.ValidationFailed,
    AuthErrorCode.EmailAddressInvalid,
    AuthErrorCode.SamePassword,
    -> RepositoryError.Validation(errorDescription)

    AuthErrorCode.BadJwt,
    AuthErrorCode.NoAuthorization,
    AuthErrorCode.SessionNotFound,
    AuthErrorCode.SessionExpired,
    AuthErrorCode.RefreshTokenNotFound,
    AuthErrorCode.RefreshTokenAlreadyUsed,
    AuthErrorCode.ReauthenticationNeeded,
    -> RepositoryError.Unauthenticated

    AuthErrorCode.UserBanned,
    AuthErrorCode.NotAdmin,
    AuthErrorCode.EmailAddressNotAuthorized,
    -> RepositoryError.Forbidden

    AuthErrorCode.EmailNotConfirmed,
    AuthErrorCode.PhoneNotConfirmed,
    AuthErrorCode.SignupDisabled,
    AuthErrorCode.EmailProviderDisabled,
    AuthErrorCode.ProviderDisabled,
    AuthErrorCode.OverRequestRateLimit,
    AuthErrorCode.OverEmailSendRateLimit,
    AuthErrorCode.OverSmsSendRateLimit,
    -> RepositoryError.Conflict(errorDescription)

    AuthErrorCode.RequestTimeout,
    AuthErrorCode.HookTimeout,
    AuthErrorCode.HookTimeoutAfterRetry,
    -> RepositoryError.Offline

    else -> statusCodeError()
}

private fun PostgrestRestException.toRepositoryError(): RepositoryError = when (code) {
    POSTGRES_UNIQUE_VIOLATION -> RepositoryError.AlreadyExists("record")
    POSTGRES_INSUFFICIENT_PRIVILEGE -> RepositoryError.Forbidden
    POSTGRES_FOREIGN_KEY_VIOLATION,
    POSTGRES_CHECK_VIOLATION,
    POSTGRES_NOT_NULL_VIOLATION,
    POSTGRES_INVALID_PARAMETER,
    POSTGRES_RAISE_EXCEPTION,
    -> RepositoryError.Validation(message ?: "The server rejected the supplied value")

    else -> statusCodeError()
}

private fun RestException.statusCodeError(): RepositoryError = when (statusCode) {
    401 -> RepositoryError.Unauthenticated
    403 -> RepositoryError.Forbidden
    404 -> RepositoryError.NotFound(error, "")
    409 -> RepositoryError.Conflict(description ?: "The record conflicts with the current server state")
    422 -> RepositoryError.Validation(description ?: "The server rejected the supplied value")
    429 -> RepositoryError.Conflict("Too many attempts. Wait a moment and try again")
    else -> RepositoryError.Unavailable(diagnostics())
}

private fun Throwable.diagnostics(): String = buildString {
    append(this@diagnostics::class.simpleName ?: "Throwable")
    if (this@diagnostics is RestException) {
        append(" HTTP ").append(statusCode)
        append(" ").append(error)
        description?.let { append(": ").append(it.lineSequence().first()) }
    } else {
        message?.let { append(": ").append(it.lineSequence().first()) }
    }
}

private fun Throwable.looksLikeNetworkFailure(): Boolean {
    val details = "${message.orEmpty()} ${cause?.message.orEmpty()}".lowercase()
    return NETWORK_FAILURE_MARKERS.any { it in details }
}

private val NETWORK_FAILURE_MARKERS = listOf(
    "timeout",
    "timed out",
    "network",
    "connection",
    "failed to connect",
    "resolve host",
    "unreachable",
    "socket",
)

private const val POSTGRES_UNIQUE_VIOLATION = "23505"
private const val POSTGRES_FOREIGN_KEY_VIOLATION = "23503"
private const val POSTGRES_NOT_NULL_VIOLATION = "23502"
private const val POSTGRES_CHECK_VIOLATION = "23514"
private const val POSTGRES_INVALID_PARAMETER = "22023"
private const val POSTGRES_RAISE_EXCEPTION = "P0001"
private const val POSTGRES_INSUFFICIENT_PRIVILEGE = "42501"

internal class DomainNotFound(
    val entity: String,
    val identifier: String,
) : Throwable()

internal class DomainForbidden : Throwable()
