package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

    fun formatTime(timestamp: Long): String {
        return try {
            timeFormat.format(Date(timestamp))
        } catch (e: Exception) {
            "12:00 PM"
        }
    }

    fun formatDate(timestamp: Long): String {
        return try {
            dateFormat.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }
}
