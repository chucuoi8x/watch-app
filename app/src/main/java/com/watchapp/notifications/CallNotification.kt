package com.watchapp.notifications

data class CallNotification(
    val packageName: String,
    val callerName: String,
    val callType: CallType,
    val isVideoCall: Boolean,
    val timestamp: Long = System.currentTimeMillis()
) {
    enum class CallType {
        INCOMING,
        MISSED,
        OUTGOING,
        UNKNOWN
    }

    val appName: String
        get() = when (packageName) {
            "com.zing.zalo" -> "Zalo"
            "com.facebook.orca" -> "Messenger"
            else -> "Unknown"
        }
}
