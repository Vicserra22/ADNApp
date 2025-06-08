package com.example.adnapp.ui.dashboard

import android.graphics.Color
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.prolificinteractive.materialcalendarview.CalendarDay
import java.text.SimpleDateFormat
import java.util.*
import androidx.core.graphics.toColorInt

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

    //Carga los alimentos guardados:
    fun loadDataForDate(fecha: String) {
        val userId = auth.currentUser?.uid ?: run {
            Log.e("DashboardViewModel", "No hay usuario autenticado")
            return
        }

        // 1. Carga consumo diario
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

        // 2. Carga objetivos de dieta directamente desde usuario.dieta
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

    private val _decoraciones = MutableLiveData<Map<CalendarDay, Int>>()
    val decoraciones: LiveData<Map<CalendarDay, Int>> get() = _decoraciones

    fun cargarDiasCumplidos() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("usuarios")
            .document(userId)
            .collection("consumoDiario")
            .get()
            .addOnSuccessListener { snapshot ->
                val resultado = mutableMapOf<CalendarDay, Int>()

                for (doc in snapshot.documents) {
                    val fechaStr = doc.id
                    val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val fechaParseada = formato.parse(fechaStr)

                    if (fechaParseada == null) {
                        Log.w("ViewModel", "Fecha inválida: $fechaStr")
                        continue
                    }

                    val fecha = Calendar.getInstance().apply {
                        time = fechaParseada
                    }

                    val valores = listOf("calorias", "proteinas", "carbos", "grasas", "azucar").mapNotNull {
                        val objetivo = _objetivos.value?.get(it) ?: return@mapNotNull null
                        val consumo = doc.getDouble(it) ?: return@mapNotNull null
                        if (objetivo == 0.0) return@mapNotNull null
                        (consumo / objetivo) * 100
                    }

                    if (valores.isNotEmpty()) {
                        val promedio = valores.average()
                        val color = when (promedio) {
                            in 20.0..70.0 -> "#FF9800".toColorInt() // naranja
                            in 70.1..90.0 -> "#FFD93D".toColorInt() // amarillo
                            in 90.1..110.0 -> "#4CAF50".toColorInt() // verde
                            in 110.1..130.0 -> "#FF9800".toColorInt() // naranja
                            else -> "#F44336".toColorInt() // rojo
                        }
                        resultado[CalendarDay.from(
                            fecha.get(Calendar.YEAR),
                            fecha.get(Calendar.MONTH),
                            fecha.get(Calendar.DAY_OF_MONTH)
                        )] = color
                    }
                }

                _decoraciones.value = resultado
            }
    }


}
