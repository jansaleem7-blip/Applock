package com.example.ui.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("d MMM", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    fun formatFullDate(millis: Long = System.currentTimeMillis()): String {
        return dateFormat.format(Date(millis))
    }

    fun formatShortDate(millis: Long): String {
        return shortDateFormat.format(Date(millis))
    }

    fun formatTime(millis: Long = System.currentTimeMillis()): String {
        return timeFormat.format(Date(millis))
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "good_morning"
            in 12..16 -> "good_afternoon"
            else -> "good_evening"
        }
    }

    fun isToday(millis: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    fun isThisWeek(millis: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.WEEK_OF_YEAR) == cal2.get(Calendar.WEEK_OF_YEAR)
    }

    fun isThisMonth(millis: Long): Boolean {
        val cal1 = Calendar.getInstance()
        val cal2 = Calendar.getInstance().apply { timeInMillis = millis }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }
}

object DailyQuotes {
    private val quotes = listOf(
        "\"Small daily improvements over time lead to stunning results.\" — Robin Sharma",
        "\"Focus on being productive instead of busy.\" — Tim Ferriss",
        "\"Do what you can, with what you have, where you are.\" — Theodore Roosevelt",
        "\"Action is the foundational key to all success.\" — Pablo Picasso",
        "\"The secret of getting ahead is getting started.\" — Mark Twain",
        "\"Your mind is for having ideas, not holding them.\" — David Allen",
        "\"Simplicity is the ultimate sophistication.\" — Leonardo da Vinci",
        "\"Every day is a fresh beginning; take a deep breath and start again.\"",
        "\"Consistency is the true foundation of trust and mastery.\"",
        "\"Peace of mind begins when you organize your day with purpose.\""
    )

    fun getTodayQuote(): String {
        val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
        return quotes[dayOfYear % quotes.size]
    }
}
