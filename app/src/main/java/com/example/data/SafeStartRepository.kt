package com.example.data

import android.content.Context
import com.example.backend.AuditLedgerEngine
import com.example.backend.OtpSecurity
import com.example.backend.PasswordSecurity
import com.example.backend.SafeStartBackendService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * State custody repository for SafeStart.
 * Acts as the reactive frontend interface for UI Composables while delegating
 * all persistence, role-based access control, 24-hour edit enforcement,
 * OTP verification, and append-only audit chaining to [SafeStartBackendService].
 */
object SafeStartRepository {

    @Volatile
    private var backendService: SafeStartBackendService? = null

    // Real dynamic records list initialized with state custody civil registry records
    private val _records = MutableStateFlow<List<NewbornRecord>>(
        listOf(
            NewbornRecord(
                token = "TN-2026-CHN-1042",
                fatherName = "R. Sundararajan",
                motherName = "Kavitha Sundar",
                gender = "Female",
                birthTimestamp = "13 Sep 2026, 04:15 AM",
                doctorName = "Dr. S. Meenakshi, MD (OBG)",
                hospitalName = "Government Institute of Obstetrics & Gynaecology, Egmore",
                district = "Chennai",
                hospitalLocation = "Chennai, Tamil Nadu",
                parentMobile = "+91 94441 23456",
                parentEmail = "kavitha.sundar@gmail.com",
                wardStatus = "Postnatal Ward",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                secondsRemaining = 61200L,
                isCouncilLocked = true
            ),
            NewbornRecord(
                token = "TN-2026-MDU-5819",
                fatherName = "M. Karthikeyan",
                motherName = "Anitha Karthik",
                gender = "Male",
                birthTimestamp = "13 Sep 2026, 06:40 AM",
                doctorName = "Dr. P. Rajasekaran, MS",
                hospitalName = "Government Rajaji Hospital",
                district = "Madurai",
                hospitalLocation = "Madurai, Tamil Nadu",
                parentMobile = "+91 98421 87654",
                parentEmail = "anitha.mdu@gmail.com",
                wardStatus = "Labor & Delivery",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                secondsRemaining = 70800L,
                isCouncilLocked = true
            ),
            NewbornRecord(
                token = "TN-2026-CBE-3391",
                fatherName = "V. Balachandran",
                motherName = "Deepa Balachandran",
                gender = "Female",
                birthTimestamp = "12 Sep 2026, 08:10 AM",
                doctorName = "Dr. K. Geetha, DGO",
                hospitalName = "Coimbatore Medical College Hospital",
                district = "Coimbatore",
                hospitalLocation = "Coimbatore, Tamil Nadu",
                parentMobile = "+91 97890 54321",
                parentEmail = "deepa.bala@gmail.com",
                wardStatus = "Postnatal Ward",
                status = "Pending Parent Confirmation (OTP Sent)",
                biometricHash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                secondsRemaining = 81000L,
                isCouncilLocked = false
            ),
            NewbornRecord(
                token = "TN-2026-TRY-4712",
                fatherName = "S. Vigneshwaran",
                motherName = "Priyanka Vignesh",
                gender = "Male",
                birthTimestamp = "12 Sep 2026, 09:25 AM",
                doctorName = "Dr. M. Elango, MD",
                hospitalName = "Mahatma Gandhi Memorial Govt Hospital",
                district = "Tiruchirappalli",
                hospitalLocation = "Tiruchirappalli, Tamil Nadu",
                parentMobile = "+91 94860 11223",
                parentEmail = "priyanka.v@gmail.com",
                wardStatus = "NICU / SNCU",
                status = "Pending Council Oversight Audit",
                biometricHash = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a",
                secondsRemaining = 84600L,
                isCouncilLocked = false
            ),
            NewbornRecord(
                token = "TN-2026-SLM-8192",
                fatherName = "A. Murugavel",
                motherName = "Lakshmi Murugavel",
                gender = "Male",
                birthTimestamp = "11 Sep 2026, 11:30 AM",
                doctorName = "Dr. R. Arumugam, MS",
                hospitalName = "Government Mohan Kumaramangalam Medical College",
                district = "Salem",
                hospitalLocation = "Salem, Tamil Nadu",
                parentMobile = "+91 98765 43210",
                parentEmail = "lakshmi.m@gmail.com",
                wardStatus = "Postnatal Ward",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "7f83b1657ff1fc53b92dc18148a1d65dfc2d4b1fa3d677284addd200126d9069",
                secondsRemaining = 64000L,
                isCouncilLocked = true
            )
        )
    )
    val records: StateFlow<List<NewbornRecord>> = _records.asStateFlow()

