package com.example.thegentlemensbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class ProductAdapter(
    private var products: List<Product>,
    private val onProductClick: (Product) -> Unit
) : RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productImage: ImageView = itemView.findViewById(R.id.productImage)
        val productName: TextView = itemView.findViewById(R.id.productName)
        val productRating: TextView = itemView.findViewById(R.id.productRating)
        val productStock: TextView = itemView.findViewById(R.id.productStock)
        val productPrice: TextView = itemView.findViewById(R.id.productPrice)
        val addToCartBtn: Button = itemView.findViewById(R.id.addToCartBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]

        holder.productImage.setImageResource(product.imageResId)
        holder.productName.text = product.name
        holder.productRating.text = "⭐ ${product.rating}"
        holder.productStock.text = if (product.inStock) "● In Stock" else "● Out of Stock"
        holder.productStock.setTextColor(
            if (product.inStock)
                holder.itemView.context.getColor(R.color.green)
            else
                holder.itemView.context.getColor(R.color.red)
        )
        holder.productPrice.text = "R${String.format("%.2f", product.price)}"

        holder.addToCartBtn.setOnClickListener {
            Products.addToCart(product)
            Toast.makeText(holder.itemView.context, "${product.name} added to cart", Toast.LENGTH_SHORT).show()
        }

        holder.itemView.setOnClickListener {
            onProductClick(product)
        }
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
    }
}
