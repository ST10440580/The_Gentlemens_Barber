package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var upcomingText: TextView
    private lateinit var spendText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseRepo.currentUid == null) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
            return
        }
        setContentView(R.layout.activity_main)

        welcomeText = findViewById(R.id.welcomeUser)
        upcomingText = findViewById(R.id.upcomingAppointmentsText)
        spendText = findViewById(R.id.totalSpendText)

        findViewById<ImageButton>(R.id.bookAppointmentBtn).setOnClickListener {
            startActivity(Intent(this, Booking::class.java))
        }
        findViewById<ImageButton>(R.id.myBookingsBtn).setOnClickListener {
            startActivity(Intent(this, MyBookings::class.java))
        }
        findViewById<ImageButton>(R.id.servicesBtn).setOnClickListener {
            startActivity(Intent(this, Services::class.java))
        }
        findViewById<ImageButton>(R.id.paymentsBtn).setOnClickListener {
            startActivity(Intent(this, PaymentHistory::class.java))
        }
        findViewById<ImageButton>(R.id.productsBtn).setOnClickListener {
            startActivity(Intent(this, Products::class.java))
        }
        findViewById<ImageButton>(R.id.profileBtn).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Log out?")
                .setPositiveButton("Log out") { _, _ ->
                    FirebaseRepo.logout()
                    getSharedPreferences("UserPref", MODE_PRIVATE).edit().clear().apply()
                    startActivity(
                        Intent(this, WelcomeActivity::class.java)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    )
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (FirebaseRepo.currentUid != null) loadDashboardData()
    }

    private fun loadDashboardData() {
        welcomeText.text = getSharedPreferences("UserPref", MODE_PRIVATE).getString("USERNAME", "Guest")

        FirebaseRepo.getMyBookings { list ->
            val now = System.currentTimeMillis()
            val upcoming = list.count {
                (it.status == "pending" || it.status == "confirmed") && it.startMillis >= now
            }
            upcomingText.text = "You have $upcoming appointment(s)"
        }
        FirebaseRepo.getMyPayments { payments ->
            val total = payments.filter { it.status == "paid" }.sumOf { it.amount }
            spendText.text = String.format(Locale.US, "R%.2f", total)
        }
    }
}