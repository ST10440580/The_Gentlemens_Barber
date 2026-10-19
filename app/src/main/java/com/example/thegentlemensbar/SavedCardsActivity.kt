package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class SavedCardsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CardAdapter
    private lateinit var noCardsText: TextView
    private val cardsList = mutableListOf<CardModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_cards)

        recyclerView = findViewById(R.id.cardsRecyclerView)
        noCardsText = findViewById(R.id.noCardsText)
        val addBtn = findViewById<FloatingActionButton>(R.id.addCardFab)

        adapter = CardAdapter(cardsList) { card ->
            confirmDelete(card)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        addBtn.setOnClickListener {
            startActivity(Intent(this, AddCardActivity::class.java))
        }

        loadCards()
    }

    override fun onResume() {
        super.onResume()
        loadCards()
    }

    private fun loadCards() {
        FirebaseRepo.getSavedCards { list ->
            cardsList.clear()
            cardsList.addAll(list)
            adapter.notifyDataSetChanged()
            noCardsText.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun confirmDelete(card: CardModel) {
        AlertDialog.Builder(this)
            .setTitle("Delete Card")
            .setMessage("Are you sure you want to remove this card?")
            .setPositiveButton("Delete") { _, _ ->
                FirebaseRepo.deleteCard(card.id) { success ->
                    if (success) {
                        loadCards()
                        Toast.makeText(this, "Card deleted", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
