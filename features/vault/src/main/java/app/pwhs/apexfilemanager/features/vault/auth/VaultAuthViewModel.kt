package app.pwhs.apexfilemanager.features.vault.auth

import androidx.lifecycle.viewModelScope
import app.pwhs.apexfilemanager.core.base.BaseViewModel
import app.pwhs.apexfilemanager.core.storage.domain.usecase.AuthenticateVaultUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.CheckVaultSetupUseCase
import app.pwhs.apexfilemanager.core.storage.domain.usecase.SetVaultPinUseCase
import app.pwhs.apexfilemanager.features.vault.R
import kotlinx.coroutines.launch

class VaultAuthViewModel(
    private val checkVaultSetupUseCase: CheckVaultSetupUseCase,
    private val authenticateVaultUseCase: AuthenticateVaultUseCase,
    private val setVaultPinUseCase: SetVaultPinUseCase
) : BaseViewModel<VaultAuthUiState, VaultAuthUiAction, VaultAuthUiEvent>(
    VaultAuthUiState()
) {

    init {
        checkStatus()
    }

    private fun checkStatus() {
        viewModelScope.launch {
            val isSetUp = checkVaultSetupUseCase()
            val isBio = if (isSetUp) authenticateVaultUseCase.isBiometricEnabled() else false
            updateState {
                copy(
                    isSetupMode = !isSetUp,
                    setupStep = SetupStep.ENTER_NEW,
                    isBiometricAvailable = isBio
                )
            }
            if (isSetUp && isBio) {
                sendEvent(VaultAuthUiEvent.LaunchBiometricPrompt)
            }
        }
    }

    override fun onAction(action: VaultAuthUiAction) {
        when (action) {
            is VaultAuthUiAction.DigitClick -> handleDigit(action.digit)
            is VaultAuthUiAction.BackspaceClick -> handleBackspace()
            is VaultAuthUiAction.ClearClick -> updateState { copy(enteredPin = "", errorMsgResId = null) }
            is VaultAuthUiAction.BiometricClick -> {
                sendEvent(VaultAuthUiEvent.LaunchBiometricPrompt)
            }
            is VaultAuthUiAction.BiometricSuccess -> {
                sendEvent(VaultAuthUiEvent.NavigateToVault)
            }
        }
    }

    private fun handleDigit(digit: String) {
        val current = currentState.enteredPin
        if (current.length >= 6) return
        val newPin = current + digit
        updateState { copy(enteredPin = newPin, errorMsgResId = null) }

        if (newPin.length >= 4) {
            if (!currentState.isSetupMode) {
                verifyExistingPin(newPin)
            } else {
                if (newPin.length == 4 || newPin.length == 6) {
                    handleSetupStep(newPin)
                }
            }
        }
    }

    private fun handleBackspace() {
        val current = currentState.enteredPin
        if (current.isNotEmpty()) {
            updateState { copy(enteredPin = current.dropLast(1), errorMsgResId = null) }
        }
    }

    private fun verifyExistingPin(pin: String) {
        viewModelScope.launch {
            val success = authenticateVaultUseCase.verifyPin(pin)
            if (success) {
                sendEvent(VaultAuthUiEvent.NavigateToVault)
            } else if (pin.length >= 6) {
                updateState { copy(enteredPin = "", errorMsgResId = R.string.vault_auth_wrong_pin) }
            }
        }
    }

    private fun handleSetupStep(pin: String) {
        if (currentState.setupStep == SetupStep.ENTER_NEW) {
            updateState {
                copy(
                    setupStep = SetupStep.CONFIRM_NEW,
                    firstPin = pin,
                    enteredPin = "",
                    errorMsgResId = null
                )
            }
        } else {
            if (pin == currentState.firstPin) {
                viewModelScope.launch {
                    val result = setVaultPinUseCase.setup(pin)
                    if (result.isSuccess) {
                        sendEvent(VaultAuthUiEvent.NavigateToVault)
                    } else {
                        updateState {
                            copy(
                                setupStep = SetupStep.ENTER_NEW,
                                firstPin = "",
                                enteredPin = "",
                                errorMsgResId = R.string.vault_import_failed
                            )
                        }
                    }
                }
            } else {
                updateState {
                    copy(
                        enteredPin = "",
                        errorMsgResId = R.string.vault_auth_pin_mismatch
                    )
                }
            }
        }
    }
}
