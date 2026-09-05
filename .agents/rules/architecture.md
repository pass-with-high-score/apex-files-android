# Quy Chuẩn Kiến Trúc Dự Án (Architecture & Coding Standards)

Quy tắc này là **bắt buộc** đối với tất cả tác vụ lập trình (developer và AI). Mọi đoạn code mới hoặc chỉnh sửa phải tuân thủ nghiêm ngặt các nguyên tắc dưới đây.

---

## 1. Clean Architecture + MVI (Model-View-Intent)

Dự án áp dụng mô hình phân lớp Clean Architecture kết hợp luồng dữ liệu một chiều UDF (Unidirectional Data Flow / MVI).

### 1.1. Phân chia Layer

```text
app/src/main/java/app/pwhs/apexfilemanager/
├── core/                               # Thành phần dùng chung toàn app
│   ├── base/                           # BaseActivity, BaseViewModel, UiContract (UiState, UiAction, UiEvent)
│   ├── designsystem/                   # Theme (Color, Theme, Type), Reusable UI Components
│   └── util/                           # Extensions, Helpers
├── domain/                             # Pure Kotlin - KHÔNG phụ thuộc Android Framework
│   ├── model/                          # Domain entities (vd: StorageItem.kt)
│   ├── repository/                     # Repository interfaces (vd: StorageRepository.kt)
│   └── usecase/                        # Single-responsibility UseCases (vd: GetStorageItemsUseCase.kt)
├── data/                               # Triển khai truy xuất và xử lý dữ liệu
│   ├── repository/                     # Repository implementations (vd: StorageRepositoryImpl.kt)
│   ├── source/                         # Data sources (local: Room/DataStore, remote: API)
│   └── mapper/                         # Chuyển đổi giữa DTO/Entity và Domain Model
└── features/                           # Presentation theo từng tính năng (Feature-driven packaging)
    └── <feature_name>/
        ├── <Feature>Contract.kt        # Khai báo UiState, UiAction, UiEvent
        ├── <Feature>ViewModel.kt       # Kế thừa BaseViewModel, xử lý logic
        ├── <Feature>Screen.kt          # Stateful Composable & Stateless Content
        └── components/                 # Các Composable con của riêng feature (nếu cần tách nhỏ)
```

---

## 2. Chuẩn Màn Hình (1 Screen = 1 Contract + 1 ViewModel + 1 Screen + BaseActivity)

Mỗi màn hình bắt buộc phải tuân theo cấu trúc 4 phần rõ ràng:

### 2.1. `<Feature>Contract.kt`
Mỗi màn hình chỉ có **duy nhất 1 file Contract** chứa:
- `UiState`: Immutable data class biểu diễn toàn bộ trạng thái render của UI.
- `UiAction`: Sealed interface biểu diễn các hành động/ý định từ người dùng (click, input, refresh...).
- `UiEvent`: Sealed interface biểu diễn sự kiện 1 lần (One-time event như Toast, Navigation, Snackbar, Dialog).

```kotlin
// Ví dụ: HomeContract.kt
package app.pwhs.apexfilemanager.features.home

import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState
import app.pwhs.apexfilemanager.domain.model.StorageItem

data class HomeUiState(
    val isLoading: Boolean = false,
    val items: List<StorageItem> = emptyList(),
    val errorMessage: String? = null
) : UiState

sealed interface HomeUiAction : UiAction {
    data object Refresh : HomeUiAction
    data class ItemClick(val item: StorageItem) : HomeUiAction
}

sealed interface HomeUiEvent : UiEvent {
    data class ShowToast(val messageResId: Int) : HomeUiEvent
    data class NavigateToDetail(val path: String) : HomeUiEvent
}
```

### 2.2. `<Feature>ViewModel.kt`
- Bắt buộc kế thừa `BaseViewModel<UiState, UiAction, UiEvent>`.
- Chỉ xử lý nhận tác vụ qua `override fun onAction(action: UiAction)`.
- Cập nhật state thông qua `updateState { copy(...) }`.
- Bắn event qua `sendEvent(...)`.
- **CẤM** expose `MutableStateFlow` ra bên ngoài.

```kotlin
// Ví dụ: HomeViewModel.kt
class HomeViewModel(
    private val getStorageItemsUseCase: GetStorageItemsUseCase
) : BaseViewModel<HomeUiState, HomeUiAction, HomeUiEvent>(HomeUiState()) {

    init {
        loadData()
    }

    override fun onAction(action: HomeUiAction) {
        when (action) {
            is HomeUiAction.Refresh -> loadData()
            is HomeUiAction.ItemClick -> {
                sendEvent(HomeUiEvent.NavigateToDetail(action.item.path))
            }
        }
    }

    private fun loadData() {
        updateState { copy(isLoading = true) }
        // Xử lý coroutine gọi UseCase và cập nhật state
    }
}
```

