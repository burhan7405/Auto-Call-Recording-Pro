package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneCallback
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneCallback
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.ui.components.ActiveCallBanner
import com.example.ui.components.PermissionBanner
import com.example.ui.theme.CallIncomingGreen
import com.example.ui.theme.RecordingRed
import com.example.ui.viewmodel.MainViewModel
import com.example.util.PermissionHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val recordings by viewModel.recordings.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedFilter.collectAsStateWithLifecycle()
    val permissionState by viewModel.permissionState.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val totalStorageBytes by viewModel.totalStorageBytes.collectAsStateWithLifecycle()

    // Active Recording Status from Tracker
    val isRecordingActive by viewModel.callStateTracker.isRecording.collectAsStateWithLifecycle()
    val activePhoneNumber by viewModel.callStateTracker.activePhoneNumber.collectAsStateWithLifecycle()
    val activeContactName by viewModel.callStateTracker.activeContactName.collectAsStateWithLifecycle()
    val activeCallType by viewModel.callStateTracker.activeCallType.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.callStateTracker.durationSeconds.collectAsStateWithLifecycle()
    val currentAmplitude by viewModel.callStateTracker.currentAmplitude.collectAsStateWithLifecycle()

    // Audio Playback
    val playingRecordingId by viewModel.audioPlayer.playingRecordingId.collectAsStateWithLifecycle()
    val isPlaying by viewModel.audioPlayer.isPlaying.collectAsStateWithLifecycle()
    val currentPositionMs by viewModel.audioPlayer.currentPositionMs.collectAsStateWithLifecycle()
    val durationMs by viewModel.audioPlayer.durationMs.collectAsStateWithLifecycle()
    val playbackSpeed by viewModel.audioPlayer.playbackSpeed.collectAsStateWithLifecycle()

    // Settings
    val isAutoRecordEnabled by viewModel.settingsRepository.isAutoRecordEnabled.collectAsStateWithLifecycle()
    val recordMode by viewModel.settingsRepository.recordMode.collectAsStateWithLifecycle()
    val audioSource by viewModel.settingsRepository.audioSource.collectAsStateWithLifecycle()
    val audioFormat by viewModel.settingsRepository.audioFormat.collectAsStateWithLifecycle()
    val autoSpeakerBoost by viewModel.settingsRepository.autoSpeakerBoost.collectAsStateWithLifecycle()
    val autoDeleteDays by viewModel.settingsRepository.autoDeleteDays.collectAsStateWithLifecycle()
    val excludedNumbers by viewModel.settingsRepository.excludedNumbers.collectAsStateWithLifecycle()

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        viewModel.refreshPermissions()
    }

    LaunchedEffect(Unit) {
        viewModel.refreshPermissions()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "CallRecorder Pro",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Android 10 - 17 Engine",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isAutoRecordEnabled) {
                            CallIncomingGreen.copy(alpha = 0.15f)
                        } else {
                            Color.Gray.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isAutoRecordEnabled) CallIncomingGreen else Color.Gray
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isAutoRecordEnabled) "ARMED" else "OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAutoRecordEnabled) CallIncomingGreen else Color.Gray
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bottom_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (totalCount > 0) {
                                    Badge { Text("$totalCount") }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.GraphicEq else Icons.Outlined.GraphicEq,
                                contentDescription = "Recordings"
                            )
                        }
                    },
                    label = { Text("Recordings", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_recordings")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Filled.PhoneCallback else Icons.Outlined.PhoneCallback,
                            contentDescription = "Simulator"
                        )
                    },
                    label = { Text("Simulator", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_simulator")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { viewModel.setSelectedTab(2) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Filled.Tune else Icons.Outlined.Tune,
                            contentDescription = "Rules & Settings"
                        )
                    },
                    label = { Text("Rules", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_rules")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { viewModel.setSelectedTab(3) },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 3) Icons.Filled.Info else Icons.Outlined.Info,
                            contentDescription = "Android 10-17 Guide"
                        )
                    },
                    label = { Text("OS Guide", fontSize = 11.sp) },
                    modifier = Modifier.testTag("tab_guide")
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 0 && !isRecordingActive) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.setSelectedTab(1) },
                    icon = {
                        Icon(
                            Icons.Default.PhoneInTalk,
                            contentDescription = null,
                            tint = Color.White
                        )
                    },
                    text = {
                        Text(
                            "Simulate Test Call",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("fab_simulate_call")
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Ongoing Recording Banner (appears whenever recording is active)
            ActiveCallBanner(
                isRecording = isRecordingActive,
                phoneNumber = activePhoneNumber,
                contactName = activeContactName,
                callType = activeCallType,
                durationSeconds = recordingDuration,
                amplitude = currentAmplitude,
                onStopClick = { viewModel.stopActiveRecording() }
            )

            // Permissions Status Banner
            PermissionBanner(
                permissionState = permissionState,
                onRequestPermissions = {
                    permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                },
                onRequestBatteryOptimization = {
                    PermissionHelper.requestBatteryOptimizationExemption(context)
                }
            )

            // Tab Content with smooth fade transitions
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                modifier = Modifier.weight(1f),
                label = "TabContent"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> RecordingsTab(
                        recordings = recordings,
                        searchQuery = searchQuery,
                        onSearchQueryChange = { viewModel.setSearchQuery(it) },
                        selectedFilter = selectedFilter,
                        onFilterSelect = { viewModel.setSelectedFilter(it) },
                        totalCount = totalCount,
                        totalStorageBytes = totalStorageBytes,
                        isAutoRecordEnabled = isAutoRecordEnabled,
                        playingRecordingId = playingRecordingId,
                        isPlaying = isPlaying,
                        currentPositionMs = currentPositionMs,
                        totalDurationMs = durationMs,
                        playbackSpeed = playbackSpeed,
                        onTogglePlay = { id, path -> viewModel.audioPlayer.togglePlayPause(context, id, path) },
                        onSeekTo = { pos -> viewModel.audioPlayer.seekTo(pos) },
                        onSkipForward = { viewModel.audioPlayer.skipForward(10) },
                        onSkipBackward = { viewModel.audioPlayer.skipBackward(10) },
                        onSpeedChange = { speed -> viewModel.audioPlayer.setSpeed(speed) },
                        onFavoriteClick = { item -> viewModel.toggleFavorite(item) },
                        onUpdateNotes = { id, notes -> viewModel.updateNotes(id, notes) },
                        onDeleteRecording = { item -> viewModel.deleteRecording(item) },
                        onNavigateToSimulator = { viewModel.setSelectedTab(1) }
                    )

                    1 -> SimulatorTab(
                        isRecording = isRecordingActive,
                        recordingDurationSeconds = recordingDuration,
                        currentAmplitude = currentAmplitude,
                        activePhoneNumber = activePhoneNumber,
                        activeContactName = activeContactName,
                        activeCallType = activeCallType,
                        onStartSimulation = { number, contact, isIncoming ->
                            viewModel.startTestCallSimulation(number, contact, isIncoming)
                        },
                        onStopSimulation = { viewModel.stopActiveRecording() },
                        onNavigateToRecordings = { viewModel.setSelectedTab(0) }
                    )

                    2 -> RulesTab(
                        isAutoRecordEnabled = isAutoRecordEnabled,
                        onAutoRecordChange = { viewModel.setAutoRecord(it) },
                        recordMode = recordMode,
                        onRecordModeChange = { viewModel.setRecordMode(it) },
                        audioSource = audioSource,
                        onAudioSourceChange = { viewModel.setAudioSource(it) },
                        audioFormat = audioFormat,
                        onAudioFormatChange = { viewModel.setAudioFormat(it) },
                        autoSpeakerBoost = autoSpeakerBoost,
                        onSpeakerBoostChange = { viewModel.setSpeakerBoost(it) },
                        autoDeleteDays = autoDeleteDays,
                        onAutoDeleteDaysChange = { viewModel.setAutoDeleteDays(it) },
                        excludedNumbers = excludedNumbers,
                        onAddExcludedNumber = { viewModel.addExcludedNumber(it) },
                        onRemoveExcludedNumber = { viewModel.removeExcludedNumber(it) }
                    )

                    3 -> GuideTab(
                        permissionState = permissionState,
                        onRequestPermissions = {
                            permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
                        },
                        onRequestBatteryOptimization = {
                            PermissionHelper.requestBatteryOptimizationExemption(context)
                        },
                        onNavigateToSimulator = { viewModel.setSelectedTab(1) }
                    )
                }
            }
        }
    }
}
