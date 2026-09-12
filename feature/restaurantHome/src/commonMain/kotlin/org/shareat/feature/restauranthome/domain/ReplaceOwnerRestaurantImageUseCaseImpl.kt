package org.shareat.feature.restauranthome.domain

import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository

class ReplaceOwnerRestaurantImageUseCaseImpl(
    private val authorizer: RestaurantOwnerAuthorizer,
    private val restaurantRepository: RestaurantRepository,
    private val imageRepository: ImageRepository,
) : ReplaceOwnerRestaurantImageUseCase {
    override suspend fun invoke(upload: ImageUpload): RepositoryResult<ImageRef> {
        val owner = when (val result = authorizer.authorize()) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        val restaurant = when (val result = restaurantRepository.getRestaurantForOwner(owner.id)) {
            is RepositoryResult.Success -> result.value
            is RepositoryResult.Failure -> return result
        }
        if (restaurant.ownerAccountId != owner.id) return forbidden()
        return imageRepository.replaceImage(ImageTarget.RestaurantHero(restaurant.id), upload)
    }
}
