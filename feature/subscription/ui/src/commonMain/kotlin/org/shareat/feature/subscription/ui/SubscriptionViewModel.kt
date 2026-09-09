package org.shareat.feature.subscription.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.shareat.feature.subscription.domain.CustomerInfoState
import org.shareat.feature.subscription.domain.CustomerSubscriptionInfo
import org.shareat.feature.subscription.domain.SubscriptionProduct
import org.shareat.feature.subscription.domain.SubscriptionRepository
import org.shareat.feature.subscription.domain.SubscriptionResult

class SubscriptionViewModel(
    private val repository: SubscriptionRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        observeCustomerInfo()
        load()
    }

    fun retry() = load()

    fun purchase(product: SubscriptionProduct) = runCustomerOperation {
        repository.purchase(product)
    }

    fun restorePurchases() = runCustomerOperation {
        repository.restorePurchases()
    }

    fun showPaywall() = _uiState.update {
        it.copy(showPaywall = true, errorMessage = null)
    }

    fun dismissPaywall() = _uiState.update { it.copy(showPaywall = false) }

    fun showCustomerCenter() {
        if (!_uiState.value.canManageSubscription) return
        _uiState.update { it.copy(showCustomerCenter = true, errorMessage = null) }
    }

    fun dismissCustomerCenter() {
        _uiState.update { it.copy(showCustomerCenter = false, isProcessing = true) }
        viewModelScope.launch {
            applyCustomerResult(repository.refreshCustomerInfo())
        }
    }

    fun onRevenueCatUiCompleted() {
        _uiState.update { it.copy(showPaywall = false, isProcessing = true) }
        viewModelScope.launch {
            applyCustomerResult(repository.refreshCustomerInfo())
        }
    }

    fun onRevenueCatUiError(message: String) {
        _uiState.update { it.copy(isProcessing = false, errorMessage = message) }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            val customerResult = repository.refreshCustomerInfo()
            applyCustomerResult(customerResult)
            val offeringResult = repository.getCurrentOffering()
            when (offeringResult) {
                is SubscriptionResult.Success -> _uiState.update {
                    it.copy(packages = offeringResult.value)
                }
                is SubscriptionResult.Failure -> _uiState.update {
                    it.copy(errorMessage = offeringResult.error.message)
                }
            }
        }
    }

    private fun observeCustomerInfo() {
        viewModelScope.launch {
            repository.customerInfo.collect { state ->
                when (state) {
                    CustomerInfoState.Loading -> Unit
                    is CustomerInfoState.Available -> _uiState.update {
                        it.copy(customerInfo = state.value, isLoading = false)
                    }
                    is CustomerInfoState.Failed -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            isProcessing = false,
                            errorMessage = state.error.message,
                        )
                    }
                    is CustomerInfoState.Unsupported -> _uiState.update {
                        it.copy(isLoading = false, errorMessage = state.reason)
                    }
                }
            }
        }
    }

    private fun runCustomerOperation(
        operation: suspend () -> SubscriptionResult<CustomerSubscriptionInfo>,
    ) {
        if (_uiState.value.isProcessing) return
        _uiState.update { it.copy(isProcessing = true, errorMessage = null) }
        viewModelScope.launch { applyCustomerResult(operation()) }
    }

    private fun applyCustomerResult(result: SubscriptionResult<CustomerSubscriptionInfo>) {
        when (result) {
            is SubscriptionResult.Success -> _uiState.update {
                it.copy(
                    customerInfo = result.value,
                    isLoading = false,
                    isProcessing = false,
                    errorMessage = null,
                )
            }
            is SubscriptionResult.Failure -> _uiState.update {
                it.copy(
                    isLoading = false,
                    isProcessing = false,
                    errorMessage = result.error.takeUnless { error ->
                        error.isUserCancellation
                    }?.message,
                )
            }
        }
    }
}
