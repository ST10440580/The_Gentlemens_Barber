package com.example.thegentlemensbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CardAdapter(
    private val cards: MutableList<CardModel>,
    private val onDelete: (CardModel) -> Unit
) : RecyclerView.Adapter<CardAdapter.CardVH>() {

    inner class CardVH(v: View) : RecyclerView.ViewHolder(v) {
        val brand: TextView = v.findViewById(R.id.itemCardBrand)
        val number: TextView = v.findViewById(R.id.itemCardNumber)
        val holder: TextView = v.findViewById(R.id.itemCardHolder)
        val expiry: TextView = v.findViewById(R.id.itemCardExpiry)
        val deleteBtn: ImageButton = v.findViewById(R.id.deleteCardBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardVH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return CardVH(v)
    }

    override fun onBindViewHolder(holder: CardVH, position: Int) {
        val card = cards[position]
        holder.brand.text = card.brand
        // Mask card number except last 4
        val masked = if (card.cardNumber.length >= 4) {
            "**** **** **** ${card.cardNumber.takeLast(4)}"
        } else {
            card.cardNumber
        }
        holder.number.text = masked
        holder.holder.text = card.holderName.uppercase()
        holder.expiry.text = card.expiry
        
        holder.deleteBtn.setOnClickListener { onDelete(card) }
    }

    override fun getItemCount() = cards.size
}
