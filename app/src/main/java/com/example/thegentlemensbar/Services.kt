package com.example.thegentlemensbar

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.SimpleAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class Services : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_services)


        val services = listOf(
            Service("Haircut", "30 Mins", "Classic or modern styles", "R150", R.drawable.haircut),
            Service("Beard Trim", "30 Mins", "Sharp lines and tidy finish", "R80", R.drawable.shave),
            Service("Hot Towel Shave", "30 Mins", "Traditional shave experience", "R120", R.drawable.hottowel_shave),
            Service("Facial", "45 Mins", "Relaxing skin treatment", "R200", R.drawable.facial)
        )

        val recyclerView = findViewById<RecyclerView>(R.id.servicesRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ServiceAdapter(services)

    }
}
