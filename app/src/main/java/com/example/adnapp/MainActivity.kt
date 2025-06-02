package com.example.adnapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adnapp.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Tema para splash antes de mostrar UI
        setTheme(R.style.Theme_ADNApp)

        super.onCreate(savedInstanceState)

        // Primero comprueba si el usuario tiene datos, para evitar mostrar UI si no tiene perfil
        checkUserData()
    }

    private fun checkUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val db = Firebase.firestore
            val userRef = db.collection("usuarios").document(currentUser.uid)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Usuario tiene datos: muestra la UI de MainActivity
                        setupUI()
                    } else {
                        // Usuario no tiene datos: lanza flujo registro y cierra MainActivity
                        startActivity(Intent(this, DataRegistrationActivity::class.java))
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al comprobar datos del usuario", Toast.LENGTH_SHORT).show()
                    // En caso de error también muestra UI para evitar quedar bloqueado
                    setupUI()
                }
        } else {
            // No debería llegar aquí porque Splash ya redirige si no hay user
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupUI() {
        // Infla layout y configura navegación solo si el usuario tiene perfil
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)



        navView.setupWithNavController(navController)
    }
}
