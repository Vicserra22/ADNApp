package com.adn.adnapp.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.DailyConsumption
import com.adn.adnapp.data.model.entity.FoodEntry
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.adn.adnapp.domain.repository.UserRepository
import com.adn.adnapp.domain.repository.NutritionRepository
import com.adn.adnapp.domain.repository.DietRepository
import com.adn.adnapp.domain.model.NutritionTargets
import com.adn.adnapp.domain.service.GoalCalculator
import com.adn.adnapp.data.repository.FoodSearchException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.LocalDate
import com.adn.adnapp.domain.model.EntryDateChoice

data class HomeUiState(
    val screenDate: String = LocalDate.now().toString(),
    val dateChoice: EntryDateChoice? = null,
    val searchQuery: String = "",
    val searchResults: List<Product> = emptyList(),
    val freshFoods: List<Product> = emptyList(),
    val recentFoods: List<Product> = emptyList(),
    val favoriteFoods: List<Product> = emptyList(),
    val favoriteCodes: Set<String> = emptySet(),
    val foodSection: FoodSection = FoodSection.PRODUCTS,
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
    val targets: NutritionTargets? = null,
    val error: String? = null,
    val successMessage: String? = null
)

enum class FoodSection { PRODUCTS, FRESH, CUSTOM, SAVED }

class HomeViewModel(
    private val foodRepository: FoodRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val nutritionRepository: NutritionRepository,
    private val dietRepository: DietRepository,
    private val today: () -> LocalDate = { LocalDate.now() }
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(screenDate = today().toString()))
    val uiState = _uiState.asStateFlow()
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    init {
        observeDailyConsumption()
        observeFoodLibrary()
        loadTargets()
    }

    private fun observeFoodLibrary() {
        viewModelScope.launch {
            foodRepository.observeFreshFoods().collect { foods ->
                _uiState.update { it.copy(freshFoods = foods) }
            }
        }
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            foodRepository.observeRecentFoods(uid).collect { foods ->
                _uiState.update { it.copy(recentFoods = foods) }
            }
        }
        viewModelScope.launch {
            foodRepository.observeFavoriteFoods(uid).collect { foods ->
                _uiState.update { it.copy(favoriteFoods = foods, favoriteCodes = foods.mapTo(mutableSetOf()) { food -> food.code }) }
            }
        }
    }

    fun onFoodSectionChanged(section: FoodSection) {
        _uiState.update { it.copy(foodSection = section, error = null) }
    }

    private fun loadTargets() {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            val profile = userRepository.getUserProfile(uid).getOrNull() ?: return@launch
            val nutrition = nutritionRepository.getNutritionProfile(uid).getOrNull() ?: return@launch
            val diet = dietRepository.getDiet(nutrition.dietId, uid).getOrNull() ?: return@launch
            _uiState.update {
                it.copy(targets = GoalCalculator.targets(
                    profile, diet, macroTolerance = nutrition.macroTolerance
                ))
            }
        }
    }

    private fun observeDailyConsumption() {
        val uid = authRepository.getCurrentUserId() ?: return
        val dateKey = _uiState.value.screenDate
        
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

    fun onBarcodeScanned(barcode: String) {
        if (_uiState.value.isSearching) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, error = null, successMessage = null) }
            foodRepository.getFoodByBarcode(barcode).fold(
                onSuccess = { product ->
                    _uiState.update { it.copy(isSearching = false, selectedProduct = product) }
                },
                onFailure = {
                    _uiState.update { it.copy(isSearching = false, error = "No encontramos ese código. Puedes buscarlo por nombre o añadirlo manualmente.") }
                }
            )
        }
    }

    fun onBarcodeScanFailed() {
        _uiState.update { it.copy(error = "No se pudo abrir el lector de códigos") }
    }

    fun toggleFavorite(product: Product) {
        val uid = authRepository.getCurrentUserId() ?: return
        viewModelScope.launch {
            foodRepository.toggleFavorite(uid, product).onFailure {
                _uiState.update { state -> state.copy(error = "No se pudo actualizar favoritos") }
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
        requestEntry(
                FoodEntry(
                    name = state.manualName.ifBlank { "Ajuste manual" },
                    calories = values[0]!!, proteins = values[1]!!, carbs = values[2]!!,
                    fats = values[3]!!, sugar = values[4]!!, kind = FoodEntry.KIND_MANUAL
                ), product = null
        )
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

            requestEntry(entry, product)
    }

    private data class PendingEntry(val entry: FoodEntry, val product: Product?)
    private var pendingEntry: PendingEntry? = null

    private fun requestEntry(entry: FoodEntry, product: Product?) {
        if (_uiState.value.isSavingManual || pendingEntry != null) return
        val currentDate = today().toString()
        val screenDate = _uiState.value.screenDate
        val pending = PendingEntry(entry, product)
        if (screenDate != currentDate) {
            pendingEntry = pending
            _uiState.update { it.copy(dateChoice = EntryDateChoice(screenDate, currentDate)) }
        } else persistEntry(pending, screenDate)
    }

    fun cancelDateChoice() {
        pendingEntry = null
        _uiState.update { it.copy(dateChoice = null) }
    }

    fun confirmEntryDate(date: String) {
        val pending = pendingEntry ?: return
        val choice = _uiState.value.dateChoice ?: return
        if (date != choice.screenDate && date != choice.today) return
        // The displayed options are immutable dates, including if midnight passes again.
        pendingEntry = null
        _uiState.update { it.copy(dateChoice = null) }
        persistEntry(pending, date)
    }

    private fun persistEntry(pending: PendingEntry, date: String) {
        val uid = authRepository.getCurrentUserId() ?: run {
            _uiState.update { it.copy(error = "Usuario no autenticado") }
            return
        }
        _uiState.update { it.copy(isSavingManual = true, error = null, successMessage = null) }
        viewModelScope.launch {
            foodRepository.saveFoodEntry(uid, pending.entry, date).fold(
                onSuccess = {
                    pending.product?.let { runCatching { foodRepository.recordProductUsed(uid, it) } }
                    _uiState.update {
                        if (pending.product == null) it.copy(
                            manualName = "", manualCalories = "", manualProteins = "", manualCarbs = "",
                            manualFats = "", manualSugar = "", isSavingManual = false,
                            successMessage = if (date == today().toString()) "Datos añadidos al día de hoy"
                                else "Datos añadidos al " + date
                        ) else it.copy(
                            selectedProduct = null, quantityToAdd = "", searchQuery = "",
                            searchResults = emptyList(), isSavingManual = false,
                            successMessage = "Alimento añadido al " + date
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isSavingManual = false, error = "No se pudo guardar la entrada") }
                }
            )
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
