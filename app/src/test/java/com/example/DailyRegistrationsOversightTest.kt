package com.example

import com.example.data.NewbornRecord
import com.example.data.SafeStartRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyRegistrationsOversightTest {

    @Test
    fun testRepositoryRecordsHaveWardStatusesAndGenders() {
        val records = SafeStartRepository.records.value
        assertTrue("Repository must have initial records", records.isNotEmpty())

        val maleCount = records.count { it.gender.equals("Male", ignoreCase = true) }
        val femaleCount = records.count { it.gender.equals("Female", ignoreCase = true) }

        assertTrue("Should have male records", maleCount > 0)
        assertTrue("Should have female records", femaleCount > 0)
        assertEquals("Total equals sum of males and females", records.size, maleCount + femaleCount)

        val postnatalCount = records.count { it.wardStatus.contains("Postnatal", ignoreCase = true) }
        val laborCount = records.count { it.wardStatus.contains("Labor", ignoreCase = true) }
        assertTrue("Should have records in Postnatal Ward", postnatalCount > 0)
        assertTrue("Should have records in Labor & Delivery", laborCount > 0)
    }

    @Test
    fun testAddNewbornRecordWithWardStatus() {
        val initialSize = SafeStartRepository.records.value.size
        val record = SafeStartRepository.addNewbornRecord(
            motherName = "Bhavani Devi",
            fatherName = "G. Venkatesh",
            gender = "Female",
            doctorName = "Dr. M. Chitra",
            hospitalName = "Tirunelveli Govt Medical College",
            hospitalDistrict = "Tirunelveli",
            parentMobile = "+91 98401 23456",
            parentEmail = "bhavani.v@gmail.com",
            birthTimestamp = "13 Sep 2026, 02:00 PM",
            wardStatus = "NICU / SNCU"
        )

        assertEquals("Female", record.gender)
        assertEquals("NICU / SNCU", record.wardStatus)
        assertEquals(initialSize + 1, SafeStartRepository.records.value.size)
    }
}
