package app.pwhs.apexfilemanager.features.vault.auth

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import app.pwhs.apexfilemanager.core.base.BaseActivity
import app.pwhs.apexfilemanager.features.vault.R
import app.pwhs.apexfilemanager.features.vault.main.VaultActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class VaultAuthActivity : BaseActivity() {

    private val viewModel: VaultAuthViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            VaultAuthScreen(
                viewModel = viewModel,
                onNavigateBack = { finish() },
                onNavigateToVault = {
                    startActivity(Intent(this, VaultActivity::class.java))
                    finish()
                },
                onLaunchBiometric = { launchBiometric() }
            )
        }
    }

    private fun launchBiometric() {
        val executor = ContextCompat.getMainExecutor(this)
        val prompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    viewModel.onAction(VaultAuthUiAction.BiometricSuccess)
                }
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.vault_auth_biometric_prompt_title))
            .setSubtitle(getString(R.string.vault_auth_biometric_prompt_subtitle))
            .setNegativeButtonText(getString(R.string.vault_auth_use_pin))
            .build()

        prompt.authenticate(promptInfo)
    }
}
