package com.example.data

enum class CallType {
    INCOMING,
    OUTGOING,
    SIMULATED;

    fun getDisplayName(): String = when (this) {
        INCOMING -> "Incoming Call"
        OUTGOING -> "Outgoing Call"
        SIMULATED -> "Test Simulator Call"
    }
}
