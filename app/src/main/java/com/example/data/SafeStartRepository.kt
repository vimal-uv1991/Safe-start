package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SafeStartRepository {

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
            ),
            NewbornRecord(
                token = "TN-2026-TVL-9041",
                fatherName = "P. Selvakumar",
                motherName = "Revathi Selvakumar",
                gender = "Female",
                birthTimestamp = "11 Sep 2026, 03:45 PM",
                doctorName = "Dr. N. Balamurugan, MD",
                hospitalName = "Tirunelveli Medical College Hospital",
                district = "Tirunelveli",
                hospitalLocation = "Tirunelveli, Tamil Nadu",
                parentMobile = "+91 94432 87654",
                parentEmail = "revathi.s@gmail.com",
                wardStatus = "Labor & Delivery",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "5e884898da28047151d0e56f8dc6292773603d0d6aabbdd62a11ef721d1542d8",
                secondsRemaining = 72000L,
                isCouncilLocked = true
            ),
            NewbornRecord(
                token = "TN-2026-VEL-2104",
                fatherName = "K. Saravanan",
                motherName = "Malathi Saravanan",
                gender = "Male",
                birthTimestamp = "10 Sep 2026, 07:15 AM",
                doctorName = "Dr. T. Srinivasan, DGO",
                hospitalName = "Adukkamparai Government Hospital",
                district = "Vellore",
                hospitalLocation = "Vellore, Tamil Nadu",
                parentMobile = "+91 99940 11223",
                parentEmail = "malathi.s@gmail.com",
                wardStatus = "Special Care Nursery",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "2c26b46b68ffc68ff99b453c1d30413413422d706483bfa0f98a5e886266e7ae",
                secondsRemaining = 80000L,
                isCouncilLocked = true
            ),
            NewbornRecord(
                token = "TN-2026-KCH-3918",
                fatherName = "G. Dhandapani",
                motherName = "Sangeetha Dhandapani",
                gender = "Female",
                birthTimestamp = "09 Sep 2026, 10:20 AM",
                doctorName = "Dr. H. Radhika, MD",
                hospitalName = "Kanchipuram District Headquarters Hospital",
                district = "Kanchipuram",
                hospitalLocation = "Kanchipuram, Tamil Nadu",
                parentMobile = "+91 98410 55667",
                parentEmail = "sangeetha.d@gmail.com",
                wardStatus = "Postnatal Ward",
                status = "3-Party Validated (Parent OTP Confirmed)",
                biometricHash = "4b227777d4dd1fc61c6f884f48641d02b4d121d3fd328cb08b5531fcacdabf8a",
                secondsRemaining = 54000L,
                isCouncilLocked = true
            )
        )
    )
    val records: StateFlow<List<NewbornRecord>> = _records.asStateFlow()

    // Real registered institutional accounts
    private val _registeredAccounts = MutableStateFlow<List<InstitutionalAccount>>(
        listOf(
            InstitutionalAccount(
                id = "HOSP-TN-CHN-1042",
                fullName = "Dr. S. Meenakshi, MD (OBG)",
                institutionName = "Govt Institute of Obstetrics & Gynaecology, Egmore",
                district = "Chennai",
                officialEmail = "vimal.uv1991@gmail.com", // User's email for active testing
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
                officialEmail = "council.audit@safestart.tn.gov.in",
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

    private val _securityAlert = MutableStateFlow(
        CouncilSecurityAlert(
            alertId = "#TN-SEC-SYS-001",
            title = "Unusual Login Anomaly Monitor",
            location = "State Central HSM Node",
            ipAddress = "10.0.0.1",
            timestamp = "System Active",
            anomalyDescription = "System idle. Monitoring active for unauthenticated nodal egress.",
            isOverridden = true
        )
    )
    val securityAlert: StateFlow<CouncilSecurityAlert> = _securityAlert.asStateFlow()

    private val _passwordResets = MutableStateFlow<List<PasswordResetRequest>>(
        listOf(
            PasswordResetRequest(
                id = "REQ-HOSP-7429",
                hospitalName = "Thanjavur Medical College Hospital",
                district = "Thanjavur",
                registrarName = "Dr. R. Muthukumar, MD",
                reason = "Hardware Security Key rotation for nodal biometric terminal station",
                timestamp = "12 Sep 2026, 11:30 AM",
                status = "PENDING"
            )
        )
    )
    val passwordResets: StateFlow<List<PasswordResetRequest>> = _passwordResets.asStateFlow()

    private val _disputeCases = MutableStateFlow<List<ParentageDisputeCase>>(
        listOf(
            ParentageDisputeCase(
                caseId = "CASE-2026-MDU-042",
                childName = "Baby of Anitha & Karthikeyan",
                childAge = "3 weeks",
                childPhotoUrl = SafeStartAssets.DISPUTE_CHILD_PHOTO,
                reportingFacility = "Government Rajaji Hospital, Madurai",
                grievanceCategory = "Maternity Ward Tag Identification Inquiry",
                atBirthHash = "0x8f3c4e92a17b5d6e",
                disputeScanHash = "0x8f3c4e92a17b5d6e",
                matchPercentage = 99.98,
                isResolved = false
            )
        )
    )
    val disputeCases: StateFlow<List<ParentageDisputeCase>> = _disputeCases.asStateFlow()

    private val _complaints = MutableStateFlow<List<HospitalComplaint>>(
        listOf(
            HospitalComplaint(
                complaintId = "CMP-2026-CHN-881",
                hospitalAdminId = "HOSP-TN-CHN-1042",
                hospitalName = "Govt Institute of Obstetrics & Gynaecology, Egmore",
                hospitalLocation = "Chennai",
                childName = "Baby of Kavitha Sundaram",
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

    fun toggleCollisionSimulation() {
        _collisionSimulated.update { !it }
    }

    fun registerAccount(account: InstitutionalAccount) {
        _registeredAccounts.update { list ->
            listOf(account) + list.filter { it.id != account.id && it.officialEmail != account.officialEmail }
        }
        _activeAccount.value = account
    }

    fun authenticate(loginIdOrEmail: String, passwordAttempt: String): InstitutionalAccount? {
        val query = loginIdOrEmail.trim().lowercase()
        val match = _registeredAccounts.value.find {
            it.id.lowercase() == query || it.officialEmail.lowercase() == query
        }
        if (match != null && match.password == passwordAttempt) {
            _activeAccount.value = match
            return match
        }
        return null
    }

    fun getActiveAccount(): InstitutionalAccount? = _activeAccount.value

    fun setActiveAccount(account: InstitutionalAccount?) {
        _activeAccount.value = account
    }

    fun logout() {
        _activeAccount.value = null
    }

    fun addRecord(record: NewbornRecord) {
        _records.update { listOf(record) + it }
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
        _complaints.update { listOf(complaint) + it }
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
        return newId
    }

    fun approvePasswordReset(id: String) {
        _passwordResets.update { list ->
            list.map { if (it.id == id) it.copy(status = "APPROVED") else it }
        }
    }

    fun rejectPasswordReset(id: String) {
        _passwordResets.update { list ->
            list.map { if (it.id == id) it.copy(status = "REJECTED") else it }
        }
    }

    fun resolveDispute(caseId: String) {
        _disputeCases.update { list ->
            list.map { if (it.caseId == caseId) it.copy(isResolved = true) else it }
        }
    }

    private val _selectedLogoChoice = MutableStateFlow(3)
    val selectedLogoChoice: StateFlow<Int> = _selectedLogoChoice.asStateFlow()

    fun setSelectedLogoChoice(choice: Int) {
        _selectedLogoChoice.value = if (choice in 1..4) choice else 3
    }
}
