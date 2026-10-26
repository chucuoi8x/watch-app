# Hướng dẫn Build APK qua GitHub Actions

## Bước 1: Tạo repository trên GitHub

1. Truy cập https://github.com/new
2. Repository name: `watch-app`
3. Chọn **Private** (hoặc Public)
4. **KHÔNG** tick "Add a README file" (vì đã có sẵn)
5. Click **Create repository**

## Bước 2: Push code lên GitHub

Mở terminal tại thư mục `C:\Users\Administrator\Desktop\watch-app` và chạy:

```bash
# Initialize git
git init
git add .
git commit -m "Initial commit: Watch App notification forwarder"

# Add remote (thay YOUR_USERNAME bằng username GitHub của bạn)
git remote add origin https://github.com/YOUR_USERNAME/watch-app.git

# Push
git branch -M main
git push -u origin main
```

## Bước 3: GitHub Actions tự động build

Sau khi push, GitHub Actions sẽ tự động:
1. Checkout code
2. Setup JDK 17
3. Build APK
4. Upload artifact

Thời gian build: ~5-10 phút

## Bước 4: Download APK

1. Truy cập: `https://github.com/YOUR_USERNAME/watch-app/actions`
2. Click vào workflow run mới nhất (có dấu ✓ xanh)
3. Kéo xuống phần **Artifacts**
4. Click **watch-app-debug** để download
5. File APK: `app-debug.apk`

## Bước 5: Cài đặt APK lên điện thoại

1. Copy file APK sang OPPO Find X8 Ultra
2. Mở File Manager → tìm file APK
3. Cho phép "Install from unknown sources" nếu được hỏi
4. Cài đặt APK
5. Mở app và cấp quyền Notification Access

## Troubleshooting

### Lỗi "gradlew not found"
```bash
git update-index --chmod=+x gradlew
git commit -m "Fix gradlew permissions"
git push
```

### Lỗi build
- Kiểm tra tab "Actions" trên GitHub để xem log lỗi
- Thường do version conflict hoặc missing dependencies

### Không thấy artifact
- Artifact chỉ lưu 30 ngày
- Có thể tăng `retention-days` trong `.github/workflows/build.yml`
