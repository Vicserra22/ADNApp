package com.example.adnapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Aquí tienes tu splash visual

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Fuerza refrescar token para verificar que el usuario sigue válido
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Usuario válido -> MainActivity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                } else {
                    // Token inválido o error -> cerrar sesión y Welcome
                    auth.signOut()
                    startActivity(Intent(this, WelcomeActivity::class.java))
                    finish()
                }
            }
        } else {
            // No hay usuario -> WelcomeActivity
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
}
