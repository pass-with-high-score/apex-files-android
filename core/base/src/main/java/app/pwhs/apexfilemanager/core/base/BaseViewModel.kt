package app.pwhs.apexfilemanager.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Lớp cơ sở ViewModel cho toàn bộ ứng dụng theo mô hình MVI / UDF.
 *
 * @param S Kiểu trạng thái UI, kế thừa từ [UiState]
 * @param A Kiểu hành động người dùng, kế thừa từ [UiAction]
 * @param E Kiểu sự kiện một lần, kế thừa từ [UiEvent]
 */
abstract class BaseViewModel<S : UiState, A : UiAction, E : UiEvent>(
    initialState: S
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    private val _uiEvent = Channel<E>(Channel.BUFFERED)
    val uiEvent: Flow<E> = _uiEvent.receiveAsFlow()

    /**
     * Trạng thái UI tức thời hiện tại.
     */
    protected val currentState: S
        get() = _uiState.value

    /**
     * Điểm tiếp nhận hành động duy nhất từ phía UI.
     */
    abstract fun onAction(action: A)

    /**
     * Cập nhật trạng thái an toàn trên luồng (thread-safe).
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update(reducer)
    }

    /**
     * Phát ra sự kiện một lần (Toast, Navigation...) thông qua Channel.
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch {
            _uiEvent.send(event)
        }
    }
}
