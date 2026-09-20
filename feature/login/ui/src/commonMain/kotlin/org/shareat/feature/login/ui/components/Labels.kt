package org.shareat.feature.login.ui.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.shareat.app.domain.model.AccountRole
import org.shareat.feature.login.ui.model.LoginError
import shareat.feature.login.ui.generated.resources.Res
import shareat.feature.login.ui.generated.resources.login_error_already_exists
import shareat.feature.login.ui.generated.resources.login_error_email_required
import shareat.feature.login.ui.generated.resources.login_error_forbidden
import shareat.feature.login.ui.generated.resources.login_error_generic
import shareat.feature.login.ui.generated.resources.login_error_invalid_credentials
import shareat.feature.login.ui.generated.resources.login_error_invalid_input
import shareat.feature.login.ui.generated.resources.login_error_not_found
import shareat.feature.login.ui.generated.resources.login_error_offline
import shareat.feature.login.ui.generated.resources.login_error_session
import shareat.feature.login.ui.generated.resources.login_error_unavailable
import shareat.feature.login.ui.generated.resources.login_role_customer
import shareat.feature.login.ui.generated.resources.login_role_customer_hint
import shareat.feature.login.ui.generated.resources.login_role_restaurant
import shareat.feature.login.ui.generated.resources.login_role_restaurant_hint

@Composable
internal fun LoginError.label(): String = stringResource(
    when (this) {
        LoginError.INVALID_INPUT -> Res.string.login_error_invalid_input
        LoginError.EMAIL_REQUIRED -> Res.string.login_error_email_required
        LoginError.INVALID_CREDENTIALS -> Res.string.login_error_invalid_credentials
        LoginError.OFFLINE -> Res.string.login_error_offline
        LoginError.UNAUTHENTICATED -> Res.string.login_error_session
        LoginError.FORBIDDEN -> Res.string.login_error_forbidden
        LoginError.TEMPORARILY_UNAVAILABLE -> Res.string.login_error_unavailable
        LoginError.ALREADY_EXISTS -> Res.string.login_error_already_exists
        LoginError.NOT_FOUND -> Res.string.login_error_not_found
        LoginError.UNKNOWN -> Res.string.login_error_generic
    },
)

@Composable
internal fun AccountRole.label(): String = stringResource(
    when (this) {
        AccountRole.Customer -> Res.string.login_role_customer
        AccountRole.Restaurant -> Res.string.login_role_restaurant
    },
)

@Composable
internal fun AccountRole.hint(): String = stringResource(
    when (this) {
        AccountRole.Customer -> Res.string.login_role_customer_hint
        AccountRole.Restaurant -> Res.string.login_role_restaurant_hint
    },
)
