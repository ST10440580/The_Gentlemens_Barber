package com.example.thegentlemensbar

import android.content.Context
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class UserProfile(val uid: String, val fullName: String, val role: String)

data class ServiceModel(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val durationMins: Int = 30,
    val price: Double = 0.0,
    val imageKey: String = "",
    val active: Boolean = true
)

data class StaffModel(
    val id: String = "",
    val uid: String = "",
    val name: String = "",
    val title: String = "",
    val workingHours: Map<String, Map<String, String>> = emptyMap(), // "mon" -> {start,end}
    val daysOff: List<String> = emptyList(),
    val active: Boolean = true
)

data class BookingModel(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val staffId: String = "",
    val staffName: String = "",
    val serviceId: String = "",
    val serviceName: String = "",
    val durationMins: Int = 30,
    val price: Double = 0.0,
    val date: String = "",        // yyyy-MM-dd
    val time: String = "",        // HH:mm
    val startMillis: Long = 0L,
    val status: String = "pending", // pending | confirmed | completed | cancelled | no_show
    val paymentStatus: String = "unpaid",
    val slotKeys: List<String> = emptyList()
)

data class PaymentModel(
    val id: String,
    val method: String,
    val amount: Double,
    val status: String,
    val type: String,
    val paidMillis: Long
)

data class CardModel(
    val id: String = "",
    val cardNumber: String = "",
    val expiry: String = "",
    val holderName: String = "",
    val brand: String = "Visa"
)

fun saveProfileToPrefs(context: Context, profile: UserProfile) {
    context.getSharedPreferences("UserPref", Context.MODE_PRIVATE)
        .edit().putString("USERNAME", profile.fullName).apply()
}

fun routeByRole(context: Context, role: String) {
    val target = if (role == "staff" || role == "admin") StaffDashboard::class.java
    else MainActivity::class.java
    context.startActivity(Intent(context, target))
}

fun serviceIcon(name: String): Int = when (name) {
    "Haircut" -> R.drawable.haircut
    "Hot Towel Shave", "Shave" -> R.drawable.hottowel_shave
    "Beard Trim" -> R.drawable.shave
    "Facial" -> R.drawable.facial
    else -> R.drawable.ic_booking
}

fun todayString(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())