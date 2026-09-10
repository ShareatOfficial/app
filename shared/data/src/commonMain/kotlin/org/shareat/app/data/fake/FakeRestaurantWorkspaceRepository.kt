package org.shareat.app.data.fake

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EuAllergen
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuItem
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantWorkspaceRepository

/** Deterministic in-memory counterpart of the transactional Supabase starter-workspace RPC. */
class FakeRestaurantWorkspaceRepository(
    private val data: FakeShareatData,
    private val scenario: FakeDataScenario = FakeDataScenario.Populated,
) : RestaurantWorkspaceRepository {
    override suspend fun ensureRestaurantWorkspace(
        ownerAccountId: AccountId,
    ): RepositoryResult<Restaurant> = when (scenario) {
        FakeDataScenario.Offline -> RepositoryResult.Failure(RepositoryError.Offline)
        FakeDataScenario.Unavailable -> RepositoryResult.Failure(RepositoryError.Unavailable())
        FakeDataScenario.Populated,
        FakeDataScenario.Empty,
        -> ensure(ownerAccountId)
    }

    private fun ensure(ownerAccountId: AccountId): RepositoryResult<Restaurant> {
        val account = data.accounts.firstOrNull { it.id == ownerAccountId }
            ?: return RepositoryResult.Failure(RepositoryError.Unauthenticated)
        if (account.role != AccountRole.Restaurant || account.status != AccountStatus.Active) {
            return RepositoryResult.Failure(RepositoryError.Forbidden)
        }

        // This is deliberately before every mutation. A repeat must never seed missing records
        // into a pre-existing workspace either.
        data.restaurants.firstOrNull { it.ownerAccountId == ownerAccountId }?.let {
            return RepositoryResult.Success(it)
        }

        val restaurantId = RestaurantId("restaurant-${ownerAccountId.value}")
        val menuId = MenuId("menu-${restaurantId.value}")
        val bravasId = DishId("dish-${ownerAccountId.value}-patatas-bravas")
        val croquettesId = DishId("dish-${ownerAccountId.value}-croquetas")
        val restaurant = Restaurant(
            id = restaurantId,
            ownerAccountId = ownerAccountId,
            name = DEFAULT_RESTAURANT_NAME,
            description = DEFAULT_RESTAURANT_DESCRIPTION,
            address = PostalAddress(
                streetLine = "Calle de las Tapas, 1",
                locality = "Madrid",
                postalCode = "28001",
                region = "Comunidad de Madrid",
            ),
            openingHours = WeeklyOpeningHours(emptyList()),
            publicationState = RestaurantPublicationState.Draft,
        )
        val menu = Menu(
            id = menuId,
            restaurantId = restaurantId,
            name = "Carta",
            description = "Tu carta inicial. Edita o añade platos cuando quieras.",
            publicationState = MenuPublicationState.Draft,
        )
        val dishes = listOf(
            Dish(
                id = bravasId,
                restaurantId = restaurantId,
                name = "Patatas bravas",
                description = "Patatas crujientes con salsa brava casera.",
                allergenDeclaration = AllergenDeclaration(setOf(EuAllergen.Eggs)),
                isEnabled = true,
            ),
            Dish(
                id = croquettesId,
                restaurantId = restaurantId,
                name = "Croquetas de jamón",
                description = "Croquetas cremosas de jamón ibérico.",
                allergenDeclaration = AllergenDeclaration(
                    setOf(EuAllergen.CerealsContainingGluten, EuAllergen.Eggs, EuAllergen.Milk),
                ),
                isEnabled = true,
            ),
        )
        val items = listOf(
            MenuItem(menuId, bravasId, Money(650), 0, true, DishCategory.SmallBites),
            MenuItem(menuId, croquettesId, Money(850), 1, true, DishCategory.Starters),
        )

        // All values have been constructed and validated before the shared mutable state changes.
        data.restaurants += restaurant
        data.menus += menu
        data.dishes += dishes
        data.menuItems += items
        return RepositoryResult.Success(restaurant)
    }

    private companion object {
        const val DEFAULT_RESTAURANT_NAME = "Rincón de Paco"
        const val DEFAULT_RESTAURANT_DESCRIPTION = "Lugar típico para tomarse unas tapas."
    }
}
