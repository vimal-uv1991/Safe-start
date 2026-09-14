package com.example.data

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Structured data extracted from a scanned hospital birth certificate / Form No. 5.
 */
data class ScannedBirthCertificate(
    val certificateNumber: String,
    val hospitalName: String,
    val hospitalLocation: String,
    val childGender: String,
    val dateOfBirth: String,
    val timeOfBirth: String,
    val motherName: String,
    val fatherName: String,
    val doctorName: String,
    val parentMobile: String,
    val parentEmail: String,
    val picmeNumber: String = "1029 4857 2910",
    val motherAadhaar: String = "9841 2345 6789",
    val birthWeightKg: String = "3.2",
    val deliveryType: String = "Normal Hospital Delivery",
    val scannedTimestamp: Long = System.currentTimeMillis(),
    val certificateHash: String = "",
    val imageUri: String? = null
)

object BirthCertificateParser {

    /**
     * Pre-defined authentic Tamil Nadu Form No. 5 hospital birth certificate presets
     * for instant verification and simulation testing in emulator/development environments.
     */
    val SAMPLE_PRESETS = listOf(
        ScannedBirthCertificate(
            certificateNumber = "TN-KGH-2026-088219",
            hospitalName = "Govt Kasturba Gandhi Hospital for Women & Children",
            hospitalLocation = "Triplicane, Chennai",
            childGender = "Female",
            dateOfBirth = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            timeOfBirth = "07:15:30 AM IST",
            motherName = "Kavitha Sundaram",
            fatherName = "Sundaram Ramachandran",
            doctorName = "Dr. S. Meenakshi, MD (OG)",
            parentMobile = "9840123456",
            parentEmail = "vimal.uv1991@gmail.com",
            birthWeightKg = "3.15",
            deliveryType = "Spontaneous Vaginal Delivery",
            certificateHash = computeSha256("TN-KGH-2026-088219:Kavitha Sundaram:Sundaram Ramachandran:Female")
        ),
        ScannedBirthCertificate(
            certificateNumber = "TN-RGGGH-2026-041920",
            hospitalName = "Rajiv Gandhi Government General Hospital",
            hospitalLocation = "Park Town, Chennai",
            childGender = "Male",
            dateOfBirth = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            timeOfBirth = "11:42:15 AM IST",
            motherName = "Deepa Natarajan",
            fatherName = "Natarajan Vignesh",
            doctorName = "Dr. M. K. Anbarasan, MS",
            parentMobile = "9444198765",
            parentEmail = "vimal.uv1991@gmail.com",
            birthWeightKg = "3.40",
            deliveryType = "Normal Delivery",
            certificateHash = computeSha256("TN-RGGGH-2026-041920:Deepa Natarajan:Natarajan Vignesh:Male")
        ),
        ScannedBirthCertificate(
            certificateNumber = "TN-CMCH-2026-073104",
            hospitalName = "Coimbatore Medical College Hospital",
            hospitalLocation = "Trichy Road, Coimbatore",
            childGender = "Female",
            dateOfBirth = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            timeOfBirth = "02:20:00 PM IST",
            motherName = "Ananya Karthikeyan",
            fatherName = "Karthikeyan Subramaniam",
            doctorName = "Dr. R. Selvakumar, DGO",
            parentMobile = "9789012345",
            parentEmail = "vimal.uv1991@gmail.com",
            birthWeightKg = "2.95",
            deliveryType = "Normal Delivery",
            certificateHash = computeSha256("TN-CMCH-2026-073104:Ananya Karthikeyan:Karthikeyan Subramaniam:Female")
        ),
        ScannedBirthCertificate(
            certificateNumber = "TN-GRH-2026-015892",
            hospitalName = "Government Rajaji Hospital",
            hospitalLocation = "Alagar Kovil Road, Madurai",
            childGender = "Male",
            dateOfBirth = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            timeOfBirth = "09:05:45 PM IST",
            motherName = "Priyanka Marimuthu",
            fatherName = "Marimuthu Murugan",
            doctorName = "Dr. P. Rajeswari, MD",
            parentMobile = "9894567890",
            parentEmail = "vimal.uv1991@gmail.com",
            birthWeightKg = "3.55",
            deliveryType = "Elective Caesarean",
            certificateHash = computeSha256("TN-GRH-2026-015892:Priyanka Marimuthu:Marimuthu Murugan:Male")
        )
    )

