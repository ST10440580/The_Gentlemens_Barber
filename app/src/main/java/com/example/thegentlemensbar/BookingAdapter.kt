package com.example.thegentlemensbar

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class BookingsAdapter(
    private val bookings: MutableList<BookingModel>,
    private val context: Context
) : RecyclerView.Adapter<BookingsAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val icon: ImageView = itemView.findViewById(R.id.bookingIcon)
        val serviceName: TextView = itemView.findViewById(R.id.bookingService)
        val dateTime: TextView = itemView.findViewById(R.id.bookingDateTime)
        val manageBtn: Button = itemView.findViewById(R.id.manageBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false))

    override fun onBindViewHolder(holder: VH, position: Int) {
        val b = bookings[position]
        holder.icon.setImageResource(serviceIcon(b.serviceName))
        holder.serviceName.text = b.serviceName
        holder.dateTime.text =
            "${b.date} at ${b.time}\nwith ${b.staffName} • ${b.status.replace("_", " ").replaceFirstChar { it.uppercase() }}"

        val canCancel = b.status == "pending" || b.status == "confirmed"
        holder.manageBtn.visibility = if (canCancel) View.VISIBLE else View.GONE

        holder.manageBtn.setOnClickListener {
            // Changed bindingAdapterPosition to adapterPosition to resolve the compilation error
            val pos = holder.adapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            FirebaseRepo.cancelBooking(bookings[pos]) { ok ->
                if (ok) {
                    bookings[pos] = bookings[pos].copy(status = "cancelled")
                    notifyItemChanged(pos)
                    Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error cancelling booking", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun getItemCount() = bookings.size
}
