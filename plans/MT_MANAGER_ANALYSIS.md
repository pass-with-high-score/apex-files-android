# MT Manager — đối chiếu cho ApexFileManager

## Mẫu phân tích

- Package: `bin.mt.plus`
- Phiên bản manifest: **2.26.8** (versionCode `26081193`)
- minSdk 21, targetSdk 30
- APK được lấy từ thiết bị phát triển và decompile bằng JADX.
- Đây là phân tích cạnh tranh/tính năng, không dùng dữ liệu tài khoản hay khóa của ứng dụng.

Báo cáo kỹ thuật đầy đủ (manifest, resource, service, native/JNI và bằng chứng code) nằm tại [android-re-workspace/analysis/bin.mt.plus/ANALYSIS.md](../android-re-workspace/analysis/bin.mt.plus/ANALYSIS.md).

## Những điểm MT Manager làm tốt

1. **File workflow**: hai pane, bookmark có nhóm, chọn nhiều, batch rename/copy/move/delete, task queue có pause/resume/cancel, notification tiến độ và recycle bin.
2. **Preview và editor**: text/code editor có encoding, backup `.bak` và phát hiện file bị thay đổi; hex editor; image/GIF, font, SVG, 9-patch, PDF, video, music player.
3. **Archive**: xem/tạo archive, mật khẩu, mã hóa tên file, nén độc lập từng mục, test archive và ultra recompression.
4. **Network storage**: client `SMB`, `SFTP`, `WebDAV`, `FTP`, `FTPS`, `OSS`; cấu hình host/port/credential/key/initial path; SMB có signing/encryption.
5. **Chia sẻ LAN**: FTP server, web manager và HTTP streaming chạy bằng foreground service để không bị dừng khi rời Activity.
6. **Android power user**: SAF, root/shell, Shizuku, Dhizuku, Accessibility service, terminal và DocumentsProvider cho thư mục data.
7. **APK toolkit**: APK info/clone/install/sign/optimize, APKS/XAPK, AXML/ARSC editor, Dex2Jar/Dex2Smali, Dex editor/compare/redivide/repair và APK MCP cho AI/MCP client.

## Kiến trúc rút ra

- Tác vụ dài được gom vào foreground service `TaskManager`; server tách riêng thành `FTPServer`, `WebManager`, `StreamingServer`, `ApkMcpServer`; terminal có service riêng.
- Native `libmt3.so` cung cấp read/write/seek/tell/length/truncate/sync và rename/delete trên file descriptor; `libmt1.so` xử lý analyze/UID/ABI; `libterm.so` quản lý subprocess/PTY.
- `libhook.so` và `liblsplant.so` phục vụ ART hook/deoptimization cho các tính năng phân tích APK/Dex. Đây không phải dependency cần đưa vào MVP.
- Manifest yêu cầu quyền rộng (`MANAGE_EXTERNAL_STORAGE`, install/delete package, overlay, foreground service, superuser). Apex nên xin quyền theo từng tình huống và ưu tiên SAF.

## Điều chỉnh roadmap ApexFileManager

### P0/P1 nên ưu tiên

- Hai pane, multi-select, batch operation và task queue có pause/resume/retry.
- Recycle bin với preview, restore và cảnh báo xóa vĩnh viễn.
- Preview text/image/video/audio; text editor có charset, backup và file-change detection.
- ZIP/7z/RAR cơ bản, password và progress có thể hủy.
- SAF-first: bookmark, hidden files, lỗi quyền Android 11+ có hướng dẫn rõ.

### P2

- SMB, SFTP, WebDAV; sau đó FTP/FTPS và object storage.
- Web server LAN opt-in, QR, timeout tự động, foreground notification và HTTPS khi có thể.
- Credential lưu bằng Android Keystore; copy giữa hai remote phải có resume và kiểm soát traffic.

### Module power-user tách riêng

- APK install/backup/sign, AXML/ARSC/Dex editor.
- Root/Shizuku/Dhizuku bridge, terminal root và DocumentsProvider injection.
- APK MCP chỉ bind localhost mặc định, có session token, giới hạn path, opt-in LAN và audit log.

## Kết luận thiết kế

