package com.adn.adnapp

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adn.adnapp.core.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class P1NavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun wellnessAndFridgeDestinationsOpenFromNutritionHome() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { MainScreen({}, {}) } }
            compose.onNodeWithContentDescription("Abrir Agua").performScrollTo().assertIsDisplayed().performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Rituales diarios").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Rituales diarios").assertExists()
            compose.onNodeWithText("Toca la botella para añadir 250 ml").assertExists()
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithText("Abrir nevera").performScrollTo().performClick()
            compose.onNodeWithText("La nevera").assertIsDisplayed()
            compose.onNodeWithText("Tu enciclopedia personal de alimentos guardados y usados.").assertIsDisplayed()
        }
    }
}
