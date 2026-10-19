package com.example.thegentlemensbar

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CartLogicTest {

    @Before
    fun setup() {
        Products.cartItems.clear()
    }

    @Test
    fun `test cart item total price calculation`() {
        val product = Product(1, "Test Product", 100.0, "Description", 0, "Category")
        val cartItem = CartItem(product, 3)
        
        assertEquals(300.0, cartItem.getTotalPrice(), 0.01)
    }

    @Test
    fun `test adding same product to cart increases quantity`() {
        val product = Product(1, "Test Product", 100.0, "Description", 0, "Category")
        
        Products.addToCart(product)
        Products.addToCart(product)
        
        assertEquals(1, Products.cartItems.size)
        assertEquals(2, Products.cartItems[0].quantity)
    }

    @Test
    fun `test adding different products to cart`() {
        val product1 = Product(1, "Product 1", 100.0, "Description", 0, "Category")
        val product2 = Product(2, "Product 2", 200.0, "Description", 0, "Category")
        
        Products.addToCart(product1)
        Products.addToCart(product2)
        
        assertEquals(2, Products.cartItems.size)
    }
    
    @Test
    fun `test cart total calculation`() {
        val product1 = Product(1, "Product 1", 100.0, "Description", 0, "Category")
        val product2 = Product(2, "Product 2", 50.0, "Description", 0, "Category")
        
        Products.addToCart(product1) // Qty 1
        Products.addToCart(product2) // Qty 1
        Products.addToCart(product1) // Qty 2
        
        val total = Products.cartItems.sumOf { it.getTotalPrice() }
        assertEquals(250.0, total, 0.01)
    }
}
