package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class Checkout : AppCompatActivity() {

    private lateinit var payBtn: Button
    private lateinit var payInStoreBtn: Button
    private var totalAmount = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payments)

        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        val totalText = String.format("%.2f", totalAmount)

        findViewById<TextView>(R.id.paymentsTitle).text = "CHECKOUT - R$totalText"

        payBtn = findViewById(R.id.payBtn)
        payInStoreBtn = findViewById(R.id.payInStoreBtn)
        payBtn.text = "Pay R$totalText"

        // Pay by card (demo: card details are only checked, never stored or sent anywhere)
        payBtn.setOnClickListener {
            val cardNumber = findViewById<EditText>(R.id.cardNumber).text.toString().trim()
            val expiryDate = findViewById<EditText>(R.id.expiryDate).text.toString().trim()
            val cvv = findViewById<EditText>(R.id.cvv).text.toString().trim()

            if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cardNumber.filter { it.isDigit() }.length !in 12..19 || cvv.length !in 3..4) {
                Toast.makeText(this, "Please check your card details", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            placeOrder("card", "Payment successful! Total: R$totalText")
        }

        // Pay in store
        payInStoreBtn.setOnClickListener {
            placeOrder("in_store", "You chose to pay in store. Total: R$totalText")
        }
    }

    private fun placeOrder(method: String, successMessage: String) {
        if (Products.cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show()
            return
        }
        payBtn.isEnabled = false
        payInStoreBtn.isEnabled = false

        FirebaseRepo.createOrder(Products.cartItems.toList(), totalAmount, method) { ok ->
            if (ok) {
                Toast.makeText(this, successMessage, Toast.LENGTH_LONG).show()
                Products.cartItems.clear()

                val intent = Intent(this, Products::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                payBtn.isEnabled = true
                payInStoreBtn.isEnabled = true
                Toast.makeText(this, "Error placing order. Try again.", Toast.LENGTH_LONG).show()
            }
        }
    }
}