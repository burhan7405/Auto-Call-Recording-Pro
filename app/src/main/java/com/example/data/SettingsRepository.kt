package com.example.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("call_recorder_settings", Context.MODE_PRIVATE)

    private val _isAutoRecordEnabled =
        MutableStateFlow(prefs.getBoolean(KEY_AUTO_RECORD, true))
    val isAutoRecordEnabled: StateFlow<Boolean> = _isAutoRecordEnabled.asStateFlow()

    private val _recordMode =
        MutableStateFlow(prefs.getString(KEY_RECORD_MODE, RECORD_MODE_ALL) ?: RECORD_MODE_ALL)
    val recordMode: StateFlow<String> = _recordMode.asStateFlow()

    private val _audioSource =
        MutableStateFlow(prefs.getString(KEY_AUDIO_SOURCE, SOURCE_VOICE_COMMUNICATION) ?: SOURCE_VOICE_COMMUNICATION)
    val audioSource: StateFlow<String> = _audioSource.asStateFlow()

    private val _audioFormat =
        MutableStateFlow(prefs.getString(KEY_AUDIO_FORMAT, FORMAT_M4A) ?: FORMAT_M4A)
    val audioFormat: StateFlow<String> = _audioFormat.asStateFlow()

    private val _autoSpeakerBoost =
        MutableStateFlow(prefs.getBoolean(KEY_SPEAKER_BOOST, false))
    val autoSpeakerBoost: StateFlow<Boolean> = _autoSpeakerBoost.asStateFlow()

    private val _autoDeleteDays =
        MutableStateFlow(prefs.getInt(KEY_AUTO_DELETE_DAYS, 0))
    val autoDeleteDays: StateFlow<Int> = _autoDeleteDays.asStateFlow()

    private val _showRecordingNotification =
        MutableStateFlow(prefs.getBoolean(KEY_SHOW_NOTIFICATION, true))
    val showRecordingNotification: StateFlow<Boolean> = _showRecordingNotification.asStateFlow()

    private val _excludedNumbers =
        MutableStateFlow(prefs.getStringSet(KEY_EXCLUDED_NUMBERS, emptySet()) ?: emptySet())
    val excludedNumbers: StateFlow<Set<String>> = _excludedNumbers.asStateFlow()

    fun setAutoRecordEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_RECORD, enabled).apply()
        _isAutoRecordEnabled.value = enabled
    }

    fun setRecordMode(mode: String) {
        prefs.edit().putString(KEY_RECORD_MODE, mode).apply()
        _recordMode.value = mode
    }

    fun setAudioSource(source: String) {
        prefs.edit().putString(KEY_AUDIO_SOURCE, source).apply()
        _audioSource.value = source
    }

    fun setAudioFormat(format: String) {
        prefs.edit().putString(KEY_AUDIO_FORMAT, format).apply()
        _audioFormat.value = format
    }

    fun setAutoSpeakerBoost(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPEAKER_BOOST, enabled).apply()
        _autoSpeakerBoost.value = enabled
    }

    fun setAutoDeleteDays(days: Int) {
        prefs.edit().putInt(KEY_AUTO_DELETE_DAYS, days).apply()
        _autoDeleteDays.value = days
    }

    fun setShowRecordingNotification(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_NOTIFICATION, show).apply()
        _showRecordingNotification.value = show
    }

    fun addExcludedNumber(number: String) {
        val updated = _excludedNumbers.value.toMutableSet().apply { add(number.trim()) }
        prefs.edit().putStringSet(KEY_EXCLUDED_NUMBERS, updated).apply()
        _excludedNumbers.value = updated
    }

    fun removeExcludedNumber(number: String) {
        val updated = _excludedNumbers.value.toMutableSet().apply { remove(number.trim()) }
        prefs.edit().putStringSet(KEY_EXCLUDED_NUMBERS, updated).apply()
        _excludedNumbers.value = updated
    }

    fun shouldRecordCall(phoneNumber: String, isKnownContact: Boolean): Boolean {
        if (!_isAutoRecordEnabled.value) return false
        val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
        if (_excludedNumbers.value.any { it.replace(Regex("[^0-9+]"), "") == cleanNumber }) {
            return false
        }
        return when (_recordMode.value) {
            RECORD_MODE_CONTACTS_ONLY -> isKnownContact
            RECORD_MODE_UNKNOWN_ONLY -> !isKnownContact
            else -> true
        }
    }

    companion object {
        const val KEY_AUTO_RECORD = "key_auto_record"
        const val KEY_RECORD_MODE = "key_record_mode"
        const val KEY_AUDIO_SOURCE = "key_audio_source"
        const val KEY_AUDIO_FORMAT = "key_audio_format"
        const val KEY_SPEAKER_BOOST = "key_speaker_boost"
        const val KEY_AUTO_DELETE_DAYS = "key_auto_delete_days"
        const val KEY_SHOW_NOTIFICATION = "key_show_notification"
        const val KEY_EXCLUDED_NUMBERS = "key_excluded_numbers"

        const val RECORD_MODE_ALL = "ALL"
        const val RECORD_MODE_CONTACTS_ONLY = "CONTACTS_ONLY"
        const val RECORD_MODE_UNKNOWN_ONLY = "UNKNOWN_ONLY"

        const val SOURCE_VOICE_COMMUNICATION = "VOICE_COMMUNICATION"
        const val SOURCE_MIC = "MIC"
        const val SOURCE_VOICE_RECOGNITION = "VOICE_RECOGNITION"

        const val FORMAT_M4A = "M4A"
        const val FORMAT_3GP = "3GP"
    }
}
