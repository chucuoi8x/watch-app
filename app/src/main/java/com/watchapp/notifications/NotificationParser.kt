package com.watchapp.notifications

object NotificationParser {

    fun parseCallNotification(
        packageName: String,
        title: String,
        text: String
    ): CallNotification? {
        return when (packageName) {
            "com.zing.zalo" -> parseZaloCall(title, text)
            "com.facebook.orca" -> parseMessengerCall(title, text)
            else -> null
        }
    }

    private fun parseZaloCall(title: String, text: String): CallNotification? {
        // Zalo notification formats:
        // - "Cuộc gọi đến từ [Tên]"
        // - "[Tên] đang gọi cho bạn"
        // - "Cuộc gọi nhỡ từ [Tên]"
        // - "[Tên] đã gọi cho bạn"

        val callerName = extractCallerName(title, text) ?: return null

        val callType = when {
            title.contains("đang gọi", ignoreCase = true) ||
            title.contains("Cuộc gọi đến", ignoreCase = true) -> CallNotification.CallType.INCOMING

            title.contains("nhỡ", ignoreCase = true) ||
            title.contains("bỏ lỡ", ignoreCase = true) -> CallNotification.CallType.MISSED

            text.contains("đã gọi", ignoreCase = true) -> CallNotification.CallType.OUTGOING

            else -> CallNotification.CallType.UNKNOWN
        }

        val isVideoCall = text.contains("video", ignoreCase = true)

        return CallNotification(
            packageName = "com.zing.zalo",
            callerName = callerName,
            callType = callType,
            isVideoCall = isVideoCall
        )
    }

    private fun parseMessengerCall(title: String, text: String): CallNotification? {
        // Messenger notification formats:
        // - Title: "[Tên]", Text: "Cuộc gọi video" or "Cuộc gọi thoại"
        // - Title: "[Tên]", Text: "Đang gọi cho bạn..."
        // - Title: "[Tên]", Text: "Cuộc gọi nhỡ"

        val callerName = title.takeIf { it.isNotBlank() } ?: return null

        val callType = when {
            text.contains("đang gọi", ignoreCase = true) ||
            text.contains("calling", ignoreCase = true) -> CallNotification.CallType.INCOMING

            text.contains("nhỡ", ignoreCase = true) ||
            text.contains("missed", ignoreCase = true) -> CallNotification.CallType.MISSED

            else -> CallNotification.CallType.UNKNOWN
        }

        val isVideoCall = text.contains("video", ignoreCase = true)

        return CallNotification(
            packageName = "com.facebook.orca",
            callerName = callerName,
            callType = callType,
            isVideoCall = isVideoCall
        )
    }

    private fun extractCallerName(title: String, text: String): String? {
        // Try to extract name from common patterns
        val patterns = listOf(
            Regex("Cuộc gọi đến từ (.+)"),
            Regex("(.+) đang gọi cho bạn"),
            Regex("Cuộc gọi nhỡ từ (.+)"),
            Regex("(.+) đã gọi cho bạn")
        )

        for (pattern in patterns) {
            pattern.find(title)?.let { match ->
                return match.groupValues[1].trim()
            }
            pattern.find(text)?.let { match ->
                return match.groupValues[1].trim()
            }
        }

        // Fallback: if title looks like a name (not a generic message)
        return title.takeIf { it.isNotBlank() && !it.contains("cuộc gọi", ignoreCase = true) }
    }
}
