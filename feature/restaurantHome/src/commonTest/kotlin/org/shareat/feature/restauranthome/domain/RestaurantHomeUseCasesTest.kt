package org.shareat.feature.restauranthome.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.shareat.app.domain.model.Account
import org.shareat.app.domain.model.AccountId
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.AllergenDeclaration
import org.shareat.app.domain.model.AuthSession
import org.shareat.app.domain.model.AuthSessionState
import org.shareat.app.domain.model.Currency
import org.shareat.app.domain.model.Dish
import org.shareat.app.domain.model.DishCategory
import org.shareat.app.domain.model.DishDraft
import org.shareat.app.domain.model.DishId
import org.shareat.app.domain.model.EmailAddress
import org.shareat.app.domain.model.ImageRef
import org.shareat.app.domain.model.ImageTarget
import org.shareat.app.domain.model.ImageUpload
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
import org.shareat.app.domain.model.RestaurantPublicationState
import org.shareat.app.domain.model.RestaurantProfileDraft
import org.shareat.app.domain.model.Review
import org.shareat.app.domain.model.ReviewId
import org.shareat.app.domain.model.ReviewModerationStatus
import org.shareat.app.domain.model.ReviewTarget
import org.shareat.app.domain.model.ReviewVisibility
import org.shareat.app.domain.model.WeeklyOpeningHours
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.AuthRepository
import org.shareat.app.domain.repository.DishRepository
import org.shareat.app.domain.repository.ImageRepository
import org.shareat.app.domain.repository.MenuRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.RestaurantRepository
import org.shareat.app.domain.repository.ReviewRepository
import org.shareat.feature.restauranthome.domain.model.OwnerDishDraft
import org.shareat.feature.restauranthome.domain.model.OwnerDishCreateDraft
import org.shareat.feature.restauranthome.domain.model.OwnerRestaurantInfoDraft
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RestaurantHomeUseCasesTest {
    @Test
    fun loadsPrivateOwnerMenuWithDisabledDishesAndRatings() = runTest {
        val fixture = Fixture()

        val result = fixture.loadHome()()

        val home = assertIs<RepositoryResult.Success<*>>(result).value as org.shareat.feature.restauranthome.domain.model.OwnerRestaurantHome
        assertEquals(RatingSummary(averageTenths = 45, ratingCount = 2), home.restaurantRatingSummary)
        assertEquals(2, home.menu?.dishes?.size)
        assertFalse(home.menu!!.dishes[1].menuDish.dish.isEnabled)
        assertEquals(RatingSummary(averageTenths = 40, ratingCount = 1), home.menu.dishes[0].ratingSummary)
        assertEquals(setOf(DishCategory.MainCourses, DishCategory.Desserts), home.dishCategories)
    }

    @Test
    fun loadRequiresAnAuthenticatedActiveRestaurantOwner() = runTest {
        val fixture = Fixture().apply { auth.session = null }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Unauthenticated),
            fixture.loadHome()(),
        )

        fixture.auth.session = AuthSession(fixture.owner.id, fixture.owner.loginEmail)
        fixture.accounts.account = fixture.owner.copy(role = AccountRole.Customer)
        assertEquals(RepositoryResult.Failure(RepositoryError.Forbidden), fixture.loadHome()())
    }

    @Test
    fun loadReturnsRepositoryErrorsInsteadOfMaskingOwnerFeedback() = runTest {
        val fixture = Fixture().apply {
            reviews.restaurantRatingResult = RepositoryResult.Failure(RepositoryError.Offline)
        }

        assertEquals(RepositoryResult.Failure(RepositoryError.Offline), fixture.loadHome()())
    }

    @Test
    fun updatesOnlyEditableRestaurantInfo() = runTest {
        val fixture = Fixture()
        val infoResult = fixture.updateInfo()(
            OwnerRestaurantInfoDraft(
                name = "  Casa Actualizada ",
                description = "  Nueva descripción ",
                address = fixture.restaurant.address.copy(streetLine = "Calle Nueva 3"),
            ),
        )

        val updated = assertIs<RepositoryResult.Success<Restaurant>>(infoResult).value
        assertEquals("Casa Actualizada", updated.name)
        assertEquals("Nueva descripción", updated.description)
        assertEquals("Calle Nueva 3", updated.address.streetLine)
        assertEquals(fixture.restaurant.heroImage, updated.heroImage)
        assertEquals(RestaurantPublicationState.Draft, updated.publicationState)
    }

    @Test
    fun publishesMenuBeforeRestaurantAndPreservesEveryMenuItemField() = runTest {
        val fixture = Fixture()

        val published = assertIs<RepositoryResult.Success<Restaurant>>(
            fixture.updatePublication()(RestaurantPublicationState.Published),
        ).value

        assertEquals(RestaurantPublicationState.Published, published.publicationState)
        assertEquals(RestaurantPublicationState.Published, fixture.restaurants.restaurant.publicationState)
        assertEquals(MenuPublicationState.Published, fixture.menu.currentDetails.menu.publicationState)
        assertEquals(listOf("menu:Published", "restaurant:Published"), fixture.operations)
        val savedItems = fixture.menu.savedDrafts.single().items
        assertEquals(2, savedItems.size)
        assertEquals(Money(1200), savedItems[0].price)
        assertEquals(0, savedItems[0].position)
        assertTrue(savedItems[0].isEnabled)
        assertEquals(DishCategory.MainCourses, savedItems[0].category)
        assertEquals(Money(850), savedItems[1].price)
        assertEquals(1, savedItems[1].position)
        assertFalse(savedItems[1].isEnabled)
        assertEquals(DishCategory.Desserts, savedItems[1].category)
    }

    @Test
    fun unpublishesRestaurantBeforeMenuAndMapsDisabledState() = runTest {
        val fixture = Fixture().apply {
            restaurants.restaurant = restaurant.copy(publicationState = RestaurantPublicationState.Published)
            menu.setPublicationState(MenuPublicationState.Published)
        }

        assertIs<RepositoryResult.Success<Restaurant>>(
            fixture.updatePublication()(RestaurantPublicationState.Draft),
        )
        assertEquals(RestaurantPublicationState.Draft, fixture.restaurants.restaurant.publicationState)
        assertEquals(MenuPublicationState.Unpublished, fixture.menu.currentDetails.menu.publicationState)
        assertEquals(listOf("restaurant:Draft", "menu:Unpublished"), fixture.operations)

        fixture.operations.clear()
        assertIs<RepositoryResult.Success<Restaurant>>(
            fixture.updatePublication()(RestaurantPublicationState.Disabled),
        )
        assertEquals(MenuPublicationState.Disabled, fixture.menu.currentDetails.menu.publicationState)
        assertEquals(listOf("restaurant:Disabled", "menu:Disabled"), fixture.operations)
    }

    @Test
    fun publishFailureRollsMenuBackAndReturnsTheRestaurantError() = runTest {
        val fixture = Fixture().apply { restaurants.failedUpdateCalls += 1 }

        val result = fixture.updatePublication()(RestaurantPublicationState.Published)

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Unavailable("Restaurant update failed")),
            result,
        )
        assertEquals(RestaurantPublicationState.Draft, fixture.restaurants.restaurant.publicationState)
        assertEquals(MenuPublicationState.Unpublished, fixture.menu.currentDetails.menu.publicationState)
        assertEquals(
            listOf("menu:Published", "restaurant:Published", "menu:Unpublished"),
            fixture.operations,
        )
    }

    @Test
    fun unpublishReturnsConflictWhenRestaurantRollbackFails() = runTest {
        val fixture = Fixture().apply {
            restaurants.restaurant = restaurant.copy(publicationState = RestaurantPublicationState.Published)
            menu.setPublicationState(MenuPublicationState.Published)
            menu.failedSaveCalls += 1
            restaurants.failedUpdateCalls += 2
        }

        val result = fixture.updatePublication()(RestaurantPublicationState.Draft)

        assertEquals(
            RepositoryResult.Failure(
                RepositoryError.Conflict(
                    "Restaurant and menu publication could not be kept consistent because rollback failed",
                ),
            ),
            result,
        )
        assertEquals(RestaurantPublicationState.Draft, fixture.restaurants.restaurant.publicationState)
        assertEquals(MenuPublicationState.Published, fixture.menu.currentDetails.menu.publicationState)
        assertEquals(2, fixture.restaurants.updateCalls)
    }

    @Test
    fun dishEditAfterPublishingKeepsTheMenuPublished() = runTest {
        val fixture = Fixture()
        assertIs<RepositoryResult.Success<Restaurant>>(
            fixture.updatePublication()(RestaurantPublicationState.Published),
        )

        val editResult = fixture.updateDish()(
            fixture.firstDish.id,
            OwnerDishDraft("Arroz nuevo", null, null, true, Money(1450)),
        )

        assertIs<RepositoryResult.Success<*>>(editResult)
        assertEquals(MenuPublicationState.Published, fixture.menu.savedDraft?.publicationState)
        assertEquals(MenuPublicationState.Published, fixture.menu.currentDetails.menu.publicationState)
    }

    @Test
    fun rejectsInvalidRestaurantAndDishDraftsWithoutWrites() = runTest {
        val fixture = Fixture()

        val restaurantResult = fixture.updateInfo()(
            OwnerRestaurantInfoDraft("  ", null, fixture.restaurant.address),
        )
        val dishResult = fixture.updateDish()(
            fixture.firstDish.id,
            OwnerDishDraft("", null, null, true, Money(100)),
        )

        assertIs<RepositoryResult.Failure>(restaurantResult)
        assertIs<RepositoryResult.Failure>(dishResult)
        assertEquals(0, fixture.restaurants.updateCalls)
        assertNull(fixture.dishes.savedDraft)
        assertNull(fixture.menu.savedDraft)
    }

    @Test
    fun updatesDishPriceAndAvailabilityWhilePreservingOtherMenuItemsAndCategories() = runTest {
        val fixture = Fixture()

        val result = fixture.updateDish()(
            fixture.firstDish.id,
            OwnerDishDraft(
                name = "Arroz de temporada",
                description = "Con verduras",
                allergenDeclaration = AllergenDeclaration(emptySet()),
                isEnabled = false,
                price = Money(1795),
            ),
        )

        val update = assertIs<RepositoryResult.Success<*>>(result).value as org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate
        assertEquals("Arroz de temporada", update.dish.name)
        assertFalse(update.dish.isEnabled)
        val savedMenu = requireNotNull(fixture.menu.savedDraft)
        assertEquals(2, savedMenu.items.size)
        assertEquals(Money(1795), savedMenu.items[0].price)
        assertFalse(savedMenu.items[0].isEnabled)
        assertEquals(DishCategory.MainCourses, savedMenu.items[0].category)
        assertEquals(fixture.secondDish.id, savedMenu.items[1].dishId)
        assertEquals(Money(850), savedMenu.items[1].price)
        assertEquals(DishCategory.Desserts, savedMenu.items[1].category)
    }

    @Test
    fun dishEditRollsBackDishAndReturnsOriginalMenuFailure() = runTest {
        val fixture = Fixture().apply {
            menu.saveResult = RepositoryResult.Failure(RepositoryError.Offline)
        }
        val draft = OwnerDishDraft("Arroz", null, null, true, Money(1000))

        assertEquals(RepositoryResult.Failure(RepositoryError.Offline), fixture.updateDish()(fixture.firstDish.id, draft))
        assertEquals(fixture.firstDish, fixture.dishes.dishes[fixture.firstDish.id])
        assertEquals(2, fixture.dishes.saveCalls)
    }

    @Test
    fun dishEditReturnsConflictWhenDishRollbackFails() = runTest {
        val fixture = Fixture().apply {
            menu.saveResult = RepositoryResult.Failure(RepositoryError.Offline)
            dishes.failedSaveCalls += 2
        }

        val result = fixture.updateDish()(
            fixture.firstDish.id,
            OwnerDishDraft("Arroz nuevo", null, null, false, Money(1000)),
        )

        assertEquals(
            RepositoryResult.Failure(
                RepositoryError.Conflict(
                    "Menu update failed and the original dish could not be restored",
                ),
            ),
            result,
        )
        assertEquals(2, fixture.dishes.saveCalls)
        assertEquals("Arroz nuevo", fixture.dishes.dishes[fixture.firstDish.id]?.name)
    }

    @Test
    fun dishEditRejectsDishesFromAnotherRestaurant() = runTest {
        val fixture = Fixture()
        val draft = OwnerDishDraft("Arroz", null, null, true, Money(1000))

        val foreign = Dish(DishId("foreign"), RestaurantId("other"), "Ajeno", isEnabled = true)
        fixture.dishes.dishes[foreign.id] = foreign
        assertEquals(RepositoryResult.Failure(RepositoryError.Forbidden), fixture.updateDish()(foreign.id, draft))
    }

    @Test
    fun createsDishAndAppendsItToTheSingleMenu() = runTest {
        val fixture = Fixture()

        val result = fixture.createDish()(
            OwnerDishCreateDraft("Croquetas", "De jamón", null, true, Money(950)),
        )

        val update = assertIs<RepositoryResult.Success<*>>(result).value as org.shareat.feature.restauranthome.domain.model.OwnerDishUpdate
        assertEquals("Croquetas", update.dish.name)
        val savedMenu = requireNotNull(fixture.menu.savedDraft)
        assertEquals(3, savedMenu.items.size)
        assertEquals(update.dish.id, savedMenu.items.last().dishId)
        assertEquals(2, savedMenu.items.last().position)
        assertEquals(Money(950), savedMenu.items.last().price)
    }

    @Test
    fun removesCreatedDishWhenMenuSaveFails() = runTest {
        val fixture = Fixture().apply { menu.saveResult = RepositoryResult.Failure(RepositoryError.Offline) }

        assertEquals(
            RepositoryResult.Failure(RepositoryError.Offline),
            fixture.createDish()(OwnerDishCreateDraft("Croquetas", null, null, true, Money(950))),
        )
        assertEquals(2, fixture.dishes.dishes.size)
        assertEquals(1, fixture.dishes.deleteCalls)
    }

    @Test
    fun replacesImagesOnlyAfterVerifyingRestaurantOwnership() = runTest {
        val fixture = Fixture()
        val upload = ImageUpload(byteArrayOf(1), "image/jpeg", "Nueva imagen")

        assertEquals(
            ImageRef("https://images.test/restaurants/${fixture.restaurant.id.value}", "Nueva imagen"),
            assertIs<RepositoryResult.Success<ImageRef>>(fixture.replaceRestaurantImage()(upload)).value,
        )
        assertEquals(
            ImageRef("https://images.test/dishes/${fixture.firstDish.id.value}", "Nueva imagen"),
            assertIs<RepositoryResult.Success<ImageRef>>(
                fixture.replaceDishImage()(fixture.firstDish.id, upload),
            ).value,
        )
        assertEquals(
            listOf(
                ImageTarget.RestaurantHero(fixture.restaurant.id),
                ImageTarget.DishImage(fixture.firstDish.id),
            ),
            fixture.images.targets,
        )

        fixture.auth.session = null
        assertEquals(
            RepositoryResult.Failure(RepositoryError.Unauthenticated),
            fixture.replaceDishImage()(fixture.firstDish.id, upload),
        )
        assertEquals(2, fixture.images.targets.size)
    }
}

