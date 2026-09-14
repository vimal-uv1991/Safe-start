package com.example.data

import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Real-time statutory data validator for Tamil Nadu Government Newborn & Maternal IDs.
 * Strictly verifies standard formats using regular expressions:
 * - TN DPH PICME / RCH ID (12 Digits)
 * - UIDAI Aadhaar Number (12 Digits, cannot start with 0 or 1)
 * - TN Civil Registration System (CRS) Form-5 / Birth Certificate Registration Number
 * - Parent Mobile Number (Indian / TN Standard, 10 digits starting with 6-9)
 * - Birth Date (DD/MM/YYYY)
 * - Birth Time (IST Timestamp)
 * - Parent Intimation Email
 * - Doctor & Parent Names
 */
data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val helperText: String? = null,
    val formattedValue: String? = null
) {
    val isPending: Boolean get() = errorMessage == null && !isValid
}

object TamilNaduIdValidator {

    // =========================================================================
    // REGEX DEFINITIONS (TAMIL NADU & INDIAN STATUTORY SPECIFICATIONS)
    // =========================================================================

    // 1. TN PICME / RCH ID: Exactly 12 numerical digits
    // Directorate of Public Health & Preventive Medicine, Tamil Nadu
    private val PICME_CLEAN_REGEX = Regex("""^\d{12}$""")

    // 2. UIDAI Aadhaar UID: 12 digits, cannot start with 0 or 1, cannot be all identical digits
    private val AADHAAR_CLEAN_REGEX = Regex("""^[2-9]\d{11}$""")

    // 3. Tamil Nadu Civil Registration System (Form-5) Registration Numbers:
    // Format A: B-YYYY: DD-MMMMM-NNNNNN (e.g. B-2026: 02-00892-000123)
    // Format B: B/YYYY/MM/DDD/NNNNNN or YYYY/MM/DDDDD/NNNNNN (e.g. 2026/02/00892/000123)
    // Format C: TN-[HOSP_CODE]-YYYY-[TOKEN] (e.g. TN-KGH-2026-088219 or TN-REG-2026-90812)
    // Format D: TN/YYYY/NNNNNN (e.g. TN/2026/088219)
    // Format E: FORM5-TN-YYYY-NNNNNN
    private val TN_CRS_FORM5_REGEX = Regex(
        """^(B-\d{4}:\s?\d{2}-\d{4,5}-\d{4,6}|(B/)?\d{4}/\d{1,2}/\d{1,5}/\d{4,6}|TN-[A-Z]{2,6}-\d{4}-[A-Z0-9]{4,8}|TN/\d{4}/\d{4,8}|FORM5-TN-\d{4}-\d{4,8})$""",
        RegexOption.IGNORE_CASE
    )

    // 4. Indian Mobile Number (10 digits starting with 6, 7, 8, 9, optional +91 or 0 prefix)
    private val INDIAN_MOBILE_CLEAN_REGEX = Regex("""^[6-9]\d{9}$""")

    // 5. Parent Email (RFC 5322 compliant standard)
    private val EMAIL_REGEX = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")

    // 6. Date of Birth (DD/MM/YYYY)
    private val DATE_REGEX = Regex("""^(0[1-9]|[12]\d|3[01])/(0[1-9]|1[012])/(19|20)\d{2}$""")

    // 7. Time of Birth (12h or 24h with optional IST indicator)
    private val TIME_REGEX = Regex(
        """^((0?[1-9]|1[0-2]):[0-5]\d(:[0-5]\d)?\s?(AM|PM|am|pm)|([01]?\d|2[0-3]):[0-5]\d(:[0-5]\d)?)\s?(IST|ist)?$"""
    )

    // 8. Person Names (minimum 3 characters, alphabetic, spaces, dots, apostrophes, hyphens)
    private val NAME_REGEX = Regex("""^[A-Za-z][A-Za-z\s.'\-]{2,50}$""")

    // =========================================================================
    // VALIDATION METHODS WITH DETAILED REAL-TIME FEEDBACK
    // =========================================================================

