package com.adn.adnapp

import androidx.activity.compose.setContent
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.adn.adnapp.core.ui.MainScreen
import org.junit.Rule
import org.junit.Test
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.text.TextLayoutResult
import org.junit.runner.RunWith
import java.io.File
import android.graphics.Bitmap

@RunWith(AndroidJUnit4::class)
class P0NavigationTest {
    @get:Rule val compose = createEmptyComposeRule()

    @Test fun homeRoutesAndCenteredDnaRemainAccessible() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity -> activity.setContent { MainScreen({}, {}) } }
            compose.onNodeWithText("Súper").assertIsDisplayed()
            compose.onNodeWithText("Mercadillo").assertIsDisplayed()
            compose.onNodeWithText("Lo mejor de la casa").assertIsDisplayed()
            compose.onNodeWithContentDescription("Cambiar área").assertIsDisplayed()
            val dna = compose.onNodeWithContentDescription("Cambiar área").fetchSemanticsNode().boundsInRoot
            val home = compose.onNodeWithContentDescription("Home").fetchSemanticsNode().boundsInRoot
            val root = compose.onRoot().fetchSemanticsNode().boundsInRoot
            assertEquals(root.center.x, dna.center.x, 1f)
            assertEquals(home.center.y, dna.center.y, 1f)
            capture("p0-home")
            listOf("Home", "Análisis", "Más", "Ajustes").forEach { label ->
                val layouts = mutableListOf<TextLayoutResult>()
                compose.onNodeWithText(label, useUnmergedTree = true).performSemanticsAction(SemanticsActions.GetTextLayoutResult) { it(layouts) }
                val layout = layouts.single()
                assertFalse("Etiqueta recortada: $label size=${layout.size} paragraph=${layout.multiParagraph.width}x${layout.multiParagraph.height} constraints=${layout.layoutInput.constraints}", layout.hasVisualOverflow)
            }
            capture("p0-home")
            compose.onNodeWithContentDescription("Abrir Mercadillo").performClick()
            compose.waitUntil(10_000) { compose.onAllNodesWithText("Filtrar alimento fresco").fetchSemanticsNodes().isNotEmpty() }
            compose.onNodeWithText("Filtrar alimento fresco").assertExists()
            compose.waitUntil(10_000) { compose.onAllNodesWithContentDescription("Categoría Carnes").fetchSemanticsNodes().isNotEmpty() }
            capture("p0-mercadillo")
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithContentDescription("Abrir Lo mejor de la casa").performClick()
            compose.onNodeWithText("Nombre del plato").assertExists()
            capture("p0-plato")
            compose.onNodeWithContentDescription("Volver").performClick()
            compose.onNodeWithContentDescription("Abrir Súper").performClick()
            compose.onNodeWithText("Producto o marca").assertExists()
            capture("p0-super")
            compose.onNodeWithContentDescription("Home").performClick()
            compose.onNodeWithContentDescription("Cambiar área").performClick()
            compose.onNodeWithText("Elige un área").assertIsDisplayed()
        }
    }

    private fun capture(name: String) {
        compose.waitForIdle()
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        instrumentation.uiAutomation.waitForIdle(400, 5_000)
        Thread.sleep(350)
        val screenshot = compose.onRoot().captureToImage().asAndroidBitmap()
        val file = File(instrumentation.targetContext.getExternalFilesDir(null), "$name.png")
        file.outputStream().use { screenshot.compress(Bitmap.CompressFormat.PNG, 100, it) }
        screenshot.recycle()
    }
}
