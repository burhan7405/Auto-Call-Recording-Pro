package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatDuration(seconds: Int): String {
        val s = if (seconds < 0) 0 else seconds
        val hours = s / 3600
        val minutes = (s % 3600) / 60
        val remainingSeconds = s % 60

        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, remainingSeconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, remainingSeconds)
        }
    }

    fun formatDurationMs(millis: Long): String {
        return formatDuration((millis / 1000).toInt())
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format(Locale.getDefault(), "%.1f MB", mb)
            else -> String.format(Locale.getDefault(), "%.0f KB", kb)
        }
    }

    fun formatTimestamp(timestampMs: Long): String {
        val now = Calendar.getInstance()
        val recordingTime = Calendar.getInstance().apply { timeInMillis = timestampMs }

        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        val formattedTime = timeFormat.format(Date(timestampMs))

        val isToday = now.get(Calendar.YEAR) == recordingTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == recordingTime.get(Calendar.DAY_OF_YEAR)

        val isYesterday = now.get(Calendar.YEAR) == recordingTime.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) - recordingTime.get(Calendar.DAY_OF_YEAR) == 1

        return when {
            isToday -> "Today, $formattedTime"
            isYesterday -> "Yesterday, $formattedTime"
            now.get(Calendar.YEAR) == recordingTime.get(Calendar.YEAR) -> {
                val dateFormat = SimpleDateFormat("MMM d, ", Locale.getDefault())
                dateFormat.format(Date(timestampMs)) + formattedTime
            }
            else -> {
                val fullDateFormat = SimpleDateFormat("MMM d, yyyy, ", Locale.getDefault())
                fullDateFormat.format(Date(timestampMs)) + formattedTime
            }
        }
    }
}
