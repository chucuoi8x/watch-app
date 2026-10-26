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

        // Singleton để MainActivity có thể truy cập logs
        var instance: CallNotificationListener? = null
        private val _callLogs = mutableListOf<CallNotification>()
        val callLogs: List<CallNotification> get() = _callLogs.toList()
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

        Log.d(TAG, "Raw notification from $packageName:")
        Log.d(TAG, "  Title: $title")
        Log.d(TAG, "  Text: $text")

        // Parse notification
        val callNotification = NotificationParser.parseCallNotification(packageName, title, text)

        if (callNotification != null) {
            Log.d(TAG, "Parsed call notification:")
            Log.d(TAG, "  App: ${callNotification.appName}")
            Log.d(TAG, "  Caller: ${callNotification.callerName}")
            Log.d(TAG, "  Type: ${callNotification.callType}")
            Log.d(TAG, "  Video: ${callNotification.isVideoCall}")

            // Add to logs
            _callLogs.add(0, callNotification)
            if (_callLogs.size > 50) {
                _callLogs.removeAt(_callLogs.size - 1)
            }

            // Notify MainActivity if it's active
            instance?.let {
                // Trigger UI update if needed
            }
        } else {
            Log.d(TAG, "Not a call notification, ignoring")
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return

        val packageName = sbn.packageName
        if (packageName !in SUPPORTED_PACKAGES) {
            return
        }

        Log.d(TAG, "Notification removed from $packageName")
    }
}
