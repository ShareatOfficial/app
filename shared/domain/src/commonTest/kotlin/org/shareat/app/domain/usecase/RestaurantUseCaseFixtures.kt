package org.shareat.app.domain.usecase

import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.IsoTimestamp
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuDish
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.MenuPublicationState
import org.shareat.app.domain.model.Money
import org.shareat.app.domain.model.PostalAddress
import org.shareat.app.domain.model.Rating
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantMenuDraft
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository

internal fun restaurantFixture(
    id: String = "restaurant-1",
    name: String = "Casa Naranja",
): Restaurant = Restaurant(
    id = RestaurantId(id),
    ownerAccountId = AccountId("owner-1"),
    name = name,
    heroImage = ImageRef(url = "https://images.example.com/restaurants/$id.jpg"),
    address = PostalAddress(
        streetLine = "Calle del Olmo, 18",
        locality = "Madrid",
        postalCode = "28015",
    ),
    openingHours = WeeklyOpeningHours(emptyList()),
    publicationState = RestaurantPublicationState.Published,
)

internal fun dishFixture(
    id: String,
    restaurantId: RestaurantId,
    name: String,
    isEnabled: Boolean = true,
): Dish = Dish(
    id = DishId(id),
    restaurantId = restaurantId,
    name = name,
    isEnabled = isEnabled,
)

internal fun menuFixture(
    id: String,
    restaurantId: RestaurantId,
    name: String = "Carta",
    publicationState: MenuPublicationState = MenuPublicationState.Published,
): Menu = Menu(
    id = MenuId(id),
    restaurantId = restaurantId,
    name = name,
    publicationState = publicationState,
)

internal fun menuDishFixture(
    dish: Dish,
    position: Int,
    isEnabled: Boolean = true,
): MenuDish = MenuDish(
    dish = dish,
    price = Money(1_800),
    position = position,
    isEnabled = isEnabled,
)

private var reviewSequence = 0

internal fun reviewFixture(
    comment: String?,
    rating: Int,
    createdAt: String,
): Review = Review(
    id = ReviewId("review-${reviewSequence++}"),
    authorAccountId = AccountId("author-1"),
    target = ReviewTarget.Dish(DishId("unused")),
    rating = Rating(rating),
    comment = comment,
    visibility = ReviewVisibility.Public,
    moderationStatus = ReviewModerationStatus.Visible,
    createdAt = IsoTimestamp(createdAt),
    updatedAt = IsoTimestamp(createdAt),
)

internal class FakeRestaurantRepository(
    private val restaurants: () -> RepositoryResult<List<Restaurant>>,
) : RestaurantRepository {
    override suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>> = restaurants()

    override suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant> =
        when (val result = restaurants()) {
            is RepositoryResult.Success -> result.value.firstOrNull { it.id == id }
                ?.let { RepositoryResult.Success(it) }
                ?: RepositoryResult.Failure(RepositoryError.NotFound("Restaurant", id.value))

            is RepositoryResult.Failure -> result
        }

    override suspend fun getRestaurantForOwner(accountId: AccountId) = unavailable<Restaurant>()
    override suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ) = unavailable<Restaurant>()

    override suspend fun updateRestaurant(restaurant: Restaurant) = unavailable<Restaurant>()
}

internal class FakeDishRepository(
    private val dishesByRestaurant: Map<RestaurantId, List<Dish>>,
) : DishRepository {
    override suspend fun getDish(id: DishId) = unavailable<Dish>()
    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> =
        RepositoryResult.Success(dishesByRestaurant[restaurantId].orEmpty())

    override suspend fun saveDish(draft: DishDraft) = unavailable<Dish>()
    override suspend fun archiveDish(id: DishId) = unavailable<Unit>()
    override suspend fun deleteDish(id: DishId) = unavailable<Unit>()
}

internal class FakeMenuRepository(
    private val menusByRestaurant: Map<RestaurantId, List<Menu>> = emptyMap(),
    private val dishesByMenu: Map<MenuId, List<MenuDish>> = emptyMap(),
) : MenuRepository {
    override suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>> =
        RepositoryResult.Success(menusByRestaurant[restaurantId].orEmpty())

    override suspend fun getPublishedMenu(restaurantId: RestaurantId): RepositoryResult<MenuDetails> {
        val menu = menusByRestaurant[restaurantId].orEmpty()
            .firstOrNull { it.publicationState == MenuPublicationState.Published }
            ?: return RepositoryResult.Failure(RepositoryError.NotFound("PublishedMenu", restaurantId.value))
        // Mirrors the real contract: a public read only ever sees sellable items.
        val items = dishesByMenu[menu.id].orEmpty().filter { it.isEnabled && it.dish.isEnabled }
        return RepositoryResult.Success(MenuDetails(menu = menu, items = items))
    }

    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> {
        val menu = menusByRestaurant.values.flatten().firstOrNull { it.id == id }
            ?: return RepositoryResult.Failure(RepositoryError.NotFound("Menu", id.value))
        return RepositoryResult.Success(MenuDetails(menu = menu, items = dishesByMenu[id].orEmpty()))
    }

    override suspend fun saveMenu(draft: RestaurantMenuDraft) = unavailable<MenuDetails>()
    override suspend fun deleteMenu(id: MenuId) = unavailable<Unit>()
}

internal class FakeReviewRepository(
    private val reviewsByDish: Map<DishId, List<Review>> = emptyMap(),
    private val ratingSummaryResult: () -> RepositoryResult<RatingSummary> = {
        RepositoryResult.Success(RatingSummary(averageTenths = 48, ratingCount = 2))
    },
) : ReviewRepository {
    var dishReviewRequests = 0
        private set

    override suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>> =
        when (target) {
            is ReviewTarget.Dish -> RepositoryResult.Success(reviewsByDish[target.dishId].orEmpty())
            is ReviewTarget.Restaurant -> RepositoryResult.Success(emptyList())
        }

    override suspend fun getPublicDishReviews(
        dishIds: Set<DishId>,
    ): RepositoryResult<Map<DishId, List<Review>>> {
        dishReviewRequests++
        return RepositoryResult.Success(
            dishIds.associateWith { reviewsByDish[it].orEmpty() }
                .filterValues(List<Review>::isNotEmpty),
        )
    }

    override suspend fun getReviewsByAuthor(accountId: AccountId) = unavailable<List<Review>>()
    override suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary> =
        ratingSummaryResult()

    override suspend fun saveReview(draft: ReviewDraft) = unavailable<Review>()
    override suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId) = unavailable<Unit>()
}

internal fun <T> unavailable(): RepositoryResult<T> = RepositoryResult.Failure(RepositoryError.Unavailable())
