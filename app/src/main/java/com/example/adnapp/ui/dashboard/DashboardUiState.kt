package com.example.adnapp.ui.dashboard

import com.prolificinteractive.materialcalendarview.CalendarDay

data class DashboardUiState(
    val selectedDate: CalendarDay = CalendarDay.today(),
    val consumo: Map<String, Double> = emptyMap(),
    val objetivos: Map<String, Double> = emptyMap(),
    val decoraciones: Map<CalendarDay, Int> = emptyMap(),
    val isLoading: Boolean = false
)
