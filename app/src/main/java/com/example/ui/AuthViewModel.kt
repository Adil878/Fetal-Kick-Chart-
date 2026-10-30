package com.example.ui

import android.app.Application
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import com.example.data.security.SecureStorageManager
import com.example.security.BiometricAuthHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AuthState {
    CHECKING,
    UNLOCKED,
    LOCKED,
    SETUP_PIN,
    CONFIRM_PIN
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val secureStorage = SecureStorageManager(application)

    private val _authState = MutableStateFlow(AuthState.CHECKING)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _pinErrorMessage = MutableStateFlow<String?>(null)
    val pinErrorMessage: StateFlow<String?> = _pinErrorMessage.asStateFlow()

    private var temporaryPinForSetup: String? = null

    init {
        checkInitialLockState()
    }

    fun checkInitialLockState() {
        if (!secureStorage.isAppLockEnabled) {
            _authState.value = AuthState.UNLOCKED
        } else if (!secureStorage.hasPinSet()) {
            _authState.value = AuthState.SETUP_PIN
        } else {
            _authState.value = AuthState.LOCKED
        }
    }

    fun tryBiometricUnlock(activity: FragmentActivity) {
        if (_authState.value != AuthState.LOCKED) return
        if (!secureStorage.isBiometricEnabled || !BiometricAuthHelper.isBiometricAvailable(activity)) {
            return
        }

        BiometricAuthHelper.showBiometricPrompt(
            activity = activity,
            onSuccess = {
                _authState.value = AuthState.UNLOCKED
                _enteredPin.value = ""
                _pinErrorMessage.value = null
            },
            onError = { _, errString ->
                // Biometrics cancelled or error -> fall back to PIN keypad
                _pinErrorMessage.value = errString.toString()
            },
            onFailed = {
                _pinErrorMessage.value = "Biometric not recognized. Please use PIN."
            }
        )
    }

    fun onPinDigit(digit: String) {
        if (_enteredPin.value.length < 4) {
            val newPin = _enteredPin.value + digit
            _enteredPin.value = newPin
            _pinErrorMessage.value = null

            if (newPin.length == 4) {
                handleCompletePin(newPin)
            }
        }
    }

    fun onPinDelete() {
        if (_enteredPin.value.isNotEmpty()) {
            _enteredPin.value = _enteredPin.value.dropLast(1)
            _pinErrorMessage.value = null
        }
    }

    private fun handleCompletePin(pin: String) {
        when (_authState.value) {
            AuthState.LOCKED -> {
                if (secureStorage.verifyPin(pin)) {
                    _authState.value = AuthState.UNLOCKED
                    _enteredPin.value = ""
                    _pinErrorMessage.value = null
                } else {
                    _enteredPin.value = ""
                    _pinErrorMessage.value = "Incorrect PIN. Try again."
                }
            }
            AuthState.SETUP_PIN -> {
                temporaryPinForSetup = pin
                _enteredPin.value = ""
                _authState.value = AuthState.CONFIRM_PIN
            }
            AuthState.CONFIRM_PIN -> {
                if (pin == temporaryPinForSetup) {
                    secureStorage.setPin(pin)
                    _authState.value = AuthState.UNLOCKED
                    _enteredPin.value = ""
                    _pinErrorMessage.value = null
                    temporaryPinForSetup = null
                } else {
                    _enteredPin.value = ""
                    _pinErrorMessage.value = "PINs do not match. Try again."
                    _authState.value = AuthState.SETUP_PIN
                    temporaryPinForSetup = null
                }
            }
            else -> {}
        }
    }

    fun lockApp() {
        if (secureStorage.isAppLockEnabled && secureStorage.hasPinSet()) {
            _authState.value = AuthState.LOCKED
            _enteredPin.value = ""
            _pinErrorMessage.value = null
        }
    }

    fun initiatePinReset() {
        _authState.value = AuthState.SETUP_PIN
        _enteredPin.value = ""
        _pinErrorMessage.value = null
        temporaryPinForSetup = null
    }
}
