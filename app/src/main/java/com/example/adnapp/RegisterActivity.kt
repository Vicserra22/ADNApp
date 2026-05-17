package com.example.adnapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()

        setContent {
            MaterialTheme {
                RegisterScreen(
                    onRegisterClick = { email, password ->
                        performRegister(email, password)
                    }
                )
            }
        }
    }

    private fun performRegister(email: String, password: String) {
        if (email.isNotEmpty() && password.length >= 6) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, DataRegistrationActivity::class.java))
                        finish()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(
                this,
                "Introduce un email válido y una contraseña de al menos 6 caracteres",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun RegisterScreen(
    onRegisterClick: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val backgroundLight = Color(0xFFF8FFF6)
    val adnDarkGreen = Color(0xFF3E865D)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(32.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = stringResource(R.string.registrarse),
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(R.string.correo_de_usuario),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 32.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.ingrese_su_correo_de_usuario)) },
            singleLine = true
        )

        Text(
            text = stringResource(R.string.contrase_a),
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(top = 32.dp, bottom = 4.dp)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.ingrese_su_contrase_a)) },
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Button(
            onClick = { onRegisterClick(email, password) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
            colors = ButtonDefaults.buttonColors(containerColor = adnDarkGreen),
            shape = MaterialTheme.shapes.small
        ) {
            Text(
                text = stringResource(R.string.registrarse),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    MaterialTheme {
        RegisterScreen(onRegisterClick = { _, _ -> })
    }
}
