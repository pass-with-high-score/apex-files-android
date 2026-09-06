# Quy Định Về Tương Tác Thiết Bị (Device Control Policy)

## TUYỆT ĐỐI CẤM ĐIỀU KHIỂN THIẾT BỊ CỦA NGƯỜI DÙNG

1. **Cấm gửi lệnh input qua ADB:**
   - TUYỆT ĐỐI KHÔNG dùng `adb shell input tap`.
   - TUYỆT ĐỐI KHÔNG dùng `adb shell input swipe`.
   - TUYỆT ĐỐI KHÔNG dùng `adb shell input keyevent` (Back, Home, Menu...).
   - TUYỆT ĐỐI KHÔNG dùng `adb shell input text` để tự gõ phím.

2. **Cấm tự ý mở màn hình / test thay người dùng:**
   - TUYỆT ĐỐI KHÔNG tự ý chạy các luồng kiểm thử giao diện bằng cách điều khiển thiết bị thật hoặc máy ảo của người dùng.
   - Việc trải nghiệm, nhấn chạm, kiểm tra luồng UI/UX trên thiết bị hoàn toàn do **NGƯỜI DÙNG** tự thao tác.

3. **Phạm vi được phép của AI:**
   - Biên dịch và xác minh build: `./gradlew assembleDebug`, `./gradlew test...`.
   - Cài đặt APK khi người dùng yêu cầu: `adb install -r ...`.
   - Chụp màn hình tĩnh khi người dùng yêu cầu kiểm tra hiển thị.
