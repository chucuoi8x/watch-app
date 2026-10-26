# Hướng dẫn cấu hình Huawei Health

## Vấn đề
Đồng hồ hiển thị "Số không xác định" thay vì tên người gọi.

## Nguyên nhân
Huawei Health cần được cấu hình để sync notification từ app "Watch App".

## Giải pháp

### Bước 1: Kiểm tra Notification Settings trong Huawei Health
1. Mở app **Huawei Health** trên điện thoại
2. Vào **Devices** → Chọn đồng hồ của bạn
3. Tìm **Notification** hoặc **Thông báo**
4. Đảm bảo đã bật **Tất cả thông báo** hoặc thêm **Watch App** vào danh sách

### Bước 2: Kiểm tra quyền notification của Watch App
1. Vào **Settings** → **Apps** → **Watch App**
2. Vào **Notifications**
3. Đảm bảo tất cả các loại notification đều được bật

### Bước 3: Test notification
1. Mở app Watch App
2. Nhờ ai đó gọi Zalo
3. Kiểm tra xem notification có hiển thị trên đồng hồ không
4. Nếu vẫn không hiển thị, thử restart đồng hồ

### Bước 4: Kiểm tra Bluetooth
1. Đảm bảo đồng hồ đã kết nối Bluetooth với điện thoại
2. Vào **Settings** → **Bluetooth** → Chọn đồng hồ
3. Đảm bảo **Phone calls** và **Contact sharing** đã bật

## Lưu ý
- Một số đồng hồ Huawei chỉ sync notification từ các app được whitelist
- Nếu vẫn không hoạt động, có thể cần root hoặc custom firmware
- Giải pháp thay thế: Sử dụng Tasker + AutoNotification plugin