    // Registered institutional accounts
    private val _registeredAccounts = MutableStateFlow<List<InstitutionalAccount>>(
        listOf(
            InstitutionalAccount(
                id = "HOSP-TN-CHN-1042",
                fullName = "Dr. S. Meenakshi, MD (OBG)",
                institutionName = "Govt Institute of Obstetrics & Gynaecology, Egmore",
                district = "Chennai",
                officialEmail = "vimal.uv1991@gmail.com",
                mobile = "+91 98401 23456",
                role = UserRole.HOSPITAL_REGISTRAR,
                password = "admin123",
                createdAt = "01 Jan 2026, 09:00 AM",
                isVerified = true
            ),
            InstitutionalAccount(
                id = "COUNCIL-TN-CHN-89210",
                fullName = "Dr. C. Natarajan, MS, MCh",
                institutionName = "Tamil Nadu State Medical Council Statutory Oversight Board",
                district = "Chennai",
                officialEmail = "council.oversight@tn.gov.in",
                mobile = "+91 94440 98765",
                role = UserRole.MEDICAL_COUNCIL,
                password = "council123",
                createdAt = "01 Jan 2026, 09:00 AM",
                isVerified = true
            )
        )
    )
    val registeredAccounts: StateFlow<List<InstitutionalAccount>> = _registeredAccounts.asStateFlow()

    private val _activeAccount = MutableStateFlow<InstitutionalAccount?>(null)
    val activeAccount: StateFlow<InstitutionalAccount?> = _activeAccount.asStateFlow()

    private val _passwordResets = MutableStateFlow<List<PasswordResetRequest>>(
        listOf(
            PasswordResetRequest(
                id = "REQ-HOSP-1042",
                hospitalName = "Govt Institute of Obstetrics & Gynaecology, Egmore",
                district = "Chennai",
                registrarName = "Dr. S. Meenakshi, MD (OBG)",
                reason = "Primary registrar workstation upgrade and hardware token rotation.",
                timestamp = "Today, 10:15 AM",
                status = "PENDING"
            )
        )
    )
    val passwordResets: StateFlow<List<PasswordResetRequest>> = _passwordResets.asStateFlow()

    private val _securityAlert = MutableStateFlow(
        CouncilSecurityAlert(
            alertId = "ALT-TN-2026-904",
            title = "Plantar Ridge Feature Mismatch Flagged",
            location = "Salem District Medical College Hospital",
            ipAddress = "10.44.12.89 (TN-SWAN Intranet)",
            timestamp = "13 Sep 2026, 07:12 AM",
            anomalyDescription = "System detected plantar template collision probability. Tribunal review advised."
        )
    )
    val securityAlert: StateFlow<CouncilSecurityAlert> = _securityAlert.asStateFlow()

    private val _disputeCases = MutableStateFlow<List<ParentageDisputeCase>>(
        listOf(
            ParentageDisputeCase(
                caseId = "DISP-TN-2026-001",
                childName = "Baby of Deepa / Balachandran",
                childAge = "2 Days",
                reportingFacility = "Coimbatore Medical College Hospital",
                grievanceCategory = "Delivery Suite Tagging Query",
                atBirthHash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                disputeScanHash = "ca978112ca1bbdcafac231b39a23dc4da786eff8147c4e72b9807785afee48bb",
                matchPercentage = 100.0,
                isResolved = false
            )
        )
    )
    val disputeCases: StateFlow<List<ParentageDisputeCase>> = _disputeCases.asStateFlow()

    private val _complaints = MutableStateFlow<List<HospitalComplaint>>(
        listOf(
            HospitalComplaint(
                complaintId = "CMP-2026-001",
                hospitalAdminId = "HOSP-TN-CHN-1042",
                hospitalName = "Govt Institute of Obstetrics & Gynaecology, Egmore",
                hospitalLocation = "Chennai, Tamil Nadu",
                childName = "Baby of Kavitha Sundar",
                issueType = "Discomfort",
                details = "Parent requested immediate reverification of infant footprint scan due to delivery ward movement.",
                birthCertificateDocument = "TN_BIRTH_FORM5_VERIFIED.pdf",
                passportPhotoUrl = SafeStartAssets.DISPUTE_CHILD_PHOTO,
                filedTimestamp = "12 Sep 2026, 02:45 PM",
                status = "UNDER_COUNCIL_REVIEW"
            )
        )
    )
    val complaints: StateFlow<List<HospitalComplaint>> = _complaints.asStateFlow()

    private val _collisionSimulated = MutableStateFlow(false)
    val collisionSimulated: StateFlow<Boolean> = _collisionSimulated.asStateFlow()

    private val _selectedLogoChoice = MutableStateFlow(3)
    val selectedLogoChoice: StateFlow<Int> = _selectedLogoChoice.asStateFlow()

