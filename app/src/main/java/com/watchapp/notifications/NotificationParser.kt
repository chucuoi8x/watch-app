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
        // - Title: "[Tên]", Text: "Đang gọi đến" (incoming)
        // - Title: "[Tên]", Text: "Cuộc gọi nhỡ" (missed)
        // - Title: "Cuộc gọi đến từ [Tên]", Text: "..." (old format)

        val callType = when {
            text.contains("đang gọi", ignoreCase = true) ||
            text.contains("Đang gọi đến", ignoreCase = true) -> CallNotification.CallType.INCOMING

            text.contains("nhỡ", ignoreCase = true) ||
            text.contains("bỏ lỡ", ignoreCase = true) -> CallNotification.CallType.MISSED

            text.contains("đã gọi", ignoreCase = true) -> CallNotification.CallType.OUTGOING

            else -> CallNotification.CallType.UNKNOWN
        }

        // Try to extract caller name from title
        val callerName = when {
            // New format: Title is the caller name
            callType == CallNotification.CallType.INCOMING && text.contains("gọi", ignoreCase = true) -> {
                title.takeIf { it.isNotBlank() }
            }
            // Old format: "Cuộc gọi đến từ [Tên]"
            title.contains("Cuộc gọi đến từ", ignoreCase = true) -> {
                Regex("Cuộc gọi đến từ (.+)").find(title)?.groupValues?.get(1)?.trim()
            }
            title.contains("Cuộc gọi nhỡ từ", ignoreCase = true) -> {
                Regex("Cuộc gọi nhỡ từ (.+)").find(title)?.groupValues?.get(1)?.trim()
            }
            // Fallback: use title if it looks like a name
            else -> title.takeIf { it.isNotBlank() && !it.contains("cuộc gọi", ignoreCase = true) }
        } ?: return null

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
