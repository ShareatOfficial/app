package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class ReplaceOwnerDishImageUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
    private val dishRepository: DishRepository,
    private val imageRepository: ImageRepository,
) : ReplaceOwnerDishImageUseCase {
    override suspend fun invoke(dishId: DishId, upload: ImageUpload): RepositoryResult<ImageRef> {
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurantRepository.getRestaurantForOwner(owner.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (restaurant.ownerAccountId != owner.id) return forbidden()
        val dish = when (val result = dishRepository.getDish(dishId)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (dish.restaurantId != restaurant.id) return forbidden()
        return imageRepository.replaceImage(ImageTarget.DishImage(dishId), upload)
    }
}
