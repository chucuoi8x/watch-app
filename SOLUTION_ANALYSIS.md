# Phân tích chi tiết Giải pháp 1: Notification-Only

## Tổng quan

Giải pháp này chỉ hiển thị thông báo cuộc gọi Zalo/Messenger lên đồng hồ, không cho phép trả lời từ đồng hồ.

## Kiến trúc kỹ thuật

### 1. NotificationListenerService (Android)

**Cơ chế hoạt động:**
- Android cung cấp `NotificationListenerService` cho phép app đọc tất cả thông báo từ system
- Khi Zalo/Messenger nhận cuộc gọi, chúng tạo notification với:
  - Package name: `com.zing.zalo` hoặc `com.facebook.orca` (Messenger)
  - Title: "Cuộc gọi đến từ [Tên]"
  - Content: "Đang đổ chuông..."
  - Extras: Có thể chứa caller ID, avatar URL

**Code structure:**
```kotlin
class CallNotificationListener : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val packageName = sbn.packageName
        val notification = sbn.notification
        
        // Filter chỉ Zalo/Messenger
        if (packageName in listOf("com.zing.zalo", "com.facebook.orca")) {
            val title = notification.extras.getString(Notification.EXTRA_TITLE)
            val text = notification.extras.getString(Notification.EXTRA_TEXT)
            
            // Parse tên người gọi
            val callerName = extractCallerName(title, text)
            
            // Log hoặc forward đến Huawei Health
            Log.d("CallNotification", "Cuộc gọi từ: $callerName")
        }
    }
}
```

### 2. Huawei Health Notification Sync

**Cơ chế:**
- Huawei Health app đã có sẵn tính năng sync notification từ điện thoại lên đồng hồ
- Khi app bật "Notification" trong Huawei Health, tất cả notification từ điện thoại sẽ được đẩy lên đồng hồ
- Đồng hồ hiển thị:
  - Icon app (Zalo/Messenger)
  - Title (tên người gọi)
  - Content (trạng thái cuộc gọi)

**Yêu cầu:**
- Người dùng phải bật notification sync trong Huawei Health
- Không cần code thêm, chỉ cần đảm bảo notification được tạo đúng format

### 3. Limitations

**Không thể trả lời từ đồng hồ:**
- Notification chỉ là text, không có action button
- Để trả lời, người dùng phải:
  1. Mở điện thoại
  2. Mở app Zalo/Messenger
  3. Nhấn "Trả lời"

**Tên người gọi:**
- Zalo: Thường hiển thị tên từ danh bạ Zalo
- Messenger: Hiển thị tên từ danh bạ Facebook
- Nếu không có trong danh bạ, hiển thị số điện thoại (Zalo) hoặc tên Facebook (Messenger)

**Độ trễ:**
- Notification → Đồng hồ: ~1-3 giây (Bluetooth latency)
- Phụ thuộc vào khoảng cách Bluetooth và số lượng thiết bị kết nối

## Plan triển khai

### Phase 1: Setup project (Ngày 1)

**Tasks:**
1. Tạo Android project với Kotlin
2. Cấu hình `AndroidManifest.xml`:
   - Request `BIND_NOTIFICATION_LISTENER_SERVICE` permission
   - Register `CallNotificationListener`
3. Setup Gradle dependencies:
   - AndroidX Core
   - Kotlin Coroutines (cho async operations)

**Deliverables:**
- Project structure hoàn chỉnh
- Build thành công trên Android Studio

---

### Phase 2: Implement NotificationListenerService (Ngày 2)

**Tasks:**
1. Tạo `CallNotificationListener` class
2. Override `onNotificationPosted()` và `onNotificationRemoved()`
3. Filter package name:
   - Zalo: `com.zing.zalo`
   - Messenger: `com.facebook.orca`
4. Parse notification extras:
   - `EXTRA_TITLE`: Tiêu đề thông báo
   - `EXTRA_TEXT`: Nội dung thông báo
   - `EXTRA_BIG_TEXT`: Nội dung dài (nếu có)
