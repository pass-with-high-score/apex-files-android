# Project Instructions

## Workflows & Rules
- **Build Verification:** Bắt buộc build test xong (`./gradlew assembleDebug`) mới được báo "done" hoặc xác nhận hoàn thành tác vụ. Đảm bảo project compile thành công và không có lỗi hồi quy.
- **File Size & Reusability:** Không có file source code nào được vượt quá 500 dòng. Chủ động tách nhỏ thành các component logic. Luôn ưu tiên tái sử dụng component thay vì viết code dư thừa.
- **Temporary Scripts Cleanup:** Nếu tạo file Python hoặc script tạm để code/parse data, bắt buộc phải xóa nó đi sau khi dùng xong để tránh rác project.
- **Explicit Instruction Only:** Đảm bảo không tự ý làm hoặc thay đổi các logic không liên quan khi chưa nhận được yêu cầu rõ ràng từ user.
- **Safe Modifications:** Luôn kiểm tra kỹ diff change (so sánh thay đổi) trước và sau khi sửa file để đảm bảo không xóa mất code đang có của user.

## Architectural Guidelines (BẮT BUỘC TUÂN THỦ ĐỂ TRÁNH CODE NHẦM)
Chi tiết xem tại `.agents/rules/architecture.md`. Khi triển khai màn hình hoặc tính năng mới, AI BẮT BUỘC tuân theo:

1. **Clean Architecture 3 Lớp:**
   - `presentation/`: MVI, UI (Stateful Screen + Stateless Content), ViewModel, Contract.
   - `domain/`: Pure Kotlin (CẤM import Android SDK). Chứa Models, Repository interfaces, UseCases (Single Responsibility, format `<Verb><Noun>UseCase.kt`).
   - `data/`: Triển khai Repository, Local/Remote DataSources, Mappers.
2. **Kiến Trúc 1 Màn Hình (MVI / UDF):**
   - **1 Contract (`<Feature>Contract.kt`):** Chứa `UiState` (Immutable data class), `UiAction` (Sealed interface cho user actions), `UiEvent` (Sealed interface cho sự kiện 1 lần: Toast, Navigation...).
   - **1 ViewModel (`<Feature>ViewModel.kt`):** Kế thừa `BaseViewModel<S, A, E>`. Chỉ nhận input qua `fun onAction(action: A)`. Cập nhật state bằng `updateState { copy(...) }`, gửi event bằng `sendEvent(...)`. CẤM expose `MutableStateFlow`.
   - **1 Screen Composable (`<Feature>Screen.kt`):** Tách rõ **Stateful** (lắng nghe ViewModel & Events) và **Stateless** (`<Feature>Content` chỉ nhận State & lambda `onAction`), phục vụ viết Preview và Unit Test.
   - **BaseActivity:** Kế thừa `BaseActivity` (đã kích hoạt edge-to-edge và bọc `ApexFileManagerTheme`).
3. **Quy Tắc Điều Hướng (Navigation):**
   - **TUYỆT ĐỐI KHÔNG DÙNG COMPOSE NAVIGATION:** Dự án sử dụng mô hình Multi-Activity với **Activity Intent** truyền thống.
   - Mỗi màn hình/tính năng là một Activity riêng biệt kế thừa `BaseActivity`.
   - Điều hướng được kích hoạt từ `UiEvent` (vd: `NavigateToDetail`), Activity hoặc Stateful Composable sẽ khởi chạy qua `Intent(context, TargetActivity::class.java)`.
4. **Quy Tắc Theme & Màu Sắc:**
   - **TUYỆT ĐỐI KHÔNG FIX CỨNG MÃ MÀU:** Cấm dùng `Color(0xFF...)` trong giao diện. Bắt buộc dùng semantic tokens của `MaterialTheme.colorScheme` (`primary`, `surface`, `onSurface`, `error`...).
   - Đảm bảo hiển thị tự nhiên ở cả Light/Dark Mode và Dynamic Color (Material 3).
5. **Quy Tắc String Resources:**
   - **TUYỆT ĐỐI KHÔNG HARDCODE TEXT:** Cấm viết text chuỗi trực tiếp trong code UI. Mọi text phải nằm trong `res/values/strings.xml` và truy xuất qua `stringResource(R.string.<id>)`.
   - Đặt tiền tố rõ ràng theo feature (vd: `home_title`, `home_btn_refresh`).

## AI Guidance & Custom Skills
This project uses specialized Gemini CLI skills to automate complex tasks. When you encounter the following scenarios, you SHOULD activate and follow the corresponding skill:

- **Implementing New Features:** Use the `cook` skill. It provides a structured "kitchen" workflow (Research -> Recipe -> Cook -> Taste Test).
  - Location: `.gemini/skills/cook/SKILL.md`
- **Creating New Modules:** Use the `create-module` skill. It automates generating Android library/feature modules and updates `settings.gradle.kts`.
  - Location: `.agents/skills/create-module/SKILL.md`
  - Command: `./scripts/create-module.sh <module_name> [--type lib|compose-lib|feature]`
- **Releasing New Versions:** Use the `upgrade-app` skill. It automates version bumping, changelog generation, tagging, and GitHub releases.
  - Location: `.gemini/skills/upgrade-app/SKILL.md`
- **Translating CSV Files:** Use the `csv-translator` skill. It handles splitting large files, translation, and auto-importing strings into Android resources.
  - Location: `.gemini/skills/csv-translator/SKILL.md`

Always refer to the project-specific memory in `.gemini/tmp/ApexFileManager/memory/MEMORY.md` for private local context.
