package com.watchapp.notifications

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class CallNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "CallNotificationListener"
        private val SUPPORTED_PACKAGES = setOf(
            "com.zing.zalo",
            "com.facebook.orca"
        )

        var instance: CallNotificationListener? = null
        private val _callLogs = mutableListOf<CallNotification>()
        val callLogs: List<CallNotification> get() = _callLogs.toList()

        // Raw logs for debugging
        private val _rawLogs = mutableListOf<String>()
        val rawLogs: List<String> get() = _rawLogs.toList()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "Service started")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Service stopped")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName !in SUPPORTED_PACKAGES) {
            return
        }

        val notification = sbn.notification
        val extras = notification.extras

        val title = extras?.getCharSequence("android.title")?.toString() ?: ""
        val text = extras?.getCharSequence("android.text")?.toString() ?: ""
        val bigText = extras?.getCharSequence("android.bigText")?.toString() ?: ""
        val summary = extras?.getCharSequence("android.summaryText")?.toString() ?: ""

        // Log raw data for debugging
        val rawLog = """
            === $packageName ===
            Title: "$title"
            Text: "$text"
            BigText: "$bigText"
            Summary: "$summary"
            Category: ${notification.category}
            Time: ${sbn.postTime}
        """.trimIndent()

        Log.d(TAG, rawLog)
        _rawLogs.add(0, rawLog)
        if (_rawLogs.size > 20) {
            _rawLogs.removeAt(_rawLogs.size - 1)
        }

        // Parse notification
        val callNotification = NotificationParser.parseCallNotification(packageName, title, text)

        if (callNotification != null) {
            Log.d(TAG, "Parsed: ${callNotification.callerName} - ${callNotification.callType}")
            _callLogs.add(0, callNotification)
            if (_callLogs.size > 50) {
                _callLogs.removeAt(_callLogs.size - 1)
            }
        } else {
            Log.d(TAG, "Not a call notification")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val packageName = sbn.packageName
        if (packageName !in SUPPORTED_PACKAGES) return
        Log.d(TAG, "Notification removed from $packageName")
    }
}
