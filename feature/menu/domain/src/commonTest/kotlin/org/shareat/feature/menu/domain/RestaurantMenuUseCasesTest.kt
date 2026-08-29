package org.shareat.feature.menu.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDraft
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.model.RegistrationCredentials
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class RestaurantMenuUseCasesTest {
    @Test
    fun createsDraftForAuthenticatedOwner() = runTest {
        val menus = MenuFake()
        val useCase = CreateDraftMenuUseCaseImpl(AuthFake(), RestaurantFake(), menus)

        val result = useCase(CreateDraftMenuParams("Carta principal", "De temporada"))

        assertEquals("Carta principal", assertIs<RepositoryResult.Success<Menu>>(result).value.name)
        assertEquals(MenuPublicationState.Draft, menus.created?.publicationState)
    }

    @Test
    fun unauthenticatedOwnerCannotCreateMenu() = runTest {
        val useCase = CreateDraftMenuUseCaseImpl(AuthFake(unauthenticated = true), RestaurantFake(), MenuFake())

        assertIs<RepositoryResult.Failure>(useCase(CreateDraftMenuParams("Carta")))
    }
}

private class AuthFake(private val unauthenticated: Boolean = false) : AuthRepository {
    override fun observeSession(): Flow<AuthSessionState> = throw UnsupportedOperationException()
    override suspend fun currentSession() = if (unauthenticated) {
        RepositoryResult.Failure(RepositoryError.Unauthenticated)
    } else RepositoryResult.Success(AuthSession(AccountId("owner"), EmailAddress("owner@example.test")))
    override suspend fun register(credentials: RegistrationCredentials) = throw UnsupportedOperationException()
    override suspend fun signIn(email: EmailAddress, password: String) = throw UnsupportedOperationException()
    override suspend fun signOut() = throw UnsupportedOperationException()
    override suspend fun requestPasswordReset(email: EmailAddress) = throw UnsupportedOperationException()
    override suspend fun updatePassword(password: String) = throw UnsupportedOperationException()
}

private class RestaurantFake : RestaurantRepository {
    override suspend fun getPublishedRestaurants() = throw UnsupportedOperationException()
    override suspend fun getRestaurant(id: RestaurantId) = throw UnsupportedOperationException()
    override suspend fun getRestaurantForOwner(accountId: AccountId) = RepositoryResult.Success(
        Restaurant(
            id = RestaurantId("restaurant"),
            ownerAccountId = accountId,
            name = "Restaurante",
            address = PostalAddress("Calle 1", "Madrid", "28001"),
            openingHours = WeeklyOpeningHours(emptyList()),
            publicationState = RestaurantPublicationState.Draft,
        ),
    )
    override suspend fun createRestaurantProfile(ownerAccountId: AccountId, draft: RestaurantProfileDraft) = throw UnsupportedOperationException()
    override suspend fun updateRestaurant(restaurant: Restaurant) = throw UnsupportedOperationException()
}

private class MenuFake : MenuRepository {
    var created: Menu? = null
    override suspend fun getMenus(restaurantId: RestaurantId) = RepositoryResult.Success(emptyList<Menu>())
    override suspend fun getPublishedMenu(restaurantId: RestaurantId): RepositoryResult<MenuDetails> = throw UnsupportedOperationException()
    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> = throw UnsupportedOperationException()
    override suspend fun createDraftMenu(restaurantId: RestaurantId, draft: MenuDraft) = RepositoryResult.Success(
        Menu(MenuId("menu"), restaurantId, draft.name, draft.description, MenuPublicationState.Draft)
            .also { created = it },
    )
}
