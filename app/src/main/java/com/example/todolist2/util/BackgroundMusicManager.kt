package com.example.todolist2.util

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

/**
 * Manager for background music during focus sessions
 * Supports looping ambient music for concentration
 */
class BackgroundMusicManager(private val context: Context) {
    
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying: Boolean = false
    private var volume: Float = 0.3f // Default volume (30%)
    
    /**
     * Start playing background music
     * @param resourceId Raw resource ID of the music file
     */
    fun startMusic(resourceId: Int) {
        if (isPlaying) {
            stopMusic()
        }
        
        try {
            mediaPlayer = MediaPlayer.create(context, resourceId)
            mediaPlayer?.apply {
                isLooping = true
                setVolume(volume, volume)
                setOnPreparedListener {
                    start()
                    this@BackgroundMusicManager.isPlaying = true
                    Log.d("BackgroundMusic", "Music started")
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("BackgroundMusic", "Error playing music: what=$what, extra=$extra")
                    false
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Failed to start music", e)
        }
    }
    
    /**
     * Stop playing music
     */
    fun stopMusic() {
        try {
            mediaPlayer?.apply {
                if (isPlaying) {
                    stop()
                }
                release()
            }
            mediaPlayer = null
            isPlaying = false
            Log.d("BackgroundMusic", "Music stopped")
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Error stopping music", e)
        }
    }
    
    /**
     * Pause music (can be resumed)
     */
    fun pauseMusic() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                isPlaying = false
                Log.d("BackgroundMusic", "Music paused")
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Error pausing music", e)
        }
    }
    
    /**
     * Resume paused music
     */
    fun resumeMusic() {
        try {
            mediaPlayer?.apply {
                if (!this@BackgroundMusicManager.isPlaying) {
                    start()
                    this@BackgroundMusicManager.isPlaying = true
                    Log.d("BackgroundMusic", "Music resumed")
                }
            }
        } catch (e: Exception) {
            Log.e("BackgroundMusic", "Error resuming music", e)
        }
    }
    
    /**
     * Set volume (0.0f to 1.0f)
     */
    fun setVolume(volume: Float) {
        this.volume = volume.coerceIn(0f, 1f)
        mediaPlayer?.setVolume(this.volume, this.volume)
    }
    
    /**
     * Check if music is currently playing
     */
    fun isMusicPlaying(): Boolean = isPlaying
    
    /**
     * Clean up resources
     */
    fun release() {
        stopMusic()
    }
}

