package com.example.thegentlemensbar

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddCardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_card)

        val cardNumberInput = findViewById<EditText>(R.id.addCardNumber)
        val expiryInput = findViewById<EditText>(R.id.addCardExpiry)
        val holderNameInput = findViewById<EditText>(R.id.addCardHolder)
        val saveBtn = findViewById<Button>(R.id.saveCardBtn)

        saveBtn.setOnClickListener {
            val number = cardNumberInput.text.toString().trim()
            val expiry = expiryInput.text.toString().trim()
            val holder = holderNameInput.text.toString().trim()

            if (number.length < 16) {
                Toast.makeText(this, "Enter a valid 16-digit card number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (expiry.isEmpty() || !expiry.contains("/")) {
                Toast.makeText(this, "Enter expiry in MM/YY format", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (holder.isEmpty()) {
                Toast.makeText(this, "Enter cardholder name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val card = CardModel(
                cardNumber = number,
                expiry = expiry,
                holderName = holder,
                brand = if (number.startsWith("4")) "Visa" else "MasterCard"
            )

            FirebaseRepo.saveCard(card) { success ->
                if (success) {
                    Toast.makeText(this, "Card saved", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Toast.makeText(this, "Failed to save card", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
