package com.adn.adnapp.feature.dashboard

import com.adn.adnapp.data.model.entity.DailyConsumption
import java.time.LocalDate
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProgressChartDataTest {
    private val endDate = LocalDate.of(2026, 9, 2)
    private val score: (DailyConsumption) -> Double = { it.calories / 2_000.0 }

    @Test
    fun weekly_keepsSevenCalendarSlotsAndMarksMissingDays() {
        val history = mapOf(
            "2026-08-27" to day("2026-08-27", 1_000.0),
            "2026-09-02" to day("2026-09-02", 2_000.0)
        )

        val result = ProgressChartData.weekly(history, endDate, Locale.ENGLISH, score)

        assertEquals(7, result.points.size)
        assertEquals(LocalDate.of(2026, 8, 27), result.points.first().startDate)
        assertEquals(50f, result.points.first().value, 0f)
        assertFalse(result.points[1].hasData)
        assertTrue(result.points.last().hasData)
        assertEquals(2, result.trackedDays)
        assertEquals(75f, result.average, 0f)
    }

    @Test
    fun monthly_aggregatesThirtyDaysIntoFiveSixDayBuckets() {
        val history = buildMap {
            (0L until 30L).forEach { offset ->
                val date = endDate.minusDays(29).plusDays(offset)
                val calories = if (offset < 6) 1_000.0 else 2_000.0
                put(date.toString(), day(date.toString(), calories))
            }
        }

        val result = ProgressChartData.monthly(history, endDate, score)

        assertEquals(5, result.points.size)
        assertEquals(50f, result.points.first().value, 0f)
        assertEquals(100f, result.points.last().value, 0f)
        assertEquals(30, result.trackedDays)
        assertTrue(result.trend > 0f)
    }

    @Test
    fun valuesAreClampedAndEmptyHistoryDoesNotProduceNan() {
        val clamped = ProgressChartData.weekly(
            mapOf("2026-09-02" to day("2026-09-02", 4_000.0)), endDate, Locale.ENGLISH, score
        )
        val empty = ProgressChartData.monthly(emptyMap(), endDate, score)

        assertEquals(100f, clamped.points.last().value, 0f)
        assertEquals(0f, empty.average, 0f)
        assertEquals(0f, empty.trend, 0f)
        assertEquals(0, empty.trackedDays)
    }

    private fun day(date: String, calories: Double) = DailyConsumption(date = date, calories = calories)
}
