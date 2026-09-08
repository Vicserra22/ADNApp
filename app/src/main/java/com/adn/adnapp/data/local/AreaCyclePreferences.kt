package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.AppArea

class AreaCyclePreferences(context: Context) {
    private val prefs = context.getSharedPreferences("area_navigation", Context.MODE_PRIVATE)
    fun load(): List<AppArea> = prefs.getString("cycle", null)?.split(",")
        ?.mapNotNull(AppArea::fromId)?.distinct()?.takeIf { it.size >= 2 } ?: defaultAreaCycle
    fun save(areas: List<AppArea>) {
        require(areas.distinct().size >= 2)
        prefs.edit().putString("cycle", areas.distinct().joinToString(",") { it.id }).apply()
    }
}

val defaultAreaCycle = listOf(
    AppArea.NUTRITION, AppArea.SPORTS, AppArea.FINANCE, AppArea.PHILOSOPHY, AppArea.AGENDA
)

fun nextArea(current: AppArea, cycle: List<AppArea>): AppArea {
    if (cycle.isEmpty()) return current
    val index = cycle.indexOf(current)
    return cycle[(index + 1) % cycle.size]
}

