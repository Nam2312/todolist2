package com.example.todolist2.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.todolist2.data.local.dao.FocusSessionDao
import com.example.todolist2.data.local.dao.GroupDao
import com.example.todolist2.data.local.dao.TaskDao
import com.example.todolist2.data.local.dao.TodoListDao
import com.example.todolist2.data.local.entity.FocusSessionEntity
import com.example.todolist2.data.local.entity.GroupEntity
import com.example.todolist2.data.local.entity.GroupMemberEntity
import com.example.todolist2.data.local.entity.TaskEntity
import com.example.todolist2.data.local.entity.TodoListEntity

@Database(
    entities = [
        TaskEntity::class,
        TodoListEntity::class,
        FocusSessionEntity::class,
        GroupEntity::class,
        GroupMemberEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class TodoDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun todoListDao(): TodoListDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun groupDao(): GroupDao
    
    companion object {
        const val DATABASE_NAME = "todo_database"
    }
}