private class Fixture {
    val operations = mutableListOf<String>()
    val owner = Account(AccountId("owner"), EmailAddress("owner@example.com"), AccountRole.Restaurant, AccountStatus.Active)
    val restaurant = Restaurant(
        RestaurantId("restaurant"), owner.id, "Casa Original", "Original", ImageRef("https://old.test/hero"),
        address = PostalAddress("Calle Mayor 1", "Madrid", "28001"),
        openingHours = WeeklyOpeningHours(emptyList()), publicationState = RestaurantPublicationState.Draft,
    )
    val firstDish = Dish(DishId("dish-1"), restaurant.id, "Arroz", isEnabled = true)
    val secondDish = Dish(DishId("dish-2"), restaurant.id, "Tarta", isEnabled = false)
    val auth = TestAuthRepository(AuthSession(owner.id, owner.loginEmail))
    val accounts = TestAccountRepository(owner)
    val restaurants = TestRestaurantRepository(restaurant, operations)
    val dishes = TestDishRepository(linkedMapOf(firstDish.id to firstDish, secondDish.id to secondDish))
    val menu = TestMenuRepository(
        MenuDetails(
            Menu(MenuId("menu"), restaurant.id, "Carta", publicationState = MenuPublicationState.Unpublished),
            listOf(
                MenuDish(firstDish, Money(1200), 0, category = DishCategory.MainCourses),
                MenuDish(secondDish, Money(850), 1, isEnabled = false, category = DishCategory.Desserts),
            ),
        ),
        operations,
    )
    val reviews = TestReviewRepository(restaurant.id, firstDish.id)
    val images = TestImageRepository()

