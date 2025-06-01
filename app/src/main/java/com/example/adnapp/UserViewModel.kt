package com.example.adnapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class UserViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // LiveData con los datos de los objetivos nutricionales
    private val _objetivosNutricionales = MutableLiveData<Map<String, Any>>()
    val objetivosNutricionales: LiveData<Map<String, Any>> get() = _objetivosNutricionales

    // Llamar a esta función desde cualquier fragmento para obtener los objetivos actuales
    fun cargarObjetivosDietaActual() {
        val userId = auth.currentUser?.uid ?: return

        // 1. Obtener el nombre de la dieta seleccionada del usuario
        firestore.collection("usuarios").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val dietaSeleccionada = document.get("dieta.nombre") as? String

                if (dietaSeleccionada != null) {
                    // 2. Usar ese nombre para acceder a la colección `dieta`
                    firestore.collection("dieta").document(dietaSeleccionada)
                        .get()
                        .addOnSuccessListener { dietaDoc ->
                            if (dietaDoc.exists()) {
                                _objetivosNutricionales.value = dietaDoc.data ?: emptyMap()
                            } else {
                                Log.e("UserViewModel", "No existe la dieta '$dietaSeleccionada'")
                            }
                        }
                        .addOnFailureListener {
                            Log.e("UserViewModel", "Error al obtener la dieta", it)
                        }
                } else {
                    Log.e("UserViewModel", "El usuario no tiene una dieta asignada")
                }
            }
            .addOnFailureListener {
                Log.e("UserViewModel", "Error al obtener el usuario", it)
            }
    }
}
