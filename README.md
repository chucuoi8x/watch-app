# Watch App - Notification Forwarder

Ứng dụng Android đọc thông báo từ Zalo và Facebook Messenger, chuẩn bị để đẩy lên đồng hồ.

## Yêu cầu hệ thống
- Android 7.0+ (API 24+)
- OPPO Find X8 Ultra
- Huawei GT 3 Pro (qua Huawei Health app)

## Hạn chế kỹ thuật
Đồng hồ Huawei GT 3 Pro sử dụng giao thức Bluetooth độc quyền. Ứng dụng này chỉ có thể:
1. Đọc thông báo từ Zalo/Facebook Messenger
2. Hiển thị log thông báo
3. Gửi intent đến Huawei Health (nếu hỗ trợ)

## Cài đặt
1. Build app bằng Android Studio
2. Cài đặt APK lên điện thoại
3. Cấp quyền Notification Access trong Settings
4. Cấp quyền Accessibility Service

## Cấu trúc
- `NotificationListenerService`: Bắt thông báo từ Zalo/Messenger
- `MainActivity`: Hiển thị trạng thái và log
- `NotificationReceiver`: Xử lý và lọc thông báo cuộc gọi

## Mở rộng
Để đẩy thông báo lên đồng hồ Huawei, cần:
- Reverse-engineer giao thức Bluetooth của Huawei
- Hoặc sử dụng Huawei Health Kit API (nếu có)
- Hoặc tích hợp với Tasker/AutoNotification để trigger Huawei Health