    private fun authorizer() = RestaurantOwnerAuthorizer(auth, accounts)
    fun loadHome() = LoadOwnerRestaurantHomeUseCaseImpl(authorizer(), restaurants, menu, reviews)
    fun updateInfo() = UpdateOwnerRestaurantInfoUseCaseImpl(authorizer(), restaurants)
    fun updatePublication() = UpdateRestaurantPublicationStateUseCaseImpl(authorizer(), restaurants, menu)
    fun updateDish() = UpdateOwnerDishUseCaseImpl(authorizer(), restaurants, dishes, menu)
    fun createDish() = CreateOwnerDishUseCaseImpl(authorizer(), restaurants, dishes, menu)
    fun replaceRestaurantImage() = ReplaceOwnerRestaurantImageUseCaseImpl(authorizer(), restaurants, images)
    fun replaceDishImage() = ReplaceOwnerDishImageUseCaseImpl(authorizer(), restaurants, dishes, images)
}

private class TestAuthRepository(var session: AuthSession?) : AuthRepository {
    override fun observeSession(): Flow<AuthSessionState> = emptyFlow()
    override suspend fun currentSession(): RepositoryResult<AuthSession?> = RepositoryResult.Success(session)
    override suspend fun register(credentials: org.shareat.app.domain.model.RegistrationCredentials): RepositoryResult<AuthSession> = failure()
    override suspend fun signIn(email: EmailAddress, password: String): RepositoryResult<AuthSession> = failure()
    override suspend fun signOut(): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
    override suspend fun requestPasswordReset(email: EmailAddress): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
    override suspend fun updatePassword(password: String): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
}

