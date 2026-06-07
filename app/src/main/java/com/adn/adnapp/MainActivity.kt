package com.adn.adnapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.adn.adnapp.core.navigation.AdnNavGraph
import com.adn.adnapp.core.theme.AdnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdnTheme {
                val navController = rememberNavController()
                AdnNavGraph(navController = navController)
            }
        }
    }
}
