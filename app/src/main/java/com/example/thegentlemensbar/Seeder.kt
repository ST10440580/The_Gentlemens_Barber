package com.example.thegentlemensbar

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

// Runs once after an ADMIN logs in, only if the collections are empty.
object Seeder {
    private val db get() = FirebaseFirestore.getInstance()

    fun seedIfEmpty() {
        db.collection("services").limit(1).get().addOnSuccessListener { if (it.isEmpty) seedServices() }
        db.collection("staff").limit(1).get().addOnSuccessListener { if (it.isEmpty) seedStaff() }
    }

    private fun seedServices() {
        val list = listOf(
            Triple("haircut", "Haircut", 150.0) to 30,
            Triple("beard-trim", "Beard Trim", 80.0) to 30,
            Triple("hot-towel-shave", "Hot Towel Shave", 120.0) to 30,
            Triple("facial", "Facial", 200.0) to 60
        )
        val batch = db.batch()
        list.forEach { (t, mins) ->
            batch.set(db.collection("services").document(t.first), hashMapOf(
                "name" to t.second, "description" to "", "price" to t.third,
                "durationMins" to mins, "imageKey" to t.first, "active" to true
            ))
        }
        batch.commit().addOnFailureListener { Log.e("Seeder", "services", it) }
    }

    private fun seedStaff() {
        val hours = hashMapOf(
            "mon" to hashMapOf("start" to "09:00", "end" to "17:00"),
            "tue" to hashMapOf("start" to "09:00", "end" to "17:00"),
            "wed" to hashMapOf("start" to "09:00", "end" to "17:00"),
            "thu" to hashMapOf("start" to "09:00", "end" to "17:00"),
            "fri" to hashMapOf("start" to "09:00", "end" to "17:00"),
            "sat" to hashMapOf("start" to "09:00", "end" to "14:00")
            // no "sun" key = closed on Sundays
        )
        val batch = db.batch()
        listOf("barber1" to "Barber One", "barber2" to "Barber Two").forEach { (id, name) ->
            batch.set(db.collection("staff").document(id), hashMapOf(
                "uid" to "", "name" to name, "title" to "Barber",
                "workingHours" to hours, "daysOff" to emptyList<String>(), "active" to true
            ))
        }
        batch.commit().addOnFailureListener { Log.e("Seeder", "staff", it) }
    }
}