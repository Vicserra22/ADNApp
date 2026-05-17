package com.example.adnapp.ui.dashboard

import com.prolificinteractive.materialcalendarview.CalendarDay

sealed class DashboardIntent {
    data class LoadDataForDate(val date: CalendarDay) : DashboardIntent()
    object LoadHistory : DashboardIntent()
}
