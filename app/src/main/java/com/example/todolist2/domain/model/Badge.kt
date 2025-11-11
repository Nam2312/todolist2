package com.example.todolist2.domain.model

/**
 * Module 3: Gamification Badge system
 */
data class Badge(
    val id: String,
    val name: String,
    val description: String,
    val iconName: String,
    val requirement: Int, // Number required to unlock
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

enum class BadgeType(
    val id: String,
    val displayName: String,
    val description: String,
    val requirement: Int
) {
    FIRST_TASK("first_task", "Khởi đầu", "Hoàn thành task đầu tiên", 1),
    TASK_10("task_10", "Người chăm chỉ", "Hoàn thành 10 tasks", 10),
    TASK_50("task_50", "Chuyên gia", "Hoàn thành 50 tasks", 50),
    TASK_100("task_100", "Bậc thầy", "Hoàn thành 100 tasks", 100),
    
    STREAK_3("streak_3", "3 ngày liên tiếp", "Hoàn thành task 3 ngày liên tục", 3),
    STREAK_7("streak_7", "Tuần hoàn hảo", "Hoàn thành task 7 ngày liên tục", 7),
    STREAK_30("streak_30", "Tháng vàng", "Hoàn thành task 30 ngày liên tục", 30),
    
    FOCUS_10("focus_10", "Tập trung", "Hoàn thành 10 phiên Focus", 10),
    FOCUS_50("focus_50", "Zen Master", "Hoàn thành 50 phiên Focus", 50),
    
    LEVEL_5("level_5", "Cấp 5", "Đạt level 5", 5),
    LEVEL_10("level_10", "Cấp 10", "Đạt level 10", 10),
    LEVEL_20("level_20", "Cấp 20", "Đạt level 20", 20)
}


