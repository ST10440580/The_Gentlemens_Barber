package com.example.thegentlemensbar

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale

class StaffBookingAdapter(
    private val items: List<BookingModel>,
    private val onManage: (BookingModel) -> Unit
) : RecyclerView.Adapter<StaffBookingAdapter.VH>() {

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        val time: TextView = v.findViewById(R.id.staffBookingTime)
        val customer: TextView = v.findViewById(R.id.staffBookingCustomer)
        val service: TextView = v.findViewById(R.id.staffBookingService)
        val status: TextView = v.findViewById(R.id.staffBookingStatus)
        val manage: Button = v.findViewById(R.id.staffManageBtn)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_staff_booking, parent, false))

    override fun onBindViewHolder(h: VH, position: Int) {
        val b = items[position]
        h.time.text = b.time
        h.customer.text = b.customerName
        h.service.text = String.format(Locale.US, "%s • R%.0f • %s", b.serviceName, b.price, b.staffName)
        h.status.text = b.status.replace("_", " ").replaceFirstChar { it.uppercase() } +
                if (b.paymentStatus == "paid") " • Paid" else ""
        h.status.setTextColor(
            Color.parseColor(
                when (b.status) {
                    "pending" -> "#E65100"
                    "confirmed" -> "#1565C0"
                    "completed" -> "#2E7D32"
                    else -> "#9E9E9E"
                }
            )
        )
        h.manage.setOnClickListener { onManage(b) }
    }

    override fun getItemCount() = items.size
}