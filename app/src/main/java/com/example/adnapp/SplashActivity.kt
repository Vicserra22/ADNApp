package com.example.adnapp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {

    private val auth by lazy { FirebaseAuth.getInstance() }
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                SplashScreen()
            }
        }

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

@Composable
fun SplashScreen() {
    val lightGreen = Color(0xFFC8E6C9) // Using adn_light_green equivalent from colors.xml

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightGreen),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.adn_logo),
                contentDescription = stringResource(R.string.app_name),
                modifier = Modifier.size(200.dp)
            )

            Text(
                text = stringResource(R.string.adn),
                color = Color.Black,
                fontSize = 40.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = stringResource(R.string.splash_subtitle),
                color = Color.Black,
                fontSize = 22.sp,
                modifier = Modifier.padding(top = 48.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen()
    }
}
