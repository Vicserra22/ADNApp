package com.adn.adnapp.core.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp

/**
 * Lucide outline icons, adapted from https://github.com/lucide-icons/lucide.
 * ISC license: assets/licenses/lucide.txt. Shared rounded strokes across the navigation.
 */
internal object NavigationIcons {
    val Home by lazy { outline("House",
        "M15 21v-8a1 1 0 0 0-1-1h-4a1 1 0 0 0-1 1v8",
        "M3 10a2 2 0 0 1 .709-1.528l7-6a2 2 0 0 1 2.582 0l7 6A2 2 0 0 1 21 10v9a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"
    ) }
    val Analysis by lazy { outline("TrendMonitor",
        "M8 3H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-3",
        "M6 15l4-5 4 3 7-9",
        "M16 4h5v5",
        "M7 18h2", "M12 18h2"
    ) }
    val Soon by lazy { outline("Sparkles",
        "M11.017 2.814a1 1 0 0 1 1.966 0l1.051 5.558a2 2 0 0 0 1.594 1.594l5.558 1.051a1 1 0 0 1 0 1.966l-5.558 1.051a2 2 0 0 0-1.594 1.594l-1.051 5.558a1 1 0 0 1-1.966 0l-1.051-5.558a2 2 0 0 0-1.594-1.594l-5.558-1.051a1 1 0 0 1 0-1.966l5.558-1.051a2 2 0 0 0 1.594-1.594z",
        "M20 2v4", "M22 4h-4", "M6 20a2 2 0 1 1-4 0a2 2 0 1 1 4 0"
    ) }
    val Settings by lazy { outline("Settings",
        "M9.671 4.136a2.34 2.34 0 0 1 4.659 0 2.34 2.34 0 0 0 3.319 1.915 2.34 2.34 0 0 1 2.33 4.033 2.34 2.34 0 0 0 0 3.831 2.34 2.34 0 0 1-2.33 4.033 2.34 2.34 0 0 0-3.319 1.915 2.34 2.34 0 0 1-4.659 0 2.34 2.34 0 0 0-3.32-1.915 2.34 2.34 0 0 1-2.33-4.033 2.34 2.34 0 0 0 0-3.831A2.34 2.34 0 0 1 6.35 6.051a2.34 2.34 0 0 0 3.319-1.915",
        "M15 12a3 3 0 1 1-6 0a3 3 0 1 1 6 0"
    ) }
    val Fridge by lazy { outline("Refrigerator",
        "M5 3h14v18H5z", "M9 7v3", "M9 14v3", "M15 7v3", "M15 14v3"
    ) }
    val Profile by lazy { outline("Profile",
        "M20 21a8 8 0 0 0-16 0", "M12 13a4 4 0 1 0 0-8a4 4 0 0 0 0 8"
    ) }

    private fun outline(name: String, vararg paths: String): ImageVector {
        val builder = ImageVector.Builder(name, 24.dp, 24.dp, 24f, 24f)
        paths.forEach { path ->
            builder.addPath(
                pathData = PathParser().parsePathString(path).toNodes(),
                fill = null,
                stroke = SolidColor(Color.Black),
                strokeLineWidth = 1.75f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round
            )
        }
        return builder.build()
    }
}

