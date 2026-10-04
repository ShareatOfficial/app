package org.shareat.feature.review.domain

import kotlinx.coroutines.test.runTest
import org.shareat.app.data.fake.FakeAccountRepository
import org.shareat.app.data.fake.FakeAuthRepository
import org.shareat.app.data.fake.FakeIds
import org.shareat.app.data.fake.FakeShareatData
import org.shareat.app.data.fake.FakeUnlistedDishReviewRepository
import org.shareat.app.domain.model.AccountRole
import org.shareat.app.domain.model.AccountStatus
import org.shareat.app.domain.model.ImageUpload
import org.shareat.app.domain.model.UnlistedDishReview
import org.shareat.app.domain.model.UnlistedDishReviewDraft
import org.shareat.app.domain.repository.AccountRepository
import org.shareat.app.domain.repository.RepositoryError
import org.shareat.app.domain.repository.RepositoryResult
import org.shareat.app.domain.repository.UnlistedDishReviewRepository
import org.shareat.feature.review.domain.model.SubmitUnlistedDishReviewParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class SubmitUnlistedDishReviewUseCaseTest {
    @Test fun trimsInputAndSavesForTheAuthenticatedCustomer() = runTest {
        val data = FakeShareatData.preview()
        val repository = FakeUnlistedDishReviewRepository(data)
        val useCase = SubmitUnlistedDishReviewUseCaseImpl(FakeAuthRepository(initiallyAuthenticated = true), FakeAccountRepository(data), repository)
        val result = assertIs<RepositoryResult.Success<UnlistedDishReview>>(useCase(validParams))
        assertEquals("Casa", result.value.restaurantName)
        assertEquals("Tortilla", result.value.dishName)
        assertEquals("Deliciosa", result.value.comment)
        assertEquals(FakeIds.customerAccount, result.value.authorAccountId)
        assertEquals(listOf(result.value), assertIs<RepositoryResult.Success<List<UnlistedDishReview>>>(repository.getByAuthor(FakeIds.customerAccount)).value)
    }

    @Test fun rejectsMissingFieldsAndInvalidRatingsBeforeWriting() = runTest {
        val data = FakeShareatData.preview()
        val useCase = SubmitUnlistedDishReviewUseCaseImpl(FakeAuthRepository(initiallyAuthenticated = true), FakeAccountRepository(data), mustNotWrite)
        listOf(
            validParams.copy(restaurantName = " "), validParams.copy(dishName = " "),
            validParams.copy(comment = " "), validParams.copy(rating = 0), validParams.copy(rating = 6),
            validParams.copy(restaurantName = "r".repeat(121)), validParams.copy(dishName = "d".repeat(121)),
            validParams.copy(comment = "c".repeat(2001)),
            validParams.copy(image = ImageUpload(byteArrayOf(1), "image/png")),
        ).forEach { params ->
            assertIs<RepositoryError.Validation>(assertIs<RepositoryResult.Failure>(useCase(params)).error)
        }
    }

    @Test fun rejectsGuestsAndInactiveOrRestaurantAccountsBeforeWriting() = runTest {
        val data = FakeShareatData.preview()
        val accounts = FakeAccountRepository(data)
        assertEquals(RepositoryResult.Failure(RepositoryError.Unauthenticated), SubmitUnlistedDishReviewUseCaseImpl(FakeAuthRepository(), accounts, mustNotWrite)(validParams))
        val customer = assertIs<RepositoryResult.Success<org.shareat.app.domain.model.Account>>(accounts.getAccount(FakeIds.customerAccount)).value
        listOf(customer.copy(role = AccountRole.Restaurant), customer.copy(status = AccountStatus.Disabled), customer.copy(status = AccountStatus.DeletionPending)).forEach { account ->
            val restricted = object : AccountRepository by accounts {
                override suspend fun getAccount(id: org.shareat.app.domain.model.AccountId) = RepositoryResult.Success(account)
            }
            assertEquals(RepositoryResult.Failure(RepositoryError.Forbidden), SubmitUnlistedDishReviewUseCaseImpl(FakeAuthRepository(initiallyAuthenticated = true), restricted, mustNotWrite)(validParams))
        }
    }

    @Test fun propagatesRepositoryErrors() = runTest {
        val repository = object : UnlistedDishReviewRepository by mustNotWrite {
            override suspend fun create(draft: UnlistedDishReviewDraft) = RepositoryResult.Failure(RepositoryError.Offline)
        }
        assertEquals(RepositoryResult.Failure(RepositoryError.Offline), SubmitUnlistedDishReviewUseCaseImpl(FakeAuthRepository(initiallyAuthenticated = true), FakeAccountRepository(FakeShareatData.preview()), repository)(validParams))
    }
}

private val validParams = SubmitUnlistedDishReviewParams("  Casa  ", "  Tortilla  ", ImageUpload(byteArrayOf(1), "image/jpeg"), 5, "  Deliciosa  ")
private val mustNotWrite = object : UnlistedDishReviewRepository {
    override suspend fun create(draft: UnlistedDishReviewDraft): RepositoryResult<UnlistedDishReview> = error("Unexpected write")
    override suspend fun getByAuthor(accountId: org.shareat.app.domain.model.AccountId): RepositoryResult<List<UnlistedDishReview>> = error("Unexpected read")
}
