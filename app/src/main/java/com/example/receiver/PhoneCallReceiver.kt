package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.TelephonyManager
import android.util.Log
import com.example.CallRecorderApplication
import com.example.data.CallType
import com.example.service.CallRecordService
import com.example.util.ContactHelper

class PhoneCallReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) {
            return
        }

        val stateStr = intent.getStringExtra(TelephonyManager.EXTRA_STATE) ?: return
        val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

        if (!incomingNumber.isNullOrBlank()) {
            savedNumber = incomingNumber
        }

        val state = when (stateStr) {
            TelephonyManager.EXTRA_STATE_IDLE -> TelephonyManager.CALL_STATE_IDLE
            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
            else -> return
        }

        onCallStateChanged(context, state, savedNumber)
    }

    private fun onCallStateChanged(context: Context, state: Int, number: String?) {
        if (lastState == state) {
            return
        }

        when (state) {
            TelephonyManager.CALL_STATE_RINGING -> {
                isIncoming = true
                savedNumber = number
                Log.d(TAG, "Incoming call ringing: $number")
            }

            TelephonyManager.CALL_STATE_OFFHOOK -> {
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    // Incoming call answered
                    isIncoming = true
                    Log.d(TAG, "Incoming call answered: $savedNumber")
                    triggerAutoRecord(context, savedNumber ?: "Unknown", CallType.INCOMING)
                } else {
                    // Outgoing call started
                    isIncoming = false
                    Log.d(TAG, "Outgoing call offhook: $savedNumber")
                    triggerAutoRecord(context, savedNumber ?: "Unknown", CallType.OUTGOING)
                }
            }

            TelephonyManager.CALL_STATE_IDLE -> {
                Log.d(TAG, "Call ended (IDLE). Stopping recorder.")
                CallRecordService.stopRecording(context)
                isIncoming = false
                savedNumber = null
            }
        }

        lastState = state
    }

    private fun triggerAutoRecord(context: Context, number: String, callType: CallType) {
        try {
            val contactName = ContactHelper.getContactName(context, number)
            val settingsRepo = CallRecorderApplication.instance.settingsRepository

            if (settingsRepo.shouldRecordCall(number, contactName != null)) {
                Log.i(TAG, "Starting auto call recording for $number ($callType)")
                CallRecordService.startRecording(context, number, contactName, callType)
            } else {
                Log.i(TAG, "Skipping auto call recording for $number due to rules/exclusions.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in triggerAutoRecord: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "PhoneCallReceiver"
        private var lastState = TelephonyManager.CALL_STATE_IDLE
        private var isIncoming = false
        private var savedNumber: String? = null
    }
}
