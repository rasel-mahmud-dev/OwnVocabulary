package com.rs.ownvocabulary.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object DateFormatter {

    fun formatToHumanDate(millis: Long, pattern: String = "MMM dd, yyyy"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(millis))
    }

    fun formatToHumanDateTime(millis: Long, pattern: String = "MMM dd, yyyy 'at' hh:mm a"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(millis))
    }


    fun formatToRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - millis
        val absDiff = abs(diff)

        return when {
            absDiff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            absDiff < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(absDiff)
                if (diff > 0) "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
                else "in $minutes ${if (minutes == 1L) "minute" else "minutes"}"
            }
            absDiff < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(absDiff)
                if (diff > 0) "$hours ${if (hours == 1L) "hour" else "hours"} ago"
                else "in $hours ${if (hours == 1L) "hour" else "hours"}"
            }
            absDiff < TimeUnit.DAYS.toMillis(7) -> {
                val days = TimeUnit.MILLISECONDS.toDays(absDiff)
                if (diff > 0) "$days ${if (days == 1L) "day" else "days"} ago"
                else "in $days ${if (days == 1L) "day" else "days"}"
            }
            absDiff < TimeUnit.DAYS.toMillis(30) -> {
                val weeks = TimeUnit.MILLISECONDS.toDays(absDiff) / 7
                if (diff > 0) "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
                else "in $weeks ${if (weeks == 1L) "week" else "weeks"}"
            }
            absDiff < TimeUnit.DAYS.toMillis(365) -> {
                val months = TimeUnit.MILLISECONDS.toDays(absDiff) / 30
                if (diff > 0) "$months ${if (months == 1L) "month" else "months"} ago"
                else "in $months ${if (months == 1L) "month" else "months"}"
            }
            else -> {
                val years = TimeUnit.MILLISECONDS.toDays(absDiff) / 365
                if (diff > 0) "$years ${if (years == 1L) "year" else "years"} ago"
                else "in $years ${if (years == 1L) "year" else "years"}"
            }
        }
    }


    fun formatSmart(millis: Long, now: Long = System.currentTimeMillis()): String {
        val diff = abs(now - millis)

        return when {
            diff < TimeUnit.DAYS.toMillis(7) -> formatToRelativeTime(millis, now)
            diff < TimeUnit.DAYS.toMillis(365) -> formatToHumanDate(millis, "MMM dd")
            else -> formatToHumanDate(millis, "MMM dd, yyyy")
        }
    }


    fun formatTimeOnly(millis: Long, is24Hour: Boolean = false): String {
        val pattern = if (is24Hour) "HH:mm" else "hh:mm a"
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(Date(millis))
    }


    fun formatChatTime(millis: Long, now: Long = System.currentTimeMillis()): String {
        val calendar = Calendar.getInstance()
        val messageCalendar = Calendar.getInstance().apply { timeInMillis = millis }

        return when {
            calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) == messageCalendar.get(Calendar.DAY_OF_YEAR) -> {
                formatTimeOnly(millis)
            }
            calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) &&
                    calendar.get(Calendar.DAY_OF_YEAR) - messageCalendar.get(Calendar.DAY_OF_YEAR) == 1 -> {
                "Yesterday"
            }
            abs(now - millis) < TimeUnit.DAYS.toMillis(7) -> {
                formatToHumanDate(millis, "EEEE")
            }
            calendar.get(Calendar.YEAR) == messageCalendar.get(Calendar.YEAR) -> {
                formatToHumanDate(millis, "MMM dd")
            }
            else -> formatToHumanDate(millis, "MMM dd, yyyy")
        }
    }

    object Patterns {
        const val FULL_DATE = "EEEE, MMMM dd, yyyy"
        const val SHORT_DATE = "MM/dd/yyyy"
        const val ISO_DATE = "yyyy-MM-dd"
        const val MONTH_DAY = "MMM dd"
        const val MONTH_YEAR = "MMM yyyy"
        const val TIME_12 = "hh:mm a"
        const val TIME_24 = "HH:mm"
        const val FULL_DATETIME = "MMM dd, yyyy 'at' hh:mm a"
        const val ISO_DATETIME = "yyyy-MM-dd HH:mm:ss"
    }
}