private class TestAccountRepository(var account: Account) : AccountRepository {
    override suspend fun getAccount(id: AccountId): RepositoryResult<Account> = RepositoryResult.Success(account)
    override suspend fun getCustomerProfile(accountId: AccountId): RepositoryResult<org.shareat.app.domain.model.CustomerProfile> = failure()
    override suspend fun updateCustomerProfile(profile: org.shareat.app.domain.model.CustomerProfile): RepositoryResult<org.shareat.app.domain.model.CustomerProfile> = failure()
}

private class TestRestaurantRepository(
    var restaurant: Restaurant,
    private val operations: MutableList<String>,
) : RestaurantRepository {
    var updateCalls = 0
    val failedUpdateCalls = mutableSetOf<Int>()
    override suspend fun getPublishedRestaurants(): RepositoryResult<List<Restaurant>> = RepositoryResult.Success(listOf(restaurant))
    override suspend fun getRestaurant(id: RestaurantId): RepositoryResult<Restaurant> = RepositoryResult.Success(restaurant)
    override suspend fun getRestaurantForOwner(accountId: AccountId): RepositoryResult<Restaurant> = RepositoryResult.Success(restaurant)
    override suspend fun createRestaurantProfile(ownerAccountId: AccountId, draft: RestaurantProfileDraft): RepositoryResult<Restaurant> = failure()
    override suspend fun updateRestaurant(restaurant: Restaurant): RepositoryResult<Restaurant> {
        updateCalls++
        operations += "restaurant:${restaurant.publicationState}"
        if (updateCalls in failedUpdateCalls) {
            return RepositoryResult.Failure(RepositoryError.Unavailable("Restaurant update failed"))
        }
        this.restaurant = restaurant
        return RepositoryResult.Success(restaurant)
    }
}

