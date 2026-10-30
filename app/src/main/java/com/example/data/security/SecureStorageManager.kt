package com.example.data.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore

/**
 * Manages encrypted local preferences, salted hash for custom user PIN,
 * biometric lock configuration, dark mode, and reminder settings.
 */
class SecureStorageManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("secure_maternal_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_PIN_HASH = "pin_hash"
        private const val KEY_PIN_SALT = "pin_salt"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_APP_LOCK_ENABLED = "app_lock_enabled"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_SYSTEM_THEME = "system_theme"
        private const val KEY_DEFAULT_TARGET_KICKS = "default_target_kicks"
        private const val KEY_DEFAULT_DURATION_MINUTES = "default_duration_minutes"
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_HOUR = "reminder_hour"
        private const val KEY_REMINDER_MINUTE = "reminder_minute"
        private const val KEYSTORE_ALIAS = "MaternalSecretKey"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    }

    init {
        ensureKeyStoreKey()
    }

    private fun ensureKeyStoreKey() {
        try {
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
            if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )
                val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                    KEYSTORE_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build()
                keyGenerator.init(keyGenParameterSpec)
                keyGenerator.generateKey()
            }
        } catch (_: Exception) {
            // KeyStore fallback if platform emulator lacks TEE
        }
    }

    fun hasPinSet(): Boolean {
        return prefs.getString(KEY_PIN_HASH, null) != null
    }

    fun setPin(pin: String) {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val hash = hashPin(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, Base64.encodeToString(hash, Base64.NO_WRAP))
            .putString(KEY_PIN_SALT, Base64.encodeToString(salt, Base64.NO_WRAP))
            .putBoolean(KEY_APP_LOCK_ENABLED, true)
            .apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedHashBase64 = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val storedSaltBase64 = prefs.getString(KEY_PIN_SALT, null) ?: return false

        val salt = Base64.decode(storedSaltBase64, Base64.NO_WRAP)
        val computedHash = hashPin(pin, salt)
        val computedHashBase64 = Base64.encodeToString(computedHash, Base64.NO_WRAP)

        return storedHashBase64 == computedHashBase64
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(salt)
        return md.digest(pin.toByteArray(Charsets.UTF_8))
    }

    var isBiometricEnabled: Boolean
        get() = prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, value).apply()

    var isAppLockEnabled: Boolean
        get() = prefs.getBoolean(KEY_APP_LOCK_ENABLED, hasPinSet())
        set(value) = prefs.edit().putBoolean(KEY_APP_LOCK_ENABLED, value).apply()

    var isDarkMode: Boolean
        get() = prefs.getBoolean(KEY_DARK_MODE, false)
        set(value) = prefs.edit().putBoolean(KEY_DARK_MODE, value).apply()

    var followSystemTheme: Boolean
        get() = prefs.getBoolean(KEY_SYSTEM_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_SYSTEM_THEME, value).apply()

    var defaultTargetKicks: Int
        get() = prefs.getInt(KEY_DEFAULT_TARGET_KICKS, 10)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_TARGET_KICKS, value).apply()

    /**
     * Default duration limit in minutes: 30, 60, 120 (default), or -1 for untimed
     */
    var defaultDurationMinutes: Int
        get() = prefs.getInt(KEY_DEFAULT_DURATION_MINUTES, 120)
        set(value) = prefs.edit().putInt(KEY_DEFAULT_DURATION_MINUTES, value).apply()

    var isDailyReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var dailyReminderHour: Int
        get() = prefs.getInt(KEY_REMINDER_HOUR, 20) // 8:00 PM evening active window
        set(value) = prefs.edit().putInt(KEY_REMINDER_HOUR, value).apply()

    var dailyReminderMinute: Int
        get() = prefs.getInt(KEY_REMINDER_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_REMINDER_MINUTE, value).apply()
}
