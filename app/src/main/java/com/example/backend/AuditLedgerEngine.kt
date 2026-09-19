package com.example.backend

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Append-only cryptographic audit ledger engine.
 * Implements SHA-256 hash-chaining:
 * Block[i].currentHash = SHA-256(Block[i].previousHash + Block[i].payload)
 *
 * Provides tamper-detection verification across all security-sensitive events.
 * Academic Prototype — Honest Architecture (Not Hardware HSM / FIPS).
 */
object AuditLedgerEngine {
    const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"

    data class AuditEntry(
        val eventId: String,
        val sequenceNumber: Long,
        val timestampMs: Long,
        val timestampStr: String,
        val actorId: String,
        val actorRole: String,
        val hospitalId: String?,
        val eventType: String,
        val affectedRecordId: String?,
        val details: String,
        val previousHash: String,
        val currentHash: String
    )

    data class ChainVerificationResult(
        val isValid: Boolean,
        val totalBlocks: Int,
        val brokenBlockIndex: Int? = null,
        val message: String
    )

    /**
     * Creates a new chained audit entry from the given parameters and previous hash.
     */
    fun createEntry(
        sequenceNumber: Long,
        actorId: String,
        actorRole: String,
        hospitalId: String?,
        eventType: String,
        affectedRecordId: String?,
        details: String,
        previousHash: String
    ): AuditEntry {
        val nowMs = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm:ss a", Locale.getDefault()).format(Date(nowMs))
        val eventId = "EVT-${UUID.randomUUID().toString().take(8).uppercase()}"

        val currentHash = computeBlockHash(
            previousHash = previousHash,
            sequenceNumber = sequenceNumber,
            timestampMs = nowMs,
            actorId = actorId,
            actorRole = actorRole,
            hospitalId = hospitalId ?: "NONE",
            eventType = eventType,
            affectedRecordId = affectedRecordId ?: "NONE",
            details = details
        )

        return AuditEntry(
            eventId = eventId,
            sequenceNumber = sequenceNumber,
            timestampMs = nowMs,
            timestampStr = dateStr,
            actorId = actorId,
            actorRole = actorRole,
            hospitalId = hospitalId,
            eventType = eventType,
            affectedRecordId = affectedRecordId,
            details = details,
            previousHash = previousHash,
            currentHash = currentHash
        )
    }

    /**
     * Computes the SHA-256 hash over block header and payload.
     */
    fun computeBlockHash(
        previousHash: String,
        sequenceNumber: Long,
        timestampMs: Long,
        actorId: String,
        actorRole: String,
        hospitalId: String,
        eventType: String,
        affectedRecordId: String,
        details: String
    ): String {
        val payload = "$previousHash|$sequenceNumber|$timestampMs|$actorId|$actorRole|$hospitalId|$eventType|$affectedRecordId|$details"
        return calculateHash(previousHash, payload)
    }

    /**
     * Convenience hashing function for chaining tests and payload verification.
     */
    fun calculateHash(previousHash: String, payload: String): String {
        val combined = "$previousHash||$payload"
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(combined.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Verifies the full chain integrity from genesis to tip.
     * Detects any mutation, deletion, or reordering of audit blocks.
     */
    fun verifyChainIntegrity(chain: List<AuditEntry>): ChainVerificationResult {
        if (chain.isEmpty()) {
            return ChainVerificationResult(
                isValid = true,
                totalBlocks = 0,
                message = "Audit chain is empty. Integrity intact."
            )
        }

        var expectedPrevHash = GENESIS_HASH

        for (i in chain.indices) {
            val block = chain[i]

            // 1. Verify previous hash pointer
            if (block.previousHash != expectedPrevHash) {
                return ChainVerificationResult(
                    isValid = false,
                    totalBlocks = chain.size,
                    brokenBlockIndex = i,
                    message = "Broken Hash Pointer at block #$i (ID: ${block.eventId}). Previous hash mismatch."
                )
            }

            // 2. Verify current block hash integrity
            val recomputedHash = computeBlockHash(
                previousHash = block.previousHash,
                sequenceNumber = block.sequenceNumber,
                timestampMs = block.timestampMs,
                actorId = block.actorId,
                actorRole = block.actorRole,
                hospitalId = block.hospitalId ?: "NONE",
                eventType = block.eventType,
                affectedRecordId = block.affectedRecordId ?: "NONE",
                details = block.details
            )

            if (recomputedHash != block.currentHash) {
                return ChainVerificationResult(
                    isValid = false,
                    totalBlocks = chain.size,
                    brokenBlockIndex = i,
                    message = "Tampered Payload detected at block #$i (ID: ${block.eventId}). Stored hash does not match computed data."
                )
            }

            expectedPrevHash = block.currentHash
        }

        return ChainVerificationResult(
            isValid = true,
            totalBlocks = chain.size,
            message = "All ${chain.size} audit blocks cryptographically verified. Hash chain is unbroken."
        )
    }
}
