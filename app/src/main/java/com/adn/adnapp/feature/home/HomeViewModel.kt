package com.adn.adnapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.data.repository.FoodSearchException
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
    val manualName: String = "",
    val manualCalories: String = "",
    val manualProteins: String = "",
    val manualCarbs: String = "",
    val manualFats: String = "",
    val manualSugar: String = "",
    val isSavingManual: Boolean = false,
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
        _uiState.update { it.copy(searchQuery = query, error = null) }
    }

    fun searchFood() {
        val query = _uiState.value.searchQuery
        if (query.trim().length < 2) {
            _uiState.update { it.copy(error = "Escribe al menos 2 caracteres") }
            return
        }
        if (_uiState.value.isSearching) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null, successMessage = null) }
            val result = foodRepository.searchFoods(query)
            if (result.isSuccess) {
                val products = result.getOrNull().orEmpty()
                _uiState.update { it.copy(searchResults = products, isSearching = false,
                    error = if (products.isEmpty()) "No hay productos completos para esa búsqueda. Prueba con marca y nombre." else null) }
            } else {
                val message = when ((result.exceptionOrNull() as? FoodSearchException)?.reason) {
                    FoodSearchException.Reason.RATE_LIMIT -> "Límite de búsquedas alcanzado. Espera un minuto; no hace falta repetir varias veces."
                    FoodSearchException.Reason.SERVICE_UNAVAILABLE -> "Open Food Facts está saturado. Inténtalo más tarde."
                    else -> "No se pudo conectar con Open Food Facts. Comprueba la conexión."
                }
                _uiState.update { it.copy(isSearching = false, error = message) }
            }
        }
    }

    fun onManualNameChanged(value: String) = manualUpdate { copy(manualName = value) }
    fun onManualCaloriesChanged(value: String) = manualUpdate { copy(manualCalories = value.decimalInput()) }
    fun onManualProteinsChanged(value: String) = manualUpdate { copy(manualProteins = value.decimalInput()) }
    fun onManualCarbsChanged(value: String) = manualUpdate { copy(manualCarbs = value.decimalInput()) }
    fun onManualFatsChanged(value: String) = manualUpdate { copy(manualFats = value.decimalInput()) }
    fun onManualSugarChanged(value: String) = manualUpdate { copy(manualSugar = value.decimalInput()) }

    fun saveManualEntry() {
        val state = _uiState.value
        val values = listOf(state.manualCalories, state.manualProteins, state.manualCarbs,
            state.manualFats, state.manualSugar).map { it.numberOrZero() }
        if (values.any { it == null || it < 0 } || values.all { it == 0.0 }) {
            _uiState.update { it.copy(error = "Añade al menos una cantidad válida mayor que cero") }
            return
        }
        val uid = authRepository.getCurrentUserId()
        if (uid == null) {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingManual = true, error = null, successMessage = null) }
            foodRepository.saveFoodEntry(
                uid,
                FoodEntry(
                    name = state.manualName.ifBlank { "Ajuste manual" },
                    calories = values[0]!!, proteins = values[1]!!, carbs = values[2]!!,
                    fats = values[3]!!, sugar = values[4]!!, kind = FoodEntry.KIND_MANUAL
                ),
                dateFormat.format(Date())
            ).fold(
                onSuccess = { _uiState.update { it.copy(
                    manualName = "", manualCalories = "", manualProteins = "", manualCarbs = "",
                    manualFats = "", manualSugar = "", isSavingManual = false,
                    successMessage = "Datos añadidos al día de hoy"
                ) } },
                onFailure = { _uiState.update { it.copy(isSavingManual = false, error = "No se pudieron guardar los datos") } }
            )
        }
    }

    fun onProductSelected(product: Product) {
        _uiState.update { it.copy(selectedProduct = product, quantityToAdd = "", error = null, successMessage = null) }
    }

    fun onProductSelectedDismissed() {
        _uiState.update { it.copy(selectedProduct = null, quantityToAdd = "") }
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
                fats = product.fats * factor,
                sugar = product.sugars * factor
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

    private inline fun manualUpdate(transform: HomeUiState.() -> HomeUiState) {
        _uiState.update { transform(it).copy(error = null, successMessage = null) }
    }
}

private fun String.numberOrZero(): Double? = if (isBlank()) 0.0 else replace(',', '.').toDoubleOrNull()
private fun String.decimalInput(): String = filterIndexed { index, char ->
    char.isDigit() || ((char == ',' || char == '.') && index > 0 && take(index).none { it == ',' || it == '.' })
}
