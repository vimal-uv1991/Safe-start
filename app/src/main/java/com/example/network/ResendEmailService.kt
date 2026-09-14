package com.example.network

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/**
 * Service for dispatching real transactional emails and OTP verifications
 * via the Resend API (https://resend.com).
 */
object ResendEmailService {
    private const val TAG = "ResendEmailService"
    private const val RESEND_API_URL = "https://api.resend.com/emails"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    // Key can be initialized from BuildConfig or updated dynamically in the app UI
    private var customApiKey: String? = null

    /**
     * Resolves the current Resend API key: custom key if set, otherwise BuildConfig.
     */
    val apiKey: String
        get() {
            customApiKey?.let { if (it.isNotBlank()) return it }
            return try {
                val field = BuildConfig::class.java.getField("RESEND_API_KEY")
                val key = field.get(null) as? String ?: ""
                if (key == "re_placeholder_key" || key.isBlank()) "" else key
            } catch (e: Exception) {
                ""
            }
        }

    /**
     * Set a custom Resend API key at runtime.
     */
    fun setApiKey(key: String) {
        customApiKey = key.trim()
    }

    /**
     * Check if a valid Resend API key is available.
     */
    fun isConfigured(): Boolean {
        val key = apiKey
        return key.startsWith("re_") && key.length > 5
    }

