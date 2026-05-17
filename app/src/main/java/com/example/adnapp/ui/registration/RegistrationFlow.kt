package com.example.adnapp.ui.registration

import androidx.compose.runtime.*
import com.example.adnapp.SharedViewModel
import com.example.adnapp.models.Diet
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import android.util.Log

@Composable
fun RegistrationFlow(
    sharedViewModel: SharedViewModel,
    onComplete: () -> Unit
) {
    var currentStep by remember { mutableStateOf(0) }
    var diets by remember { mutableStateOf<List<Diet>>(emptyList()) }

    // Cargar dietas una vez
    LaunchedEffect(Unit) {
        val db = Firebase.firestore
        db.collection("dietas")
            .get()
            .addOnSuccessListener { result ->
                val list = result.map { it.toObject(Diet::class.java) }
                diets = list
            }
            .addOnFailureListener { exception ->
                Log.e("RegistrationFlow", "Error al cargar dietas", exception)
            }
    }

    when (currentStep) {
        0 -> UserInfoScreen(
            onNext = { userInfo ->
                sharedViewModel.userInfoData = userInfo
                currentStep = 1
            }
        )
        1 -> DietSelectionScreen(
            diets = diets,
            onNext = { diet ->
                sharedViewModel.dietSelectionData = com.example.adnapp.DietSelectionData(diet)
                onComplete()
            }
        )
    }
}
