package com.example.adnapp.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _consumoDiario = MutableLiveData<Map<String, Double>>()
    val consumoDiario: LiveData<Map<String, Double>> get() = _consumoDiario

    private val _objetivosDieta = MutableLiveData<Map<String, Double>>()
    val objetivosDieta: LiveData<Map<String, Double>> get() = _objetivosDieta

    fun loadDataDaily() {
        val userId = auth.currentUser?.uid ?: return
        val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        // Obtener consumo diario
        firestore.collection("usuarios")
            .document(userId)
            .collection("consumoDiario")
            .document(fechaHoy)
            .get()
            .addOnSuccessListener { doc ->
                val datos = mutableMapOf<String, Double>()
                for (clave in listOf(
                    "calorias",
                    "proteinas",
                    "carbohidratos",
                    "grasas",
                    "azucar",
                    "agua",
                    "vitaminaD"
                )) {
                    datos[clave] = doc.getDouble(clave) ?: 0.0
                }
                _consumoDiario.value = datos
            }

        // Obtener dieta seleccionada y objetivos
        firestore.collection("usuarios")
            .document(userId)
            .get()
            .addOnSuccessListener { userDoc ->
                val dietaId =
                    userDoc.get("info.dietaSeleccionada") as? String ?: return@addOnSuccessListener
                firestore.collection("dieta")
                    .document(dietaId)
                    .get()
                    .addOnSuccessListener { dietaDoc ->
                        val objetivos = mutableMapOf<String, Double>()
                        for (clave in listOf(
                            "calorias",
                            "proteinas",
                            "carbohidratos",
                            "grasas",
                            "azucar",
                            "agua",
                            "vitaminaD"
                        )) {
                            objetivos[clave] = dietaDoc.getDouble(clave) ?: 0.0
                        }
                        _objetivosDieta.value = objetivos
                    }
            }
    }
}
