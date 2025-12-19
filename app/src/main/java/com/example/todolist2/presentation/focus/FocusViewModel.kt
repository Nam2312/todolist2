package com.example.todolist2.presentation.focus

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todolist2.domain.model.FocusDuration
import com.example.todolist2.domain.model.FocusSession
import com.example.todolist2.domain.model.Task
import com.example.todolist2.domain.repository.AuthRepository
import com.example.todolist2.domain.repository.FocusSessionRepository
import com.example.todolist2.domain.repository.GamificationRepository
import com.example.todolist2.domain.repository.TaskRepository
import com.example.todolist2.util.BackgroundMusicManager
import com.example.todolist2.util.Constants
import com.example.todolist2.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class FocusState(
    val tasks: List<Task> = emptyList(),
    val selectedTask: Task? = null,
    val selectedDuration: FocusDuration = FocusDuration.POMODORO,
    val isTimerRunning: Boolean = false,
    val isTimerPaused: Boolean = false,
    val remainingSeconds: Int = 0,
    val totalSeconds: Int = 0,
    val currentSession: FocusSession? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isBackgroundMusicEnabled: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val focusSessionRepository: FocusSessionRepository,
    private val authRepository: AuthRepository,
    private val gamificationRepository: GamificationRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {
    
    private val _state = MutableStateFlow(FocusState())
    val state = _state.asStateFlow()
    
    private var timerJob: Job? = null
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    private var totalPausedTime: Long = 0
    
    private val backgroundMusicManager = BackgroundMusicManager(context)
    
    private val userId: String?
        get() = authRepository.getCurrentUser()?.uid
    
    init {
        loadTasks()
    }
    
    private fun loadTasks() {
        val uid = userId ?: return
        
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            taskRepository.observeAllTasks(uid)
                .collect { tasks ->
                    val incompleteTasks = tasks.filter { !it.isCompleted }
                    _state.update { 
                        it.copy(
                            tasks = incompleteTasks,
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun selectTask(task: Task?) {
        _state.update { it.copy(selectedTask = task) }
    }
    
    fun selectDuration(duration: FocusDuration) {
        _state.update { 
            it.copy(
                selectedDuration = duration,
                remainingSeconds = duration.minutes * 60,
                totalSeconds = duration.minutes * 60
            )
        }
    }
    
    fun startTimer() {
        val currentState = _state.value
        if (currentState.isTimerRunning) return
        
        val uid = userId ?: return
        val task = currentState.selectedTask
        val duration = currentState.selectedDuration
        
        // Update state first (synchronously)
        val session = FocusSession(
            id = UUID.randomUUID().toString(),
            userId = uid,
            taskId = task?.id,
            taskTitle = task?.title ?: "Không có task",
            durationInMinutes = duration.minutes,
            startTime = System.currentTimeMillis(),
            completed = false,
            pointsEarned = 0
        )
        
        startTime = System.currentTimeMillis()
        totalPausedTime = 0
        
        val remainingSeconds = if (currentState.remainingSeconds > 0) {
            currentState.remainingSeconds
        } else {
            duration.minutes * 60
        }
        
        val totalSeconds = if (currentState.totalSeconds > 0) {
            currentState.totalSeconds
        } else {
            duration.minutes * 60
        }
        
        _state.update {
            it.copy(
                isTimerRunning = true,
                isTimerPaused = false,
                currentSession = session,
                remainingSeconds = remainingSeconds,
                totalSeconds = totalSeconds
            )
        }
        
        // Start background music if enabled
        if (currentState.isBackgroundMusicEnabled) {
            startBackgroundMusic()
        }
        
        // Start countdown immediately
        startCountdown()
        
        // Save session to database in background
        viewModelScope.launch {
            focusSessionRepository.createSession(uid, session)
        }
    }
    
    fun pauseTimer() {
        if (!_state.value.isTimerRunning || _state.value.isTimerPaused) return
        
        pausedTime = System.currentTimeMillis()
        timerJob?.cancel()
        
        // Pause background music
        backgroundMusicManager.pauseMusic()
        
        _state.update { it.copy(isTimerPaused = true) }
    }
    
    fun resumeTimer() {
        if (!_state.value.isTimerPaused) return
        
        val pauseDuration = System.currentTimeMillis() - pausedTime
        totalPausedTime += pauseDuration
        
        // Resume background music if enabled
        if (_state.value.isBackgroundMusicEnabled) {
            backgroundMusicManager.resumeMusic()
        }
        
        _state.update { it.copy(isTimerPaused = false) }
        startCountdown()
    }
    
    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        
        // Stop background music
        backgroundMusicManager.stopMusic()
        
        val currentState = _state.value
        val session = currentState.currentSession
        
        viewModelScope.launch {
            if (session != null) {
                val uid = userId ?: return@launch
                
                // Update session as incomplete
                val updatedSession = session.copy(
                    endTime = System.currentTimeMillis(),
                    completed = false
                )
                focusSessionRepository.updateSession(uid, updatedSession)
            }
            
            _state.update {
                it.copy(
                    isTimerRunning = false,
                    isTimerPaused = false,
                    remainingSeconds = currentState.totalSeconds,
                    currentSession = null
                )
            }
        }
    }
    
    private fun startCountdown() {
        timerJob?.cancel()
        
        timerJob = viewModelScope.launch {
            while (_state.value.remainingSeconds > 0 && _state.value.isTimerRunning && !_state.value.isTimerPaused) {
                delay(1000)
                
                _state.update { state ->
                    if (state.remainingSeconds > 0) {
                        state.copy(remainingSeconds = state.remainingSeconds - 1)
                    } else {
                        state
                    }
                }
            }
            
            // Timer finished
            if (_state.value.remainingSeconds == 0 && _state.value.isTimerRunning) {
                onTimerFinished()
            }
        }
    }
    
    private fun onTimerFinished() {
        val currentState = _state.value
        val session = currentState.currentSession ?: return
        val uid = userId ?: return
        
        viewModelScope.launch {
            val duration = currentState.selectedDuration
            val pointsEarned = duration.points
            
            // Update session as completed
            val completedSession = session.copy(
                endTime = System.currentTimeMillis(),
                completed = true,
                pointsEarned = pointsEarned
            )
            
            focusSessionRepository.updateSession(uid, completedSession)
            
            // Add points to user
            gamificationRepository.addPoints(uid, pointsEarned)
            
            _state.update {
                it.copy(
                    isTimerRunning = false,
                    isTimerPaused = false,
                    currentSession = null,
                    remainingSeconds = 0
                )
            }
        }
    }
    
    fun toggleBackgroundMusic() {
        val newState = !_state.value.isBackgroundMusicEnabled
        _state.update { it.copy(isBackgroundMusicEnabled = newState) }
        
        if (newState && _state.value.isTimerRunning && !_state.value.isTimerPaused) {
            startBackgroundMusic()
        } else if (!newState) {
            backgroundMusicManager.stopMusic()
        }
    }
    
    private fun startBackgroundMusic() {
        try {
            // Try to load music from raw folder
            // User needs to add a music file named "focus_music" (any format: mp3, ogg, wav)
            // to app/src/main/res/raw/ folder
            val resourceId = context.resources.getIdentifier("focus_music", "raw", context.packageName)
            if (resourceId != 0) {
                backgroundMusicManager.startMusic(resourceId)
            } else {
                // Music file not found - user needs to add music file to res/raw/
                // The toggle will still work, but no music will play
                android.util.Log.w("FocusViewModel", "Music file not found. Please add focus_music to res/raw/")
            }
        } catch (e: Exception) {
            android.util.Log.e("FocusViewModel", "Error starting background music", e)
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        backgroundMusicManager.release()
    }
}

