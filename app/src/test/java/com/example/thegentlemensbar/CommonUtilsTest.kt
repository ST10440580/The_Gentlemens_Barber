package com.example.thegentlemensbar

import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CommonUtilsTest {

    @Test
    fun `test serviceIcon returns correct drawable resource`() {
        assertEquals(R.drawable.haircut, serviceIcon("Haircut"))
        assertEquals(R.drawable.hottowel_shave, serviceIcon("Hot Towel Shave"))
        assertEquals(R.drawable.hottowel_shave, serviceIcon("Shave"))
        assertEquals(R.drawable.shave, serviceIcon("Beard Trim"))
        assertEquals(R.drawable.facial, serviceIcon("Facial"))
        assertEquals(R.drawable.ic_booking, serviceIcon("Unknown Service"))
    }

    @Test
    fun `test todayString returns current date in yyyy-MM-dd format`() {
        val expected = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        assertEquals(expected, todayString())
    }
}
