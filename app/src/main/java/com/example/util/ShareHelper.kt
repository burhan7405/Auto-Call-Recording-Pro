package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.CallRecordingEntity
import java.io.File

object ShareHelper {

    fun shareRecording(context: Context, recording: CallRecordingEntity) {
        val file = File(recording.filePath)
        if (!file.exists()) {
            Toast.makeText(context, "Recording file not found on device.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val authority = "${context.packageName}.fileprovider"
            val uri: Uri = FileProvider.getUriForFile(context, authority, file)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (recording.audioFormat == "3GP") "audio/3gpp" else "audio/mp4"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(
                    Intent.EXTRA_SUBJECT,
                    "Call Recording: ${recording.contactName ?: recording.phoneNumber}"
                )
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Call recording with ${recording.contactName ?: recording.phoneNumber} (${Formatters.formatTimestamp(recording.timestamp)})"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Call Recording")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to share: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
