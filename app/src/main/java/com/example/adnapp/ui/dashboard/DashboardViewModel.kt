package com.example.adnapp.ui.dashboard

import android.util.Log
import androidx.core.graphics.toColorInt
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.prolificinteractive.materialcalendarview.CalendarDay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var objectivesListener: ListenerRegistration? = null
    private var consumptionListener: ListenerRegistration? = null
    private var historyListener: ListenerRegistration? = null
    private var lastHistoryDocs: List<com.google.firebase.firestore.DocumentSnapshot> = emptyList()

    init {
        startObjectivesListener()
        startHistoryListener()
        // Load default date
        loadDataForDate(CalendarDay.today())
    }

    fun handleIntent(intent: DashboardIntent) {
        when (intent) {
            is DashboardIntent.LoadDataForDate -> loadDataForDate(intent.date)
            is DashboardIntent.LoadHistory -> { /* Already handled by historyListener */
            }
        }
    }

    private fun startObjectivesListener() {
        val userId = auth.currentUser?.uid ?: return

        objectivesListener?.remove()
        objectivesListener = firestore.collection("usuarios")
            .document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DashboardViewModel", "Error al escuchar objetivos", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val dietaMap = snapshot.get("dieta") as? Map<String, Any> ?: emptyMap()

                    fun getDoubleValue(key: String, map: Map<String, Any>): Double {
                        val value = map[key]
                        return when (value) {
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

                    _uiState.update { it.copy(objetivos = objetivos) }

                    if (lastHistoryDocs.isNotEmpty()) {
                        actualizarDecoraciones(lastHistoryDocs)
                    }
                }
            }
    }

    private fun loadDataForDate(date: CalendarDay) {
        val userId = auth.currentUser?.uid ?: return
        val fecha = String.format(Locale.US, "%04d-%02d-%02d", date.year, date.month + 1, date.day)

        _uiState.update { it.copy(selectedDate = date, isLoading = true) }

        consumptionListener?.remove()
        consumptionListener = firestore.collection("usuarios")
            .document(userId)
            .collection("consumoDiario")
            .document(fecha)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DashboardViewModel", "Error al escuchar consumo", e)
                    _uiState.update { it.copy(isLoading = false) }
                    return@addSnapshotListener
                }

                val claves = listOf("calorias", "proteinas", "carbos", "grasas", "azucar")
                val datos = if (snapshot != null && snapshot.exists()) {
                    claves.associateWith { snapshot.getDouble(it) ?: 0.0 }
                } else {
                    claves.associateWith { 0.0 }
                }
                _uiState.update { it.copy(consumo = datos, isLoading = false) }
            }
    }

    private fun startHistoryListener() {
        val userId = auth.currentUser?.uid ?: return

        historyListener?.remove()
        historyListener = firestore.collection("usuarios")
            .document(userId)
            .collection("consumoDiario")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.e("DashboardViewModel", "Error al escuchar historial", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    lastHistoryDocs = snapshot.documents
                    actualizarDecoraciones(lastHistoryDocs)
                }
            }
    }

    private fun actualizarDecoraciones(documents: List<com.google.firebase.firestore.DocumentSnapshot>) {
        val resultado = mutableMapOf<CalendarDay, Int>()
        val objetivos = _uiState.value.objetivos

        for (doc in documents) {
            val fechaStr = doc.id
            val formato = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val fechaParseada = formato.parse(fechaStr) ?: continue

            val fecha = Calendar.getInstance().apply {
                time = fechaParseada
            }

            val valores = listOf("calorias", "proteinas", "carbos", "grasas", "azucar").mapNotNull {
                val objetivo = objetivos[it] ?: return@mapNotNull null
                val consumo = doc.getDouble(it) ?: return@mapNotNull null
                if (objetivo == 0.0) return@mapNotNull null
                (consumo / objetivo) * 100
            }

            if (valores.isNotEmpty()) {
                val promedio = valores.average()
                val color = when (promedio) {
                    in 20.0..70.0 -> "#FF9800".toColorInt()
                    in 70.1..90.0 -> "#FFD93D".toColorInt()
                    in 90.1..95.0 -> "#4CAF50".toColorInt()
                    in 95.01..105.0 -> "#00C853".toColorInt()
                    in 105.1..111.0 -> "#4CAF50".toColorInt()
                    in 111.1..130.0 -> "#FF9800".toColorInt()
                    else -> "#F44336".toColorInt()
                }
                resultado[CalendarDay.from(
                    fecha.get(Calendar.YEAR),
                    fecha.get(Calendar.MONTH),
                    fecha.get(Calendar.DAY_OF_MONTH)
                )] = color
            }
        }
        _uiState.update { it.copy(decoraciones = resultado) }
    }

    override fun onCleared() {
        super.onCleared()
        objectivesListener?.remove()
        consumptionListener?.remove()
        historyListener?.remove()
    }
}
