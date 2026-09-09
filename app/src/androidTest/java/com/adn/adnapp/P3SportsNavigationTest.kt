package com.adn.adnapp

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adn.adnapp.core.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class P3SportsNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun sportsAreaShowsAdaptiveSessionFormAndAnalysis() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { MainScreen({}, {}) } }
            compose.onNodeWithContentDescription("Cambiar área").performClick()
            compose.onNodeWithContentDescription("Abrir Deporte").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Tu movimiento").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Tu movimiento").assertIsDisplayed()
            compose.onNodeWithText("Fuerza").performClick()
            compose.onNodeWithText("Series").assertIsDisplayed()
            compose.onNodeWithText("Repeticiones").assertIsDisplayed()
            compose.onNodeWithContentDescription("Análisis").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Tendencias de correr").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Tendencias de correr").assertIsDisplayed()
        }
    }
}
