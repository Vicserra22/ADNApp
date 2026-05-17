package com.example.adnapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class WelcomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WelcomeScreen(
                    onLoginClick = { startActivity(Intent(this, LoginActivity::class.java)) },
                    onSignupClick = { startActivity(Intent(this, RegisterActivity::class.java)) }
                )
            }
        }
    }
}

@Composable
fun WelcomeScreen(
    onLoginClick: () -> Unit,
    onSignupClick: () -> Unit
) {
    val lightGreen = Color(0xFFE6FFE1)
    val adnDarkGreen = Color(0xFF398F60)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(lightGreen)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.adn),
            color = adnDarkGreen,
            fontSize = 62.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 64.dp)
        )

        Image(
            painter = painterResource(id = R.drawable.adn_logo),
            contentDescription = stringResource(R.string.adn),
            modifier = Modifier
                .size(200.dp)
                .padding(bottom = 32.dp)
        )

        Text(
            text = stringResource(R.string.log_in),
            color = Color.Black,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
                .clickable { onLoginClick() }
        )

        Text(
            text = stringResource(R.string.sign_up),
            color = Color(0xFF4CAF50),
            fontSize = 40.sp,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 120.dp)
                .clickable { onSignupClick() }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    MaterialTheme {
        WelcomeScreen(onLoginClick = {}, onSignupClick = {})
    }
}
