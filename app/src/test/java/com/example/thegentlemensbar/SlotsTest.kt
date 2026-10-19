package com.example.thegentlemensbar

import org.junit.Assert.assertEquals
import org.junit.Test

class SlotsTest {

    @Test
    fun `test toMins converts time string to minutes`() {
        assertEquals(0, Slots.toMins("00:00"))
        assertEquals(630, Slots.toMins("10:30"))
        assertEquals(1439, Slots.toMins("23:59"))
    }

    @Test
    fun `test fmt converts minutes to time string`() {
        assertEquals("00:00", Slots.fmt(0))
        assertEquals("10:30", Slots.fmt(630))
        assertEquals("23:59", Slots.fmt(1439))
    }

    @Test
    fun `test slotsNeeded returns correct list of slots`() {
        // 30 min service starting at 10:00 -> 1 slot
        assertEquals(listOf("10:00"), Slots.slotsNeeded("10:00", 30))
        
        // 45 min service starting at 10:00 -> 2 slots (10:00, 10:30)
        assertEquals(listOf("10:00", "10:30"), Slots.slotsNeeded("10:00", 45))
        
        // 60 min service starting at 10:00 -> 2 slots
        assertEquals(listOf("10:00", "10:30"), Slots.slotsNeeded("10:00", 60))
        
        // 15 min service starting at 10:00 -> 1 slot
        assertEquals(listOf("10:00"), Slots.slotsNeeded("10:00", 15))
    }

    @Test
    fun `test freeSlots returns empty when on holiday`() {
        val staff = StaffModel(id = "s1", workingHours = mapOf("mon" to mapOf("start" to "09:00", "end" to "17:00")))
        val service = ServiceModel(durationMins = 30)
        val holidays = mapOf("2023-12-25" to "Christmas")
        
        val slots = Slots.freeSlots("2023-12-25", staff, service, emptySet(), holidays)
        
        assertEquals(0, slots.size)
    }

    @Test
    fun `test freeSlots returns empty when staff is off`() {
        val staff = StaffModel(id = "s1", daysOff = listOf("2023-10-10"))
        val service = ServiceModel(durationMins = 30)
        
        val slots = Slots.freeSlots("2023-10-10", staff, service, emptySet(), emptyMap())
        
        assertEquals(0, slots.size)
    }
}
