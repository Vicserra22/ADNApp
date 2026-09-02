package com.adn.adnapp.data.local

import android.content.Context
import com.adn.adnapp.domain.model.AppArea

class DashboardPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun getVisibleAreas(): Set<AppArea> {
        val storedIds = preferences.getStringSet(VISIBLE_AREAS_KEY, null)
            ?: return AppArea.entries.toSet()
        return storedIds.mapNotNull(AppArea::fromId).toSet().ifEmpty { setOf(AppArea.NUTRITION) }
    }

    fun setVisibleAreas(areas: Set<AppArea>) {
        preferences.edit().putStringSet(VISIBLE_AREAS_KEY, areas.map { it.id }.toSet()).apply()
    }

    private companion object {
        const val PREFERENCES_NAME = "dashboard_preferences"
        const val VISIBLE_AREAS_KEY = "visible_areas"
    }
}
