package com.example.backend

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.data.*

/**
 * Native Android SQLite database persistence layer for SafeStart.
 * Guarantees persistent storage on disk across application restarts, process termination,
 * and device reboots.
 */
class SafeStartDatabase(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "safestart_secure.db"
        const val DATABASE_VERSION = 1

        @Volatile
        private var instance: SafeStartDatabase? = null

        fun getInstance(context: Context): SafeStartDatabase {
            return instance ?: synchronized(this) {
                instance ?: SafeStartDatabase(context).also { instance = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        // 1. Accounts Table (Hashed passwords & salts)
        db.execSQL(
            """
            CREATE TABLE accounts (
                id TEXT PRIMARY KEY,
                official_name TEXT NOT NULL,
                official_email TEXT NOT NULL UNIQUE,
                facility_name TEXT NOT NULL,
                district TEXT NOT NULL,
                role TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                salt_hex TEXT NOT NULL,
                failed_attempts INTEGER DEFAULT 0,
                locked_until_ms INTEGER DEFAULT 0,
                created_at TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 2. Newborn Records Table
        db.execSQL(
            """
            CREATE TABLE records (
                token TEXT PRIMARY KEY,
                father_name TEXT NOT NULL,
                mother_name TEXT NOT NULL,
                gender TEXT NOT NULL,
                birth_timestamp TEXT NOT NULL,
                created_timestamp_ms INTEGER NOT NULL,
                doctor_name TEXT NOT NULL,
                hospital_name TEXT NOT NULL,
                hospital_id TEXT NOT NULL,
                district TEXT NOT NULL,
                hospital_location TEXT NOT NULL,
                parent_mobile TEXT NOT NULL,
                parent_email TEXT NOT NULL,
                picme_number TEXT,
                mother_aadhaar TEXT,
                crs_registration_number TEXT,
                status TEXT NOT NULL,
                biometric_hash TEXT NOT NULL,
                is_council_locked INTEGER DEFAULT 0,
                ward_status TEXT NOT NULL,
                updated_at_ms INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // 3. Append-Only Cryptographic Audit Ledger Table
        db.execSQL(
            """
            CREATE TABLE audit_ledger (
                event_id TEXT PRIMARY KEY,
                sequence_num INTEGER NOT NULL,
                timestamp_ms INTEGER NOT NULL,
                timestamp_str TEXT NOT NULL,
                actor_id TEXT NOT NULL,
                actor_role TEXT NOT NULL,
                hospital_id TEXT,
                event_type TEXT NOT NULL,
                affected_record_id TEXT,
                details TEXT NOT NULL,
                previous_hash TEXT NOT NULL,
                current_hash TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 4. Password Reset Requests Table
        db.execSQL(
            """
            CREATE TABLE password_resets (
                id TEXT PRIMARY KEY,
                hospital_name TEXT NOT NULL,
                hospital_id TEXT NOT NULL,
                district TEXT NOT NULL,
                registrar_name TEXT NOT NULL,
                reason TEXT NOT NULL,
                timestamp_str TEXT NOT NULL,
                status TEXT NOT NULL,
                approval_token TEXT,
                expires_at_ms INTEGER DEFAULT 0,
                is_redeemed INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        // 5. Hospital Complaints Table
        db.execSQL(
            """
            CREATE TABLE complaints (
                complaint_id TEXT PRIMARY KEY,
                hospital_name TEXT NOT NULL,
                hospital_id TEXT NOT NULL,
                child_name TEXT NOT NULL,
                issue_type TEXT NOT NULL,
                details TEXT NOT NULL,
                timestamp_str TEXT NOT NULL,
                status TEXT NOT NULL
            )
            """.trimIndent()
        )

        // 6. Parentage Disputes Table
        db.execSQL(
            """
            CREATE TABLE disputes (
                case_id TEXT PRIMARY KEY,
                child_name TEXT NOT NULL,
                child_age TEXT NOT NULL,
                reporting_facility TEXT NOT NULL,
                hospital_id TEXT NOT NULL,
                grievance_category TEXT NOT NULL,
                status TEXT NOT NULL,
                match_percentage INTEGER NOT NULL,
                is_resolved INTEGER DEFAULT 0
            )
            """.trimIndent()
        )

        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle migrations if schema changes
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()

        // Seed Default Institutional Accounts with secure PBKDF2 hashes
        val hospitalHash = PasswordSecurity.hashPassword("admin123")
        val councilHash = PasswordSecurity.hashPassword("council123")

        val accountValues = ContentValues().apply {
            put("id", "HOSP-TN-8821")
            put("official_name", "Dr. S. Meenakshi Sundaram")
            put("official_email", "vimal.uv1991@gmail.com")
            put("facility_name", "Madurai Govt Rajaji Hospital")
            put("district", "Madurai")
            put("role", UserRole.HOSPITAL_REGISTRAR.name)
            put("password_hash", hospitalHash.hashHex)
            put("salt_hex", hospitalHash.saltHex)
            put("failed_attempts", 0)
            put("locked_until_ms", 0)
            put("created_at", "10 Sep 2026, 09:00 AM")
        }
        db.insert("accounts", null, accountValues)

        val councilValues = ContentValues().apply {
            put("id", "COUNCIL-TN-9901")
            put("official_name", "Dr. P. Rajeswari, IAS / DME")
            put("official_email", "council.dme@tn.gov.in")
            put("facility_name", "Tamil Nadu Medical Council Directorate")
            put("district", "Chennai Central")
            put("role", UserRole.MEDICAL_COUNCIL.name)
            put("password_hash", councilHash.hashHex)
            put("salt_hex", councilHash.saltHex)
            put("failed_attempts", 0)
            put("locked_until_ms", 0)
            put("created_at", "01 Sep 2026, 10:00 AM")
        }
        db.insert("accounts", null, councilValues)

        // Seed initial audit blocks starting from Genesis
        var prevHash = AuditLedgerEngine.GENESIS_HASH

        val genesisBlock = AuditLedgerEngine.createEntry(
            sequenceNumber = 1L,
            actorId = "SYSTEM_INITIALIZER",
            actorRole = "SYSTEM",
            hospitalId = null,
            eventType = "SYSTEM_INITIALIZATION",
            affectedRecordId = null,
            details = "SafeStart Prototype SQLite persistence database initialized with cryptographic hash chaining.",
            previousHash = prevHash
        )
        insertAuditEntryDirect(db, genesisBlock)
        prevHash = genesisBlock.currentHash

        val seedRecords = listOf(
            NewbornRecord(
                token = "TN-2026-MDU-8842",
                fatherName = "R. Karthikeyan",
                motherName = "K. Deepa Lakshmi",
                gender = "Female",
                birthTimestamp = "13 Sep 2026, 04:30 AM",
                doctorName = "Dr. S. Meenakshi Sundaram",
                hospitalName = "Madurai Govt Rajaji Hospital",
                district = "Madurai",
                hospitalLocation = "Madurai, Tamil Nadu",
                parentMobile = "+91 98401 88392",
                parentEmail = "karthik.deepa@gmail.com",
                picmeNumber = "2026/TN/09/88219",
                motherAadhaar = "XXXXXXXX4921",
                crsRegistrationNumber = "CRS/2026/MDU/00482",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "3f798e12d4a5b6c7e8f90123456789abcdef0123456789abcdef0123456789ab",
                secondsRemaining = 76442L,
                isCouncilLocked = false,
                wardStatus = "Postnatal Ward"
            ),
            NewbornRecord(
                token = "TN-2026-CHN-1029",
                fatherName = "M. Senthil Kumar",
                motherName = "S. Priya Dharshini",
                gender = "Male",
                birthTimestamp = "13 Sep 2026, 02:15 AM",
                doctorName = "Dr. R. Arumugam",
                hospitalName = "Institute of Child Health (ICH) Egmore",
                district = "Chennai",
                hospitalLocation = "Chennai, Tamil Nadu",
                parentMobile = "+91 94440 12894",
                parentEmail = "senthil.priya@gmail.com",
                picmeNumber = "2026/TN/09/10928",
                motherAadhaar = "XXXXXXXX9012",
                crsRegistrationNumber = "CRS/2026/CHN/01029",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "a1b2c3d4e5f60718293a4b5c6d7e8f90123456789abcdef0123456789abcdef0",
                secondsRemaining = 68200L,
                isCouncilLocked = false,
                wardStatus = "Labor & Delivery"
            ),
            NewbornRecord(
                token = "TN-2026-CBE-4491",
                fatherName = "A. Murugesan",
                motherName = "M. Revathi",
                gender = "Female",
                birthTimestamp = "12 Sep 2026, 11:20 PM",
                doctorName = "Dr. K. Jayanthi",
                hospitalName = "Coimbatore Medical College Hospital",
                district = "Coimbatore",
                hospitalLocation = "Coimbatore, Tamil Nadu",
                parentMobile = "+91 97890 34561",
                parentEmail = "murugesan.rev@gmail.com",
                picmeNumber = "2026/TN/09/44910",
                motherAadhaar = "XXXXXXXX1183",
                crsRegistrationNumber = "CRS/2026/CBE/04491",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "9876543210fedcba0987654321fedcba9876543210fedcba0987654321fedcba",
                secondsRemaining = 58910L,
                isCouncilLocked = false,
                wardStatus = "NICU / SNCU"
            ),
            NewbornRecord(
                token = "TN-2026-TIR-9912",
                fatherName = "P. Suresh Babu",
                motherName = "S. Radhika",
                gender = "Male",
                birthTimestamp = "11 Sep 2026, 06:10 PM",
                doctorName = "Dr. V. Chandran",
                hospitalName = "Tiruchirappalli K.A.P.V. Govt Medical College",
                district = "Tiruchirappalli",
                hospitalLocation = "Tiruchirappalli, Tamil Nadu",
                parentMobile = "+91 99400 77123",
                parentEmail = "suresh.radhika@gmail.com",
                picmeNumber = "2026/TN/09/99121",
                motherAadhaar = "XXXXXXXX7724",
                crsRegistrationNumber = "CRS/2026/TIR/09912",
                status = "Statutory Record (24h Window Closed)",
                biometricHash = "456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123",
                secondsRemaining = 0L,
                isCouncilLocked = true,
                wardStatus = "Special Care Nursery"
            )
        )

        for (rec in seedRecords) {
            val recordValues = ContentValues().apply {
                put("token", rec.token)
                put("father_name", rec.fatherName)
                put("mother_name", rec.motherName)
                put("gender", rec.gender)
                put("birth_timestamp", rec.birthTimestamp)
                put("created_timestamp_ms", now - (86400L - rec.secondsRemaining) * 1000L)
                put("doctor_name", rec.doctorName)
                put("hospital_name", rec.hospitalName)
                put("hospital_id", "HOSP-" + rec.district.take(3).uppercase())
                put("district", rec.district)
                put("hospital_location", rec.hospitalLocation)
                put("parent_mobile", rec.parentMobile)
                put("parent_email", rec.parentEmail)
                put("picme_number", rec.picmeNumber)
                put("mother_aadhaar", rec.motherAadhaar)
                put("crs_registration_number", rec.crsRegistrationNumber)
                put("status", rec.status)
                put("biometric_hash", rec.biometricHash)
                put("is_council_locked", if (rec.isCouncilLocked) 1 else 0)
                put("ward_status", rec.wardStatus)
                put("updated_at_ms", now)
            }
            db.insert("records", null, recordValues)

            val auditBlock = AuditLedgerEngine.createEntry(
                sequenceNumber = db.compileStatement("SELECT COUNT(*) FROM audit_ledger").simpleQueryForLong() + 1L,
                actorId = "HOSP-" + rec.district.take(3).uppercase(),
                actorRole = UserRole.HOSPITAL_REGISTRAR.name,
                hospitalId = "HOSP-" + rec.district.take(3).uppercase(),
                eventType = "RECORD_SEEDED",
                affectedRecordId = rec.token,
                details = "Initial record committed with token ${rec.token} for ${rec.motherName} & ${rec.fatherName}.",
                previousHash = prevHash
            )
            insertAuditEntryDirect(db, auditBlock)
            prevHash = auditBlock.currentHash
        }

        // Seed Default Password Reset Requests
        val resetValues = ContentValues().apply {
            put("id", "REQ-HOSP-4491")
            put("hospital_name", "Coimbatore Medical College Hospital")
            put("hospital_id", "HOSP-TN-4491")
            put("district", "Coimbatore")
            put("registrar_name", "Dr. K. Jayanthi (Lead Registrar)")
            put("reason", "Lead registrar workstation terminal reassigned following department rotation.")
            put("timestamp_str", "Today, 08:30 AM")
            put("status", "PENDING")
            put("approval_token", null as String?)
            put("expires_at_ms", 0L)
            put("is_redeemed", 0)
        }
        db.insert("password_resets", null, resetValues)

        // Seed Default Complaints
        val complaintValues = ContentValues().apply {
            put("complaint_id", "CMP-2026-8812")
            put("hospital_name", "Madurai Govt Rajaji Hospital")
            put("hospital_id", "HOSP-TN-8821")
            put("child_name", "Baby of Lakshmi (Token #TN-2026-MDU-8842)")
            put("issue_type", "Discomfort")
            put("details", "Family expressed concern regarding post-delivery discharge protocol. Verification of plantar prints requested.")
            put("timestamp_str", "13 Sep 2026, 09:15 AM")
            put("status", "SUBMITTED_TO_COUNCIL")
        }
        db.insert("complaints", null, complaintValues)

        // Seed Default Disputes
        val disputeValues = ContentValues().apply {
            put("case_id", "DISP-TN-2026-081")
            put("child_name", "Baby of Meenakshi / Deepa")
            put("child_age", "3 Days Old")
            put("reporting_facility", "Madurai Govt Rajaji Hospital")
            put("hospital_id", "HOSP-TN-8821")
            put("grievance_category", "Maternity Ward Tag Confusion Raised by Attendant")
            put("status", "UNDER_TRIBUNAL_CROSSCHECK")
            put("match_percentage", 100)
            put("is_resolved", 0)
        }
        db.insert("disputes", null, disputeValues)
    }

    private fun insertAuditEntryDirect(db: SQLiteDatabase, entry: AuditLedgerEngine.AuditEntry) {
        val values = ContentValues().apply {
            put("event_id", entry.eventId)
            put("sequence_num", entry.sequenceNumber)
            put("timestamp_ms", entry.timestampMs)
            put("timestamp_str", entry.timestampStr)
            put("actor_id", entry.actorId)
            put("actor_role", entry.actorRole)
            put("hospital_id", entry.hospitalId)
            put("event_type", entry.eventType)
            put("affected_record_id", entry.affectedRecordId)
            put("details", entry.details)
            put("previous_hash", entry.previousHash)
            put("current_hash", entry.currentHash)
        }
        db.insert("audit_ledger", null, values)
    }
}
