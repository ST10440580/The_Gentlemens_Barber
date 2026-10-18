
package com.example.thegentlemensbar

data class Product(
    val id: Int,
    val name: String,
    val price: Double,
    val description: String,
    val imageResId: Int,
    val category: String,
    val rating: Float = 4.5f,
    val inStock: Boolean = true
)