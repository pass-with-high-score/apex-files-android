# ApexFileManager — Kế hoạch chức năng

Tài liệu này định nghĩa phạm vi sản phẩm và thứ tự ưu tiên phát triển cho ứng dụng quản lý file Android ApexFileManager.

## Định vị sản phẩm

ApexFileManager giúp người dùng tìm đúng file nhanh, sắp xếp dễ dàng và luôn biết thao tác file đang diễn ra thế nào. Sản phẩm phục vụ người dùng phổ thông trước, sau đó mở rộng cho người dùng cần xử lý APK, archive, NAS và cloud.

## P0 — MVP bắt buộc

### 1. Trang chủ

- File gần đây và file mới tải xuống
- Truy cập nhanh: Ảnh, Video, Âm thanh, Tài liệu, APK, Archive
- Dung lượng đã dùng và còn trống
- Shortcut tới thư mục yêu thích
- Tìm kiếm luôn có thể truy cập

### 2. Duyệt file và thư mục

- Bộ nhớ trong
- Thẻ SD và USB OTG khi thiết bị cho phép
- Chế độ danh sách và lưới
- Sắp xếp theo tên, ngày sửa, kích thước và loại file
- Lọc theo loại file
- Hiển thị hoặc ẩn file ẩn
- Tạo thư mục, đổi tên, xem thông tin file
- Sao chép, di chuyển, xóa và chia sẻ
- Chọn nhiều file
- Kéo thả trên tablet hoặc màn hình lớn

### 3. Tìm kiếm

- Tìm theo tên và phần mở rộng
- Lọc theo kích thước và khoảng thời gian
- Tìm trong thư mục hiện tại hoặc toàn bộ bộ nhớ
- Hỗ trợ tiếng Việt có dấu và không dấu
- Lịch sử tìm kiếm
- Giữ kết quả tìm kiếm khi mở file rồi quay lại

### 4. Tác vụ file an toàn

- Thanh tiến trình cho copy, move, delete và compress
- Hủy tác vụ
- Hàng đợi tác vụ
- Xử lý file trùng tên: ghi đè, bỏ qua hoặc đổi tên
- Kiểm tra file đích sau khi sao chép
- Không xóa file gốc nếu thao tác di chuyển thất bại
- Báo lỗi theo từng file với hướng xử lý rõ ràng
- Tiếp tục tác vụ khi có thể

### 5. Archive cơ bản

- Tạo và giải nén ZIP
- Tạo và giải nén 7z
- Giải nén RAR/RAR5
- Archive có mật khẩu
- Xem nội dung archive trước khi giải nén
- Giải nén một phần file hoặc thư mục
- Chọn thư mục đích
- Tiến trình và lỗi theo từng file

## P1 — Hoàn thiện trải nghiệm

### 6. Phân loại nội dung

- Ảnh, Video, Âm thanh, Tài liệu, APK và File nén
- File tải xuống, file mới, file lớn và file trùng

### 7. Dọn dẹp dung lượng

- Phân tích theo thư mục và loại file
- Tìm file lớn, file trùng và file cũ
- Tìm cache có thể xóa an toàn
- Xem trước trước khi xóa
- Thùng rác, khôi phục và xóa vĩnh viễn

### 8. Xem trước file

- Xem ảnh và thumbnail
- Phát video và âm thanh
- Đọc văn bản
- Mở PDF bằng ứng dụng hệ thống
- Xem APK như archive
- Sao chép đường dẫn file

### 9. Yêu thích và lịch sử

- Đánh dấu thư mục yêu thích
- Ghim thư mục lên trang chủ
- Lịch sử thư mục và file đã mở
- Shortcut ra màn hình chính
- Ghi nhớ chế độ xem theo từng thư mục

### 10. Chia sẻ

- Android Sharesheet và Quick Share
- Chia sẻ nhiều file
- Chia sẻ đường dẫn file
- Chia sẻ qua Wi-Fi nội bộ
- Web server tạm thời để tải file từ máy tính

## P1 — Người dùng Android nâng cao

### 11. APK và ứng dụng

- Tên package, version và version code của APK
- Cài đặt và chia sẻ APK
- Sao lưu APK đã cài
- Nhận diện XAPK/APKS
- Hiển thị OBB liên quan
- Cảnh báo an toàn trước khi cài APK
- Chỉ yêu cầu quyền xem danh sách ứng dụng khi thực sự cần

