package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.example.data.AppDatabase
import com.example.data.RecordingRepository
import com.example.data.SettingsRepository

class CallRecorderApplication : Application() {

    lateinit var database: AppDatabase
        private set

    lateinit var recordingRepository: RecordingRepository
        private set

    lateinit var settingsRepository: SettingsRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        database = AppDatabase.getInstance(this)
        recordingRepository = RecordingRepository(database.recordingDao(), this)
        settingsRepository = SettingsRepository(this)

        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Channel for active ongoing call recording
            val recordChannel = NotificationChannel(
                CHANNEL_RECORDING_ID,
                "Ongoing Call Recording",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live call recording status and controls"
                setShowBadge(false)
            }

            // Channel for completed recording notifications
            val summaryChannel = NotificationChannel(
                CHANNEL_SUMMARY_ID,
                "Call Recording Saved",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts when a new call has been recorded and saved"
            }

            notificationManager.createNotificationChannel(recordChannel)
            notificationManager.createNotificationChannel(summaryChannel)
        }
    }

    companion object {
        const val CHANNEL_RECORDING_ID = "call_recording_active_channel"
        const val CHANNEL_SUMMARY_ID = "call_recording_summary_channel"

        lateinit var instance: CallRecorderApplication
            private set
    }
}
