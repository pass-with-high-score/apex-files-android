package app.pwhs.apexfilemanager.features.vault.auth

import androidx.annotation.StringRes
import app.pwhs.apexfilemanager.core.base.UiAction
import app.pwhs.apexfilemanager.core.base.UiEvent
import app.pwhs.apexfilemanager.core.base.UiState

enum class SetupStep {
    ENTER_NEW,
    CONFIRM_NEW
}

data class VaultAuthUiState(
    val isSetupMode: Boolean = false,
    val setupStep: SetupStep = SetupStep.ENTER_NEW,
    val enteredPin: String = "",
    val firstPin: String = "",
    @StringRes val errorMsgResId: Int? = null,
    val isBiometricAvailable: Boolean = false
) : UiState

sealed interface VaultAuthUiAction : UiAction {
    data class DigitClick(val digit: String) : VaultAuthUiAction
    data object BackspaceClick : VaultAuthUiAction
    data object ClearClick : VaultAuthUiAction
    data object BiometricSuccess : VaultAuthUiAction
    data object BiometricClick : VaultAuthUiAction
}

sealed interface VaultAuthUiEvent : UiEvent {
    data object NavigateToVault : VaultAuthUiEvent
    data object LaunchBiometricPrompt : VaultAuthUiEvent
    data class ShowToast(@StringRes val messageRes: Int) : VaultAuthUiEvent
}
