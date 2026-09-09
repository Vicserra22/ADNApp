package com.adn.adnapp

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.adn.adnapp.core.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class P5FinanceNavigationTest {
    @get:Rule val compose = createEmptyComposeRule()
    @Test fun financeAreaShowsBalanceGoalsPortfolioAndAnalysis() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { MainScreen({}, {}) } }
            compose.onNodeWithContentDescription("Cambiar área").performClick()
            compose.onNodeWithContentDescription("Abrir Finanzas").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Tu dinero en contexto").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Balanza").assertIsDisplayed()
            compose.onNodeWithText("Hucha y objetivos").performScrollTo().assertIsDisplayed()
            compose.onNodeWithContentDescription("Análisis").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Qué margen te queda").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Qué margen te queda").assertIsDisplayed()
        }
    }
}
