package com.example

import com.example.data.TamilNaduIdValidator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TamilNaduIdValidatorTest {

    @Test
    fun testValidPicmeFormats() {
        val valid1 = TamilNaduIdValidator.validatePicme("1029 4857 2910")
        assertTrue("Expected PICME with spaces to be valid", valid1.isValid)
        assertEquals("1029 4857 2910", valid1.formattedValue)

        val valid2 = TamilNaduIdValidator.validatePicme("102948572910")
        assertTrue("Expected continuous 12-digit PICME to be valid", valid2.isValid)
        assertEquals("1029 4857 2910", valid2.formattedValue)

        val valid3 = TamilNaduIdValidator.validatePicme("2091-3847-5612")
        assertTrue("Expected hyphenated 12-digit PICME to be valid", valid3.isValid)
        assertEquals("2091 3847 5612", valid3.formattedValue)
    }

    @Test
    fun testInvalidPicmeFormats() {
        val shortPicme = TamilNaduIdValidator.validatePicme("10294857")
        assertFalse("Expected short PICME to be invalid", shortPicme.isValid)
        assertNotNull(shortPicme.errorMessage)
        assertTrue(shortPicme.errorMessage!!.contains("12 digits"))

        val alphaPicme = TamilNaduIdValidator.validatePicme("10294857ABCD")
        assertFalse("Expected alphanumeric PICME to be invalid", alphaPicme.isValid)

        val emptyPicme = TamilNaduIdValidator.validatePicme("")
        assertFalse("Expected empty PICME to be invalid", emptyPicme.isValid)
    }

    @Test
    fun testValidAadhaarFormats() {
        val validAadhaar1 = TamilNaduIdValidator.validateAadhaar("9841 2345 6789")
        assertTrue("Expected Aadhaar starting with 9 to be valid", validAadhaar1.isValid)

        val validAadhaar2 = TamilNaduIdValidator.validateAadhaar("234567890123")
        assertTrue("Expected Aadhaar starting with 2 to be valid", validAadhaar2.isValid)
    }

    @Test
    fun testInvalidAadhaarFormats() {
        val zeroStart = TamilNaduIdValidator.validateAadhaar("0841 2345 6789")
        assertFalse("Aadhaar starting with 0 is invalid per UIDAI standards", zeroStart.isValid)
        assertTrue(zeroStart.errorMessage!!.contains("cannot begin with 0 or 1"))

        val oneStart = TamilNaduIdValidator.validateAadhaar("1234 5678 9012")
        assertFalse("Aadhaar starting with 1 is invalid per UIDAI standards", oneStart.isValid)

        val repeating = TamilNaduIdValidator.validateAadhaar("9999 9999 9999")
        assertFalse("Aadhaar with 12 identical digits is invalid", repeating.isValid)
    }

    @Test
    fun testValidCrsForm5Formats() {
        val crs1 = TamilNaduIdValidator.validateCrsForm5("B-2026: 02-00892-000123")
        assertTrue("Expected standard Form-5 number to be valid", crs1.isValid)

        val crs2 = TamilNaduIdValidator.validateCrsForm5("2026/02/00892/000123")
        assertTrue("Expected slash format Form-5 to be valid", crs2.isValid)

        val crs3 = TamilNaduIdValidator.validateCrsForm5("TN-REG-2026-90812")
        assertTrue("Expected alphanumeric hospital reg number to be valid", crs3.isValid)
    }

    @Test
    fun testValidMobileFormats() {
        val mobile1 = TamilNaduIdValidator.validateMobile("+91 98401 23456")
        assertTrue("Expected Indian mobile with +91 to be valid", mobile1.isValid)

        val mobile2 = TamilNaduIdValidator.validateMobile("9840123456")
        assertTrue("Expected 10-digit mobile to be valid", mobile2.isValid)

        val invalidMobile = TamilNaduIdValidator.validateMobile("1234567890")
        assertFalse("Mobile starting with 1 should be invalid in India", invalidMobile.isValid)
    }

    @Test
    fun testDateAndTimeValidation() {
        val validDate = TamilNaduIdValidator.validateBirthDate("13/09/2026")
        assertTrue("Expected valid DD/MM/YYYY date", validDate.isValid)

        val invalidDate = TamilNaduIdValidator.validateBirthDate("2026-09-13")
        assertFalse("Expected non DD/MM/YYYY format to trigger format prompt", invalidDate.isValid)

        val validTime = TamilNaduIdValidator.validateBirthTime("07:15:30 AM IST")
        assertTrue("Expected valid IST time", validTime.isValid)
    }

    @Test
    fun testEmailValidation() {
        val validEmail = TamilNaduIdValidator.validateEmail("vimal.uv1991@gmail.com")
        assertTrue("Expected valid user email", validEmail.isValid)

        val invalidEmail = TamilNaduIdValidator.validateEmail("invalid_email")
        assertFalse("Expected invalid email to fail", invalidEmail.isValid)
    }
}
