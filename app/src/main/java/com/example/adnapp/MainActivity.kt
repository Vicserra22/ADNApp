package com.example.adnapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.adnapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ADNApp)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Inflamos ya la vista (necesario para evitar problemas con NavController)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Comprobamos si hay que redirigir al flujo de registro
        checkUserData()

        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            Log.e("CRASH", "Error inesperado: ${e.message}", e)
        }
    }

    private fun checkUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val db = Firebase.firestore
            val userRef = db.collection("usuarios").document(currentUser.uid)

            userRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // Usuario tiene datos, continúa con la UI
                        setupNavigation()
                    } else {
                        // No tiene datos: lanzamos flujo y cerramos esta Activity
                        val intent = Intent(this, DataRegistrationActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Error al comprobar datos del usuario", Toast.LENGTH_SHORT).show()
                    // Incluso si falla, permitimos usar la app
                    setupNavigation()
                }
        } else {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupNavigation() {
        val navController = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main)
            ?.let { it as? NavHostFragment }
            ?.navController

        if (navController == null) {
            Toast.makeText(this, "No se pudo cargar la navegación", Toast.LENGTH_SHORT).show()
            return
        }

        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_profile
            )
        )

        binding.navView.setupWithNavController(navController)
    }

    private fun logOut(context: Context) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }
}
