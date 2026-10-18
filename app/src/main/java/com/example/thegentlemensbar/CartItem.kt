
package com.example.thegentlemensbar

data class CartItem(
    val product: Product,
    var quantity: Int = 1
) {
    fun getTotalPrice(): Double = product.price * quantity
}