    /**
     * Validates Tamil Nadu PICME / RCH ID (Pregnancy & Infant Cohort Monitoring and Evaluation).
     */
    fun validatePicme(raw: String): ValidationResult {
        val trimmed = raw.trim()
        val clean = trimmed.replace(Regex("""[\s\-]"""), "")

        if (clean.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Statutory 12-digit TN PICME number issued by DPH (e.g. 1029 4857 2910)"
            )
        }

        if (!clean.all { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Only numerical digits are allowed in PICME ID"
            )
        }

        if (clean.length < 12) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Incomplete: Entered ${clean.length} of 12 digits (PICME format: 12 numerical digits)"
            )
        }

        if (clean.length > 12) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid: Exceeds 12 digits (${clean.length} digits entered)"
            )
        }

        if (PICME_CLEAN_REGEX.matches(clean)) {
            val formatted = clean.chunked(4).joinToString(" ")
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid TN DPH PICME Number (12 Digits Verified)",
                formattedValue = formatted
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid PICME format. Must be 12 numerical digits."
        )
    }

    /**
     * Validates Mother's / Father's Aadhaar UID (UIDAI Standard).
     */
    fun validateAadhaar(raw: String): ValidationResult {
        val trimmed = raw.trim()
        val clean = trimmed.replace(Regex("""[\s\-]"""), "")

        if (clean.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "12-digit UIDAI Aadhaar number (cannot start with 0 or 1, e.g. 9841 2345 6789)"
            )
        }

        if (!clean.all { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Only digits are allowed in Aadhaar UID"
            )
        }

        if (clean.startsWith("0") || clean.startsWith("1")) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid UIDAI format: Aadhaar number cannot begin with 0 or 1"
            )
        }

        if (clean.length < 12) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Incomplete: Entered ${clean.length} of 12 digits"
            )
        }

        if (clean.length > 12) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid: Exceeds 12 digits (${clean.length} digits entered)"
            )
        }

        if (clean.length == 12 && clean.toSet().size == 1) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid Aadhaar: Number cannot have 12 identical digits"
            )
        }

        if (AADHAAR_CLEAN_REGEX.matches(clean)) {
            val formatted = clean.chunked(4).joinToString(" ")
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid UIDAI Aadhaar Number Format",
                formattedValue = formatted
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid Aadhaar number format"
        )
    }

    /**
     * Validates Tamil Nadu Civil Registration System (Form-5) Birth Certificate Number.
     */
    fun validateCrsForm5(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "TN Civil Registration Form 5 number (e.g. B-2026: 02-00892-000123 or TN-KGH-2026-088219)"
            )
        }

        if (TN_CRS_FORM5_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid TN Civil Registration (Form 5) Format",
                formattedValue = trimmed.uppercase()
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid TN Form 5 format. Expected: B-YYYY: DD-MMMMM-NNNNNN or TN-HOSP-YYYY-XXXXXX"
        )
    }

    /**
     * Validates Parent Mobile Number with Indian Telecom Standard.
     */
    fun validateMobile(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "10-digit Indian mobile starting with 6-9, optional +91 (e.g. +91 98401 23456)"
            )
        }

        // Clean out spaces, dashes, +91 or leading 0
        var clean = trimmed.replace(Regex("""[\s\-]"""), "")
        if (clean.startsWith("+91")) {
            clean = clean.removePrefix("+91")
        } else if (clean.startsWith("91") && clean.length == 12) {
            clean = clean.removePrefix("91")
        } else if (clean.startsWith("0") && clean.length == 11) {
            clean = clean.removePrefix("0")
        }

        if (!clean.all { it.isDigit() }) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Only digits and optional +91 prefix allowed in mobile number"
            )
        }

        if (clean.isNotEmpty() && clean.first() !in listOf('6', '7', '8', '9')) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid Indian mobile: Must begin with 6, 7, 8, or 9 (entered: ${clean.first()})"
            )
        }

        if (clean.length < 10) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Incomplete: Entered ${clean.length} of 10 digits"
            )
        }

        if (clean.length > 10) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Exceeds 10 digits (${clean.length} digits entered)"
            )
        }

        if (INDIAN_MOBILE_CLEAN_REGEX.matches(clean)) {
            val formatted = "+91 ${clean.take(5)} ${clean.takeLast(5)}"
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid 10-Digit Mobile (+91 Verified)",
                formattedValue = formatted
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid mobile number"
        )
    }

    /**
     * Validates Parent Email Address for digital custody intimation.
     */
    fun validateEmail(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Valid parent email for digital custody receipt dispatch (e.g. parent@example.com)"
            )
        }

        if (!trimmed.contains("@")) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Email address must contain an '@' symbol"
            )
        }

        if (!trimmed.contains(".")) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Email address must contain a domain (e.g. .com, .in)"
            )
        }

        if (EMAIL_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid Parent Intimation Email",
                formattedValue = trimmed.lowercase()
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid email format (e.g. parent.name@example.com)"
        )
    }

    /**
     * Validates Birth Date in DD/MM/YYYY format.
     */
    fun validateBirthDate(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Date in DD/MM/YYYY format (e.g. 13/09/2026)"
            )
        }

        if (!DATE_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid date format. Must be DD/MM/YYYY (e.g. 13/09/2026)"
            )
        }

        // Logical date range check
        return try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
                isLenient = false
            }
            sdf.parse(trimmed)
            ValidationResult(
                isValid = true,
                successMessage = "✓ Valid Calendar Date (DD/MM/YYYY)",
                formattedValue = trimmed
            )
        } catch (e: Exception) {
            ValidationResult(
                isValid = false,
                errorMessage = "❌ Invalid calendar date (check days in month or leap year)"
            )
        }
    }

    /**
     * Validates Birth Time in 12h/24h with optional IST.
     */
    fun validateBirthTime(raw: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Birth time with IST (e.g. 07:15:30 AM IST or 14:30 IST)"
            )
        }

        if (TIME_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid Birth Timestamp (IST)",
                formattedValue = trimmed
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ Invalid time format. Expected: HH:MM:SS AM/PM IST or HH:MM IST"
        )
    }

    /**
     * Validates Personal Names (Father, Mother, Doctor).
     */
    fun validateName(raw: String, roleLabel: String = "Name"): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Full $roleLabel with initials (minimum 3 letters)"
            )
        }

        if (trimmed.length < 3) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ $roleLabel is too short (minimum 3 characters)"
            )
        }

        if (NAME_REGEX.matches(trimmed)) {
            return ValidationResult(
                isValid = true,
                successMessage = "✓ Valid $roleLabel",
                formattedValue = trimmed
            )
        }

        return ValidationResult(
            isValid = false,
            errorMessage = "❌ $roleLabel must contain only alphabetic letters, spaces, and initials (e.g. Dr. S. Meenakshi, MD)"
        )
    }

    /**
     * Validates Hospital / Location name.
     */
    fun validateHospitalOrLocation(raw: String, fieldName: String): ValidationResult {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) {
            return ValidationResult(
                isValid = false,
                helperText = "Authorized $fieldName (minimum 3 characters)"
            )
        }

        if (trimmed.length < 3) {
            return ValidationResult(
                isValid = false,
                errorMessage = "❌ $fieldName must be at least 3 characters"
            )
        }

        return ValidationResult(
            isValid = true,
            successMessage = "✓ Valid $fieldName",
            formattedValue = trimmed
        )
    }

    // =========================================================================
    // SAMPLE STATUTORY PRESETS FOR FAST TESTING & AUTODISCOVERY
    // =========================================================================
    object Samples {
        const val VALID_PICME_1 = "1029 4857 2910"
        const val VALID_PICME_2 = "2026 8841 9320"
        const val VALID_AADHAAR_MOTHER = "9841 2345 6789"
        const val VALID_AADHAAR_FATHER = "8912 3456 7890"
        const val VALID_CRS_FORM5_1 = "B-2026: 02-00892-000123"
        const val VALID_CRS_FORM5_2 = "TN-KGH-2026-088219"
        const val VALID_MOBILE = "+91 98401 23456"
        const val VALID_EMAIL = "vimal.uv1991@gmail.com"
    }
}