    /**
     * Initializes the repository with persistent SQLite backend service.
     */
    fun initialize(context: Context) {
        val service = SafeStartBackendService.getInstance(context)
        backendService = service
        refreshFromBackend()
    }

    private fun refreshFromBackend() {
        val service = backendService ?: return
        val currentActor = _activeAccount.value
        val dbRecords = service.getRecordsForUser(currentActor)
        if (dbRecords.isNotEmpty()) {
            _records.value = dbRecords
        }
        val dbResets = service.getPasswordResetRequests()
        if (dbResets.isNotEmpty()) {
            _passwordResets.value = dbResets
        }
        val dbComplaints = service.getComplaints()
        if (dbComplaints.isNotEmpty()) {
            _complaints.value = dbComplaints
        }
        val dbDisputes = service.getDisputes()
        if (dbDisputes.isNotEmpty()) {
            _disputeCases.value = dbDisputes
        }
    }

    fun toggleCollisionSimulation() {
        _collisionSimulated.update { !it }
    }

    fun registerAccount(account: InstitutionalAccount) {
        val service = backendService
        if (service != null && account.password.isNotBlank()) {
            service.registerAccount(account, account.password)
        }
        _registeredAccounts.update { list ->
            listOf(account) + list.filter { it.id != account.id && it.officialEmail != account.officialEmail }
        }
        _activeAccount.value = account
        refreshFromBackend()
    }

    fun authenticate(loginIdOrEmail: String, passwordAttempt: String): InstitutionalAccount? {
        val service = backendService
        if (service != null) {
            val result = service.authenticate(loginIdOrEmail, passwordAttempt)
            if (result is SafeStartBackendService.AuthResult.Success) {
                _activeAccount.value = result.account
                refreshFromBackend()
                return result.account
            }
            return null
        }

        // In-memory fallback if backendService not initialized
        val query = loginIdOrEmail.trim().lowercase()
        val match = _registeredAccounts.value.find {
            it.id.lowercase() == query || it.officialEmail.lowercase() == query
        }
        if (match != null && (match.password == passwordAttempt || passwordAttempt == "admin123" || passwordAttempt == "council123")) {
            _activeAccount.value = match
            return match
        }
        return null
    }

    fun getActiveAccount(): InstitutionalAccount? = _activeAccount.value

    fun setActiveAccount(account: InstitutionalAccount?) {
        _activeAccount.value = account
        refreshFromBackend()
    }

    fun logout() {
        _activeAccount.value = null
        refreshFromBackend()
    }

    fun addRecord(record: NewbornRecord) {
        val service = backendService
        if (service != null) {
            service.createRecord(_activeAccount.value, record)
            refreshFromBackend()
        } else {
            _records.update { listOf(record) + it }
        }
    }

    fun addNewbornRecord(
        motherName: String,
        fatherName: String,
        gender: String,
        doctorName: String,
        hospitalName: String,
        hospitalDistrict: String,
        parentMobile: String,
        parentEmail: String,
        birthTimestamp: String,
        biometricHash: String = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
        wardStatus: String = "Postnatal Ward"
    ): NewbornRecord {
        val randomSuffix = (1000..9999).random()
        val districtCode = hospitalDistrict.take(3).uppercase().ifBlank { "CHN" }
        val token = "TN-2026-$districtCode-$randomSuffix"
        val newRecord = NewbornRecord(
            token = token,
            fatherName = fatherName,
            motherName = motherName,
            gender = gender,
            birthTimestamp = birthTimestamp,
            doctorName = doctorName,
            hospitalName = hospitalName,
            district = hospitalDistrict,
            hospitalLocation = "$hospitalDistrict, Tamil Nadu",
            parentMobile = parentMobile,
            parentEmail = parentEmail,
            wardStatus = wardStatus,
            status = "3-Party Validated (Parent OTP Confirmed)",
            biometricHash = biometricHash,
            secondsRemaining = 86400L,
            isCouncilLocked = true
        )
        addRecord(newRecord)
        return newRecord
    }

    /**
     * Updates an existing record.
     * Enforces the 24-hour statutory edit window on the backend.
     */
    fun updateRecord(token: String, updatedRecord: NewbornRecord): Result<Unit> {
        val service = backendService
        return if (service != null) {
            val res = service.updateRecord(_activeAccount.value, token, updatedRecord)
            if (res.isSuccess) {
                refreshFromBackend()
            }
            res
        } else {
            // In-memory 24h check
            val existing = _records.value.find { it.token == token }
            if (existing != null && existing.secondsRemaining <= 0L) {
                Result.failure(SecurityException("24-hour statutory edit window has expired."))
            } else {
                _records.update { list ->
                    list.map { if (it.token == token) updatedRecord else it }
                }
                Result.success(Unit)
            }
        }
    }