5. Extract caller name từ title/text

**Testing:**
- Unit test cho parsing logic
- Integration test với mock notification

**Deliverables:**
- `CallNotificationListener` hoạt động
- Log caller name khi nhận cuộc gọi

---

### Phase 3: UI và Permission handling (Ngày 3)

**Tasks:**
1. Tạo `MainActivity` với:
   - Hiển thị trạng thái service (running/stopped)
   - Hướng dẫn cấp quyền Notification Access
   - Log viewer (hiển thị cuộc gọi gần nhất)
2. Check và request permission:
   - `Settings.Secure.isNotificationListenerEnabled()`
   - Redirect đến Settings nếu chưa cấp quyền
3. Foreground service (optional):
   - Giữ service chạy nền
   - Hiển thị notification "Đang lắng nghe cuộc gọi"

**Deliverables:**
- UI hoàn chỉnh
- Permission flow hoạt động

---

### Phase 4: Testing và Optimization (Ngày 4)

**Tasks:**
1. Test trên OPPO Find X8 Ultra:
   - Cuộc gọi Zalo
   - Cuộc gọi Messenger
   - Cuộc gọi video
   - Cuộc gọi nhóm
2. Test với Huawei GT 3 Pro:
   - Notification có hiển thị trên đồng hồ không
   - Tên người gọi có đúng không
   - Độ trễ bao nhiêu giây
3. Optimization:
   - Filter false positives (notification không phải cuộc gọi)
   - Handle edge cases (cuộc gọi nhỡ, cuộc gọi đang chờ)
   - Battery optimization (không drain pin)

**Deliverables:**
- Test report
- Bug fixes
- Performance metrics

---

### Phase 5: Documentation và Release (Ngày 5)

**Tasks:**
1. Viết README.md:
   - Installation guide
   - Usage instructions
   - Troubleshooting
2. Tạo APK release build
3. Test installation trên thiết bị thực

**Deliverables:**
- APK file
- Documentation hoàn chỉnh

---

## Timeline tổng hợp

| Ngày | Task | Output |
|------|------|--------|
| 1 | Setup project | Project structure |
| 2 | NotificationListenerService | Service hoạt động |
| 3 | UI + Permission | App hoàn chỉnh |
| 4 | Testing | Test report + fixes |
| 5 | Documentation | APK + docs |

**Tổng thời gian: 5 ngày làm việc**

---

## Rủi ro và mitigation

### Rủi ro 1: Notification format thay đổi
**Vấn đề:** Zalo/Messenger có thể thay đổi format notification trong update
**Mitigation:**
- Parse linh hoạt (regex, keyword matching)
- Log raw notification để debug
- Có fallback cho unknown format

### Rủi ro 2: Huawei Health không sync notification
**Vấn đề:** Một số notification không được đẩy lên đồng hồ
**Mitigation:**
- Kiểm tra Huawei Health settings
- Test với notification từ app khác (SMS, WhatsApp)
- Nếu cần, tạo notification "giả" để trigger sync

### Rủi ro 3: Battery drain
**Vấn đề:** NotificationListenerService chạy nền liên tục
**Mitigation:**
- Chỉ process khi có notification mới
- Không dùng wake lock
- Recommend user bật battery optimization cho app

### Rủi ro 4: Permission bị revoke
**Vấn đề:** Android có thể revoke permission khi app không hoạt động
**Mitigation:**
- Check permission mỗi khi app mở
- Hiển thị warning nếu permission bị revoke
- Redirect đến Settings để re-enable

---

## Kết luận

Giải pháp 1 khả thi và ổn định, nhưng có hạn chế lớn: không thể trả lời cuộc gọi từ đồng hồ.

**Khuyến nghị:**
- Triển khai Giải pháp 1 trước (5 ngày)
- Nếu cần trả lời từ đồng hồ, cân nhắc Giải pháp 2 (auto-answer) hoặc mua smartwatch hỗ trợ VoIP (Apple Watch, Samsung Galaxy Watch với Wear OS)
