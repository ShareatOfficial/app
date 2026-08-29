package org.shareat.feature.menu.domain

import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDraft
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

data class CreateDraftMenuParams(
    val name: String,
    val description: String? = null,
) {
    init {
        require(name.isNotBlank())
        require(description == null || description.isNotBlank())
    }
}

data class OwnedRestaurantMenu(
    val restaurant: Restaurant,
    val menu: Menu?,
)

fun interface LoadOwnedRestaurantMenuUseCase {
    suspend operator fun invoke(): RepositoryResult<OwnedRestaurantMenu>
}

class LoadOwnedRestaurantMenuUseCaseImpl(
    private val auth: AuthRepository,
    private val restaurants: RestaurantRepository,
    private val menus: MenuRepository,
) : LoadOwnedRestaurantMenuUseCase {
    override suspend fun invoke(): RepositoryResult<OwnedRestaurantMenu> {
        val session = when (val result = auth.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurants.getRestaurantForOwner(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return when (val result = menus.getMenus(restaurant.id)) {
            is RepositoryResult.Success -> RepositoryResult.Success(
                OwnedRestaurantMenu(restaurant, result.value.singleOrNull()),
            )
            is RepositoryResult.Failure -> result
        }
    }
}

fun interface CreateDraftMenuUseCase {
    suspend operator fun invoke(params: CreateDraftMenuParams): RepositoryResult<Menu>
}

class CreateDraftMenuUseCaseImpl(
    private val auth: AuthRepository,
    private val restaurants: RestaurantRepository,
    private val menus: MenuRepository,
) : CreateDraftMenuUseCase {
    override suspend fun invoke(params: CreateDraftMenuParams): RepositoryResult<Menu> {
        val session = when (val result = auth.currentSession()) {
            is RepositoryResult.Success -> result.value
                ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurants.getRestaurantForOwner(session.accountId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        return menus.createDraftMenu(
            restaurant.id,
            MenuDraft(name = params.name, description = params.description),
        )
    }
}

fun RepositoryError.toMenuMessage(): String = when (this) {
    RepositoryError.Offline -> "Sin conexión. Comprueba tu red e inténtalo de nuevo."
    RepositoryError.Unauthenticated, RepositoryError.InvalidCredentials -> "Tu sesión ha caducado. Vuelve a iniciar sesión."
    RepositoryError.Forbidden -> "Esta cuenta no puede gestionar este menú."
    is RepositoryError.AlreadyExists -> "Tu restaurante ya tiene una carta."
    is RepositoryError.Conflict -> reason
    is RepositoryError.Validation -> reason
    is RepositoryError.NotFound -> "No se ha encontrado el restaurante."
    is RepositoryError.Unavailable -> "El servicio no está disponible temporalmente."
}
