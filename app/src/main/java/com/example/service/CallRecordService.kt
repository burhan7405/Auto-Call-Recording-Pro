package com.example.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioManager
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.CallRecorderApplication
import com.example.MainActivity
import com.example.data.CallRecordingEntity
import com.example.data.CallType
import com.example.data.SettingsRepository
import com.example.util.Formatters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CallRecordService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var recordingJob: Job? = null

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var recordingStartTime = 0L
    private var durationSeconds = 0

    private var currentPhoneNumber: String = "Unknown"
    private var currentContactName: String? = null
    private var currentCallType: CallType = CallType.INCOMING
    private var currentAudioSource: String = "MIC"
    private var currentAudioFormat: String = "M4A"

    private var wasSpeakerphoneOnInitial = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START_RECORDING

        when (action) {
            ACTION_START_RECORDING -> {
                val phoneNumber = intent?.getStringExtra(EXTRA_PHONE_NUMBER) ?: "Unknown"
                val contactName = intent?.getStringExtra(EXTRA_CONTACT_NAME)
                val callTypeString = intent?.getStringExtra(EXTRA_CALL_TYPE) ?: CallType.INCOMING.name
                val callType = try {
                    CallType.valueOf(callTypeString)
                } catch (_: Exception) {
                    CallType.INCOMING
                }

                startRecording(phoneNumber, contactName, callType)
            }
            ACTION_STOP_RECORDING -> {
                stopRecordingAndSave()
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecording(phoneNumber: String, contactName: String?, callType: CallType) {
        if (CallStateTracker.isRecording.value) {
            Log.d(TAG, "Already recording, skipping duplicate start.")
            return
        }

        currentPhoneNumber = phoneNumber
        currentContactName = contactName
        currentCallType = callType

        val settingsRepo = CallRecorderApplication.instance.settingsRepository
        currentAudioSource = settingsRepo.audioSource.value
        currentAudioFormat = settingsRepo.audioFormat.value

        // Check speakerphone boost setting
        if (settingsRepo.autoSpeakerBoost.value) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let {
                wasSpeakerphoneOnInitial = it.isSpeakerphoneOn
                if (!wasSpeakerphoneOnInitial) {
                    try {
                        it.mode = AudioManager.MODE_IN_CALL
                        it.isSpeakerphoneOn = true
                    } catch (_: Exception) {
                    }
                }
            }
        }

        // Start Foreground Service with notification
        val notification = buildOngoingNotification(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val serviceType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE or ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
            } else {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
            }
            try {
                startForeground(NOTIFICATION_ID, notification, serviceType)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting foreground with type: ${e.message}")
                startForeground(NOTIFICATION_ID, notification)
            }
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Prepare File & MediaRecorder
        val success = initAndStartRecorder()
        if (!success) {
            Log.e(TAG, "Failed to initialize audio recorder.")
            stopSelf()
            return
        }

        recordingStartTime = System.currentTimeMillis()
        durationSeconds = 0

        CallStateTracker.onRecordingStarted(currentPhoneNumber, currentContactName, currentCallType)

        // Launch monitoring loop for timer and amplitude
        recordingJob?.cancel()
        recordingJob = serviceScope.launch {
            while (isActive) {
                delay(500)
                durationSeconds = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
                CallStateTracker.updateDuration(durationSeconds)

                // Read amplitude safely
                try {
                    val maxAmp = mediaRecorder?.maxAmplitude ?: 0
                    val normalized = (maxAmp / 32767f).coerceIn(0f, 1f)
                    CallStateTracker.updateAmplitude(normalized)
                } catch (_: Exception) {
                }

                // Update notification every 2 seconds
                if (durationSeconds % 2 == 0) {
                    val updatedNotification = buildOngoingNotification(durationSeconds)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    manager.notify(NOTIFICATION_ID, updatedNotification)
                }
            }
        }
    }

    private fun initAndStartRecorder(): Boolean {
        val extension = if (currentAudioFormat == SettingsRepository.FORMAT_3GP) "3gp" else "m4a"
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "REC_${timeStamp}_${currentPhoneNumber.replace(Regex("[^0-9]"), "")}.$extension"

        val dir = getExternalFilesDir(android.os.Environment.DIRECTORY_RECORDINGS) ?: filesDir
        if (!dir.exists()) {
            dir.mkdirs()
        }
        val file = File(dir, fileName)
        currentRecordingFile = file

        // Try primary audio source, fallback to MIC if failed
        val preferredSource = when (currentAudioSource) {
            SettingsRepository.SOURCE_VOICE_COMMUNICATION -> MediaRecorder.AudioSource.VOICE_COMMUNICATION
            SettingsRepository.SOURCE_VOICE_RECOGNITION -> MediaRecorder.AudioSource.VOICE_RECOGNITION
            else -> MediaRecorder.AudioSource.MIC
        }

        val success = tryConfigureAndStart(file, preferredSource)
        if (!success && preferredSource != MediaRecorder.AudioSource.MIC) {
            Log.w(TAG, "Preferred audio source failed, falling back to MIC")
            return tryConfigureAndStart(file, MediaRecorder.AudioSource.MIC)
        }
        return success
    }

    private fun tryConfigureAndStart(file: File, audioSource: Int): Boolean {
        try {
            mediaRecorder?.release()
            mediaRecorder = null

            val recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(this)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            recorder.setAudioSource(audioSource)

            if (currentAudioFormat == SettingsRepository.FORMAT_3GP) {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            } else {
                recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                recorder.setAudioEncodingBitRate(96000)
                recorder.setAudioSamplingRate(44100)
            }

            recorder.setOutputFile(file.absolutePath)
            recorder.prepare()
            recorder.start()

            mediaRecorder = recorder
            return true
        } catch (e: Exception) {
            Log.e(TAG, "tryConfigureAndStart failed: ${e.message}", e)
            mediaRecorder?.release()
            mediaRecorder = null
            return false
        }
    }

    private fun stopRecordingAndSave() {
        recordingJob?.cancel()
        recordingJob = null

        // Stop media recorder safely
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "mediaRecorder stop exception (call may have been too short): ${e.message}")
        } finally {
            try {
                mediaRecorder?.reset()
                mediaRecorder?.release()
            } catch (_: Exception) {
            }
            mediaRecorder = null
        }

        // Restore speakerphone if boosted
        val settingsRepo = CallRecorderApplication.instance.settingsRepository
        if (settingsRepo.autoSpeakerBoost.value && !wasSpeakerphoneOnInitial) {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            try {
                audioManager?.isSpeakerphoneOn = false
                audioManager?.mode = AudioManager.MODE_NORMAL
            } catch (_: Exception) {
            }
        }

        CallStateTracker.onRecordingStopped()

        val file = currentRecordingFile
        if (file != null && file.exists() && file.length() > 2000 && durationSeconds >= 1) {
            // Valid recording file! Save to Room database
            val entity = CallRecordingEntity(
                phoneNumber = currentPhoneNumber,
                contactName = currentContactName,
                callType = currentCallType,
                timestamp = recordingStartTime,
                durationSeconds = durationSeconds,
                filePath = file.absolutePath,
                fileSizeBytes = file.length(),
                isFavorite = false,
                notes = "",
                audioSource = currentAudioSource,
                audioFormat = currentAudioFormat
            )

            serviceScope.launch(Dispatchers.IO) {
                CallRecorderApplication.instance.recordingRepository.insertRecording(entity)
                // Also check auto-cleanup policy
                val keepDays = settingsRepo.autoDeleteDays.value
                if (keepDays > 0) {
                    CallRecorderApplication.instance.recordingRepository.applyAutoCleanup(keepDays)
                }
            }

            showSummaryNotification(durationSeconds, currentContactName ?: currentPhoneNumber)
        } else {
            // Recording too short or empty; clean up file
            file?.delete()
            Log.d(TAG, "Recording discarded (duration < 1s or empty)")
        }

        currentRecordingFile = null
    }

    private fun buildOngoingNotification(durationSec: Int): Notification {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            0,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, CallRecordService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val name = currentContactName ?: currentPhoneNumber
        val durationFormatted = Formatters.formatDuration(durationSec)

        return NotificationCompat.Builder(this, CallRecorderApplication.CHANNEL_RECORDING_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Recording Call ($durationFormatted)")
            .setContentText(name)
            .setSubText(currentCallType.getDisplayName())
            .setOngoing(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.ic_media_pause,
                "Stop & Save",
                stopPendingIntent
            )
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun showSummaryNotification(durationSec: Int, caller: String) {
        val appIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this,
            2,
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val summary = NotificationCompat.Builder(this, CallRecorderApplication.CHANNEL_SUMMARY_ID)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentTitle("Call Recording Saved")
            .setContentText("$caller - ${Formatters.formatDuration(durationSec)}")
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        manager.notify(SUMMARY_NOTIFICATION_ID, summary)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (CallStateTracker.isRecording.value) {
            stopRecordingAndSave()
        }
    }

    companion object {
        private const val TAG = "CallRecordService"
        private const val NOTIFICATION_ID = 1001
        private const val SUMMARY_NOTIFICATION_ID = 1002

        const val ACTION_START_RECORDING = "com.example.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.action.STOP_RECORDING"

        const val EXTRA_PHONE_NUMBER = "extra_phone_number"
        const val EXTRA_CONTACT_NAME = "extra_contact_name"
        const val EXTRA_CALL_TYPE = "extra_call_type"

        fun startRecording(context: Context, phoneNumber: String, contactName: String?, callType: CallType) {
            val intent = Intent(context, CallRecordService::class.java).apply {
                action = ACTION_START_RECORDING
                putExtra(EXTRA_PHONE_NUMBER, phoneNumber)
                putExtra(EXTRA_CONTACT_NAME, contactName)
                putExtra(EXTRA_CALL_TYPE, callType.name)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopRecording(context: Context) {
            val intent = Intent(context, CallRecordService::class.java).apply {
                action = ACTION_STOP_RECORDING
            }
            context.startService(intent)
        }
    }
}
