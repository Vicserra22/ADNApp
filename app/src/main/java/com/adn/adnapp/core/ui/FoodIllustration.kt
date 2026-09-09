package com.adn.adnapp.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale

enum class FoodIllustration { CART, FRESH, PLATE, FISH, EGG, LEAF, FRUIT, DAIRY, GRAINS, WATER, SUN }

@Composable
fun FoodIllustration(kind: FoodIllustration, modifier: Modifier = Modifier) {
    // The organic cell is always green; the object inside it carries its own
    // visual language so the icon remains recognisable at a glance.
    val defaultInk = MaterialTheme.colorScheme.primary
    val defaultSoft = MaterialTheme.colorScheme.primaryContainer
    val ink = when (kind) {
        FoodIllustration.CART, FoodIllustration.FISH, FoodIllustration.WATER -> Color(0xFF55B8EA)
        FoodIllustration.PLATE -> Color(0xFF7C8490)
        FoodIllustration.EGG -> Color(0xFFB88F56)
        else -> defaultInk
    }
    val soft = when (kind) {
        FoodIllustration.CART, FoodIllustration.FISH -> Color(0xFFD9F2FF)
        FoodIllustration.WATER -> Color(0xFFB9E7FA)
        FoodIllustration.PLATE -> Color(0xFFF4F5F7)
        FoodIllustration.EGG -> Color(0xFFFFE9B6)
        else -> defaultSoft
    }
    val paper = MaterialTheme.colorScheme.surface
    Canvas(modifier) {
        scale(size.width / 100f, size.height / 100f, Offset.Zero) {
            when (kind) {
                FoodIllustration.FISH -> {
                    val tail = Path().apply { moveTo(68f, 50f); lineTo(90f, 27f); lineTo(90f, 74f); close() }
                    drawPath(tail, soft)
                    drawPath(tail, ink, style = Stroke(3f))
                    drawOval(soft, Offset(12f, 26f), Size(63f, 48f))
                    drawOval(ink, Offset(12f, 26f), Size(63f, 48f), style = Stroke(3f))
                    drawCircle(ink, 3f, Offset(29f, 45f))
                    drawArc(ink, -65f, 130f, false, Offset(20f, 33f), Size(23f, 34f), style = Stroke(2f))
                }
                FoodIllustration.EGG -> {
                    val egg = Path().apply {
                        moveTo(50f, 12f)
                        cubicTo(29f, 12f, 18f, 34f, 20f, 57f)
                        cubicTo(22f, 82f, 35f, 91f, 51f, 91f)
                        cubicTo(68f, 91f, 81f, 81f, 80f, 57f)
                        cubicTo(79f, 35f, 69f, 12f, 50f, 12f)
                        close()
                    }
                    drawPath(egg, soft)
                    drawPath(egg, ink, style = Stroke(3f))
                    drawOval(Color(0xFFFFF7DA), Offset(32f, 31f), Size(24f, 19f))
                    drawCircle(Color.White.copy(alpha = .8f), 4f, Offset(39f, 37f))
                }
                FoodIllustration.LEAF -> {
                    val leaf = Path().apply {
                        moveTo(22f, 78f); cubicTo(6f, 33f, 42f, 14f, 83f, 18f)
                        cubicTo(90f, 63f, 59f, 93f, 22f, 78f); close()
                    }
                    drawPath(leaf, soft)
                    drawPath(leaf, ink, style = Stroke(3f))
                    drawLine(ink, Offset(17f, 85f), Offset(72f, 30f), 3f, StrokeCap.Round)
                    listOf(40f, 55f).forEach { x ->
                        drawLine(ink, Offset(x, 100f - x), Offset(x - 3f, 100f - x - 22f), 2f)
                    }
                }
                FoodIllustration.FRUIT -> {
                    drawOval(Color(0xFFD78E7D), Offset(18f, 34f), Size(63f, 52f))
                    drawOval(ink, Offset(18f, 34f), Size(63f, 52f), style = Stroke(3f))
                    drawLine(ink, Offset(48f, 36f), Offset(54f, 18f), 4f, StrokeCap.Round)
                    drawOval(soft, Offset(55f, 14f), Size(24f, 14f))
                    drawArc(Color(0xFFFFE9CF), 160f, 70f, false, Offset(28f, 44f), Size(28f, 29f), style = Stroke(4f, cap = StrokeCap.Round))
                }
                FoodIllustration.DAIRY -> {
                    val glass = Path().apply {
                        moveTo(25f, 24f); lineTo(75f, 24f); lineTo(69f, 83f); lineTo(31f, 83f); close()
                    }
                    drawPath(glass, paper)
                    drawPath(glass, ink, style = Stroke(3f))
                    drawLine(ink.copy(alpha = .35f), Offset(29f, 40f), Offset(71f, 40f), 2f)
                    drawLine(soft, Offset(38f, 47f), Offset(40f, 72f), 5f, StrokeCap.Round)
                }
                FoodIllustration.GRAINS -> {
                    listOf(Offset(25f, 42f), Offset(49f, 25f), Offset(53f, 57f)).forEach { point ->
                        drawOval(Color(0xFFE4C38B), point, Size(23f, 32f))
                        drawOval(ink, point, Size(23f, 32f), style = Stroke(2f))
                        drawLine(ink.copy(alpha = .5f), point + Offset(12f, 8f), point + Offset(11f, 24f), 1.5f)
                    }
                }
                FoodIllustration.CART -> {
                    drawRoundRect(soft, Offset(31f, 26f), Size(17f, 30f), CornerRadius(5f))
                    drawOval(ink.copy(alpha = .65f), Offset(53f, 19f), Size(15f, 25f))
                    val basket = Path().apply {
                        moveTo(20f, 34f); lineTo(85f, 34f); lineTo(77f, 65f)
                        lineTo(28f, 65f); close()
                    }
                    drawPath(basket, paper.copy(alpha = .7f))
                    drawPath(basket, ink, style = Stroke(3.5f))
                    listOf(40f, 55f, 70f).forEach { x -> drawLine(ink, Offset(x, 36f), Offset(x - 2, 63f), 2f) }
                    drawLine(ink, Offset(25f, 49f), Offset(80f, 49f), 2f)
                    val handle = Path().apply {
                        moveTo(8f, 23f); lineTo(17f, 23f); lineTo(30f, 75f); lineTo(76f, 75f)
                    }
                    drawPath(handle, ink, style = Stroke(4f, cap = StrokeCap.Round))
                    drawCircle(ink, 5f, Offset(35f, 85f))
                    drawCircle(ink, 5f, Offset(72f, 85f))
                }
                FoodIllustration.FRESH -> {
                    val steak = Path().apply {
                        moveTo(20f, 30f)
                        cubicTo(30f, 12f, 58f, 12f, 78f, 27f)
                        cubicTo(94f, 38f, 94f, 58f, 80f, 74f)
                        cubicTo(68f, 88f, 49f, 91f, 39f, 79f)
                        cubicTo(31f, 69f, 14f, 72f, 11f, 56f)
                        cubicTo(9f, 47f, 14f, 37f, 20f, 30f)
                        close()
                    }
                    val rind = Color(0xFFFFE5C9)
                    val meatOutline = Color(0xFF914C4C)
                    drawPath(steak, Color(0xFFBE595E))
                    drawPath(steak, meatOutline, style = Stroke(10f))
                    drawPath(steak, rind, style = Stroke(6f))
                    val muscle = Path().apply {
                        moveTo(24f, 33f)
                        cubicTo(38f, 19f, 59f, 23f, 67f, 32f)
                        cubicTo(59f, 41f, 49f, 44f, 39f, 57f)
                        cubicTo(26f, 63f, 15f, 52f, 24f, 33f)
                        close()
                    }
                    drawPath(muscle, Color(0xFFE88480))
                    val bone = Path().apply {
                        moveTo(33f, 29f); cubicTo(46f, 38f, 57f, 42f, 76f, 43f)
                        moveTo(55f, 39f); cubicTo(53f, 52f, 48f, 60f, 47f, 75f)
                    }
                    drawPath(bone, meatOutline.copy(alpha = .35f), style = Stroke(8f, cap = StrokeCap.Round))
                    drawPath(bone, rind, style = Stroke(5f, cap = StrokeCap.Round))
                    val marbling = Path().apply {
                        moveTo(25f, 43f); quadraticTo(31f, 46f, 34f, 53f)
                        moveTo(37f, 58f); quadraticTo(40f, 53f, 44f, 51f)
                        moveTo(66f, 53f); quadraticTo(70f, 61f, 65f, 70f)
                        moveTo(59f, 68f); lineTo(65f, 63f)
                    }
                    drawPath(marbling, Color(0xFFFFB8A4), style = Stroke(2f, cap = StrokeCap.Round))
                }
                FoodIllustration.PLATE -> {
                    drawCircle(soft, 33f, Offset(52f, 51f))
                    drawCircle(ink, 33f, Offset(52f, 51f), style = Stroke(3f))
                    drawCircle(paper, 25f, Offset(52f, 51f))
                    drawCircle(ink.copy(alpha = .35f), 25f, Offset(52f, 51f), style = Stroke(1.5f))
                    drawLine(ink, Offset(10f, 23f), Offset(10f, 81f), 3.5f, StrokeCap.Round)
                    drawLine(ink, Offset(5f, 22f), Offset(5f, 37f), 2.5f, StrokeCap.Round)
                    drawLine(ink, Offset(15f, 22f), Offset(15f, 37f), 2.5f, StrokeCap.Round)
                    drawLine(ink, Offset(5f, 37f), Offset(15f, 37f), 3f, StrokeCap.Round)
                    drawLine(ink, Offset(93f, 22f), Offset(93f, 81f), 3.5f, StrokeCap.Round)
                    drawRoundRect(ink, Offset(87f, 22f), Size(7f, 27f), CornerRadius(3f))
                }
                FoodIllustration.WATER -> {
                    val drop = Path().apply {
                        moveTo(50f, 12f)
                        cubicTo(36f, 31f, 23f, 45f, 23f, 62f)
                        cubicTo(23f, 80f, 35f, 90f, 50f, 90f)
                        cubicTo(65f, 90f, 77f, 80f, 77f, 62f)
                        cubicTo(77f, 45f, 64f, 31f, 50f, 12f)
                        close()
                    }
                    drawPath(drop, soft)
                    drawPath(drop, ink, style = Stroke(3f))
                    drawArc(paper.copy(alpha = .75f), 120f, 55f, false, Offset(35f, 42f), Size(22f, 27f), style = Stroke(4f, cap = StrokeCap.Round))
                }
                FoodIllustration.SUN -> {
                    val center = Offset(50f, 50f)
                    drawCircle(Color(0xFFF3B63F), 22f, center)
                    drawCircle(ink, 22f, center, style = Stroke(3f))
                    repeat(10) { ray ->
                        val angle = Math.toRadians((ray * 36).toDouble())
                        val inner = 31f
                        val outer = 43f
                        drawLine(ink, center + Offset((kotlin.math.cos(angle) * inner).toFloat(), (kotlin.math.sin(angle) * inner).toFloat()), center + Offset((kotlin.math.cos(angle) * outer).toFloat(), (kotlin.math.sin(angle) * outer).toFloat()), 3f, StrokeCap.Round)
                    }
                    drawCircle(Color(0xFFFFE9CF), 5f, Offset(43f, 43f))
                }
            }
        }
    }
}
