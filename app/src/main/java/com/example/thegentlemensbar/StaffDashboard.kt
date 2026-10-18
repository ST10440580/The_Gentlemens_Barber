package com.example.thegentlemensbar

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Calendar
import java.util.Locale

class StaffDashboard : AppCompatActivity() {

    private lateinit var title: TextView
    private lateinit var summary: TextView
    private lateinit var dateBtn: Button
    private lateinit var adapter: StaffBookingAdapter

    private val items = mutableListOf<BookingModel>()
    private var selectedDate = todayString()
    private var role = "staff"
    private var myStaff: StaffModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff_dashboard)

        title = findViewById(R.id.staffTitle)
        summary = findViewById(R.id.staffSummary)
        dateBtn = findViewById(R.id.staffDateBtn)
        dateBtn.text = selectedDate

        adapter = StaffBookingAdapter(items) { showActions(it) }
        findViewById<RecyclerView>(R.id.staffRecycler).apply {
            layoutManager = LinearLayoutManager(this@StaffDashboard)
            adapter = this@StaffDashboard.adapter
        }

        dateBtn.setOnClickListener { pickDate() }

        findViewById<Button>(R.id.staffWalkInBtn).setOnClickListener {
            startActivity(Intent(this, Booking::class.java).putExtra("WALK_IN", true))
        }
        findViewById<Button>(R.id.staffDayOffBtn).setOnClickListener { markDayOff() }
        findViewById<Button>(R.id.staffLogoutBtn).setOnClickListener {
            FirebaseRepo.logout()
            getSharedPreferences("UserPref", MODE_PRIVATE).edit().clear().apply()
            startActivity(
                Intent(this, WelcomeActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            )
        }

        FirebaseRepo.loadProfile { p ->
            if (p == null || p.role == "customer") {
                Toast.makeText(this, "This area is for staff only", Toast.LENGTH_LONG).show()
                finish()
                return@loadProfile
            }
            role = p.role
            title.text = "Hi ${p.fullName} (${p.role})"
            if (p.role == "admin") {
                loadBookings()
            } else {
                FirebaseRepo.getStaffByUid(p.uid) { s ->
                    myStaff = s
                    if (s == null) {
                        summary.text = "Your account isn't linked to a barber profile yet. Ask the admin."
                    } else {
                        loadBookings()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // refresh after a walk-in booking is added
        if (role == "admin" || myStaff != null) loadBookings()
    }

    private fun pickDate() {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d ->
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
            dateBtn.text = selectedDate
            loadBookings()
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun loadBookings() {
        FirebaseRepo.getBookingsForDate(selectedDate, if (role == "admin") null else myStaff?.id) { list ->
            items.clear()
            items.addAll(list)
            adapter.notifyDataSetChanged()

            val active = list.filter { it.status != "cancelled" && it.status != "no_show" }
            val expected = active.sumOf { it.price }
            val collected = list.filter { it.paymentStatus == "paid" }.sumOf { it.price }
            summary.text = String.format(
                Locale.US, "%d bookings • Expected R%.0f • Collected R%.0f",
                active.size, expected, collected
            )
        }
    }

    private fun showActions(b: BookingModel) {
        val options = mutableListOf<String>()
        if (b.status == "pending") options.add("Confirm")
        if (b.status == "pending" || b.status == "confirmed") {
            options.add("Completed & paid")
            options.add("No-show")
            options.add("Cancel booking")
        }
        if (options.isEmpty()) {
            Toast.makeText(this, "No actions for a ${b.status} booking", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("${b.customerName} • ${b.serviceName}")
            .setItems(options.toTypedArray()) { _, i ->
                when (options[i]) {
                    "Confirm" -> FirebaseRepo.setStatus(b.id, "confirmed") { done(it) }
                    "Completed & paid" -> askPaymentMethod(b)
                    "No-show" -> FirebaseRepo.setStatus(b.id, "no_show") { done(it) }
                    "Cancel booking" -> FirebaseRepo.cancelBooking(b) { done(it) }
                }
            }
            .show()
    }

    private fun askPaymentMethod(b: BookingModel) {
        val methods = arrayOf("Cash", "Card", "EFT")
        AlertDialog.Builder(this)
            .setTitle("How did they pay?")
            .setItems(methods) { _, i ->
                FirebaseRepo.completeBooking(b, methods[i].lowercase()) { done(it) }
            }
            .show()
    }

    private fun done(ok: Boolean) {
        Toast.makeText(this, if (ok) "Updated" else "Something went wrong", Toast.LENGTH_SHORT).show()
        if (ok) loadBookings()
    }

    private fun markDayOff() {
        val s = myStaff
        if (s == null) {
            Toast.makeText(this, "Only barbers linked to a profile can set days off", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Mark $selectedDate as a day off?")
            .setMessage("Customers won't be able to book you that day. Existing bookings are not cancelled.")
            .setPositiveButton("Yes") { _, _ ->
                FirebaseRepo.addDayOff(s.id, selectedDate) {
                    Toast.makeText(this, if (it) "Day off saved" else "Could not save", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("No", null)
            .show()
    }
}