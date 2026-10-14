package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PhoneForwarded
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsRepository
import com.example.ui.theme.CallIncomingGreen
import com.example.ui.theme.RecordingRed

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RulesTab(
    isAutoRecordEnabled: Boolean,
    onAutoRecordChange: (Boolean) -> Unit,
    recordMode: String,
    onRecordModeChange: (String) -> Unit,
    audioSource: String,
    onAudioSourceChange: (String) -> Unit,
    audioFormat: String,
    onAudioFormatChange: (String) -> Unit,
    autoSpeakerBoost: Boolean,
    onSpeakerBoostChange: (Boolean) -> Unit,
    autoDeleteDays: Int,
    onAutoDeleteDaysChange: (Int) -> Unit,
    excludedNumbers: Set<String>,
    onAddExcludedNumber: (String) -> Unit,
    onRemoveExcludedNumber: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var newExcludedInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("rules_settings_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp)
    ) {
        // Master Auto-Record Switch Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auto_record_master_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAutoRecordEnabled) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    }
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Automatic Call Recording",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isAutoRecordEnabled) {
                                "Active • Will monitor phone state and automatically record calls"
                            } else {
                                "Disabled • Calls will not be recorded automatically"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = isAutoRecordEnabled,
                        onCheckedChange = onAutoRecordChange,
                        modifier = Modifier.testTag("auto_record_master_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CallIncomingGreen
                        )
                    )
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Recording Filter Mode
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhoneForwarded,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Call Filter Mode",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = recordMode == SettingsRepository.RECORD_MODE_ALL,
                            onClick = { onRecordModeChange(SettingsRepository.RECORD_MODE_ALL) },
                            label = { Text("All Calls", fontSize = 11.sp) },
                            modifier = Modifier.testTag("mode_all")
                        )
                        FilterChip(
                            selected = recordMode == SettingsRepository.RECORD_MODE_CONTACTS_ONLY,
                            onClick = { onRecordModeChange(SettingsRepository.RECORD_MODE_CONTACTS_ONLY) },
                            label = { Text("Contacts Only", fontSize = 11.sp) },
                            modifier = Modifier.testTag("mode_contacts")
                        )
                        FilterChip(
                            selected = recordMode == SettingsRepository.RECORD_MODE_UNKNOWN_ONLY,
                            onClick = { onRecordModeChange(SettingsRepository.RECORD_MODE_UNKNOWN_ONLY) },
                            label = { Text("Unknown Only", fontSize = 11.sp) },
                            modifier = Modifier.testTag("mode_unknown")
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Audio Engine & Hardware Config
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Audio Capture Engine",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Audio Source:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = audioSource == SettingsRepository.SOURCE_VOICE_COMMUNICATION,
                            onClick = { onAudioSourceChange(SettingsRepository.SOURCE_VOICE_COMMUNICATION) },
                            label = { Text("Voice Comm (Rec)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = audioSource == SettingsRepository.SOURCE_MIC,
                            onClick = { onAudioSourceChange(SettingsRepository.SOURCE_MIC) },
                            label = { Text("Microphone", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = audioSource == SettingsRepository.SOURCE_VOICE_RECOGNITION,
                            onClick = { onAudioSourceChange(SettingsRepository.SOURCE_VOICE_RECOGNITION) },
                            label = { Text("Voice Recog", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Encoding Format:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = audioFormat == SettingsRepository.FORMAT_M4A,
                            onClick = { onAudioFormatChange(SettingsRepository.FORMAT_M4A) },
                            label = { Text("M4A (AAC High Quality)", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = audioFormat == SettingsRepository.FORMAT_3GP,
                            onClick = { onAudioFormatChange(SettingsRepository.FORMAT_3GP) },
                            label = { Text("3GP (Compact)", fontSize = 11.sp) }
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Speakerphone Boost
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Auto Speakerphone Boost",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Enhances the other caller's voice clarity on Android 10-17",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = autoSpeakerBoost,
                            onCheckedChange = onSpeakerBoostChange,
                            modifier = Modifier.testTag("speaker_boost_switch")
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Storage & Auto Deletion
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Auto-Delete Old Recordings",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Starred favorites are always permanently protected from auto-deletion.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = autoDeleteDays == 0,
                            onClick = { onAutoDeleteDaysChange(0) },
                            label = { Text("Keep Forever", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = autoDeleteDays == 7,
                            onClick = { onAutoDeleteDaysChange(7) },
                            label = { Text("After 7d", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = autoDeleteDays == 30,
                            onClick = { onAutoDeleteDaysChange(30) },
                            label = { Text("After 30d", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = autoDeleteDays == 90,
                            onClick = { onAutoDeleteDaysChange(90) },
                            label = { Text("After 90d", fontSize = 11.sp) }
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }

        // Excluded Numbers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = null,
                            tint = RecordingRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Excluded Phone Numbers",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Calls with these phone numbers will never be recorded.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newExcludedInput,
                            onValueChange = { newExcludedInput = it },
                            placeholder = { Text("e.g. +1 555 0100") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("exclude_number_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newExcludedInput.isNotBlank()) {
                                    onAddExcludedNumber(newExcludedInput)
                                    newExcludedInput = ""
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_exclude_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add")
                            Text("Add", fontSize = 12.sp)
                        }
                    }

                    if (excludedNumbers.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            excludedNumbers.forEach { number ->
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(number, fontSize = 12.sp) },
                                    trailingIcon = {
                                        IconButton(
                                            onClick = { onRemoveExcludedNumber(number) },
                                            modifier = Modifier.size(16.dp)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "Remove",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