private class TestDishRepository(val dishes: MutableMap<DishId, Dish>) : DishRepository {
    var savedDraft: DishDraft? = null
    var saveCalls = 0
    var deleteCalls = 0
    val failedSaveCalls = mutableSetOf<Int>()
    override suspend fun getDish(id: DishId): RepositoryResult<Dish> = dishes[id]?.let {
        RepositoryResult.Success(it)
    } ?: RepositoryResult.Failure(RepositoryError.NotFound("Dish", id.value))
    override suspend fun getDishes(restaurantId: RestaurantId): RepositoryResult<List<Dish>> = RepositoryResult.Success(dishes.values.filter { it.restaurantId == restaurantId })
    override suspend fun getDishesByRestaurant(restaurantIds: Set<RestaurantId>): RepositoryResult<Map<RestaurantId, List<Dish>>> = RepositoryResult.Success(dishes.values.filter { it.restaurantId in restaurantIds }.groupBy(Dish::restaurantId))
    override suspend fun saveDish(draft: DishDraft): RepositoryResult<Dish> {
        saveCalls++
        if (saveCalls in failedSaveCalls) {
            return RepositoryResult.Failure(RepositoryError.Unavailable("Dish save failed"))
        }
        savedDraft = draft
        val dish = draft.id?.let { requireNotNull(dishes[it]).copy(name = draft.name, description = draft.description, allergenDeclaration = draft.allergenDeclaration, isEnabled = draft.isEnabled) }
            ?: Dish(DishId("dish-${dishes.size + 1}"), draft.restaurantId, draft.name, draft.description, allergenDeclaration = draft.allergenDeclaration, isEnabled = draft.isEnabled)
        dishes[dish.id] = dish
        return RepositoryResult.Success(dish)
    }
    override suspend fun archiveDish(id: DishId): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
    override suspend fun deleteDish(id: DishId): RepositoryResult<Unit> {
        deleteCalls++
        dishes.remove(id)
        return RepositoryResult.Success(Unit)
    }
}

