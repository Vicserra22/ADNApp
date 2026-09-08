package com.adn.adnapp.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import com.adn.adnapp.domain.model.AppArea

/** Shared line icons, drawn in the area's foreground colour at any density. */
@Composable
internal fun AreaIcon(area: AppArea, color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val unit = size.minDimension / 48f
        fun p(x: Float, y: Float) = Offset(x * unit, y * unit)
        fun line(x: Float, y: Float, x2: Float, y2: Float) =
            drawLine(color, p(x, y), p(x2, y2), 2.5f * unit, StrokeCap.Round)
        fun box(x: Float, y: Float, w: Float, h: Float) =
            drawRoundRect(color, p(x, y), Size(w * unit, h * unit),
                CornerRadius(3 * unit), style = Stroke(2.5f * unit))
        when (area) {
            AppArea.AGENDA -> {
                box(6f, 9f, 36f, 33f)
                line(6f, 19f, 42f, 19f)
                line(15f, 5f, 15f, 13f)
                line(33f, 5f, 33f, 13f)
                listOf(15f, 24f, 33f).forEach { x ->
                    listOf(27f, 35f).forEach { y -> drawCircle(color, 1.8f * unit, p(x, y)) }
                }
            }
            AppArea.NUTRITION -> {
                val leaf = Path().apply {
                    moveTo(9 * unit, 36 * unit)
                    cubicTo(3 * unit, 14 * unit, 28 * unit, 7 * unit, 40 * unit, 7 * unit)
                    cubicTo(43 * unit, 28 * unit, 30 * unit, 43 * unit, 9 * unit, 36 * unit)
                    close()
                }
                drawPath(leaf, color, style = Stroke(2.5f * unit))
                line(6f, 43f, 32f, 16f)
                line(20f, 29f, 19f, 19f)
                line(20f, 29f, 30f, 29f)
            }
            AppArea.SPORTS -> {
                line(15f, 24f, 33f, 24f)
                box(8f, 12f, 7f, 24f)
                box(33f, 12f, 7f, 24f)
                line(4f, 19f, 4f, 29f)
                line(44f, 19f, 44f, 29f)
            }
            AppArea.FINANCE -> {
                box(5f, 26f, 8f, 15f)
                box(20f, 18f, 8f, 23f)
                box(35f, 8f, 8f, 33f)
                line(6f, 16f, 21f, 7f)
                line(15f, 6f, 21f, 7f)
                line(21f, 7f, 19f, 13f)
            }
            AppArea.PHILOSOPHY -> {
                val book = Path().apply {
                    moveTo(24 * unit, 12 * unit)
                    quadraticTo(14 * unit, 6 * unit, 5 * unit, 10 * unit)
                    lineTo(5 * unit, 37 * unit)
                    quadraticTo(14 * unit, 33 * unit, 24 * unit, 40 * unit)
                    quadraticTo(34 * unit, 33 * unit, 43 * unit, 37 * unit)
                    lineTo(43 * unit, 10 * unit)
                    quadraticTo(34 * unit, 6 * unit, 24 * unit, 12 * unit)
                }
                drawPath(book, color, style = Stroke(2.5f * unit))
                line(24f, 12f, 24f, 40f)
                line(11f, 18f, 18f, 20f)
                line(30f, 20f, 37f, 18f)
                line(11f, 25f, 18f, 27f)
                line(30f, 27f, 37f, 25f)
            }
        }
    }
}