Không cần sao chép toàn bộ độ rộng của MT Manager ngay từ đầu. Lợi thế cạnh tranh thực tế cho Apex là thao tác file an toàn, task queue đáng tin cậy, preview/editor tốt và network storage rõ ràng; APK/Dex/terminal nên là module mở rộng sau khi lõi local ổn định.

## Phân tích subscription VIP

- **Entitlement nằm ở native bridge**: `C9065` khai báo nhiều hàm native để đọc trạng thái (`boolean`, `long`, `String`, `Map`) và cập nhật trạng thái (`m20717`, `m20718`). `m20710()` là cờ VIP được dùng lặp lại ở các feature gate; danh sách hàm nằm tại [C9065.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C9065.java:20).
- **Feature gate ở lớp UI/domain**: menu network chỉ cho FTP ở bản thường; FTPS, SFTP, SMB, WebDAV và OSS chỉ bật khi `C9065.m20710()` trả về true ([C13507.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C13507.java:42)). Editor nâng cao, translator và nhiều thao tác APK cũng dùng cùng cờ này.
- **Đồng bộ quyền sau response backend**: `C16437.m35554()` parse JSON response thành `Map`; các luồng xử lý gọi `C9065.m20718(map)` rồi `m35553()` để nạp lại entitlement và làm mới UI ([C16437.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C16437.java:2766), [C16437.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C16437.java:221)). Vì vậy không nên coi VIP là một giá trị chỉ lưu trong `SharedPreferences`.
- **Có thời hạn và lifetime**: resource có `VIP Time`, `Lifetime VIP`, `Renew VIP`; `C9065.m20719()` là một timestamp được format/hiển thị trong luồng account và được so sánh như mốc thời gian ([strings.xml](../android-re-workspace/decompiled/bin.mt.plus/resources/res/values/strings.xml:2662), [C16755.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C16755.java:944), [C8966.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C8966.java:84336)). Tên chính xác của các trường native khác vẫn bị obfuscate.
- **Tài khoản là trung tâm, không khóa theo thiết bị**: chính sách ghi rõ đăng nhập được trên thiết bị bất kỳ, nhưng một tài khoản chỉ đăng nhập một thiết bị tại một thời điểm. Multi-login là quyền riêng, cho phép bản chính `bin.mt.plus` và bản beta/clone `bin.mt.plus.canary` chạy đồng thời; cùng package name vẫn đá phiên cũ offline ([strings.xml](../android-re-workspace/decompiled/bin.mt.plus/resources/res/values/strings.xml:1586), [strings.xml](../android-re-workspace/decompiled/bin.mt.plus/resources/res/values/strings.xml:2759)).
- **Kênh thanh toán**: Alipay dùng `PayTask.payV2()` và đọc `resultStatus`; mã `9000` chạy callback thành công ([C12091.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C12091.java:371), [C12091.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C12091.java:390), [C16311.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C16311.java:70)). WeChat tạo `PayReq` từ JSON gồm `prepayId`, `nonceStr`, `timeStamp`, `packageValue`, `sign`, rồi gửi qua `IWXAPI` ([C5415.java](../android-re-workspace/decompiled/bin.mt.plus/sources/p067l/C5415.java:41)). Resource còn có PayPal, Play Store và nạp bằng key 32 ký tự; trong DEX không thấy `BillingClient`, nên chưa đủ bằng chứng để kết luận Play Store dùng Google Play Billing.
- **Chính sách sản phẩm cần phản ánh**: VIP là virtual goods, không hoàn tiền; cấm chia sẻ/cho thuê/bán tài khoản; có email hỗ trợ thanh toán. Đây là policy hiển thị trong resource, không phải suy đoán từ code ([strings.xml](../android-re-workspace/decompiled/bin.mt.plus/resources/res/values/strings.xml:2759)).

### Hàm ý cho ApexFileManager

Nên tách `SubscriptionRepository` (backend xác thực giao dịch và trả entitlement), `EntitlementStore` (lưu cache có chữ ký/expiry) và `VipGate` (chỉ đọc quyền trong UI). Mỗi giao dịch phải được xác minh ở server, hỗ trợ `activeUntil` và `lifetime`, có restore/renew, session single-device và một capability riêng cho multi-login; client chỉ dùng cache để hiển thị tạm thời và phải refresh sau login, resume và hoàn tất thanh toán.
