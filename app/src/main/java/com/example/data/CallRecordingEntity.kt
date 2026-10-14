package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "call_recordings")
data class CallRecordingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val phoneNumber: String,
    val contactName: String? = null,
    val callType: CallType,
    val timestamp: Long = System.currentTimeMillis(),
    val durationSeconds: Int = 0,
    val filePath: String,
    val fileSizeBytes: Long = 0,
    val isFavorite: Boolean = false,
    val notes: String = "",
    val audioSource: String = "MIC",
    val audioFormat: String = "M4A"
)