    /**
     * Dispatches an OTP verification code via Resend.
     */
    suspend fun sendOtpEmail(
        toEmail: String,
        otp: String,
        officerName: String = "Authorized Official",
        purpose: String = "Identity & Access Verification"
    ): Result<String> = withContext(Dispatchers.IO) {
        val otpCode = otp
        val recipientName = officerName
        val activeKey = apiKey
        if (activeKey.isBlank() || !activeKey.startsWith("re_")) {
            return@withContext Result.failure(
                IllegalStateException("Resend API Key is not configured. Please add your Resend API key (re_...) in Email Settings or Secrets panel.")
            )
        }

        val subject = "SafeStart TN: Verification OTP $otpCode for $purpose"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; margin: 0; padding: 20px; }
                .container { max-width: 560px; margin: 0 auto; background: #FFFFFF; border-radius: 8px; border: 1px solid #CBD5E1; overflow: hidden; }
                .header { background: #0E7C4A; padding: 20px; text-align: center; border-bottom: 3px solid #F59E0B; }
                .header h1 { color: #FFFFFF; margin: 0; font-size: 20px; font-weight: bold; letter-spacing: 1px; }
                .header p { color: #F59E0B; margin: 4px 0 0 0; font-size: 12px; font-weight: 600; }
                .body { padding: 24px; color: #0F172A; }
                .otp-box { text-align: center; margin: 24px 0; padding: 18px; background: #ECFDF5; border: 1.5px dashed #0E7C4A; border-radius: 8px; }
                .otp-code { font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #0E7C4A; font-family: monospace; }
                .meta { background: #F1F5F9; padding: 12px; border-radius: 6px; font-size: 11px; color: #475569; margin-top: 16px; }
                .footer { padding: 16px; text-align: center; font-size: 10.5px; color: #94A3B8; background: #F8FAFC; border-top: 1px solid #E2E8F0; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>GOVERNMENT OF TAMIL NADU</h1>
                  <p>SAFE START • NEWBORN IDENTITY & BIOMETRIC PROTECTION</p>
                </div>
                <div class="body">
                  <p>Dear <strong>$recipientName</strong>,</p>
                  <p>Your one-time statutory authorization code for <strong>$purpose</strong> has been generated:</p>
                  <div class="otp-box">
                    <div class="otp-code">$otpCode</div>
                    <p style="margin: 6px 0 0 0; font-size: 11px; color: #047857; font-weight: 600;">VALID FOR 10 MINUTES • DO NOT SHARE</p>
                  </div>
                  <div class="meta">
                    <strong>Statutory Authority:</strong> Directorate of Public Health and Preventive Medicine, Tamil Nadu<br>
                    <strong>Portal Reference:</strong> tn.gov.in/safestart • Security Level: FIPS 140-2 Level 3
                  </div>
                </div>
                <div class="footer">
                  This is an automated tamper-evident transaction from the Tamil Nadu Civil Health Portal.<br>
                  24x7 State Emergency Support: 104 / 1098
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        executeResendCall(apiKey, toEmail, subject, htmlContent)
    }

    /**
     * Sends official baby birth registration notice to parent or hospital.
     */
    suspend fun sendRegistrationNotice(
        toEmail: String,
        parentName: String,
        token: String,
        hospitalName: String,
        childGender: String,
        birthTime: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val activeKey = apiKey
        if (activeKey.isBlank() || !activeKey.startsWith("re_")) {
            return@withContext Result.failure(
                IllegalStateException("Resend API Key is not configured. Please add your Resend API key (re_...) in Email Settings.")
            )
        }

        val subject = "SafeStart TN: Official Birth Registration Confirmation - Token #$token"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; padding: 20px; }
                .container { max-width: 560px; margin: 0 auto; background: #FFFFFF; border-radius: 8px; border: 1px solid #CBD5E1; overflow: hidden; }
                .header { background: #0E7C4A; padding: 20px; text-align: center; border-bottom: 3px solid #F59E0B; }
                .header h1 { color: #FFFFFF; margin: 0; font-size: 20px; font-weight: bold; }
                .body { padding: 24px; color: #0F172A; }
                .badge { background: #ECFDF5; border: 1px solid #10B981; padding: 12px; border-radius: 6px; font-size: 14px; font-weight: bold; color: #065F46; text-align: center; margin-bottom: 16px; }
                .record-table { width: 100%; border-collapse: collapse; margin-top: 12px; }
                .record-table td { padding: 8px 12px; border-bottom: 1px solid #E2E8F0; font-size: 12px; }
                .record-table td:first-child { font-weight: bold; color: #475569; width: 40%; }
                .footer { padding: 16px; text-align: center; font-size: 11px; color: #94A3B8; background: #F8FAFC; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>GOVERNMENT OF TAMIL NADU</h1>
                  <p style="color: #F59E0B; margin: 4px 0 0 0; font-size: 12px;">SAFE START NEWBORN REGISTRY</p>
                </div>
                <div class="body">
                  <div class="badge">✓ BIOMETRICS OFFICIALLY RECORDED & SECURED</div>
                  <p>Dear <strong>$parentName</strong>,</p>
                  <p>Your newborn's birth and biometric plantar ridge minutiae record has been committed to the Sovereign State Ledger.</p>
                  <table class="record-table">
                    <tr><td>State Unique Token</td><td style="font-family: monospace; font-weight: bold; color: #0E7C4A;">$token</td></tr>
                    <tr><td>Hospital Facility</td><td>$hospitalName</td></tr>
                    <tr><td>Child Gender</td><td>$childGender</td></tr>
                    <tr><td>Time of Birth</td><td>$birthTime</td></tr>
                    <tr><td>Statutory Retention</td><td>10 Years (Tamper-Proof)</td></tr>
                  </table>
                  <p style="font-size: 11.5px; color: #64748B; margin-top: 16px;">
                    This record is immutable and verifiable at any Government Medical College Hospital, Taluk Headquarters Hospital, or Directorate of Public Health portal.
                  </p>
                </div>
                <div class="footer">
                  Civil Registration Directorate, Tamil Nadu • 24x7 Helpline: 104
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        executeResendCall(activeKey, toEmail, subject, htmlContent)
    }

    /**
     * Sends official statutory birth registration receipt via Resend Email.
     */
    suspend fun sendRegistrationReceipt(
        toEmail: String,
        token: String,
        babyGender: String,
        fatherName: String,
        motherName: String,
        hospitalName: String,
        birthDate: String
    ): Result<String> {
        return sendRegistrationNotice(
            toEmail = toEmail,
            parentName = "$fatherName & $motherName",
            token = token,
            hospitalName = hospitalName,
            childGender = babyGender,
            birthTime = birthDate
        )
    }

    /**
     * Sends official institutional account creation confirmation notice via Resend Email.
     */
    suspend fun sendAccountCreationNotice(
        toEmail: String,
        officerName: String,
        roleName: String,
        generatedId: String,
        facilityName: String,
        timestamp: String
    ): Result<String> = withContext(Dispatchers.IO) {
        val activeKey = apiKey
        if (activeKey.isBlank() || !activeKey.startsWith("re_")) {
            return@withContext Result.failure(
                IllegalStateException("Resend API Key is not configured. Please add your Resend API key (re_...) in Email Settings.")
            )
        }

        val subject = "SafeStart TN: Official Institutional Account Created - ID: $generatedId"
        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="utf-8">
              <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #F8FAFC; padding: 20px; }
                .container { max-width: 560px; margin: 0 auto; background: #FFFFFF; border-radius: 8px; border: 1px solid #CBD5E1; overflow: hidden; }
                .header { background: #0E7C4A; padding: 20px; text-align: center; border-bottom: 3px solid #F59E0B; }
                .header h1 { color: #FFFFFF; margin: 0; font-size: 20px; font-weight: bold; }
                .body { padding: 24px; color: #0F172A; }
                .badge { background: #ECFDF5; border: 1px solid #10B981; padding: 12px; border-radius: 6px; font-size: 14px; font-weight: bold; color: #065F46; text-align: center; margin-bottom: 16px; }
                .table { width: 100%; border-collapse: collapse; margin-top: 14px; }
                .table td { padding: 8px 12px; border-bottom: 1px solid #E2E8F0; font-size: 12px; }
                .table td:first-child { font-weight: bold; color: #475569; width: 40%; }
                .footer { padding: 16px; text-align: center; font-size: 11px; color: #94A3B8; background: #F8FAFC; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>GOVERNMENT OF TAMIL NADU</h1>
                  <p style="color: #F59E0B; margin: 4px 0 0 0; font-size: 12px;">SAFE START PORTAL • OFFICIAL INSTITUTIONAL ONBOARDING</p>
                </div>
                <div class="body">
                  <div class="badge">✓ OFFICIAL INSTITUTIONAL ACCOUNT ACTIVATED</div>
                  <p>Dear <strong>$officerName</strong>,</p>
                  <p>Your official credentials for the <strong>Government of Tamil Nadu Newborn Identity Custody Portal</strong> have been verified and activated via the State Email Gateway.</p>
                  <table class="table">
                    <tr><td>Designated Role</td><td><strong>$roleName</strong></td></tr>
                    <tr><td>State Unique ID</td><td style="font-family: monospace; font-weight: bold; color: #0E7C4A;">$generatedId</td></tr>
                    <tr><td>Hospital / Directorate</td><td>$facilityName</td></tr>
                    <tr><td>Registered Email</td><td>$toEmail</td></tr>
                    <tr><td>Creation Timestamp</td><td>$timestamp</td></tr>
                    <tr><td>Access Clearance</td><td>Tier-1 State Civil Custody</td></tr>
                  </table>
                  <p style="font-size: 11.5px; color: #64748B; margin-top: 16px;">
                    Please use your State Unique ID along with your confidential password to access the portal. All transactions are cryptographically signed and tracked on the sovereign audit ledger.
                  </p>
                </div>
                <div class="footer">
                  Tamil Nadu Directorate of Public Health & Preventive Medicine • Helpline: 104
                </div>
              </div>
            </body>
            </html>
        """.trimIndent()

        executeResendCall(activeKey, toEmail, subject, htmlContent)
    }

    private fun executeResendCall(
        apiKey: String,
        toEmail: String,
        subject: String,
        htmlBody: String
    ): Result<String> {
        return try {
            val jsonPayload = JSONObject().apply {
                // Default onboarding email permitted on free/sandbox accounts
                put("from", "SafeStart TN <onboarding@resend.dev>")
                put("to", JSONArray().apply { put(toEmail.trim()) })
                put("subject", subject)
                put("html", htmlBody)
            }

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(RESEND_API_URL)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                val jsonResponse = JSONObject(responseBody)
                val id = jsonResponse.optString("id", "resend_ok")
                Log.i(TAG, "Email dispatched successfully. Resend ID: $id")
                Result.success(id)
            } else {
                Log.e(TAG, "Resend API error: HTTP ${response.code}: $responseBody")
                val errorMessage = try {
                    val errJson = JSONObject(responseBody)
                    errJson.optString("message", "HTTP ${response.code}: ${response.message}")
                } catch (e: Exception) {
                    "HTTP ${response.code}: $responseBody"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network failure calling Resend API", e)
            Result.failure(e)
        }
    }
}
