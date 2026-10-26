package com.watchapp.notifications

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.watchapp.notifications.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private val refreshRunnable = object : Runnable {
        override fun run() {
            updateLogs()
            handler.postDelayed(this, 2000)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
    }

    override fun onResume() {
        super.onResume()
        updatePermissionStatus()
        handler.post(refreshRunnable)
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(refreshRunnable)
    }

    private fun setupUI() {
        binding.btnGrantPermission.setOnClickListener {
            openNotificationListenerSettings()
        }

        binding.btnShowRawLogs.setOnClickListener {
            showRawLogs()
        }
    }

    private fun updatePermissionStatus() {
        val hasPermission = isNotificationListenerEnabled()

        if (hasPermission) {
            binding.tvPermissionStatus.text = "✓ Notification Access: ĐÃ CẤP"
            binding.tvPermissionStatus.setTextColor(getColor(android.R.color.holo_green_dark))
            binding.btnGrantPermission.visibility = View.GONE
        } else {
            binding.tvPermissionStatus.text = "✗ Notification Access: CHƯA CẤP"
            binding.tvPermissionStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            binding.btnGrantPermission.visibility = View.VISIBLE
        }
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (TextUtils.isEmpty(flat)) return false

        val componentName = ComponentName(this, CallNotificationListener::class.java)
            .flattenToString()

        return flat.split(":").any { it.trim() == componentName }
    }

    private fun openNotificationListenerSettings() {
        try {
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Không thể mở cài đặt. Vui lòng mở thủ công.", Toast.LENGTH_LONG).show()
        }
    }

    private fun showRawLogs() {
        val rawLogs = CallNotificationListener.rawLogs

        if (rawLogs.isEmpty()) {
            binding.tvLogs.text = "Chưa có notification nào từ Zalo/Messenger.\n\nHãy thử gọi điện từ Zalo hoặc Messenger để kiểm tra."
            return
        }

        val sb = StringBuilder()
        sb.appendLine("=== RAW NOTIFICATION LOGS ===\n")
        sb.appendLine("(Screenshot gửi cho dev để fix parser)\n")

        for (log in rawLogs.take(5)) {
            sb.appendLine(log)
            sb.appendLine("\n---\n")
        }

        binding.tvLogs.text = sb.toString()
    }

    private fun updateLogs() {
        val logs = CallNotificationListener.callLogs

        if (logs.isEmpty()) {
            binding.tvLogs.text = "Chưa có cuộc gọi nào từ Zalo/Messenger.\n\nHãy thử gọi điện từ Zalo hoặc Messenger để kiểm tra.\n\n💡 Nếu đã gọi mà không thấy log, click \"Show Raw Logs\" để debug."
            return
        }

        val sb = StringBuilder()
        sb.appendLine("=== ${logs.size} cuộc gọi gần nhất ===\n")

        for (log in logs.take(20)) {
            val typeEmoji = when (log.callType) {
                CallNotification.CallType.INCOMING -> "📞"
                CallNotification.CallType.MISSED -> "📵"
                CallNotification.CallType.OUTGOING -> "📱"
                CallNotification.CallType.UNKNOWN -> "❓"
            }

            val videoEmoji = if (log.isVideoCall) " [VIDEO]" else ""
            val time = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(log.timestamp))

            sb.appendLine("$typeEmoji [$time] ${log.appName}: ${log.callerName}$videoEmoji")
            sb.appendLine("   Trạng thái: ${log.callType}")
            sb.appendLine()
        }

        binding.tvLogs.text = sb.toString()
    }
}