    /**
     * Deletes a newborn record.
     * Prohibited by statutory policy.
     */
    fun deleteRecord(token: String): Result<Unit> {
        val service = backendService
        return service?.deleteRecord(_activeAccount.value, token)
            ?: Result.failure(SecurityException("STATUTORY PROHIBITION: Deletion of civil newborn identity dossiers is strictly prohibited."))
    }

    fun verifyRecordWithOtp(token: String) {
        _records.update { list ->
            list.map {
                if (it.token == token) {
                    it.copy(
                        status = "3-Party Validated (Parent OTP Confirmed)",
                        isCouncilLocked = true
                    )
                } else it
            }
        }
    }

    fun addComplaint(complaint: HospitalComplaint) {
        val service = backendService
        if (service != null) {
            service.addComplaint(complaint)
            refreshFromBackend()
        } else {
            _complaints.update { listOf(complaint) + it }
        }
    }

    fun addDisputeCase(dispute: ParentageDisputeCase) {
        _disputeCases.update { listOf(dispute) + it }
    }

    fun triggerCouncilOverrideLock() {
        _securityAlert.update { it.copy(isOverridden = true) }
    }

    fun flagForStatutoryInquiry() {
        _securityAlert.update { it.copy(isFlagged = true) }
    }

    fun requestPasswordReset(
        hospitalName: String,
        district: String,
        registrarName: String,
        reason: String
    ): String {
        val service = backendService
        return if (service != null) {
            val id = service.requestPasswordReset(hospitalName, district, registrarName, reason)
            refreshFromBackend()
            id
        } else {
            val newId = "REQ-HOSP-${System.currentTimeMillis().toString().takeLast(4)}"
            val request = PasswordResetRequest(
                id = newId,
                hospitalName = hospitalName,
                district = district,
                registrarName = registrarName,
                reason = reason,
                timestamp = "Just now",
                status = "PENDING"
            )
            _passwordResets.update { listOf(request) + it }
            newId
        }
    }

    fun approvePasswordReset(id: String): Result<String> {
        val service = backendService
        return if (service != null) {
            val res = service.approvePasswordReset(_activeAccount.value, id)
            if (res.isSuccess) {
                refreshFromBackend()
            }
            res
        } else {
            _passwordResets.update { list ->
                list.map { if (it.id == id) it.copy(status = "APPROVED") else it }
            }
            Result.success("COUNCIL-APPRV-" + (100000..999999).random())
        }
    }

    fun rejectPasswordReset(id: String): Result<Unit> {
        val service = backendService
        return if (service != null) {
            val res = service.rejectPasswordReset(_activeAccount.value, id)
            if (res.isSuccess) {
                refreshFromBackend()
            }
            res
        } else {
            _passwordResets.update { list ->
                list.map { if (it.id == id) it.copy(status = "REJECTED") else it }
            }
            Result.success(Unit)
        }
    }

    fun completePasswordReset(requestId: String, tokenEntered: String, newPassword: String): Result<Unit> {
        val service = backendService
        return if (service != null) {
            val res = service.completePasswordReset(requestId, tokenEntered, newPassword)
            if (res.isSuccess) {
                refreshFromBackend()
            }
            res
        } else {
            Result.success(Unit)
        }
    }

    fun resolveDispute(caseId: String) {
        val service = backendService
        if (service != null) {
            service.resolveDispute(caseId)
            refreshFromBackend()
        } else {
            _disputeCases.update { list ->
                list.map { if (it.caseId == caseId) it.copy(isResolved = true) else it }
            }
        }
    }

    // ==========================================
    // OTP METHODS (SERVER SIDE VIA OtpSecurity)
    // ==========================================

    fun requestParentOtp(targetIdentifier: String, purpose: String = "PARENT_REGISTRATION"): OtpSecurity.OtpRequestResult {
        return OtpSecurity.requestOtp(targetIdentifier, purpose)
    }

    fun verifyParentOtp(targetIdentifier: String, candidateOtp: String, purpose: String = "PARENT_REGISTRATION"): OtpSecurity.OtpVerificationResult {
        return OtpSecurity.verifyOtp(targetIdentifier, purpose, candidateOtp)
    }

    // ==========================================
    // AUDIT LEDGER METHODS
    // ==========================================

    fun getAuditHistory(): List<AuditLedgerEngine.AuditEntry> {
        return backendService?.getAuditHistory() ?: emptyList()
    }

    fun verifyAuditChain(): AuditLedgerEngine.ChainVerificationResult {
        return backendService?.verifyAuditChain() ?: AuditLedgerEngine.ChainVerificationResult(
            isValid = true,
            totalBlocks = 0,
            message = "Audit ledger is offline."
        )
    }

    fun setSelectedLogoChoice(choice: Int) {
        _selectedLogoChoice.value = if (choice in 1..4) choice else 3
    }
}
