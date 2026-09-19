package com.example.backend

import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.InvalidKeySpecException
import java.security.spec.KeySpec
import java.util.concurrent.ConcurrentHashMap
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Enterprise-grade password security implementing PBKDF2WithHmacSHA256,
 * per-user cryptographic salts, password complexity enforcement, and
 * server-side rate limiting / lockout controls.
 */
object PasswordSecurity {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 65536
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16 // 128-bit salt
    private const val MAX_FAILED_ATTEMPTS = 5
    private const val LOCKOUT_DURATION_MS = 5 * 60 * 1000L // 5 minutes

    private val secureRandom = SecureRandom()

    // Server-side login attempt tracking
    private val failedAttempts = ConcurrentHashMap<String, Int>()
    private val lockedUntil = ConcurrentHashMap<String, Long>()

    data class HashResult(
        val saltHex: String,
        val hashHex: String
    )

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null
    )

    /**
     * Generates a secure salt and computes the PBKDF2 hash.
     */
    fun hashPassword(password: String, existingSalt: ByteArray? = null): HashResult {
        val salt = existingSalt ?: ByteArray(SALT_LENGTH).also { secureRandom.nextBytes(it) }
        val spec: KeySpec = PBEKeySpec(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return HashResult(
            saltHex = salt.toHex(),
            hashHex = hash.toHex()
        )
    }

    /**
     * Verifies a password attempt against stored salt and expected hash.
     */
    fun verifyPassword(passwordAttempt: String, saltHex: String, expectedHashHex: String): Boolean {
        return try {
            val salt = saltHex.fromHex()
            val computed = hashPassword(passwordAttempt, salt)
            // Constant-time comparison to prevent timing attacks
            constantTimeEquals(computed.hashHex, expectedHashHex)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Validates that password meets institutional security policy:
     * Minimum 8 characters, at least 1 digit, at least 1 uppercase letter, at least 1 lowercase letter.
     */
    fun validatePasswordPolicy(password: String): ValidationResult {
        if (password.length < 8) {
            return ValidationResult(false, "Password must be at least 8 characters long.")
        }
        if (!password.any { it.isDigit() }) {
            return ValidationResult(false, "Password must contain at least one digit (0-9).")
        }
        if (!password.any { it.isUpperCase() }) {
            return ValidationResult(false, "Password must contain at least one uppercase letter (A-Z).")
        }
        if (!password.any { it.isLowerCase() }) {
            return ValidationResult(false, "Password must contain at least one lowercase letter (a-z).")
        }
        return ValidationResult(true)
    }

    /**
     * Checks if an account is currently locked due to too many failed attempts.
     */
    fun isAccountLocked(userId: String): Boolean {
        val key = userId.trim().lowercase()
        val lockoutTime = lockedUntil[key] ?: return false
        val now = System.currentTimeMillis()
        if (now < lockoutTime) {
            return true
        }
        // Lockout expired, clean up
        lockedUntil.remove(key)
        failedAttempts.remove(key)
        return false
    }

    /**
     * Returns remaining lockout duration in seconds.
     */
    fun remainingLockoutSeconds(userId: String): Long {
        val key = userId.trim().lowercase()
        val lockoutTime = lockedUntil[key] ?: return 0L
        val remaining = lockoutTime - System.currentTimeMillis()
        return if (remaining > 0) remaining / 1000L else 0L
    }

    /**
     * Records a failed login attempt and locks account if threshold exceeded.
     */
    fun recordFailedLogin(userId: String): Int {
        val key = userId.trim().lowercase()
        val attempts = (failedAttempts[key] ?: 0) + 1
        failedAttempts[key] = attempts
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockedUntil[key] = System.currentTimeMillis() + LOCKOUT_DURATION_MS
        }
        return attempts
    }

    /**
     * Clears failed attempts upon successful login.
     */
    fun recordSuccessfulLogin(userId: String) {
        val key = userId.trim().lowercase()
        failedAttempts.remove(key)
        lockedUntil.remove(key)
    }

    private fun constantTimeEquals(a: String, b: String): Boolean {
        if (a.length != b.length) return false
        var result = 0
        for (i in a.indices) {
            result = result or (a[i].code xor b[i].code)
        }
        return result == 0
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray {
        val len = length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(this[i], 16) shl 4) +
                    Character.digit(this[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}
