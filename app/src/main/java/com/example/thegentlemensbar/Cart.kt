
package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Cart : AppCompatActivity() {

    private lateinit var cartRecyclerView: RecyclerView
    private lateinit var cartAdapter: CartAdapter
    private lateinit var cartTotal: TextView
    private lateinit var checkoutSection: LinearLayout
    private lateinit var emptyCartText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cart)

        cartRecyclerView = findViewById(R.id.cartRecyclerView)
        cartTotal = findViewById(R.id.cartTotal)
        checkoutSection = findViewById(R.id.checkoutSection)
        emptyCartText = findViewById(R.id.emptyCartText)

        // Setup adapter
        cartAdapter = CartAdapter(Products.cartItems) {
            // Update total when cart changes
            updateTotal()
            // Update empty state
            updateEmptyState()
        }
        cartRecyclerView.layoutManager = LinearLayoutManager(this)
        cartRecyclerView.adapter = cartAdapter

        // Back button
        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            finish()
        }

        // Checkout button
        findViewById<Button>(R.id.checkoutBtn).setOnClickListener {
            // Navigate to checkout/payment
            val total = Products.cartItems.sumOf { it.getTotalPrice() }
            val intent = Intent(this, Checkout::class.java)
            intent.putExtra("totalAmount", total)
            startActivity(intent)
        }

        updateTotal()
        updateEmptyState()
    }

    private fun updateTotal() {
        val total = Products.cartItems.sumOf { it.getTotalPrice() }
        cartTotal.text = "R${String.format("%.2f", total)}"
    }

    private fun updateEmptyState() {
        if (Products.cartItems.isEmpty()) {
            cartRecyclerView.visibility = RecyclerView.GONE
            checkoutSection.visibility = LinearLayout.GONE
            emptyCartText.visibility = TextView.VISIBLE
        } else {
            cartRecyclerView.visibility = RecyclerView.VISIBLE
            checkoutSection.visibility = LinearLayout.VISIBLE
            emptyCartText.visibility = TextView.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        cartAdapter.notifyDataSetChanged()
        updateTotal()
        updateEmptyState()
    }
}