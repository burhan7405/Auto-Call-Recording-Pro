package com.example.service

import com.example.data.CallType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object CallStateTracker {

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _activePhoneNumber = MutableStateFlow<String?>(null)
    val activePhoneNumber: StateFlow<String?> = _activePhoneNumber.asStateFlow()

    private val _activeContactName = MutableStateFlow<String?>(null)
    val activeContactName: StateFlow<String?> = _activeContactName.asStateFlow()

    private val _activeCallType = MutableStateFlow<CallType?>(null)
    val activeCallType: StateFlow<CallType?> = _activeCallType.asStateFlow()

    private val _durationSeconds = MutableStateFlow(0)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _currentAmplitude = MutableStateFlow(0f)
    val currentAmplitude: StateFlow<Float> = _currentAmplitude.asStateFlow()

    private val _recentAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val recentAmplitudes: StateFlow<List<Float>> = _recentAmplitudes.asStateFlow()

    fun onRecordingStarted(
        phoneNumber: String,
        contactName: String?,
        callType: CallType
    ) {
        _activePhoneNumber.value = phoneNumber
        _activeContactName.value = contactName
        _activeCallType.value = callType
        _durationSeconds.value = 0
        _currentAmplitude.value = 0f
        _recentAmplitudes.value = emptyList()
        _isRecording.value = true
    }

    fun updateDuration(seconds: Int) {
        _durationSeconds.value = seconds
    }

    fun updateAmplitude(amplitude: Float) {
        _currentAmplitude.value = amplitude
        val list = _recentAmplitudes.value.toMutableList()
        list.add(amplitude)
        if (list.size > 50) {
            list.removeAt(0)
        }
        _recentAmplitudes.value = list
    }

    fun onRecordingStopped() {
        _isRecording.value = false
        _activePhoneNumber.value = null
        _activeContactName.value = null
        _activeCallType.value = null
        _durationSeconds.value = 0
        _currentAmplitude.value = 0f
        _recentAmplitudes.value = emptyList()
    }
}
