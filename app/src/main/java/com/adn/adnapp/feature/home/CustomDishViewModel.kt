package com.adn.adnapp.feature.home

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adn.adnapp.data.model.entity.Product
import com.adn.adnapp.domain.model.CustomDishDraft
import com.adn.adnapp.domain.model.DishIngredient
import com.adn.adnapp.domain.model.toProduct
import com.adn.adnapp.domain.repository.AuthRepository
import com.adn.adnapp.domain.repository.FoodRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

data class CustomDishUiState(
    val draft: CustomDishDraft = CustomDishDraft(),
    val foods: List<Product> = emptyList(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val saved: Boolean = false
)

class CustomDishViewModel(
    private val foods: FoodRepository,
    private val auth: AuthRepository,
    private val savedState: SavedStateHandle
) : ViewModel() {
    private val gson = Gson()
    private val restored = savedState.get<String>("dishDraft")?.let {
        runCatching { gson.fromJson(it, CustomDishDraft::class.java) }.getOrNull()
    } ?: CustomDishDraft()
    private val _uiState = MutableStateFlow(CustomDishUiState(draft = restored))
    val uiState = _uiState.asStateFlow()

    init {
        val savedFoods = auth.getCurrentUserId()?.let(foods::observeFavoriteFoods) ?: flowOf(emptyList())
        viewModelScope.launch {
                combine(foods.observeFreshFoods(), savedFoods) { fresh, saved ->
                    (fresh + saved).distinctBy { it.code }.filterNot { it.code.startsWith("dish:") }
                }.catch { _uiState.update { it.copy(error = "No se pudieron cargar los ingredientes") } }
                    .collect { products -> _uiState.update { it.copy(foods = products) } }
            }
    }

    fun onNameChanged(value: String) = updateDraft { copy(name = value) }
    fun onWeightChanged(value: String) = updateDraft { copy(finalWeight = value) }

    fun addIngredient(product: Product, input: String) {
        val grams = input.replace(',', '.').toDoubleOrNull()
        if (grams == null || !grams.isFinite() || grams <= 0) {
            _uiState.update { it.copy(error = "Indica una cantidad válida en gramos") }
            return
        }
        updateDraft {
            copy(ingredients = ingredients.filterNot { it.product.code == product.code } + DishIngredient(product, grams))
        }
    }

    fun removeIngredient(code: String) = updateDraft { copy(ingredients = ingredients.filterNot { it.product.code == code }) }

    fun saveDish() {
        if (_uiState.value.isSaving) return
        val uid = auth.getCurrentUserId() ?: run {
            _uiState.update { it.copy(error = "Inicia sesión para guardar el plato") }
            return
        }
        val product = runCatching { _uiState.value.draft.toProduct(uid) }.getOrElse { failure ->
            _uiState.update { it.copy(error = failure.message) }
            return
        }
        _uiState.update { it.copy(isSaving = true, error = null, saved = false) }
        viewModelScope.launch {
            foods.saveCustomDish(uid, product).fold(
                onSuccess = { _uiState.update { it.copy(isSaving = false, saved = true) } },
                onFailure = { _uiState.update { it.copy(isSaving = false, error = "No se pudo guardar el plato. Tu borrador sigue aquí.") } }
            )
        }
    }

    private fun updateDraft(transform: CustomDishDraft.() -> CustomDishDraft) {
        if (_uiState.value.isSaving) return
        val draft = transform(_uiState.value.draft)
        savedState["dishDraft"] = gson.toJson(draft)
        _uiState.update { it.copy(draft = draft, error = null, saved = false) }
    }
}
