package org.shareat.app.data.fake

import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.RepositoryResult

class FakeImageRepository : ImageRepository {
    override suspend fun replaceImage(
        target: ImageTarget,
        upload: ImageUpload,
    ): RepositoryResult<ImageRef> = RepositoryResult.Success(
        ImageRef(
            url = "https://images.example.com/fake/${target.pathSegment()}",
            alternativeText = upload.alternativeText,
        ),
    )

    override suspend fun deleteImage(target: ImageTarget): RepositoryResult<Unit> =
        RepositoryResult.Success(Unit)
}

private fun ImageTarget.pathSegment(): String = when (this) {
    is ImageTarget.CustomerAvatar -> "avatars/${accountId.value}.jpg"
    is ImageTarget.RestaurantHero -> "restaurants/${restaurantId.value}.jpg"
    is ImageTarget.DishImage -> "dishes/${dishId.value}.jpg"
}
