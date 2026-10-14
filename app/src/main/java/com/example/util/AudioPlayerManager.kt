package com.example.util

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

object AudioPlayerManager {

    private const val TAG = "AudioPlayerManager"

    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _playingRecordingId = MutableStateFlow<Long?>(null)
    val playingRecordingId: StateFlow<Long?> = _playingRecordingId.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs: StateFlow<Long> = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed: StateFlow<Float> = _playbackSpeed.asStateFlow()

    fun play(context: Context, recordingId: Long, filePath: String) {
        val file = File(filePath)
        if (!file.exists()) {
            Log.e(TAG, "Audio file does not exist: $filePath")
            return
        }

        if (_playingRecordingId.value == recordingId && mediaPlayer != null) {
            if (!_isPlaying.value) {
                mediaPlayer?.start()
                _isPlaying.value = true
                startProgressTracker()
            }
            return
        }

        stop()

        try {
            val player = MediaPlayer()
            player.setDataSource(filePath)
            player.prepare()

            _durationMs.value = player.duration.toLong()
            _currentPositionMs.value = 0L
            _playingRecordingId.value = recordingId

            // Set playback speed
            applySpeed(player, _playbackSpeed.value)

            player.setOnCompletionListener {
                _isPlaying.value = false
                _currentPositionMs.value = 0L
                progressJob?.cancel()
            }

            player.setOnErrorListener { _, what, extra ->
                Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                stop()
                true
            }

            player.start()
            _isPlaying.value = true
            mediaPlayer = player

            startProgressTracker()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start audio playback: ${e.message}", e)
            stop()
        }
    }

    fun pause() {
        try {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
            }
        } catch (_: Exception) {
        }
        _isPlaying.value = false
        progressJob?.cancel()
    }

    fun resume() {
        try {
            mediaPlayer?.start()
            _isPlaying.value = true
            startProgressTracker()
        } catch (_: Exception) {
        }
    }

    fun togglePlayPause(context: Context, recordingId: Long, filePath: String) {
        if (_playingRecordingId.value == recordingId) {
            if (_isPlaying.value) {
                pause()
            } else {
                resume()
            }
        } else {
            play(context, recordingId, filePath)
        }
    }

    fun seekTo(positionMs: Long) {
        try {
            mediaPlayer?.seekTo(positionMs.toInt())
            _currentPositionMs.value = positionMs
        } catch (_: Exception) {
        }
    }

    fun skipForward(seconds: Int = 10) {
        val current = _currentPositionMs.value
        val total = _durationMs.value
        val target = (current + seconds * 1000).coerceAtMost(total)
        seekTo(target)
    }

    fun skipBackward(seconds: Int = 10) {
        val current = _currentPositionMs.value
        val target = (current - seconds * 1000).coerceAtLeast(0L)
        seekTo(target)
    }

    fun setSpeed(speed: Float) {
        _playbackSpeed.value = speed
        mediaPlayer?.let { applySpeed(it, speed) }
    }

    private fun applySpeed(player: MediaPlayer, speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val params = player.playbackParams ?: PlaybackParams()
                params.speed = speed
                player.playbackParams = params
            } catch (e: Exception) {
                Log.w(TAG, "Could not set playback speed: ${e.message}")
            }
        }
    }

    fun stop() {
        progressJob?.cancel()
        progressJob = null
        try {
            mediaPlayer?.stop()
            mediaPlayer?.reset()
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
        _isPlaying.value = false
        _playingRecordingId.value = null
        _currentPositionMs.value = 0L
        _durationMs.value = 0L
    }

    private fun startProgressTracker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive && _isPlaying.value) {
                try {
                    val pos = mediaPlayer?.currentPosition?.toLong() ?: 0L
                    _currentPositionMs.value = pos
                } catch (_: Exception) {
                }
                delay(100)
            }
        }
    }
}
