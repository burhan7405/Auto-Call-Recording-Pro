package com.example.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class RecordingRepository(
    private val recordingDao: RecordingDao,
    private val context: Context
) {

    val allRecordings: Flow<List<CallRecordingEntity>> = recordingDao.getAllRecordings()
    val favoriteRecordings: Flow<List<CallRecordingEntity>> = recordingDao.getFavoriteRecordings()
    val totalCount: Flow<Int> = recordingDao.getCount()
    val totalStorageBytes: Flow<Long> = recordingDao.getTotalStorageUsed()

    fun searchRecordings(query: String): Flow<List<CallRecordingEntity>> =
        recordingDao.searchRecordings(query)

    fun filterByType(type: CallType): Flow<List<CallRecordingEntity>> =
        recordingDao.getRecordingsByType(type)

    suspend fun getRecordingById(id: Long): CallRecordingEntity? = withContext(Dispatchers.IO) {
        recordingDao.getRecordingById(id)
    }

    suspend fun insertRecording(recording: CallRecordingEntity): Long = withContext(Dispatchers.IO) {
        recordingDao.insert(recording)
    }

    suspend fun updateRecording(recording: CallRecordingEntity) = withContext(Dispatchers.IO) {
        recordingDao.update(recording)
    }

    suspend fun toggleFavorite(id: Long, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        recordingDao.setFavorite(id, !currentFavorite)
    }

    suspend fun updateNotes(id: Long, notes: String) = withContext(Dispatchers.IO) {
        recordingDao.updateNotes(id, notes)
    }

    suspend fun deleteRecording(recording: CallRecordingEntity) = withContext(Dispatchers.IO) {
        // Delete audio file on disk
        try {
            val file = File(recording.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
        }
        recordingDao.delete(recording)
    }

    suspend fun deleteRecordingById(id: Long) = withContext(Dispatchers.IO) {
        val recording = recordingDao.getRecordingById(id)
        if (recording != null) {
            deleteRecording(recording)
        }
    }

    suspend fun applyAutoCleanup(keepDays: Int): Int = withContext(Dispatchers.IO) {
        if (keepDays <= 0) return@withContext 0
        val cutoff = System.currentTimeMillis() - (keepDays.toLong() * 24 * 60 * 60 * 1000)
        recordingDao.deleteNonFavoritesOlderThan(cutoff)
    }
}
