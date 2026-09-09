package com.adn.adnapp.core.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

val LocalFloatingNavigationInset = staticCompositionLocalOf { 0.dp }

class CellShape(private val variant: Int = 0) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val path = Path()
        val phase = variant * .83
        val lobes = if (variant < 4) 5 + variant else 3
        val amplitude = if (variant < 4) .075 else .035
        for (step in 0..180) {
            val angle = step * 2.0 * PI / 180
            val radius = .86 + amplitude * sin(lobes * angle + phase) + .025 * cos(3 * angle - phase)
            val x = (size.width * (.5 + .5 * radius * cos(angle))).toFloat()
            val y = (size.height * (.5 + .5 * radius * sin(angle))).toFloat()
            if (step == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        return Outline.Generic(path)
    }
}

@Composable
fun CellNavigationButton(
    label: String,
    icon: ImageVector,
    variant: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val measurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.labelSmall.copy(
        fontSize = 12.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
    )
    CellSurface(onClick, modifier, variant, selected) {
        BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val captionWidth = maxWidth * .72f
            val availableWidth = with(LocalDensity.current) { captionWidth.toPx() }
            val measuredWidth = measurer.measure(AnnotatedString(label), style).size.width.coerceAtLeast(1)
            val captionSize = 12.sp * minOf(1f, availableWidth * .94f / measuredWidth)
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Icon(icon, null, Modifier.size(23.dp))
                Text(label, modifier = Modifier.width(captionWidth), style = style, fontSize = captionSize, lineHeight = captionSize * 1.2f,
                    maxLines = 1, softWrap = false, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun CellSurface(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: Int = 0,
    selected: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val membrane = MaterialTheme.colorScheme.primary
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = CellShape(variant),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary,
        border = BorderStroke(if (selected) 3.dp else 1.5.dp, MaterialTheme.colorScheme.primary),
        shadowElevation = if (selected) 5.dp else 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (variant >= 4) Canvas(Modifier.matchParentSize()) {
                drawArc(membrane.copy(alpha = .35f), 205f, 42f, false,
                    Offset(size.width * .14f, size.height * .15f),
                    Size(size.width * .72f, size.height * .7f), style = Stroke(1.5.dp.toPx()))
                drawCircle(membrane.copy(alpha = .45f), 2.dp.toPx(), Offset(size.width * .78f, size.height * .75f))
                drawCircle(membrane.copy(alpha = .25f), 1.5.dp.toPx(), Offset(size.width * .73f, size.height * .8f))
            }
            content()
        }
    }
}
