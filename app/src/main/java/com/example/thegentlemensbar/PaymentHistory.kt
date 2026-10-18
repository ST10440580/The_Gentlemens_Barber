package com.example.thegentlemensbar

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentHistory : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment_history)

        val totalText = findViewById<TextView>(R.id.historyTotal)
        val list = findViewById<ListView>(R.id.historyList)
        val fmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        FirebaseRepo.getMyPayments { payments ->
            val sorted = payments.sortedByDescending { it.paidMillis }
            val total = sorted.filter { it.status == "paid" }.sumOf { it.amount }
            totalText.text = String.format(Locale.US, "Total paid: R%.2f", total)

            val rows = if (sorted.isEmpty()) listOf("No payments yet") else sorted.map {
                val date = if (it.paidMillis > 0) fmt.format(Date(it.paidMillis)) else "-"
                String.format(Locale.US, "R%.2f  •  %s  •  %s  •  %s", it.amount,
                    it.method.replace("_", " "), it.type, date)
            }
            list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rows)
        }
    }
}