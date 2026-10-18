
package com.example.thegentlemensbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val cartItems: MutableList<CartItem>,
    private val onCartUpdate: () -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val quantityText: TextView = itemView.findViewById(R.id.quantityText)
        val decreaseBtn: ImageButton = itemView.findViewById(R.id.decreaseBtn)
        val increaseBtn: ImageButton = itemView.findViewById(R.id.increaseBtn)
        val removeBtn: ImageButton = itemView.findViewById(R.id.removeBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cart, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val cartItem = cartItems[position]
        val product = cartItem.product

        holder.productImage.setImageResource(product.imageResId)
        holder.productName.text = product.name
        holder.productPrice.text = "R${String.format("%.2f", product.price)}"
        holder.quantityText.text = cartItem.quantity.toString()

        // Decrease quantity
        holder.decreaseBtn.setOnClickListener {
            if (cartItem.quantity > 1) {
                cartItem.quantity--
                holder.quantityText.text = cartItem.quantity.toString()
                onCartUpdate()
            } else {
                // Remove item if quantity is 1
                val pos = holder.adapterPosition
                cartItems.removeAt(pos)
                notifyItemRemoved(pos)
                onCartUpdate()
            }
        }

        // Increase quantity
        holder.increaseBtn.setOnClickListener {
            cartItem.quantity++
            holder.quantityText.text = cartItem.quantity.toString()
            onCartUpdate()
        }

        // Remove item
        holder.removeBtn.setOnClickListener {
            val pos = holder.adapterPosition
            cartItems.removeAt(pos)
            notifyItemRemoved(pos)
            onCartUpdate()
        }
    }

    override fun getItemCount(): Int = cartItems.size
}