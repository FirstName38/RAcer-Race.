package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class WeekCalculationInfo(
    val dayDateUpperStr: String, // e.g. "MONDAY 26 OCTOBER · WEEK 43"
    val currentWeekNumber: Int,  // e.g. 43
    val totalWeeksInYear: Int,   // e.g. 52 or 53
    val weeksRemainingInYear: Int, // e.g. 10
    val currentYear: Int,        // e.g. 2026
    val summaryText: String,     // e.g. "Today is week 43 of 53 · 10 weeks left in 2026"
    val subtitleText: String     // e.g. "10 weeks left this year. One block at a time."
)

object WeekCalculationHelper {

    const val RULE_WEEK_CONTAINING_JAN_1 = "week_containing_jan_1"
    const val RULE_FIRST_FULL_DAY = "first_full_day"

    fun getDayOfWeekInt(dayName: String): Int {
        return when (dayName.trim().uppercase(Locale.getDefault())) {
            "SUNDAY" -> Calendar.SUNDAY
            "MONDAY" -> Calendar.MONDAY
            "TUESDAY" -> Calendar.TUESDAY
            "WEDNESDAY" -> Calendar.WEDNESDAY
            "THURSDAY" -> Calendar.THURSDAY
            "FRIDAY" -> Calendar.FRIDAY
            "SATURDAY" -> Calendar.SATURDAY
            else -> Calendar.WEDNESDAY // Matches screenshot default Wednesday or user preference
        }
    }

    fun getDayDisplayName(dayName: String): String {
        return when (dayName.trim().uppercase(Locale.getDefault())) {
            "SUNDAY" -> "Sunday"
            "MONDAY" -> "Monday"
            "TUESDAY" -> "Tuesday"
            "WEDNESDAY" -> "Wednesday"
            "THURSDAY" -> "Thursday"
            "FRIDAY" -> "Friday"
            "SATURDAY" -> "Saturday"
            else -> "Wednesday"
        }
    }

    fun calculateWeekInfo(
        targetDateMillis: Long = System.currentTimeMillis(),
        weekStartDay: String = "WEDNESDAY",
        week1BeginsRule: String = RULE_WEEK_CONTAINING_JAN_1
    ): WeekCalculationInfo {
        val firstDayInt = getDayOfWeekInt(weekStartDay)
        val dayDisplay = getDayDisplayName(weekStartDay)

        val cal = Calendar.getInstance().apply {
            timeInMillis = targetDateMillis
            firstDayOfWeek = firstDayInt
            minimalDaysInFirstWeek = if (week1BeginsRule == RULE_FIRST_FULL_DAY) 7 else 1
        }

        val year = cal.get(Calendar.YEAR)
        var weekNumber = cal.get(Calendar.WEEK_OF_YEAR)
        val totalWeeks = cal.getActualMaximum(Calendar.WEEK_OF_YEAR).coerceAtLeast(52)

        // Ensure weekNumber is bounded reasonably
        if (weekNumber <= 0) weekNumber = 1
        if (weekNumber > totalWeeks) weekNumber = totalWeeks

        val weeksRemaining = (totalWeeks - weekNumber).coerceAtLeast(0)

        val dayDateFormat = SimpleDateFormat("EEEE d MMMM", Locale.ENGLISH)
        val rawDateStr = dayDateFormat.format(Date(targetDateMillis)).uppercase(Locale.ENGLISH)
        val dayDateUpperStr = "$rawDateStr · WEEK $weekNumber"

        val summaryText = "Today is week $weekNumber of $totalWeeks · $weeksRemaining weeks left in $year"
        val subtitleText = "$weeksRemaining weeks left this year. One block at a time."

        return WeekCalculationInfo(
            dayDateUpperStr = dayDateUpperStr,
            currentWeekNumber = weekNumber,
            totalWeeksInYear = totalWeeks,
            weeksRemainingInYear = weeksRemaining,
            currentYear = year,
            summaryText = summaryText,
            subtitleText = subtitleText
        )
    }
}
