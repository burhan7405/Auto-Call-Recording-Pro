package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CallMade
import androidx.compose.material.icons.automirrored.filled.CallReceived
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.CallRecordingEntity
import com.example.ui.components.RecordingCard
import com.example.ui.components.RecordingDetailDialog
import com.example.ui.theme.CallIncomingGreen
import com.example.ui.theme.CallOutgoingBlue
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.RecordingRed
import com.example.ui.viewmodel.FilterOption
import com.example.util.Formatters
import com.example.util.ShareHelper

@Composable
fun RecordingsTab(
    recordings: List<CallRecordingEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedFilter: FilterOption,
    onFilterSelect: (FilterOption) -> Unit,
    totalCount: Int,
    totalStorageBytes: Long,
    isAutoRecordEnabled: Boolean,
    playingRecordingId: Long?,
    isPlaying: Boolean,
    currentPositionMs: Long,
    totalDurationMs: Long,
    playbackSpeed: Float,
    onTogglePlay: (Long, String) -> Unit,
    onSeekTo: (Long) -> Unit,
    onSkipForward: () -> Unit,
    onSkipBackward: () -> Unit,
    onSpeedChange: (Float) -> Unit,
    onFavoriteClick: (CallRecordingEntity) -> Unit,
    onUpdateNotes: (Long, String) -> Unit,
    onDeleteRecording: (CallRecordingEntity) -> Unit,
    onNavigateToSimulator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedRecordingForDetails by remember { mutableStateOf<CallRecordingEntity?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("recordings_list"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Quick Stats Row
        item {
            QuickStatsStrip(
                totalCount = totalCount,
                totalStorageBytes = totalStorageBytes,
                isAutoRecordEnabled = isAutoRecordEnabled
            )
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search by name, number, or note...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("search_input"),
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )
        }

        // Filter Chips Row
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedFilter == FilterOption.ALL,
                        onClick = { onFilterSelect(FilterOption.ALL) },
                        label = { Text("All ($totalCount)") },
                        modifier = Modifier.testTag("filter_all")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == FilterOption.INCOMING,
                        onClick = { onFilterSelect(FilterOption.INCOMING) },
                        label = { Text("Incoming") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.CallReceived,
                                contentDescription = null,
                                tint = CallIncomingGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_incoming")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == FilterOption.OUTGOING,
                        onClick = { onFilterSelect(FilterOption.OUTGOING) },
                        label = { Text("Outgoing") },
                        leadingIcon = {
                            Icon(
                                Icons.AutoMirrored.Filled.CallMade,
                                contentDescription = null,
                                tint = CallOutgoingBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_outgoing")
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == FilterOption.FAVORITES,
                        onClick = { onFilterSelect(FilterOption.FAVORITES) },
                        label = { Text("Favorites") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFEAB308),
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("filter_favorites")
                    )
                }
            }
        }

        // Recording Items or Empty State
        if (recordings.isEmpty()) {
            item {
                EmptyRecordingsState(
                    searchQuery = searchQuery,
                    onNavigateToSimulator = onNavigateToSimulator
                )
            }
        } else {
            items(recordings, key = { it.id }) { item ->
                val isItemPlaying = playingRecordingId == item.id && isPlaying

                RecordingCard(
                    recording = item,
                    isPlaying = isItemPlaying,
                    currentPositionMs = if (playingRecordingId == item.id) currentPositionMs else 0L,
                    totalDurationMs = if (playingRecordingId == item.id) totalDurationMs else item.durationSeconds * 1000L,
                    playbackSpeed = playbackSpeed,
                    onPlayPauseClick = { onTogglePlay(item.id, item.filePath) },
                    onSeekTo = onSeekTo,
                    onSkipForward = onSkipForward,
                    onSkipBackward = onSkipBackward,
                    onSpeedChange = onSpeedChange,
                    onFavoriteClick = { onFavoriteClick(item) },
                    onShareClick = { ShareHelper.shareRecording(context, item) },
                    onDeleteClick = { onDeleteRecording(item) },
                    onEditNotesClick = { selectedRecordingForDetails = item }
                )
            }
        }
    }

    // Details & Note Dialog
    selectedRecordingForDetails?.let { recording ->
        RecordingDetailDialog(
            recording = recording,
            onDismiss = { selectedRecordingForDetails = null },
            onSaveNotes = { notes -> onUpdateNotes(recording.id, notes) },
            onShare = { ShareHelper.shareRecording(context, recording) },
            onDelete = {
                onDeleteRecording(recording)
                selectedRecordingForDetails = null
            }
        )
    }
}

@Composable
private fun QuickStatsStrip(
    totalCount: Int,
    totalStorageBytes: Long,
    isAutoRecordEnabled: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isAutoRecordEnabled) CallIncomingGreen else Color.Gray)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isAutoRecordEnabled) "Auto-Record Armed" else "Auto-Record Paused",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PhoneInTalk,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$totalCount Calls",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = Formatters.formatFileSize(totalStorageBytes),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyRecordingsState(
    searchQuery: String,
    onNavigateToSimulator: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.GraphicEq,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (searchQuery.isNotBlank()) "No recordings match \"$searchQuery\"" else "No Call Recordings Yet",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = if (searchQuery.isNotBlank()) {
                "Try searching for another caller name, number, or note tag."
            } else {
                "Incoming and outgoing calls will automatically record here. You can also test the recorder right now with the Call Simulator!"
            },
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        if (searchQuery.isBlank()) {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onNavigateToSimulator,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("launch_test_simulator_button")
            ) {
                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Test Call & Auto-Record Now")
            }
        }
    }
}
