package com.adn.adnapp.core.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/** Floating back control used by secondary screens. */
@Composable
fun OrganicBackButton(onBack: () -> Unit, modifier: Modifier = Modifier) {
    CellSurface(
        onClick = onBack,
        modifier = modifier.size(58.dp).semantics {
            contentDescription = "Volver"
            role = Role.Button
        },
        variant = 3,
        selected = true
    ) {
        Box(contentAlignment = Alignment.Center) {
            val c = MaterialTheme.colorScheme.primary
            Canvas(Modifier.size(30.dp)) {
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(size.width * .82f, size.height * .5f)
                    lineTo(size.width * .22f, size.height * .5f)
                    lineTo(size.width * .46f, size.height * .25f)
                    moveTo(size.width * .22f, size.height * .5f)
                    lineTo(size.width * .46f, size.height * .76f)
                }
                drawPath(path, c, style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round
                ))
            }
        }
    }
}
