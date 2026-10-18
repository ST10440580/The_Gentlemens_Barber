package com.example.thegentlemensbar

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ProductDetail : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_detail)

        val productId = intent.getIntExtra("productId", -1)
        val product = Products.allProducts.find { it.id == productId }

        if (product == null) {
            Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val imageView = findViewById<ImageView>(R.id.productImage)
        val nameView = findViewById<TextView>(R.id.productName)
        val ratingView = findViewById<TextView>(R.id.productRating)
        val stockView = findViewById<TextView>(R.id.productStock)
        val priceView = findViewById<TextView>(R.id.productPrice)
        val categoryView = findViewById<TextView>(R.id.productCategory)
        val descriptionView = findViewById<TextView>(R.id.productDescription)
        val addToCartBtn = findViewById<Button>(R.id.addToCartBtn)

        imageView.setImageResource(product.imageResId)
        nameView.text = product.name
        ratingView.text = "⭐ ${product.rating}"
        stockView.text = if (product.inStock) "● In Stock" else "● Out of Stock"
        stockView.setTextColor(if (product.inStock) getColor(R.color.green) else getColor(R.color.red))
        priceView.text = "R${String.format("%.2f", product.price)}"
        categoryView.text = "Category: ${product.category}"
        descriptionView.text = product.description




        // Add to cart click
        addToCartBtn.setOnClickListener {
            Products.addToCart(product)
            Toast.makeText(this, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        findViewById<ImageButton>(R.id.backBtn).setOnClickListener {
            finish()
        }
    }
}
