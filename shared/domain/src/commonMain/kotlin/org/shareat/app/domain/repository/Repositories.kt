package org.shareat.app.domain.repository

import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.CustomerProfile
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.Menu
import org.shareat.app.domain.model.MenuDraft
import org.shareat.app.domain.model.MenuDetails
import org.shareat.app.domain.model.MenuId
import org.shareat.app.domain.model.RatingSummary
import org.shareat.app.domain.model.Restaurant
import org.shareat.app.domain.model.RestaurantId
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewDraft
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewTarget

interface AccountRepository {
    suspend fun getAccount(id: AccountId): RepositoryResult<Account>
    suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<CustomerProfile>
    suspend fun updateCustomerProfile(profile: CustomerProfile): RepositoryResult<CustomerProfile>
}

interface RestaurantRepository {
    suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>>
    suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant>
    suspend fun getRestaurantForOwner(accountId: AccountId): RepositoryResult<Restaurant>
    suspend fun createRestaurantProfile(
        ownerAccountId: AccountId,
        draft: RestaurantProfileDraft,
    ): RepositoryResult<Restaurant>
    suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant>
}

interface MenuRepository {
    suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>>
    suspend fun getPublishedMenu(restaurantId: RestaurantId): RepositoryResult<MenuDetails>
    suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails>
    suspend fun createDraftMenu(
        restaurantId: RestaurantId,
        draft: MenuDraft,
    ): RepositoryResult<Menu>
}

interface DishRepository {
    suspend fun getDish(id: DishId): RepositoryResult<Dish>
    suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>>
}

interface ReviewRepository {
    suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>>
    suspend fun getReviewsByAuthor(accountId: AccountId): RepositoryResult<List<Review>>
    suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary>

    /** Creates or updates the unique review identified by author and target. */
    suspend fun saveReview(draft: ReviewDraft): RepositoryResult<Review>
    suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId): RepositoryResult<Unit>
}
