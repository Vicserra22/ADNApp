package com.example.adnapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.adnapp.api.RetrofitClient
import com.example.adnapp.models.Product
import kotlinx.coroutines.launch

class FoodViewModel : ViewModel() {

    private val _resultados = MutableLiveData<List<Product>>()
    val resultados: LiveData<List<Product>> get() = _resultados

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    fun buscarAlimentos(query: String) {
        viewModelScope.launch {
            _isLoading.value = true
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
                    } else {
                        _resultados.value = filtrados
                    }
                } else {
                    Log.e("FoodViewModel", "Error en la respuesta: ${response.code()}")
                    _error.value = "Error en la respuesta: ${response.code()}"
                }

            } catch (e: Exception) {
                Log.e("FoodViewModel", "Error en la búsqueda: ${e.message}", e)
                _error.value = "Error al buscar: ${e.message}"

            } finally {
                _isLoading.value = false
            }
        }
    }

}
