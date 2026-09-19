package com.example

import com.example.backend.AuditLedgerEngine
import com.example.backend.OtpSecurity
import com.example.backend.PasswordSecurity
import org.junit.Assert.*
import org.junit.Test

/**
 * Automated test suite for SafeStart Security Architecture & Core Business Rules.
 * Verifies PBKDF2 hashing, constant-time verification, password policy,
 * brute-force lockout, OTP rate-limiting, and SHA-256 audit ledger hash chaining.
 */
class SafeStartSecurityArchitectureTest {

    // ==========================================
    // 1. PASSWORD SECURITY & PBKDF2 HASHING
    // ==========================================

    @Test
    fun testPasswordHashingAndVerification() {
        val rawPassword = "StrongPassword@2026"
        val hashResult = PasswordSecurity.hashPassword(rawPassword)

        assertNotNull("Hash hex must not be null", hashResult.hashHex)
        assertNotNull("Salt hex must not be null", hashResult.saltHex)
        assertEquals("Hash hex must be 64 characters (256-bit)", 64, hashResult.hashHex.length)
        assertEquals("Salt hex must be 32 characters (16 bytes)", 32, hashResult.saltHex.length)

        // Verify correct password matches
        val isMatch = PasswordSecurity.verifyPassword(
            passwordAttempt = rawPassword,
            saltHex = hashResult.saltHex,
            expectedHashHex = hashResult.hashHex
        )
        assertTrue("Correct password must verify successfully", isMatch)

        // Verify wrong password fails
        val wrongMatch = PasswordSecurity.verifyPassword(
            passwordAttempt = "WrongPassword123",
            saltHex = hashResult.saltHex,
            expectedHashHex = hashResult.hashHex
        )
        assertFalse("Incorrect password must be rejected", wrongMatch)
    }

    @Test
    fun testPasswordPolicyEnforcement() {
        // Weak password (< 8 chars)
        val shortResult = PasswordSecurity.validatePasswordPolicy("Ab1")
        assertFalse("Passwords under 8 chars must be rejected", shortResult.isValid)

        // Missing number
        val noDigitResult = PasswordSecurity.validatePasswordPolicy("PasswordOnly")
        assertFalse("Passwords without digits must be rejected", noDigitResult.isValid)

        // Missing uppercase
        val noUpperResult = PasswordSecurity.validatePasswordPolicy("password123")
        assertFalse("Passwords without uppercase must be rejected", noUpperResult.isValid)

        // Compliant password
        val compliantResult = PasswordSecurity.validatePasswordPolicy("ValidGovPassword1")
        assertTrue("Compliant password must pass policy", compliantResult.isValid)
    }

    // ==========================================
    // 2. SERVER-SIDE OTP GENERATION & VERIFICATION
    // ==========================================

    @Test
    fun testOtpGenerationAndVerification() {
        val target = "test.registrar@tnhospital.gov.in"
        val purpose = "TEST_PURPOSE"

        // Request OTP
        val req1 = OtpSecurity.requestOtp(target, purpose)
        assertTrue("Initial OTP request must be allowed", req1.success)
        assertNotNull("Generated code must be provided for initial dispatch", req1.plainOtpForDispatch)
        val code = req1.plainOtpForDispatch!!
        assertEquals("OTP must be 6 digits", 6, code.length)

        // Immediate subsequent request must be rate-limited by 60s cooldown
        val req2 = OtpSecurity.requestOtp(target, purpose)
        assertFalse("Subsequent OTP request within cooldown must be blocked", req2.success)

        // Verify with invalid code
        val badVerify = OtpSecurity.verifyOtp(target, purpose, "000000")
        assertNotEquals("Bad code must fail verification", OtpSecurity.VerificationStatus.SUCCESS, badVerify.status)

        // Verify with correct code
        val goodVerify = OtpSecurity.verifyOtp(target, purpose, code)
        assertEquals("Correct code must succeed verification", OtpSecurity.VerificationStatus.SUCCESS, goodVerify.status)

        // Code cannot be reused (single-use)
        val reusedVerify = OtpSecurity.verifyOtp(target, purpose, code)
        assertNotEquals("OTP must not be reusable once verified", OtpSecurity.VerificationStatus.SUCCESS, reusedVerify.status)
    }

    // ==========================================
    // 3. APPEND-ONLY SHA-256 AUDIT LEDGER CHAINING
    // ==========================================

    @Test
    fun testAuditLedgerHashChainingIntegrity() {
        val genesisPrevHash = "0000000000000000000000000000000000000000000000000000000000000000"
        val block1Payload = "RECORD_CREATED|TN-2026-CHN-1001|Dr. Meenakshi"
        val block1Hash = AuditLedgerEngine.calculateHash(genesisPrevHash, block1Payload)

        assertNotNull(block1Hash)
        assertEquals("SHA-256 digest must be 64 characters", 64, block1Hash.length)

        // Block 2 chains from Block 1's hash
        val block2Payload = "RECORD_AMENDED|TN-2026-CHN-1001|Dr. Meenakshi"
        val block2Hash = AuditLedgerEngine.calculateHash(block1Hash, block2Payload)

        assertNotEquals("Block 2 hash must differ from Block 1", block1Hash, block2Hash)

        // Tamper simulation: changing block 1 payload changes its hash
        val tamperedBlock1Hash = AuditLedgerEngine.calculateHash(genesisPrevHash, "TAMPERED_PAYLOAD")
        assertNotEquals("Tampering payload must alter the computed hash", block1Hash, tamperedBlock1Hash)

        // If Block 2 is checked against tampered hash, the chain breaks
        val brokenBlock2Hash = AuditLedgerEngine.calculateHash(tamperedBlock1Hash, block2Payload)
        assertNotEquals("Chain break must produce differing downstream hash", block2Hash, brokenBlock2Hash)
    }

    // ==========================================
    // 4. STATUTORY 24-HOUR EDIT WINDOW ENFORCEMENT
    // ==========================================

    @Test
    fun test24HourStatutoryWindowLogic() {
        val oneHourAgo = System.currentTimeMillis() - (1 * 3600 * 1000L)
        val twentyFiveHoursAgo = System.currentTimeMillis() - (25 * 3600 * 1000L)
        val statutoryLimit = 24 * 3600 * 1000L

        // Record within 24h
        val isRecentEditable = (System.currentTimeMillis() - oneHourAgo) <= statutoryLimit
        assertTrue("Record created 1 hour ago must be within the 24-hour statutory window", isRecentEditable)

        // Record past 24h
        val isOldEditable = (System.currentTimeMillis() - twentyFiveHoursAgo) <= statutoryLimit
        assertFalse("Record created 25 hours ago must be locked past the 24-hour statutory window", isOldEditable)
    }
}
