package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.CallRecorderApplication
import com.example.data.CallRecordingEntity
import com.example.data.CallType
import com.example.data.RecordingRepository
import com.example.data.SettingsRepository
import com.example.service.CallRecordService
import com.example.service.CallStateTracker
import com.example.util.AudioPlayerManager
import com.example.util.PermissionHelper
import com.example.util.PermissionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterOption {
    ALL,
    INCOMING,
    OUTGOING,
    FAVORITES
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RecordingRepository =
        (application as CallRecorderApplication).recordingRepository
    val settingsRepository: SettingsRepository =
        (application as CallRecorderApplication).settingsRepository

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _permissionState = MutableStateFlow(
        PermissionHelper.checkPermissions(application)
    )
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    val callStateTracker = CallStateTracker
    val audioPlayer = AudioPlayerManager

    val totalCount: StateFlow<Int> = repository.totalCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalStorageBytes: StateFlow<Long> = repository.totalStorageBytes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    @OptIn(ExperimentalCoroutinesApi::class)
    val recordings: StateFlow<List<CallRecordingEntity>> =
        kotlinx.coroutines.flow.combine(
            _searchQuery,
            _selectedFilter
        ) { query, filter ->
            Pair(query, filter)
        }.flatMapLatest { (query, filter) ->
            if (query.isNotBlank()) {
                repository.searchRecordings(query)
            } else {
                when (filter) {
                    FilterOption.ALL -> repository.allRecordings
                    FilterOption.INCOMING -> repository.filterByType(CallType.INCOMING)
                    FilterOption.OUTGOING -> repository.filterByType(CallType.OUTGOING)
                    FilterOption.FAVORITES -> repository.favoriteRecordings
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedTab(index: Int) {
        _selectedTab.value = index
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    fun refreshPermissions() {
        _permissionState.value = PermissionHelper.checkPermissions(getApplication())
    }

    fun toggleFavorite(recording: CallRecordingEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(recording.id, recording.isFavorite)
        }
    }

    fun updateNotes(recordingId: Long, notes: String) {
        viewModelScope.launch {
            repository.updateNotes(recordingId, notes)
        }
    }

    fun deleteRecording(recording: CallRecordingEntity) {
        viewModelScope.launch {
            if (audioPlayer.playingRecordingId.value == recording.id) {
                audioPlayer.stop()
            }
            repository.deleteRecording(recording)
        }
    }

    fun stopActiveRecording() {
        CallRecordService.stopRecording(getApplication())
    }

    fun startTestCallSimulation(
        phoneNumber: String,
        contactName: String?,
        isIncoming: Boolean
    ) {
        val type = if (isIncoming) CallType.INCOMING else CallType.OUTGOING
        CallRecordService.startRecording(
            getApplication(),
            phoneNumber.ifBlank { "+1 (555) 789-0123" },
            contactName?.ifBlank { "Test Contact" },
            type
        )
    }

    fun setAutoRecord(enabled: Boolean) {
        settingsRepository.setAutoRecordEnabled(enabled)
    }

    fun setRecordMode(mode: String) {
        settingsRepository.setRecordMode(mode)
    }

    fun setAudioSource(source: String) {
        settingsRepository.setAudioSource(source)
    }

    fun setAudioFormat(format: String) {
        settingsRepository.setAudioFormat(format)
    }

    fun setSpeakerBoost(enabled: Boolean) {
        settingsRepository.setAutoSpeakerBoost(enabled)
    }

    fun setAutoDeleteDays(days: Int) {
        settingsRepository.setAutoDeleteDays(days)
    }

    fun addExcludedNumber(number: String) {
        settingsRepository.addExcludedNumber(number)
    }

    fun removeExcludedNumber(number: String) {
        settingsRepository.removeExcludedNumber(number)
    }
}