### 12. Tác vụ hàng loạt

- Đổi tên hàng loạt
- Thêm tiền tố, hậu tố và số thứ tự
- Đổi phần mở rộng
- Nén, di chuyển hoặc xóa nhiều nhóm file
- Xem trước kết quả trước khi xác nhận

### 13. Bảo mật

- Kho riêng tư
- Mã PIN hoặc sinh trắc học
- Mã hóa file bằng AES
- Tự khóa khi chuyển sang ứng dụng khác
- Ẩn file khỏi thư viện hệ thống
- Không tải dữ liệu người dùng lên máy chủ
- Giải thích quyền truy cập trước khi yêu cầu

## P2 — Cloud và mạng

### 14. Máy tính và NAS

- SMB/CIFS
- FTP/FTPS
- SFTP
- WebDAV
- Duyệt NAS và sao chép giữa điện thoại với máy tính
- Lưu thông tin kết nối được mã hóa
- Kiểm tra kết nối trước khi thao tác

### 15. Cloud storage

- Google Drive
- OneDrive
- Dropbox
- WebDAV cloud
- Sao chép giữa cloud và bộ nhớ máy
- Upload/download nền
- Tiếp tục tải khi gián đoạn
- Hiển thị trạng thái đồng bộ

## P2 — Tablet, Android TV và khả năng tiếp cận

- Giao diện hai cột cho tablet
- Hỗ trợ chuột, bàn phím và phím tắt
- Giao diện Android TV và điều hướng bằng remote
- Hỗ trợ TalkBack
- Cỡ chữ động
- Chế độ tương phản cao
- Tiếng Việt và tiếng Anh trước, mở rộng ngôn ngữ sau

## P3 — Tính năng power user

- Hai bảng thư mục
- Nhiều tab
- Hàng đợi tác vụ nâng cao
- Script thao tác file
- So sánh hai thư mục
- Đồng bộ hai chiều
- Kiểm tra checksum MD5/SHA-256
- Hex viewer
- Text editor và code editor
- Root explorer cho thiết bị đã root
- SQLite viewer
- Archive nhiều phần
- Sửa nội dung archive mà không giải nén toàn bộ

## Thứ tự phát triển

1. Duyệt thư mục và hiển thị file
2. Copy, move, delete và rename
3. Tìm kiếm và chọn nhiều
4. Tác vụ nền an toàn
5. ZIP, 7z và giải nén RAR
6. Trang chủ và file gần đây
7. Preview ảnh, video và văn bản
8. Dọn dung lượng và thùng rác
9. APK manager
10. Chia sẻ Wi-Fi và web server
11. SMB, SFTP và WebDAV
12. Kho riêng tư và mã hóa
13. Tablet, Android TV và power user

## Tiêu chí hoàn thành MVP

- Không làm mất file khi copy hoặc move lỗi
- Xử lý ổn định thư mục có ít nhất 10.000 file
- Tìm kiếm trả kết quả ổn định với tên tiếng Việt và ký tự đặc biệt
- Archive lỗi phải báo rõ, không tạo file hỏng âm thầm
- Cho xem trước trước khi xóa
- Không hiển thị quảng cáo trong lúc chọn, chuyển hoặc xóa file
- Giải thích rõ quyền Android trước khi yêu cầu
- Hoạt động tốt từ Android 10 trở lên
- Đã kiểm thử với file lớn, APK, OBB, ZIP, 7z, RAR và file trùng tên

## Những việc không làm trong MVP

- Diệt virus, dọn RAM hoặc tăng tốc điện thoại
- Quảng cáo toàn màn hình trong luồng thao tác file
- Tự động xóa khi chưa cho xem trước
- Yêu cầu danh bạ, vị trí hoặc Internet nếu không cần
- Xây cloud storage riêng trước khi nhu cầu được kiểm chứng
- Tính năng mạng phức tạp khi thao tác local chưa ổn định

## Ghi chú Android và Google Play

`MANAGE_EXTERNAL_STORAGE` là quyền nhạy cảm. File manager có thể thuộc nhóm được phép sử dụng, nhưng phải khai báo và được Google Play xét duyệt. Quyền này cũng không đồng nghĩa với việc có thể sửa mọi thư mục riêng của ứng dụng khác. Cần ưu tiên Storage Access Framework và MediaStore khi phù hợp, đồng thời mô tả trung thực phạm vi quyền trong Play Store.

