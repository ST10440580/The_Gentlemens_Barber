package com.example.thegentlemensbar

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ServiceAdapter(private val services: List<Service>) :
    RecyclerView.Adapter<ServiceAdapter.ServiceViewHolder>() {

    inner class ServiceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val serviceImage: ImageView = itemView.findViewById(R.id.serviceImage)
        val serviceName: TextView = itemView.findViewById(R.id.serviceName)
        val serviceDuration: TextView = itemView.findViewById(R.id.serviceDuration)
        val serviceDescription: TextView = itemView.findViewById(R.id.serviceDescription)
        val servicePrice: TextView = itemView.findViewById(R.id.servicePrice)
        val bookBtn: Button = itemView.findViewById(R.id.bookBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service, parent, false)
        return ServiceViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        val service = services[position]
        holder.serviceImage.setImageResource(service.imageResId)
        holder.serviceName.text = service.name
        holder.serviceDuration.text = service.duration
        holder.serviceDescription.text = service.description
        holder.servicePrice.text = service.price

        holder.bookBtn.setOnClickListener {
            val intent = Intent(holder.itemView.context, Booking::class.java)
            intent.putExtra("serviceName", service.name)
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = services.size
}