private class TestMenuRepository(
    details: MenuDetails,
    private val operations: MutableList<String>,
) : MenuRepository {
    var currentDetails = details
        private set
    var savedDraft: RestaurantMenuDraft? = null
    val savedDrafts = mutableListOf<RestaurantMenuDraft>()
    var saveResult: RepositoryResult<MenuDetails>? = null
    var saveCalls = 0
    val failedSaveCalls = mutableSetOf<Int>()
    override suspend fun getMenus(restaurantId: RestaurantId): RepositoryResult<List<Menu>> = RepositoryResult.Success(listOf(currentDetails.menu))
    override suspend fun getPublishedMenu(restaurantId: RestaurantId): RepositoryResult<MenuDetails> = RepositoryResult.Success(currentDetails)
    override suspend fun getMenu(id: MenuId): RepositoryResult<MenuDetails> = RepositoryResult.Success(currentDetails)
    override suspend fun saveMenu(draft: RestaurantMenuDraft): RepositoryResult<MenuDetails> {
        saveCalls++
        savedDraft = draft
        savedDrafts += draft
        operations += "menu:${draft.publicationState}"
        if (saveCalls in failedSaveCalls) return RepositoryResult.Failure(RepositoryError.Offline)
        saveResult?.let { return it }
        val currentItems = currentDetails.items.associateBy { it.dish.id }
        currentDetails = MenuDetails(
            menu = currentDetails.menu.copy(
                name = draft.name,
                description = draft.description,
                publicationState = draft.publicationState,
            ),
            items = draft.items.mapNotNull { item ->
                currentItems[item.dishId]?.copy(
                    price = item.price,
                    position = item.position,
                    isEnabled = item.isEnabled,
                    category = item.category,
                )
            },
        )
        return RepositoryResult.Success(currentDetails)
    }
    override suspend fun deleteMenu(id: MenuId): RepositoryResult<Unit> = RepositoryResult.Success(Unit)

    fun setPublicationState(state: MenuPublicationState) {
        currentDetails = currentDetails.copy(menu = currentDetails.menu.copy(publicationState = state))
    }
}

