package com.example.thegentlemensbar

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import java.util.Locale

class Booking : AppCompatActivity() {

    private lateinit var serviceSpinner: Spinner
    private lateinit var staffSpinner: Spinner
    private lateinit var dateBtn: Button
    private lateinit var timeBtn: Button
    private lateinit var confirmBtn: Button
    private lateinit var infoText: TextView

    private var services = listOf<ServiceModel>()
    private var staffList = listOf<StaffModel>()
    private var selectedDate = ""
    private var selectedTime = ""
    private var holidays = mapOf<String, String>()
    private var walkIn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booking)

        walkIn = intent.getBooleanExtra("WALK_IN", false)

        serviceSpinner = findViewById(R.id.serviceSpinner)
        staffSpinner = findViewById(R.id.staffSpinner)
        dateBtn = findViewById(R.id.dateBtn)
        timeBtn = findViewById(R.id.timeBtn)
        confirmBtn = findViewById(R.id.confirmBtn)
        infoText = findViewById(R.id.infoText)

        FirebaseRepo.getServices { list ->
            services = list
            serviceSpinner.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item,
                list.map { "${it.name} - R${it.price.toInt()} (${it.durationMins} min)" }
            )
            if (list.isEmpty()) Toast.makeText(this, "No services found", Toast.LENGTH_LONG).show()
        }

        FirebaseRepo.getStaff { list ->
            staffList = list
            staffSpinner.adapter = ArrayAdapter(
                this, android.R.layout.simple_spinner_dropdown_item, list.map { it.name }
            )
        }

        val resetTime = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                selectedTime = ""
                timeBtn.text = "Select Time"
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        serviceSpinner.onItemSelectedListener = resetTime
        staffSpinner.onItemSelectedListener = resetTime

        dateBtn.setOnClickListener { pickDate() }
        timeBtn.setOnClickListener { pickTime() }
        confirmBtn.setOnClickListener { confirm() }
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        val dlg = DatePickerDialog(this, { _, y, m, d ->
            selectedDate = String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d)
            dateBtn.text = selectedDate
            selectedTime = ""
            timeBtn.text = "Select Time"
            infoText.text = ""
            HolidayApi.getHolidays(y) { h ->
                holidays = holidays + h
                holidays[selectedDate]?.let { infoText.text = "Closed: $it (public holiday)" }
            }
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))

        if (!walkIn) dlg.datePicker.minDate = System.currentTimeMillis() - 1000
        dlg.datePicker.maxDate = System.currentTimeMillis() + 60L * 24 * 60 * 60 * 1000
        dlg.show()
    }

    private fun currentService() = services.getOrNull(serviceSpinner.selectedItemPosition)
    private fun currentStaff() = staffList.getOrNull(staffSpinner.selectedItemPosition)

    private fun pickTime() {
        val service = currentService()
        val staff = currentStaff()
        if (service == null || staff == null || selectedDate.isEmpty()) {
            Toast.makeText(this, "Choose service, barber and date first", Toast.LENGTH_SHORT).show()
            return
        }
        val year = selectedDate.substring(0, 4).toInt()
        HolidayApi.getHolidays(year) { h ->
            holidays = holidays + h
            FirebaseRepo.getBookedSlots(staff.id, selectedDate) { booked ->
                val free = Slots.freeSlots(
                    selectedDate, staff, service, booked, holidays,
                    leadMins = if (walkIn) -30 else 30
                )
                if (free.isEmpty()) {
                    Toast.makeText(this, "No free times that day. Try another date.", Toast.LENGTH_LONG).show()
                    return@getBookedSlots
                }
                AlertDialog.Builder(this)
                    .setTitle("Available times")
                    .setItems(free.toTypedArray()) { _, i ->
                        selectedTime = free[i]
                        timeBtn.text = selectedTime
                    }
                    .show()
            }
        }
    }

    private fun confirm() {
        if (currentService() == null || currentStaff() == null ||
            selectedDate.isEmpty() || selectedTime.isEmpty()
        ) {
            Toast.makeText(this, "Please select service, barber, date and time", Toast.LENGTH_SHORT).show()
            return
        }
        if (walkIn) {
            val input = EditText(this).apply { hint = "Customer name" }
            AlertDialog.Builder(this)
                .setTitle("Walk-in customer")
                .setView(input)
                .setPositiveButton("Book") { _, _ ->
                    val name = input.text.toString().trim().ifEmpty { "Walk-in" }
                    save(name, "walk-in", "confirmed")
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            val name = getSharedPreferences("UserPref", MODE_PRIVATE).getString("USERNAME", "Customer") ?: "Customer"
            save(name, FirebaseRepo.currentUid ?: "", "pending")
        }
    }

    private fun save(customerName: String, customerId: String, status: String) {
        val service = currentService()!!
        val staff = currentStaff()!!
        confirmBtn.isEnabled = false
        FirebaseRepo.createBooking(customerId, customerName, staff, service, selectedDate, selectedTime, status) { ok, err ->
            confirmBtn.isEnabled = true
            if (ok) {
                Toast.makeText(
                    this,
                    "Booked ${service.name} on $selectedDate at $selectedTime. Pay R${service.price.toInt()} in store.",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            } else {
                Toast.makeText(this, err ?: "Error saving booking", Toast.LENGTH_LONG).show()
            }
        }
    }
}