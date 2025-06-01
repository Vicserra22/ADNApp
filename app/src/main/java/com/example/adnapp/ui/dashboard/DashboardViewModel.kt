package com.example.adnapp.ui.dashboard

import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class DashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _consumoDiario = MutableLiveData<Map<String, Double>>()
    private val _objetivos = MutableLiveData<Map<String, Double>>()

    val combinado: LiveData<Pair<Map<String, Double>, Map<String, Double>>> =
        MediatorLiveData<Pair<Map<String, Double>, Map<String, Double>>>().apply {
            var consumoActual = emptyMap<String, Double>()
            var objetivosActual = emptyMap<String, Double>()

            addSource(_consumoDiario) {
                consumoActual = it
                value = consumoActual to objetivosActual
            }

            addSource(_objetivos) {
                objetivosActual = it
                value = consumoActual to objetivosActual
            }
        }

    fun loadDataDaily() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        loadDataForDate(today)
    }

    fun loadDataForDate(fecha: String) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("DashboardViewModel", "No hay usuario autenticado")
            return
        }

        // 1. Cargar consumo diario
        firestore.collection("usuarios")
            .document(userId)
            .collection("consumoDiario")
            .document(fecha)
            .get()
            .addOnSuccessListener { doc ->
                val claves = listOf("calorias", "proteinas", "carbos", "grasas", "azucar")
                val datos = claves.associateWith { doc.getDouble(it) ?: 0.0 }
                Log.d("DashboardViewModel", "Consumo para $fecha: $datos")
                _consumoDiario.value = datos
            }
            .addOnFailureListener { e ->
                Log.e("DashboardViewModel", "Error al obtener consumo diario", e)
            }

        // 2. Cargar objetivos de dieta directamente desde usuario.dieta
        firestore.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val dietaMap = userDoc.get("dieta") as? Map<String, Any> ?: emptyMap()

                fun getDoubleValue(key: String, map: Map<String, Any>): Double {
                    val value = map[key]
                    return when(value) {
                        is Number -> value.toDouble()
                        is String -> value.toDoubleOrNull() ?: 0.0
                        else -> 0.0
                    }
                }

                val objetivos = mapOf(
                    "calorias" to getDoubleValue("calorias", dietaMap),
                    "proteinas" to getDoubleValue("proteinas", dietaMap),
                    "carbos" to getDoubleValue("carbos", dietaMap),
                    "grasas" to getDoubleValue("grasas", dietaMap),
                    "azucar" to getDoubleValue("azucar", dietaMap)
                )

                Log.d("DashboardViewModel", "Objetivos dieta cargados: $objetivos")
                _objetivos.value = objetivos
            }
            .addOnFailureListener { e ->
                Log.e("DashboardViewModel", "Error al obtener objetivos", e)
            }

    }

}
