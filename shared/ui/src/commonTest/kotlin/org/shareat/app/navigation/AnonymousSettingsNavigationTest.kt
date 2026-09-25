package org.shareat.app.navigation

import androidx.compose.runtime.mutableStateOf
import androidx.navigation3.runtime.NavBackStack
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.shareat.app.auth.SessionCoordinator
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.navigation.login.LoginNavigationImpl
import org.shareat.app.navigation.profile.SettingsNavigationImpl
import org.shareat.feature.home.ui.navigation.HomeKey
import org.shareat.feature.login.ui.LoginKey
import org.shareat.feature.profile.ui.settings.SettingsKey
import org.shareat.feature.profile.ui.terms.TermsAndConditionsKey
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AnonymousSettingsNavigationTest {
    @Test
    fun guestCanOpenSettingsAndTermsWithoutLoginGate() = runTest {
        val fixture = navigationFixture()

        fixture.navigator.navigate(SettingsKey)
        fixture.navigator.navigate(TermsAndConditionsKey)

        assertEquals(SettingsKey, fixture.state.topLevelRoute)
        assertEquals(
            listOf(SettingsKey, TermsAndConditionsKey),
            fixture.state.backStacks.getValue(SettingsKey).toList(),
        )
    }

    @Test
    fun settingsLoginCanReturnToSettingsOrBeDismissed() = runTest {
        val fixture = navigationFixture()
        fixture.navigator.navigate(SettingsKey)
        val settingsNavigation = SettingsNavigationImpl(fixture.navigator)

        settingsNavigation.openLogin()

        val settingsStack = fixture.state.backStacks.getValue(SettingsKey)
        val login = assertIs<LoginKey>(settingsStack.last())
        assertEquals(SettingsKey, login.redirectRoute)

        LoginNavigationImpl(fixture.navigator).goBack()
        assertEquals(listOf(SettingsKey), settingsStack.toList())

        settingsNavigation.openLogin()
        fixture.navigator.completeLogin()
        assertEquals(SettingsKey, fixture.state.topLevelRoute)
        assertEquals(listOf(SettingsKey), settingsStack.toList())
    }

    private fun navigationFixture(): NavigationFixture {
        val state = NavigationState(
            startRoute = HomeKey,
            topLevelRoute = mutableStateOf(HomeKey),
            backStacks = mapOf(
                HomeKey to NavBackStack(HomeKey),
                SettingsKey to NavBackStack(SettingsKey),
            ),
        )
        val sessions = SessionCoordinator(GuestAuthRepository())
        return NavigationFixture(state, Navigator(state, sessions))
    }
}

private data class NavigationFixture(
    val state: NavigationState,
    val navigator: Navigator,
)

private class GuestAuthRepository : AuthRepository {
    override fun observeSession() = flowOf(AuthSessionState.Unauthenticated)

    override suspend fun currentSession(): RepositoryResult<AuthSession?> =
        RepositoryResult.Success(null)

    override suspend fun register(
        credentials: RegistrationCredentials,
    ): RepositoryResult<AuthSession> = RepositoryResult.Failure(RepositoryError.Unauthenticated)

    override suspend fun signIn(
        email: EmailAddress,
        password: String,
    ): RepositoryResult<AuthSession> = RepositoryResult.Failure(RepositoryError.Unauthenticated)

    override suspend fun signOut(): RepositoryResult<Unit> = RepositoryResult.Success(Unit)

    override suspend fun requestPasswordReset(
        email: EmailAddress,
    ): RepositoryResult<Unit> = RepositoryResult.Failure(RepositoryError.Unauthenticated)

    override suspend fun updatePassword(
        password: String,
    ): RepositoryResult<Unit> = RepositoryResult.Failure(RepositoryError.Unauthenticated)
}
