package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingDao {

    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteRecordings(): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE id = :id LIMIT 1")
    suspend fun getRecordingById(id: Long): CallRecordingEntity?

    @Query("""
        SELECT * FROM call_recordings 
        WHERE phoneNumber LIKE '%' || :query || '%' 
           OR (contactName IS NOT NULL AND contactName LIKE '%' || :query || '%') 
           OR notes LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchRecordings(query: String): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE callType = :type ORDER BY timestamp DESC")
    fun getRecordingsByType(type: CallType): Flow<List<CallRecordingEntity>>

    @Query("SELECT COUNT(*) FROM call_recordings")
    fun getCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(fileSizeBytes), 0) FROM call_recordings")
    fun getTotalStorageUsed(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(recording: CallRecordingEntity): Long

    @Update
    suspend fun update(recording: CallRecordingEntity)

    @Delete
    suspend fun delete(recording: CallRecordingEntity)

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM call_recordings WHERE isFavorite = 0 AND timestamp < :cutoffTimestamp")
    suspend fun deleteNonFavoritesOlderThan(cutoffTimestamp: Long): Int

    @Query("UPDATE call_recordings SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("UPDATE call_recordings SET notes = :notes WHERE id = :id")
    suspend fun updateNotes(id: Long, notes: String)
}