private class TestReviewRepository(private val restaurantId: RestaurantId, dishId: DishId) : ReviewRepository {
    var restaurantRatingResult: RepositoryResult<RatingSummary> = RepositoryResult.Success(RatingSummary(45, 2))
    private val dishReview = Review(ReviewId("review"), AccountId("customer"), ReviewTarget.Dish(dishId), Rating(4), visibility = ReviewVisibility.Public, moderationStatus = ReviewModerationStatus.Visible, createdAt = org.shareat.app.domain.model.IsoTimestamp("2026-01-01T00:00:00Z"), updatedAt = org.shareat.app.domain.model.IsoTimestamp("2026-01-01T00:00:00Z"))
    override suspend fun getPublicReviews(target: ReviewTarget): RepositoryResult<List<Review>> = RepositoryResult.Success(emptyList())
    override suspend fun getPublicDishReviews(dishIds: Set<DishId>): RepositoryResult<Map<DishId, List<Review>>> = RepositoryResult.Success(mapOf((dishReview.target as ReviewTarget.Dish).dishId to listOf(dishReview)))
    override suspend fun getReviewsByAuthor(accountId: AccountId): RepositoryResult<List<Review>> = RepositoryResult.Success(emptyList())
    override suspend fun getRatingSummary(target: ReviewTarget): RepositoryResult<RatingSummary> = if (target == ReviewTarget.Restaurant(restaurantId)) restaurantRatingResult else RepositoryResult.Success(RatingSummary.Unrated)
    override suspend fun getRestaurantRatingSummaries(restaurantIds: Set<RestaurantId>): RepositoryResult<Map<RestaurantId, RatingSummary>> = RepositoryResult.Success(emptyMap())
    override suspend fun saveReview(draft: org.shareat.app.domain.model.ReviewDraft): RepositoryResult<Review> = failure()
    override suspend fun deleteReview(id: ReviewId, authorAccountId: AccountId): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
}

private class TestImageRepository : ImageRepository {
    val targets = mutableListOf<ImageTarget>()
    override suspend fun replaceImage(target: ImageTarget, upload: ImageUpload): RepositoryResult<ImageRef> {
        targets += target
        val path = when (target) {
            is ImageTarget.RestaurantHero -> "restaurants/${target.restaurantId.value}"
            is ImageTarget.DishImage -> "dishes/${target.dishId.value}"
            is ImageTarget.CustomerAvatar -> "avatars/${target.accountId.value}"
        }
        return RepositoryResult.Success(ImageRef("https://images.test/$path", upload.alternativeText))
    }
    override suspend fun deleteImage(target: ImageTarget): RepositoryResult<Unit> = RepositoryResult.Success(Unit)
}

private fun <T> failure(): RepositoryResult<T> = RepositoryResult.Failure(RepositoryError.Unavailable())