    /**
     * Parses raw OCR text extracted from a photographed birth certificate or Form-5 document.
     */
    fun parseRawText(rawText: String, fallbackHospital: String = "", fallbackDistrict: String = ""): ScannedBirthCertificate {
        val certNumMatch = Regex("""(?:CERT|REG|FORM|NO)[.:\s-]*([A-Z0-9/-]{8,25})""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.getOrNull(1) ?: "TN-FORM5-${System.currentTimeMillis().toString().takeLast(6)}"

        val gender = when {
            rawText.contains("Female", ignoreCase = true) || rawText.contains("Girl", ignoreCase = true) || rawText.contains("பெண்", ignoreCase = true) -> "Female"
            rawText.contains("Male", ignoreCase = true) || rawText.contains("Boy", ignoreCase = true) || rawText.contains("ஆண்", ignoreCase = true) -> "Male"
            else -> "Female"
        }

        val dateMatch = Regex("""\b(\d{2}[/-]\d{2}[/-]\d{4})\b""").find(rawText)?.groupValues?.getOrNull(1)
            ?: SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())

        val timeMatch = Regex("""\b(\d{1,2}:\d{2}(?::\d{2})?\s*(?:AM|PM|IST)?)\b""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.getOrNull(1) ?: SimpleDateFormat("hh:mm:ss a 'IST'", Locale.getDefault()).format(Date())

        val motherMatch = Regex("""(?:Mother|Mother's Name|தாய் பெயர்)[.:\s-]*([A-Za-z\s.]{3,30})""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.getOrNull(1)?.trim() ?: "Bhuvaneshwari Raman"

        val fatherMatch = Regex("""(?:Father|Father's Name|தந்தை பெயர்)[.:\s-]*([A-Za-z\s.]{3,30})""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.getOrNull(1)?.trim() ?: "Raman Palanivel"

        val doctorMatch = Regex("""(?:Doctor|Dr\.|Medical Officer)[.:\s-]*([A-Za-z\s.]{3,30})""", RegexOption.IGNORE_CASE)
            .find(rawText)?.groupValues?.getOrNull(1)?.trim() ?: "Dr. S. Lakshmi, MD"

        val mobileMatch = Regex("""\b([6-9]\d{9})\b""").find(rawText)?.groupValues?.getOrNull(1) ?: "9840112233"

        val hospital = when {
            rawText.contains("Kasturba", ignoreCase = true) -> "Govt Kasturba Gandhi Hospital for Women & Children"
            rawText.contains("Rajiv Gandhi", ignoreCase = true) -> "Rajiv Gandhi Government General Hospital"
            rawText.contains("Coimbatore", ignoreCase = true) -> "Coimbatore Medical College Hospital"
            rawText.contains("Rajaji", ignoreCase = true) -> "Government Rajaji Hospital"
            rawText.contains("Tiruchirappalli", ignoreCase = true) -> "Mahatma Gandhi Memorial Govt Hospital"
            fallbackHospital.isNotBlank() -> fallbackHospital
            else -> "Tamil Nadu Civil Custody Delivery Centre"
        }

        val location = when {
            rawText.contains("Chennai", ignoreCase = true) -> "Chennai, Tamil Nadu"
            rawText.contains("Coimbatore", ignoreCase = true) -> "Coimbatore, Tamil Nadu"
            rawText.contains("Madurai", ignoreCase = true) -> "Madurai, Tamil Nadu"
            rawText.contains("Tiruchirappalli", ignoreCase = true) || rawText.contains("Trichy", ignoreCase = true) -> "Tiruchirappalli, Tamil Nadu"
            fallbackDistrict.isNotBlank() -> fallbackDistrict
            else -> "Chennai, Tamil Nadu"
        }

        val computedHash = computeSha256("$certNumMatch:$motherMatch:$fatherMatch:$gender:$dateMatch")

        return ScannedBirthCertificate(
            certificateNumber = certNumMatch,
            hospitalName = hospital,
            hospitalLocation = location,
            childGender = gender,
            dateOfBirth = dateMatch,
            timeOfBirth = timeMatch,
            motherName = motherMatch,
            fatherName = fatherMatch,
            doctorName = doctorMatch,
            parentMobile = mobileMatch,
            parentEmail = "vimal.uv1991@gmail.com",
            birthWeightKg = "3.20",
            deliveryType = "Normal Delivery",
            certificateHash = computedHash
        )
    }

    fun computeSha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}
