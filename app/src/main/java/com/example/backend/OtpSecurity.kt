package com.example.backend

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.concurrent.ConcurrentHashMap

/**
 * Server-side OTP management:
 * - Cryptographically secure generation (SecureRandom)
 * - Server-side SHA-256 hash storage with salt
 * - 10-minute expiry
 * - Maximum 5 failed verification attempts
 * - Rate limiting (60s minimum interval per target)
 * - Pure server-side validation; plain OTP is never returned to client during verification
 */
object OtpSecurity {
    private const val OTP_EXPIRY_MS = 10 * 60 * 1000L // 10 minutes
    private const val MAX_ATTEMPTS = 5
    private const val RATE_LIMIT_MS = 60 * 1000L // 60 seconds

    private val secureRandom = SecureRandom()

    data class OtpRecord(
        val targetIdentifier: String,
        val purpose: String,
        val salt: String,
        val otpHash: String,
        val createdAtMs: Long,
        val expiresAtMs: Long,
        var attemptCount: Int = 0,
        var isVerified: Boolean = false
    )

    data class OtpRequestResult(
        val success: Boolean,
        val message: String,
        val plainOtpForDispatch: String? = null, // Only for internal dispatch gateway (e.g. Email/SMS sender)
        val cooldownSecondsRemaining: Long = 0L
    )

    enum class VerificationStatus {
        SUCCESS,
        INVALID_CODE,
        EXPIRED,
        MAX_ATTEMPTS_EXCEEDED,
        NOT_FOUND,
        ALREADY_VERIFIED
    }

    data class OtpVerificationResult(
        val status: VerificationStatus,
        val message: String,
        val remainingAttempts: Int = 0
    )

    private val activeOtps = ConcurrentHashMap<String, OtpRecord>()
    private val lastRequestTimes = ConcurrentHashMap<String, Long>()

    /**
     * Generates a 6-digit OTP, applies rate limiting, stores salted hash, and returns
     * dispatch payload for the secure server-side messaging provider.
     */
    fun requestOtp(targetIdentifier: String, purpose: String): OtpRequestResult {
        val target = targetIdentifier.trim().lowercase()
        val key = "$target:$purpose"
        val now = System.currentTimeMillis()

        // Rate limiting check: 60-second cooldown
        val lastTime = lastRequestTimes[key] ?: 0L
        val elapsed = now - lastTime
        if (elapsed < RATE_LIMIT_MS) {
            val remainingSec = (RATE_LIMIT_MS - elapsed) / 1000L
            return OtpRequestResult(
                success = false,
                message = "Please wait $remainingSec seconds before requesting another code.",
                cooldownSecondsRemaining = remainingSec
            )
        }

        // Generate 6-digit cryptographically secure OTP
        val number = 100000 + secureRandom.nextInt(900000)
        val plainOtp = number.toString()

        // Generate salt and hash
        val saltBytes = ByteArray(16).also { secureRandom.nextBytes(it) }
        val saltHex = saltBytes.toHex()
        val otpHash = computeHash(plainOtp, saltHex)

        val record = OtpRecord(
            targetIdentifier = target,
            purpose = purpose,
            salt = saltHex,
            otpHash = otpHash,
            createdAtMs = now,
            expiresAtMs = now + OTP_EXPIRY_MS,
            attemptCount = 0,
            isVerified = false
        )

        activeOtps[key] = record
        lastRequestTimes[key] = now

        return OtpRequestResult(
            success = true,
            message = "Verification code generated and dispatched.",
            plainOtpForDispatch = plainOtp
        )
    }

    /**
     * Verifies the entered candidate OTP completely on the server-side.
     */
    fun verifyOtp(targetIdentifier: String, purpose: String, candidateOtp: String): OtpVerificationResult {
        val target = targetIdentifier.trim().lowercase()
        val key = "$target:$purpose"
        val record = activeOtps[key] ?: return OtpVerificationResult(
            status = VerificationStatus.NOT_FOUND,
            message = "No active verification request found. Please request a new code."
        )

        if (record.isVerified) {
            return OtpVerificationResult(
                status = VerificationStatus.ALREADY_VERIFIED,
                message = "Code has already been verified."
            )
        }

        val now = System.currentTimeMillis()
        if (now > record.expiresAtMs) {
            activeOtps.remove(key)
            return OtpVerificationResult(
                status = VerificationStatus.EXPIRED,
                message = "The verification code has expired. Please request a new one."
            )
        }

        if (record.attemptCount >= MAX_ATTEMPTS) {
            activeOtps.remove(key)
            return OtpVerificationResult(
                status = VerificationStatus.MAX_ATTEMPTS_EXCEEDED,
                message = "Maximum verification attempts exceeded. Please request a new code."
            )
        }

        record.attemptCount += 1
        val candidateHash = computeHash(candidateOtp.trim(), record.salt)

        if (candidateHash == record.otpHash) {
            record.isVerified = true
            return OtpVerificationResult(
                status = VerificationStatus.SUCCESS,
                message = "Verification successful."
            )
        } else {
            val remaining = MAX_ATTEMPTS - record.attemptCount
            if (remaining <= 0) {
                activeOtps.remove(key)
                return OtpVerificationResult(
                    status = VerificationStatus.MAX_ATTEMPTS_EXCEEDED,
                    message = "Invalid code. Maximum attempts reached. Request a new code."
                )
            }
            return OtpVerificationResult(
                status = VerificationStatus.INVALID_CODE,
                message = "Invalid verification code. $remaining attempt(s) remaining.",
                remainingAttempts = remaining
            )
        }
    }

    /**
     * Checks if a target has a currently verified active OTP for the specified purpose.
     */
    fun isVerified(targetIdentifier: String, purpose: String): Boolean {
        val target = targetIdentifier.trim().lowercase()
        val key = "$target:$purpose"
        val record = activeOtps[key] ?: return false
        val now = System.currentTimeMillis()
        return record.isVerified && now <= record.expiresAtMs
    }

    /**
     * Clears verification token after successful usage in transaction.
     */
    fun consumeVerification(targetIdentifier: String, purpose: String) {
        val target = targetIdentifier.trim().lowercase()
        val key = "$target:$purpose"
        activeOtps.remove(key)
    }

    private fun computeHash(input: String, saltHex: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        md.update(saltHex.toByteArray(Charsets.UTF_8))
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
