package com.watchapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat

class CallNotificationListener : NotificationListenerService() {

    companion object {
        private const val TAG = "CallNotificationListener"
        private const val CHANNEL_ID = "watch_app_calls"
        private const val NOTIFICATION_ID = 1001
        
        private val SUPPORTED_PACKAGES = setOf(
            "com.zing.zalo",
            "com.facebook.orca"
        )

        var instance: CallNotificationListener? = null
        private val _callLogs = mutableListOf<CallNotification>()
        val callLogs: List<CallNotification> get() = _callLogs.toList()

        private val _rawLogs = mutableListOf<String>()
        val rawLogs: List<String> get() = _rawLogs.toList()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
        Log.d(TAG, "Service started")
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "Service stopped")
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cuộc gọi Zalo/Messenger",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Thông báo cuộc gọi từ Zalo và Messenger"
                enableVibration(true)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
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

        // Log raw data
        val rawLog = """
            === $packageName ===
            Title: "$title"
            Text: "$text"
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

            // Forward to watch via new notification
            if (callNotification.callType == CallNotification.CallType.INCOMING) {
                forwardCallToWatch(callNotification)
            }
        } else {
            Log.d(TAG, "Not a call notification")
        }
    }

    private fun forwardCallToWatch(call: CallNotification) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("📞 ${call.appName}: ${call.callerName}")
            .setContentText("Cuộc gọi ${if (call.isVideoCall) "video" else "thoại"} đang đến")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(0, 500, 200, 500))
            .build()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
        
        Log.d(TAG, "Forwarded call notification to watch")
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        sbn ?: return
        val packageName = sbn.packageName
        if (packageName !in SUPPORTED_PACKAGES) return
        Log.d(TAG, "Notification removed from $packageName")
        
        // Cancel forwarded notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
