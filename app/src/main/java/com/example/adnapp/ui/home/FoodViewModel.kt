package com.example.adnapp.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adnapp.api.RetrofitClient
import com.example.adnapp.models.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    private val _resultados = MutableStateFlow<List<Product>>(emptyList())
    val resultados: StateFlow<List<Product>> = _resultados.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun buscarAlimentos(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                Log.d("FoodViewModel", "Buscando alimentos para: $query")

                val response = RetrofitClient.api.searchFoods(query)

                if (response.isSuccessful && response.body() != null) {
                    val productos = response.body()!!.products

                    val filtrados = productos
                        .asSequence()
                        .filter { it.name?.isNotBlank() == true }                  // Nombre no vacío
                        .filter { it.nutriments != null }                         // Tiene datos nutricionales
                        .filter { it.imageUrl?.isNotBlank() == true }             // Tiene imagen
                        .filter {
                            val n = it.nutriments!!
                            val valores = listOf(n.calories, n.fat, n.proteins, n.carbs, n.sugars)
                            valores.count { v -> v == null } <= 1                 // Le falta 1 nutriente como mucho
                        }
                        .distinctBy {
                            it.name?.lowercase()?.trim()
                        }             // Evita duplicados por nombre
                        .take(50)                                                 // Limita a 30 productos válidos
                        .toList()

                    Log.d("FoodViewModel", "Productos recibidos: ${productos.size}")
                    Log.d("FoodViewModel", "Productos válidos filtrados: ${filtrados.size}")

                    if (filtrados.isEmpty()) {
                        _error.value = "No se encontraron alimentos para tu búsqueda."
                        _resultados.value = emptyList()
                    } else {
                        _resultados.value = filtrados
                    }
                } else {
                    Log.e("FoodViewModel", "Error en la respuesta: ${response.code()}")
                    _error.value = "Error en la respuesta de la búsqueda"
                }

            } catch (e: Exception) {
                Log.e("FoodViewModel", "Error en la búsqueda: ${e.message}", e)
                _error.value = "Error al buscar:\n Fallo con la conexión"

            } finally {
                _isLoading.value = false
            }
        }
    }

}
