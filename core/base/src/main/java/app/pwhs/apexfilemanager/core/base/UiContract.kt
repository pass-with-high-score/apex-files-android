package app.pwhs.apexfilemanager.core.base

/**
 * Trạng thái giao diện (Immutable UI State).
 * Mỗi màn hình có 1 data class duy nhất triển khai interface này.
 */
interface UiState

/**
 * Hành vi / Tương tác của người dùng từ giao diện (User Action / Intent).
 * Được bắn từ UI đến ViewModel thông qua hàm `onAction(action)`.
 */
interface UiAction

/**
 * Sự kiện xảy ra một lần duy nhất (One-time event) như Toast, Navigation, Snackbar...
 * Không lưu vào UiState để tránh bị kích hoạt lại khi recompose hoặc config change.
 */
interface UiEvent
