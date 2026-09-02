package com.adn.adnapp.feature.dashboard

import com.adn.adnapp.data.model.entity.DailyConsumption
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

enum class ProgressPeriod { WEEK, MONTH }

data class ProgressChartPoint(
    val label: String,
    val value: Float,
    val hasData: Boolean,
    val startDate: LocalDate,
    val endDate: LocalDate = startDate
)

data class ProgressChartModel(
    val period: ProgressPeriod,
    val points: List<ProgressChartPoint>,
    val average: Float,
    val best: Float,
    val trend: Float,
    val trackedDays: Int
)

/** Pure data preparation used by the dashboard charts and their unit tests. */
object ProgressChartData {
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun weekly(
        history: Map<String, DailyConsumption>,
        endDate: LocalDate = LocalDate.now(),
        locale: Locale = Locale.getDefault(),
        scoreOf: (DailyConsumption) -> Double
    ): ProgressChartModel {
        val start = endDate.minusDays(6)
        val points = (0L..6L).map { offset ->
            val date = start.plusDays(offset)
            val consumption = history[date.format(dateFormatter)]
            ProgressChartPoint(
                label = date.dayOfWeek.getDisplayName(TextStyle.NARROW, locale)
                    .uppercase(locale),
                value = consumption?.let(scoreOf).toPercentage(),
                hasData = consumption != null,
                startDate = date
            )
        }
        return model(ProgressPeriod.WEEK, points)
    }

    fun monthly(
        history: Map<String, DailyConsumption>,
        endDate: LocalDate = LocalDate.now(),
        scoreOf: (DailyConsumption) -> Double
    ): ProgressChartModel {
        val start = endDate.minusDays(29)
        val points = (0 until 5).map { bucket ->
            val bucketStart = start.plusDays(bucket * 6L)
            val bucketEnd = if (bucket == 4) endDate else bucketStart.plusDays(5)
            val values = datesBetween(bucketStart, bucketEnd).mapNotNull { date ->
                history[date.format(dateFormatter)]?.let(scoreOf)?.toPercentage()
            }
            ProgressChartPoint(
                label = "S${bucket + 1}",
                value = values.averageOrZero(),
                hasData = values.isNotEmpty(),
                startDate = bucketStart,
                endDate = bucketEnd
            )
        }
        val dailyValues = datesBetween(start, endDate).mapNotNull { date ->
            history[date.format(dateFormatter)]?.let(scoreOf)?.toPercentage()
        }
        return model(ProgressPeriod.MONTH, points, dailyValues)
    }

    private fun model(
        period: ProgressPeriod,
        points: List<ProgressChartPoint>,
        summaryValues: List<Float> = points.filter { it.hasData }.map { it.value }
    ): ProgressChartModel {
        val firstHalf = summaryValues.take(summaryValues.size / 2).averageOrZero()
        val secondHalf = summaryValues.drop(summaryValues.size / 2).averageOrZero()
        val trend = if (summaryValues.size < 2) 0f else secondHalf - firstHalf
        return ProgressChartModel(
            period = period,
            points = points,
            average = summaryValues.averageOrZero(),
            best = summaryValues.maxOrNull() ?: 0f,
            trend = trend,
            trackedDays = summaryValues.size
        )
    }

    private fun datesBetween(start: LocalDate, end: LocalDate): List<LocalDate> =
        generateSequence(start) { date -> date.plusDays(1) }
            .takeWhile { date -> !date.isAfter(end) }
            .toList()

    private fun Double?.toPercentage(): Float =
        ((this ?: 0.0) * 100.0).coerceIn(0.0, 100.0).toFloat()

    private fun List<Float>.averageOrZero(): Float =
        if (isEmpty()) 0f else average().toFloat()
}
