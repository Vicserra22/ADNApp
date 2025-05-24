package com.example.adnapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        if (currentUser != null) {
            // Usuario ya autenticado, ir directamente a MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Usuario NO autenticado, ir a WelcomeActivity (pantalla login/signup)
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }
}
