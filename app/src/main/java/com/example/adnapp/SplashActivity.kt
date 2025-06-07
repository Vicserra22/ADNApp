package com.example.adnapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Log.d(TAG, "onCreate - Iniciando Splash")

        val currentUser = auth.currentUser
        Log.d(TAG, "Usuario actual: $currentUser")

        if (currentUser != null) {
            currentUser.getIdToken(true).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Token válido, comprobando datos de usuario")
                    checkUserData(currentUser.uid)
                } else {
                    Log.e(TAG, "Token inválido o error al obtener token", task.exception)
                    auth.signOut()
                    goToWelcome()
                }
            }
        } else {
            Log.d(TAG, "Usuario no autenticado, yendo a pantalla de bienvenida")
            goToWelcome()
        }
    }

    private fun checkUserData(uid: String) {
        Log.d(TAG, "checkUserData - uid: $uid")
        val db = Firebase.firestore
        val userRef = db.collection("usuarios").document(uid)

        userRef.get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Log.d(TAG, "Documento usuario existe: ${document.data}")
                    goToMain()
                } else {
                    Log.d(TAG, "Documento usuario NO existe, ir a registro de datos")
                    startActivity(Intent(this, DataRegistrationActivity::class.java))
                }
                finish()
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error accediendo a Firestore", e)
                goToWelcome()
            }
    }

    private fun goToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun goToWelcome() {
        Log.d(TAG, "Redirigiendo a WelcomeActivity")
        startActivity(Intent(this, WelcomeActivity::class.java))
        finish()
    }
}
