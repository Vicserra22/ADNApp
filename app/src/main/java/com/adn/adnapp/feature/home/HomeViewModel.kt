package com.adn.adnapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class HomeUiState(
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val isSearching: Boolean = false,
    val selectedProduct: Product? = null,
    val quantityToAdd: String = "",
    val dailyConsumption: DailyConsumption? = null,
    val error: String? = null,
    val successMessage: String? = null
)

class HomeViewModel(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        observeDailyConsumption()
    }

    private fun observeDailyConsumption() {
        val uid = authRepository.getCurrentUserId() ?: return
        val dateKey = dateFormat.format(Date())
        
        viewModelScope.launch {
            foodRepository.observeDailyConsumption(uid, dateKey)
                .catch { _uiState.update { state -> state.copy(error = "No se pudo cargar el consumo diario") } }
                .collect { consumption -> _uiState.update { it.copy(dailyConsumption = consumption) } }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun searchFood() {
        val query = _uiState.value.searchQuery
        if (query.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null, successMessage = null) }
            val result = foodRepository.searchFoods(query)
            if (result.isSuccess) {
                _uiState.update { it.copy(searchResults = result.getOrNull() ?: emptyList(), isSearching = false) }
            } else {
                _uiState.update { it.copy(isSearching = false, error = "Error en la búsqueda") }
            }
        }
    }

    fun onProductSelected(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, quantityToAdd = "", error = null, successMessage = null) }
    }

    fun onQuantityChanged(quantity: String) {
        _uiState.update { it.copy(quantityToAdd = quantity) }
    }

    fun addFoodEntry() {
        val product = _uiState.value.selectedProduct ?: return
        val quantityStr = _uiState.value.quantityToAdd
        val quantity = quantityStr.replace(',', '.').toDoubleOrNull()
        
        if (quantity == null || quantity <= 0) {
            _uiState.update { it.copy(error = "Cantidad inválida") }
            return
        }

        viewModelScope.launch {
            val uid = authRepository.getCurrentUserId()
            if (uid == null) {
                _uiState.update { it.copy(error = "Usuario no autenticado") }
                return@launch
            }

            val factor = quantity / 100.0
            val entry = FoodEntry(
                productId = product.code,
                name = product.name,
                quantity = quantity,
                calories = product.calories * factor,
                proteins = product.proteins * factor,
                carbs = product.carbs * factor,
                fats = product.fats * factor
            )

            val dateKey = dateFormat.format(Date())
            val result = foodRepository.saveFoodEntry(uid, entry, dateKey)
            
            if (result.isSuccess) {
                _uiState.update { 
                    it.copy(
                        selectedProduct = null, 
                        quantityToAdd = "", 
                        searchQuery = "", 
                        searchResults = emptyList(),
                        successMessage = "Alimento añadido correctamente"
                    )
                }
            } else {
                _uiState.update { it.copy(error = "Error al añadir alimento") }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