### 2.3. `<Feature>Screen.kt`
Bắt buộc tách thành 2 composables:
1. **Stateful Composable (`<Feature>Screen`)**:
   - Nhận ViewModel (hoặc tự inject).
   - Thu thập `uiState` bằng `collectAsStateWithLifecycle()`.
   - Lắng nghe `uiEvent` bằng `LaunchedEffect(viewModel.uiEvent)` để điều hướng hoặc hiển thị Toast/Snackbar.
   - Truyền state và lambda `onAction = viewModel::onAction` xuống Stateless Composable.
2. **Stateless Composable (`<Feature>Content`)**:
   - Chỉ nhận `state: <Feature>UiState` và `onAction: (<Feature>UiAction) -> Unit`.
   - Không chứa bất kỳ tham chiếu nào tới ViewModel hoặc coroutine context đặc biệt.
   - Dễ dàng viết `@Preview` (Light/Dark theme) và kiểm thử UI.

### 2.4. `BaseActivity`
- Kế thừa `ComponentActivity`.
- Bật `enableEdgeToEdge()` mặc định.
- Cung cấp tiện ích bọc `ApexFileManagerTheme` cho toàn bộ nội dung.
- Lắng nghe hoặc xử lý Intent khởi chạy từ các màn hình khác.

---

## 3. Quy Tắc Điều Hướng (Navigation)

- **TUYỆT ĐỐI KHÔNG DÙNG COMPOSE NAVIGATION:**
  - Dự án xây dựng theo kiến trúc **Multi-Activity** với **Android Intent** truyền thống.
  - Cấm cài đặt hoặc sử dụng `androidx.navigation:navigation-compose`.
- **Cách thức thực hiện chuyển màn hình:**
  1. ViewModel bắn sự kiện điều hướng qua `UiEvent` (ví dụ: `HomeUiEvent.NavigateToDetail(val path: String)`).
  2. Tại `Stateful Composable` hoặc `Activity`, lắng nghe `UiEvent` bằng `LaunchedEffect(viewModel.uiEvent)`:
  ```kotlin
  val context = LocalContext.current
  LaunchedEffect(viewModel.uiEvent) {
      viewModel.uiEvent.collect { event ->
          when (event) {
              is HomeUiEvent.NavigateToDetail -> {
                  val intent = Intent(context, DetailActivity::class.java).apply {
                      putExtra(DetailActivity.EXTRA_PATH, event.path)
                  }
                  context.startActivity(intent)
              }
              is HomeUiEvent.ShowToast -> {
                  Toast.makeText(context, context.getString(event.messageResId), Toast.LENGTH_SHORT).show()
              }
          }
      }
  }
  ```
  3. Mọi dữ liệu truyền qua màn hình mới đều thông qua `Intent` Extras hoặc Bundle.

---

## 3. Quy Tắc Giao Diện (UI & Theme)

- **CẤM FIX CỨNG MÃ MÀU (NO HARDCODED COLORS):**
  - Không được viết mã màu trực tiếp dạng `Color(0xFF...)` trong các màn hình giao diện.
  - Luôn sử dụng semantic tokens từ hệ thống Material 3: `MaterialTheme.colorScheme.primary`, `surface`, `onSurface`, `surfaceVariant`, `outline`, `error`...
  - Typography bắt buộc dùng từ `MaterialTheme.typography.*`.
  - Đảm bảo giao diện hiển thị chuẩn xác ở cả chế độ Light Mode, Dark Mode và Dynamic Color (Android 12+).

---

## 4. Quy Tắc Chuỗi Ký Tự (String Resources)

- **CẤM HARDCODE TEXT TRONG CODE:**
  - Tuyệt đối không viết text trực tiếp trong Composable như `Text("Xin chào")` hay `"File Manager"`.
  - Toàn bộ chuỗi hiển thị BẮT BUỘC phải đặt trong `app/src/main/res/values/strings.xml`.
  - Trong Compose dùng: `stringResource(R.string.your_string_name)`.
  - Đặt tên string có tiền tố rõ ràng theo tính năng:
    - Ví dụ: `home_screen_title`, `home_btn_refresh`, `dialog_confirm_delete`.

---

## 5. Quy Tắc Kích Thước File & Tái Sử Dụng

- **Giới hạn số dòng:** Không có file source code nào được vượt quá **500 dòng**.
- Khi một màn hình hoặc component bắt đầu dài, chủ động tách thành các file composable con nhỏ gọn đặt trong thư mục `components/` của feature.
- Tận dụng tối đa việc tái sử dụng các component dùng chung trong `core/designsystem/components/`.

---

## 6. Quy Trình Kiểm Tra & An Toàn Bắt Buộc

1. **Build Verification:** Sau bất kỳ thay đổi code nào, bắt buộc phải chạy `./gradlew assembleDebug` để xác nhận ứng dụng build thành công 100%, không phát sinh lỗi compile hoặc xung đột tài nguyên trước khi xác nhận hoàn thành.
2. **Safe Modification:** Luôn kiểm tra diff kỹ lưỡng, không tự ý xóa bỏ code sẵn có của người dùng hoặc thay đổi logic không liên quan đến yêu cầu.
