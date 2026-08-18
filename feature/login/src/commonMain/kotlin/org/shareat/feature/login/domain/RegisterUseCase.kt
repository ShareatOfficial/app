package org.shareat.feature.login.domain

import org.shareat.app.domain.model.AuthSession

fun interface RegisterUseCase {
    operator fun invoke(): AuthSession
}