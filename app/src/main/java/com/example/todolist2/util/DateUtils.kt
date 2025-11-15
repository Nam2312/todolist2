package com.example.todolist2.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    
    fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    fun formatTime(timestamp: Long, pattern: String = "HH:mm"): String {
        val date = Date(timestamp)
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(date)
    }
    
    fun formatDateTime(timestamp: Long): String {
        return formatDate(timestamp, "dd/MM/yyyy HH:mm")
    }
    
    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Vừa xong"
            diff < 3600_000 -> "${diff / 60_000} phút trước"
            diff < 86400_000 -> "${diff / 3600_000} giờ trước"
            diff < 604800_000 -> "${diff / 86400_000} ngày trước"
            else -> formatDate(timestamp)
        }
    }
    
    fun isToday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        val today = calendar.get(Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        
        return today == dateDay
    }
    
    fun isTomorrow(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val tomorrow = calendar.get(Calendar.DAY_OF_YEAR)
        
        calendar.timeInMillis = timestamp
        val dateDay = calendar.get(Calendar.DAY_OF_YEAR)
        
        return tomorrow == dateDay
    }
    
    fun getStartOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }
    
    fun getEndOfDay(timestamp: Long = System.currentTimeMillis()): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
    
    fun formatDate(calendar: Calendar, pattern: String = "yyyy-MM-dd"): String {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        return formatter.format(calendar.time)
    }
    
    fun parseDate(dateString: String, pattern: String = "yyyy-MM-dd"): Calendar {
        val formatter = SimpleDateFormat(pattern, Locale.getDefault())
        val date = formatter.parse(dateString) ?: Date()
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar
    }
    
    /**
     * Get the start of week (Monday) for a given date
     */
    fun getStartOfWeek(calendar: Calendar = Calendar.getInstance()): Calendar {
        val cal = calendar.clone() as Calendar
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        // Convert to Monday = 1, Sunday = 7
        val daysFromMonday = if (dayOfWeek == Calendar.SUNDAY) 6 else dayOfWeek - Calendar.MONDAY
        cal.add(Calendar.DAY_OF_MONTH, -daysFromMonday)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal
    }
    
    /**
     * Get week label (e.g., "Tuần 1: 01/01 - 07/01")
     */
    fun getWeekLabel(weekStart: Calendar): String {
        val startDate = formatDate(weekStart.timeInMillis, "dd/MM")
        val weekEnd = weekStart.clone() as Calendar
        weekEnd.add(Calendar.DAY_OF_MONTH, 6)
        val endDate = formatDate(weekEnd.timeInMillis, "dd/MM")
        return "$startDate - $endDate"
    }
}





