package com.adn.adnapp

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adn.adnapp.core.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class P2AgendaNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun agendaAreaOpensTheDailyWorkspace() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { MainScreen({}, {}) } }
            compose.onNodeWithContentDescription("Cambiar área").performClick()
            compose.onNodeWithContentDescription("Abrir Agenda").performClick()
            compose.waitUntil(10_000) {
                compose.onAllNodesWithText("Tu mesa de trabajo").fetchSemanticsNodes().isNotEmpty()
            }
            compose.onNodeWithText("Tu mesa de trabajo").assertIsDisplayed()
            compose.onNodeWithText("Siguiente acción").assertIsDisplayed()
            compose.onNodeWithText("Añadir a ${java.time.LocalDate.now()}").assertIsDisplayed()
            compose.onNodeWithText("Bandeja").performClick()
            compose.onNodeWithText("Captura rápida").assertIsDisplayed()
        }
    }
}
