package com.example.thegentlemensbar

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object Slots {
    const val SLOT_MINS = 30
    private val days = arrayOf("sun", "mon", "tue", "wed", "thu", "fri", "sat")

    fun toMins(t: String): Int {
        val p = t.split(":")
        return p[0].toInt() * 60 + p[1].toInt()
    }

    fun fmt(mins: Int): String = String.format(Locale.US, "%02d:%02d", mins / 60, mins % 60)

    fun parseMillis(date: String, time: String): Long =
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).parse("$date $time")!!.time

    /** Every 30-min slot a booking of this length covers, e.g. 45 min at 10:00 -> 10:00, 10:30 */
    fun slotsNeeded(startTime: String, durationMins: Int): List<String> {
        val start = toMins(startTime)
        val count = (durationMins + SLOT_MINS - 1) / SLOT_MINS
        return (0 until count).map { fmt(start + it * SLOT_MINS) }
    }

    private fun weekdayKey(date: String): String {
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date)!!
        return days[cal.get(Calendar.DAY_OF_WEEK) - 1]
    }

    // Free start times for one barber / one service / one date.leadMins: how far ahead a slot must start (30 for customers, -30 for walk-ins).
    fun freeSlots(
        date: String,
        staff: StaffModel,
        service: ServiceModel,
        booked: Set<String>,
        holidays: Map<String, String>,
        leadMins: Int = 30
    ): List<String> {
        if (holidays.containsKey(date)) return emptyList()
        if (staff.daysOff.contains(date)) return emptyList()
        val hours = staff.workingHours[weekdayKey(date)] ?: return emptyList()

        val open = toMins(hours["start"] ?: return emptyList())
        val close = toMins(hours["end"] ?: return emptyList())
        val earliest = System.currentTimeMillis() + leadMins * 60_000L

        val result = mutableListOf<String>()
        var t = open
        while (t + service.durationMins <= close) {
            val time = fmt(t)
            val allFree = slotsNeeded(time, service.durationMins).none { it in booked }
            if (allFree && parseMillis(date, time) > earliest) result.add(time)
            t += SLOT_MINS
        }
        return result
    }
}