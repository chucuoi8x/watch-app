package com.watchapp.notifications

import org.junit.Assert.*
import org.junit.Test

class NotificationParserTest {

    @Test
    fun testParseZaloIncomingCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.zing.zalo",
            title = "Cuộc gọi đến từ Nguyễn Văn A",
            text = "Đang đổ chuông..."
        )

        assertNotNull(result)
        assertEquals("Nguyễn Văn A", result?.callerName)
        assertEquals(CallNotification.CallType.INCOMING, result?.callType)
        assertEquals("Zalo", result?.appName)
        assertFalse(result?.isVideoCall ?: true)
    }

    @Test
    fun testParseZaloMissedCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.zing.zalo",
            title = "Cuộc gọi nhỡ từ Trần Thị B",
            text = ""
        )

        assertNotNull(result)
        assertEquals("Trần Thị B", result?.callerName)
        assertEquals(CallNotification.CallType.MISSED, result?.callType)
    }

    @Test
    fun testParseZaloVideoCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.zing.zalo",
            title = "Lê Văn C đang gọi cho bạn",
            text = "Cuộc gọi video"
        )

        assertNotNull(result)
        assertEquals("Lê Văn C", result?.callerName)
        assertEquals(CallNotification.CallType.INCOMING, result?.callType)
        assertTrue(result?.isVideoCall ?: false)
    }

    @Test
    fun testParseMessengerIncomingCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.facebook.orca",
            title = "John Doe",
            text = "Đang gọi cho bạn..."
        )

        assertNotNull(result)
        assertEquals("John Doe", result?.callerName)
        assertEquals(CallNotification.CallType.INCOMING, result?.callType)
        assertEquals("Messenger", result?.appName)
    }

    @Test
    fun testParseMessengerMissedCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.facebook.orca",
            title = "Jane Smith",
            text = "Cuộc gọi nhỡ"
        )

        assertNotNull(result)
        assertEquals("Jane Smith", result?.callerName)
        assertEquals(CallNotification.CallType.MISSED, result?.callType)
    }

    @Test
    fun testParseMessengerVideoCall() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.facebook.orca",
            title = "Bob Johnson",
            text = "Cuộc gọi video"
        )

        assertNotNull(result)
        assertEquals("Bob Johnson", result?.callerName)
        assertTrue(result?.isVideoCall ?: false)
    }

    @Test
    fun testParseNonCallNotification() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.zing.zalo",
            title = "Tin nhắn mới",
            text = "Xin chào"
        )

        // Should still parse but with UNKNOWN type
        assertNotNull(result)
        assertEquals(CallNotification.CallType.UNKNOWN, result?.callType)
    }

    @Test
    fun testParseUnsupportedPackage() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.whatsapp",
            title = "Test",
            text = "Test"
        )

        assertNull(result)
    }

    @Test
    fun testParseEmptyTitle() {
        val result = NotificationParser.parseCallNotification(
            packageName = "com.facebook.orca",
            title = "",
            text = "Đang gọi cho bạn..."
        )

        assertNull(result)
    }
}
