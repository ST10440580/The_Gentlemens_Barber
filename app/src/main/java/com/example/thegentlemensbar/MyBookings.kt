package com.example.thegentlemensbar

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MyBookings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_bookings)

        val recycler = findViewById<RecyclerView>(R.id.bookingsRecyclerView)
        recycler.layoutManager = LinearLayoutManager(this)

        FirebaseRepo.getMyBookings { list ->
            if (list.isEmpty()) Toast.makeText(this, "No bookings yet", Toast.LENGTH_SHORT).show()
            recycler.adapter = BookingsAdapter(list.toMutableList(), this)
        }
    }
}