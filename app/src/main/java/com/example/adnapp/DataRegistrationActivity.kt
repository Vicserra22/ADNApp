package com.example.adnapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.adnapp.databinding.ActivityDataRegistrationBinding
import com.example.adnapp.fragments.DietSelectionFragment
import com.example.adnapp.fragments.UserInfoFragment
import com.example.adnapp.interfaces.OnDietSelectionCompleteListener
import com.example.adnapp.interfaces.OnUserInfoCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

// Flujo de registro de datos
class DataRegistrationActivity : AppCompatActivity(),
    OnUserInfoCompleteListener,
    OnDietSelectionCompleteListener {

    private lateinit var binding: ActivityDataRegistrationBinding
    private var currentStep = 0

    private val sharedViewModel: SharedViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDataRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentStep) {
                    0 -> finish()
                    1 -> {
                        supportFragmentManager.popBackStack()
                        currentStep = 0
                    }
                }
            }
        })

        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, UserInfoFragment())
            .commit()
    }

    override fun onUserInfoComplete() {
        currentStep = 1
        supportFragmentManager.beginTransaction()
            .replace(binding.fragmentContainer.id, DietSelectionFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDietSelectionComplete() {
        val auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore
        val currentUser = auth.currentUser

        val userInfo = sharedViewModel.userInfoData
        val dietInfo = sharedViewModel.dietSelectionData?.selectedDiet

        if (currentUser != null && userInfo != null && dietInfo != null) {
            val infoMap = hashMapOf(
                "nombre" to userInfo.name,
                "edad" to userInfo.age,
                "peso" to userInfo.weight,
                "altura" to userInfo.height,
                "genero" to userInfo.selectedGenderId
            )

            val dietaMap = hashMapOf(
                "id" to dietInfo.id,
                "nombre" to dietInfo.name,
                "descripcion" to dietInfo.description,
                "imagen" to dietInfo.imageUrl,
                "proteinas" to dietInfo.proteins,
                "carbos" to dietInfo.carbs,
                "grasas" to dietInfo.lipids,
                "calorias" to dietInfo.calories,
                "azucar" to dietInfo.sugar,
                "agua" to dietInfo.water,
                "vitD" to dietInfo.vitD
            )

            val usuarioMap = hashMapOf(
                "info" to infoMap,
                "dieta" to dietaMap
            )

            db.collection("usuarios").document(currentUser.uid)
                .set(usuarioMap)
                .addOnSuccessListener {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Error al guardar datos en Firebase", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Faltan datos del usuario o dieta", Toast.LENGTH_SHORT).show()
        }
    }

}
