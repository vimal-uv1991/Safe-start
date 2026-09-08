package com.example.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object SafeStartRepository {

    private val initialRecords = listOf(
        NewbornRecord(
            token = "TN-2024-MDU-8831",
            fatherName = "Murugan K.",
            motherName = "Lakshmi M.",
            gender = "Female",
            birthTimestamp = "14 Oct 2024, 04:18:22 IST",
            doctorName = "Dr. R. Shanmugam, MD, DCH",
            hospitalName = "Government Rajaji Hospital, Madurai",
            district = "Madurai",
            parentMobile = "+91 98401 92821",
            status = "3-Party Validated",
            biometricHash = "9d8e27a41f802cc771a3962bcf0a84e902b1154",
            secondsRemaining = 85694L,
            isCouncilLocked = false
        ),
        NewbornRecord(
            token = "TN-2024-MDU-8830",
            fatherName = "Vignesh S.",
            motherName = "Priyanka R.",
            gender = "Male",
            birthTimestamp = "14 Oct 2024, 01:02:15 IST",
            doctorName = "Dr. S. Kanimozhi, MBBS, MD",
            hospitalName = "Government Rajaji Hospital, Madurai",
            district = "Madurai",
            parentMobile = "+91 94432 11094",
            status = "3-Party Validated",
            biometricHash = "4b1f80cc7a19280a396e902b11548e27f077421",
            secondsRemaining = 73860L,
            isCouncilLocked = false
        ),
        NewbornRecord(
            token = "TN-2024-MDU-8822",
            fatherName = "Anand C.",
            motherName = "Selvi V.",
            gender = "Male",
            birthTimestamp = "13 Oct 2024, 11:24:00 IST",
            doctorName = "Dr. M. Jayakumar, MD",
            hospitalName = "Government Rajaji Hospital, Madurai",
            district = "Madurai",
            parentMobile = "+91 97890 44321",
            status = "Immutable Ledger",
            biometricHash = "8e27a41f802cc771a3962bcf0a84e902b11549d",
            secondsRemaining = 0L,
            isCouncilLocked = true
        ),
        NewbornRecord(
            token = "TN-2024-CHN-1049",
            fatherName = "Karthik N.",
            motherName = "Deepa S.",
            gender = "Female",
            birthTimestamp = "14 Oct 2024, 06:12:44 IST",
            doctorName = "Dr. V. Rajesh, MD",
            hospitalName = "Rajiv Gandhi Govt General Hospital",
            district = "Chennai",
            parentMobile = "+91 98410 55678",
            status = "3-Party Validated",
            biometricHash = "71a3962bcf0a84e902b11549d8e27a41f802ccb",
            secondsRemaining = 86200L,
            isCouncilLocked = false
        ),
        NewbornRecord(
            token = "TN-2024-CBE-4921",
            fatherName = "Suresh P.",
            motherName = "Ananya M.",
            gender = "Male",
            birthTimestamp = "14 Oct 2024, 05:40:19 IST",
            doctorName = "Dr. P. Revathi, MBBS, DGO",
            hospitalName = "Coimbatore Medical College Hospital",
            district = "Coimbatore",
            parentMobile = "+91 99940 12345",
            status = "3-Party Validated",
            biometricHash = "a41f802cc771a3962bcf0a84e902b11549d8e27",
            secondsRemaining = 84000L,
            isCouncilLocked = false
        ),
        NewbornRecord(
            token = "TN-2024-SLM-3302",
            fatherName = "Saravanan R.",
            motherName = "Meena T.",
            gender = "Female",
            birthTimestamp = "13 Oct 2024, 22:15:30 IST",
            doctorName = "Dr. T. Balaji, MD (Pediatrics)",
            hospitalName = "Govt Mohan Kumaramangalam MCH",
            district = "Salem",
            parentMobile = "+91 94421 88765",
            status = "Immutable Ledger",
            biometricHash = "02b11549d8e27a41f802cc771a3962bcf0a84e9",
            secondsRemaining = 0L,
            isCouncilLocked = true
        ),
        NewbornRecord(
            token = "TN-2024-TRY-7110",
            fatherName = "Ramesh G.",
            motherName = "Kavitha D.",
            gender = "Female",
            birthTimestamp = "14 Oct 2024, 02:41:09 IST",
            doctorName = "Dr. K. Murugesan, MD",
            hospitalName = "Mahatma Gandhi Memorial GH",
            district = "Tiruchirappalli",
            parentMobile = "+91 98424 33112",
            status = "3-Party Validated",
            biometricHash = "bcf0a84e902b11549d8e27a41f802cc771a3962",
            secondsRemaining = 75300L,
            isCouncilLocked = false
        )
    )

    private val _records = MutableStateFlow(initialRecords)
    val records: StateFlow<List<NewbornRecord>> = _records.asStateFlow()

    private val _securityAlert = MutableStateFlow(
        CouncilSecurityAlert(
            alertId = "#TN-SEC-2025-0814",
            title = "Unusual Hospital Login Detected",
            location = "Tiruchirappalli GH Node",
            ipAddress = "10.241.88.19 (Unlisted Static ASN)",
            timestamp = "Today at 02:41 AM IST (Off-Hours)",
            anomalyDescription = "Off-hours bulk dossier export attempt with mismatching terminal cryptographic signature."
        )
    )
    val securityAlert: StateFlow<CouncilSecurityAlert> = _securityAlert.asStateFlow()

    private val _passwordResets = MutableStateFlow(
        listOf(
            PasswordResetRequest(
                id = "REQ-MCH-901",
                hospitalName = "Tirunelveli Medical College Hospital",
                district = "Tirunelveli",
                registrarName = "Dr. K. Meenakshi Sundaram",
                reason = "Registrar HSM security token rotation following biometric terminal re-calibration.",
                timestamp = "14 mins ago"
            ),
            PasswordResetRequest(
                id = "REQ-GH-412",
                hospitalName = "Erode District Headquarters Hospital",
                district = "Erode",
                registrarName = "Dr. S. Gomathi",
                reason = "Dual-factor credential expiry during off-shift emergency delivery handover.",
                timestamp = "48 mins ago"
            )
        )
    )
    val passwordResets: StateFlow<List<PasswordResetRequest>> = _passwordResets.asStateFlow()

    private val _disputeCases = MutableStateFlow(
        listOf(
            ParentageDisputeCase(
                caseId = "DSP-2025-CBE-041",
                childName = "R. Tarun",
                childAge = "9 Years",
                reportingFacility = "Coimbatore Medical College Hospital (GH)",
                grievanceCategory = "Disputed Child Swapping Claim (Civic Tribunal #402)",
                atBirthHash = "SHA256: 9d8e27a41f802cc771a3962bcf0a84e902b1154a",
                disputeScanHash = "SHA256: 9d8e27a41f802cc771a3962bcf0a84e902b1154a",
                matchPercentage = 99.94,
                isResolved = false
            )
        )
    )
    val disputeCases: StateFlow<List<ParentageDisputeCase>> = _disputeCases.asStateFlow()

    private val _complaints = MutableStateFlow(
        listOf(
            HospitalComplaint(
                complaintId = "CMP-HOSP-2025-014",
                hospitalAdminId = "HOSP-TN-MDU-74291",
                hospitalName = "Government Rajaji Hospital",
                hospitalLocation = "Madurai, Tamil Nadu",
                childName = "Baby of Selvi (Token #8822)",
                issueType = "Discomfort",
                details = "Parental dispute reported regarding identification wristband tag discrepancy during nursery observation. Requesting forensic biometric reverification.",
                birthCertificateDocument = "GRH_MADURAI_REG_8822.pdf",
                filedTimestamp = "Today, 09:30 AM IST",
                status = "TRANSMITTED_TO_GOV_COUNCIL"
            ),
            HospitalComplaint(
                complaintId = "CMP-HOSP-2025-009",
                hospitalAdminId = "HOSP-TN-CHN-31902",
                hospitalName = "Rajiv Gandhi Govt General Hospital",
                hospitalLocation = "Chennai, Tamil Nadu",
                childName = "Baby of Deepa (Token #1049)",
                issueType = "Partiality",
                details = "Extended family query on maternal ward bed allocation and infant biometric custody check.",
                birthCertificateDocument = "RGGH_CHENNAI_1049.pdf",
                filedTimestamp = "Yesterday, 04:15 PM IST",
                status = "PENDING_COUNCIL_REVIEW"
            )
        )
    )
    val complaints: StateFlow<List<HospitalComplaint>> = _complaints.asStateFlow()

    private val _collisionSimulated = MutableStateFlow(false)
    val collisionSimulated: StateFlow<Boolean> = _collisionSimulated.asStateFlow()

    fun toggleCollisionSimulation() {
        _collisionSimulated.update { !it }
    }

    fun addRecord(record: NewbornRecord) {
        _records.update { listOf(record) + it }
    }

    fun addComplaint(complaint: HospitalComplaint) {
        _complaints.update { listOf(complaint) + it }
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
}
