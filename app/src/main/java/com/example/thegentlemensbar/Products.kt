
package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Products : AppCompatActivity() {

    private lateinit var productsRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var cartBadge: TextView


    companion object {
        val allProducts = mutableListOf<Product>()
        var cartItems = mutableListOf<CartItem>()

        fun addToCart(product: Product) {
            val existing = cartItems.find { it.product.id == product.id }
            if (existing != null) {
                existing.quantity++
            } else {
                cartItems.add(CartItem(product))
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)

        productsRecyclerView = findViewById(R.id.productsRecyclerView)
        cartBadge = findViewById(R.id.cartBadge)

        // Setup products only once
        if (allProducts.isEmpty()) setupProducts()

        productAdapter = ProductAdapter(allProducts) { product ->
            val intent = Intent(this, ProductDetail::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }
        productsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        productsRecyclerView.adapter = productAdapter

        findViewById<ImageButton>(R.id.cartBtn).setOnClickListener {
            startActivity(Intent(this, Cart::class.java))
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            finish()
        }



        updateCartBadge()
    }

    override fun onResume() {
        super.onResume()
        updateCartBadge()
        productAdapter.notifyDataSetChanged()
    }

    private fun setupProducts() {
        allProducts.addAll(
            listOf(
                Product(1, "Premium Hair Wax", 149.99, "Long-lasting hold with natural shine.", R.drawable.hair_wax, "Hair Care", 4.8f),
                Product(2, "Beard Oil Kit", 189.99, "Nourishing beard oil with essential vitamins.", R.drawable.beard_oil, "Beard", 4.9f),
                Product(3, "Shaving Cream", 89.99, "Luxurious shaving cream with aloe vera.", R.drawable.shaving_cream, "Skin Care", 4.6f),
                Product(4, "Professional Clippers", 1299.99, "Professional-grade clippers.", R.drawable.clippers, "Accessories", 4.7f),
                Product(5, "Hair Conditioner", 129.99, "Deep conditioning treatment.", R.drawable.conditioner, "Hair Care", 4.5f),
                Product(6, "Beard Balm", 159.99, "Styling balm for beards.", R.drawable.beard_balm, "Beard", 4.4f),
                Product(7, "Facial Cleanser", 109.99, "Gentle facial cleanser.", R.drawable.facial_cleanser, "Skin Care", 4.7f),
                Product(8, "Barber Cape", 249.99, "Professional barber cape.", R.drawable.barber_cape, "Accessories", 4.3f)
            )
        )
    }

    fun updateCartBadge() {
        val totalItems = cartItems.sumOf { it.quantity }
        if (totalItems > 0) {
            cartBadge.text = totalItems.toString()
            cartBadge.visibility = TextView.VISIBLE
        } else {
            cartBadge.visibility = TextView.GONE
        }
    }
}
