package com.example.data

object SafeStartAssets {
    const val EMBLEM_URL = "https://lh3.googleusercontent.com/aida/AEtjO1VIVzdNDFQhuA4ChpYVsZNe4b2bxfcj6aPaki3WyiqsM9g4noo1bNePZnWZH2_IXk6FpjaEEcAvJDtvuh4AVlFT5wBPUIj4lUqli9RO5MAg51nwdTY2l5Mr3CeWadt0oibNsnr-V9UsJX9dx8hInGuQuRNOAaTf4x5VksoqYJ3qYoqIgrEG8I5NWItAYD-qU6R0R1Mtjl7gjo16FqG37Rlys0CQIBINXJeJg6H32OBrcfJHDT7Gzx0"
    const val FATHER_FOOTPRINT = "https://lh3.googleusercontent.com/aida-public/AB6AXuA0KVRBY6oIn8aEbU5JyCIeBm7OqevJDLwMBVlLXJwpAp-pSwnXzQ_KqDiouIjmu60B9PAZefCrb2t2NzTS--JQvQCWGqzUN-L1wUmuiPpnlZZWVDtrwBMwmJwHTYdMs4q42LbXcGIIgH9ojgdcTmgaeDYNZ-FBHdLzYwQKATrBdprsejXoGkAn20KsrkMH6stIrsyEIdBFGwbS6N3dwkD9QKaJK5qiOp7PGkH-MkoHdOpvh5h82Q"
    const val MOTHER_FOOTPRINT = "https://lh3.googleusercontent.com/aida-public/AB6AXuCVh9HWIHfhTvUbeaO8EFulSPbFzosnOcWN8Lkwi8BCMSJwj13Xfell5QdpdkB0eF_TEb5DXPItyBZlRHQ0BLJbDTP_deuFiOWdFzZE7LTFsCYeqAcwHg8wyYf52f4Xi6GDi9ZInsnkKbNNTpyBSY79dAQbFsIA-Ezpi2ICs2DSc8-UblgxUY4BDmjnZT3tUYI-A1NRnhb2MGbIg76BpCT3atIHgk7U3eh11a-TNYsIuRFFCgqW2w"
    const val NEWBORN_FOOTPRINT = "https://lh3.googleusercontent.com/aida-public/AB6AXuB5HMwMSa92P1_xfxHfHBMAK7mT6bG94CexH6AQFY4zwI3ARrSFXdTF_5nq7Lsr8xS_cMi37YNZjC5nDAo8Uz7xGHzpJWDP6l5nadhq3_YJO94SofA9bRvHVoheZqrXCI_DK_a3ubwFmmfqJ9d49gTEm2-xey87aLjZKS1_731ZQiO7R8GFRRp9BFJcZx6V2FaEaUHCAAsKzJbtKpZiSIBB5u8mHchiLvYCapzP3x0qj2XbGvmE5Q"
    const val DISPUTE_CHILD_PHOTO = "https://lh3.googleusercontent.com/aida-public/AB6AXuAfaMiA4L42q7P6F3YElTLaPRB2emXiFhs2EYnnZyqEHsWtcFxwna0RENXOSGhCzOhz3qude11O5aSmycuUek1zgcndqS_FWIs4lf7_ZCbiR9s18JRu2J1DaHZmYkBFHHjaMLDVkYC_LElR4g96IEnNjFx9Vjf6MmPDJYwbXmIVn7Je9Zg0onYQoJhnNsRPjBazX-uNkgrmdwTwF2s7H8zEkmW-nDYPP4_h5zyvadxQOWvfljN6gw"
}

enum class UserRole(val label: String, val tamilLabel: String, val subtitle: String) {
    HOSPITAL_REGISTRAR(
        label = "Hospital Admin / Registrar",
        tamilLabel = "மருத்துவமனை பதிவாளர் தளம்",
        subtitle = "Register births • Upload newborn footprints • 24h amend authority"
    ),
    MEDICAL_COUNCIL(
        label = "Government Medical Council",
        tamilLabel = "தமிழ்நாடு மருத்துவக் கவுன்சில் ஆய்வு",
        subtitle = "Read-only statewide registry • Collision dispute sign-off • Tamper audits"
    )
}

enum class AppWing(val title: String, val subtitle: String) {
    AUTH("Authentication Portal", "Secured Gateway"),
    HOSPITAL_REGISTRY("Hospital Registry", "Local Civil Custody"),
    COUNCIL_OVERSIGHT("Council Oversight", "Statewide Directorate"),
    DISPUTE_VERIFICATION("Dispute Verification", "Parentage Tribunal"),
    AUDIT_LEDGER("Audit Ledger", "Immutable SHA-256 Ledger")
}

enum class AuthTab(val title: String) {
    SIGN_IN("Secure Portal Sign In"),
    CREATE_ACCOUNT("Create First-Time Account"),
    COUNCIL_RESET("Council Reset Workflow")
}

data class NewbornRecord(
    val token: String,
    val fatherName: String,
    val motherName: String,
    val gender: String,
    val birthTimestamp: String,
    val doctorName: String,
    val hospitalName: String,
    val district: String,
    val hospitalLocation: String = district,
    val parentMobile: String,
    val status: String,
    val biometricHash: String,
    val secondsRemaining: Long = 76442L,
    val isCouncilLocked: Boolean = false,
    val fatherScanUrl: String = SafeStartAssets.FATHER_FOOTPRINT,
    val motherScanUrl: String = SafeStartAssets.MOTHER_FOOTPRINT,
    val childScanUrl: String = SafeStartAssets.NEWBORN_FOOTPRINT,
    val retentionYearsLeft: Int = 10,
    val autoPurgeDate: String = "14 Oct 2034"
) {
    val maskedMobile: String
        get() {
            val clean = parentMobile.replace("\\s+".toRegex(), "").replace("+91", "")
            return if (clean.length >= 10) {
                val start = clean.take(2)
                val end = clean.takeLast(2)
                "+91 $start" + "X".repeat(6) + end
            } else {
                "+91 98XXXXXX21"
            }
        }
}

data class HospitalComplaint(
    val complaintId: String,
    val hospitalAdminId: String,
    val hospitalName: String,
    val hospitalLocation: String,
    val childName: String,
    val issueType: String, // "Discomfort", "Partiality", "Others"
    val details: String,
    val birthCertificateDocument: String = "TN_BIRTH_CERT_APPROVED.pdf",
    val passportPhotoUrl: String = SafeStartAssets.DISPUTE_CHILD_PHOTO,
    val filedTimestamp: String,
    val status: String = "UNDER_COUNCIL_REVIEW"
)

data class CouncilSecurityAlert(
    val alertId: String,
    val title: String,
    val location: String,
    val ipAddress: String,
    val timestamp: String,
    val anomalyDescription: String,
    var isOverridden: Boolean = false,
    var isFlagged: Boolean = false
)

data class PasswordResetRequest(
    val id: String,
    val hospitalName: String,
    val district: String,
    val registrarName: String,
    val reason: String,
    val timestamp: String,
    var status: String = "PENDING" // "PENDING", "APPROVED", "REJECTED"
)

data class ParentageDisputeCase(
    val caseId: String,
    val childName: String,
    val childAge: String,
    val childPhotoUrl: String = SafeStartAssets.DISPUTE_CHILD_PHOTO,
    val reportingFacility: String,
    val grievanceCategory: String,
    val atBirthHash: String,
    val disputeScanHash: String,
    val matchPercentage: Double = 99.94,
    val isResolved: Boolean = false
)
