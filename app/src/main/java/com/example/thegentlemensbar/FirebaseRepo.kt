package com.example.thegentlemensbar

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.Query

object FirebaseRepo {
    private const val TAG = "FirebaseRepo"
    private val auth get() = FirebaseAuth.getInstance()
    private val db get() = FirebaseFirestore.getInstance()

    val currentUid: String? get() = auth.currentUser?.uid

    // ------------------------------------------------------------ AUTH
    fun register(
        fullName: String, email: String, phone: String, password: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user!!.uid
                val profile = hashMapOf(
                    "fullName" to fullName,
                    "email" to email,
                    "phone" to phone,
                    "role" to "customer",
                    "createdAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).set(profile)
                    .addOnSuccessListener { onResult(true, null) }
                    .addOnFailureListener { onResult(false, it.message) }
            }
            .addOnFailureListener { onResult(false, it.message) }
    }

    fun login(email: String, password: String, onResult: (Boolean, UserProfile?, String?) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                loadProfile { p ->
                    if (p != null) onResult(true, p, null)
                    else onResult(false, null, "Profile not found. Contact the shop.")
                }
            }
            .addOnFailureListener { e ->
                val msg = if (e is FirebaseAuthInvalidCredentialsException ||
                    e is FirebaseAuthInvalidUserException) "Invalid email or password" else e.message
                onResult(false, null, msg)
            }
    }

    fun loadProfile(onResult: (UserProfile?) -> Unit) {
        val uid = currentUid ?: return onResult(null)
        db.collection("users").document(uid).get()
            .addOnSuccessListener { d ->
                if (d.exists()) onResult(
                    UserProfile(uid, d.getString("fullName") ?: "", d.getString("role") ?: "customer")
                ) else onResult(null)
            }
            .addOnFailureListener { Log.e(TAG, "loadProfile", it); onResult(null) }
    }

    fun logout() = auth.signOut()

    // -------------------------------------------------------- SERVICES / STAFF
    private fun DocumentSnapshot.toService() = ServiceModel(
        id = id,
        name = getString("name") ?: "",
        description = getString("description") ?: "",
        durationMins = (getLong("durationMins") ?: 30L).toInt(),
        price = getDouble("price") ?: 0.0,
        imageKey = getString("imageKey") ?: "",
        active = getBoolean("active") ?: true
    )

    private fun DocumentSnapshot.toStaff(): StaffModel {
        val hours = mutableMapOf<String, Map<String, String>>()
        (get("workingHours") as? Map<*, *>)?.forEach { (k, v) ->
            val m = v as? Map<*, *>
            val s = m?.get("start") as? String
            val e = m?.get("end") as? String
            if (k is String && s != null && e != null) hours[k] = mapOf("start" to s, "end" to e)
        }
        val off = (get("daysOff") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
        return StaffModel(
            id = id, uid = getString("uid") ?: "", name = getString("name") ?: "",
            title = getString("title") ?: "", workingHours = hours, daysOff = off,
            active = getBoolean("active") ?: true
        )
    }

    fun getServices(onResult: (List<ServiceModel>) -> Unit) {
        db.collection("services").whereEqualTo("active", true).get()
            .addOnSuccessListener { s -> onResult(s.documents.map { it.toService() }.sortedBy { it.price }) }
            .addOnFailureListener { Log.e(TAG, "getServices", it); onResult(emptyList()) }
    }

    fun getStaff(onResult: (List<StaffModel>) -> Unit) {
        db.collection("staff").whereEqualTo("active", true).get()
            .addOnSuccessListener { s -> onResult(s.documents.map { it.toStaff() }.sortedBy { it.name }) }
            .addOnFailureListener { Log.e(TAG, "getStaff", it); onResult(emptyList()) }
    }

    fun getStaffByUid(uid: String, onResult: (StaffModel?) -> Unit) {
        db.collection("staff").whereEqualTo("uid", uid).limit(1).get()
            .addOnSuccessListener { s -> onResult(s.documents.firstOrNull()?.toStaff()) }
            .addOnFailureListener { Log.e(TAG, "getStaffByUid", it); onResult(null) }
    }

    fun addDayOff(staffId: String, date: String, onResult: (Boolean) -> Unit) {
        db.collection("staff").document(staffId).update("daysOff", FieldValue.arrayUnion(date))
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.e(TAG, "addDayOff", it); onResult(false) }
    }

    // ------------------------------------------------------------- BOOKINGS
    private fun DocumentSnapshot.toBooking() = BookingModel(
        id = id,
        customerId = getString("customerId") ?: "",
        customerName = getString("customerName") ?: "",
        staffId = getString("staffId") ?: "",
        staffName = getString("staffName") ?: "",
        serviceId = getString("serviceId") ?: "",
        serviceName = getString("serviceName") ?: "",
        durationMins = (getLong("durationMins") ?: 30L).toInt(),
        price = getDouble("price") ?: 0.0,
        date = getString("date") ?: "",
        time = getString("time") ?: "",
        startMillis = getLong("startMillis") ?: 0L,
        status = getString("status") ?: "pending",
        paymentStatus = getString("paymentStatus") ?: "unpaid",
        slotKeys = (get("slotKeys") as? List<*>)?.filterIsInstance<String>() ?: emptyList()
    )

    /** Taken 30-min slots ("HH:mm") for one barber on one date. */
    fun getBookedSlots(staffId: String, date: String, onResult: (Set<String>) -> Unit) {
        db.collection("slotLocks")
            .whereEqualTo("staffId", staffId)
            .whereEqualTo("date", date)
            .get()
            .addOnSuccessListener { s -> onResult(s.documents.mapNotNull { it.getString("time") }.toSet()) }
            .addOnFailureListener { Log.e(TAG, "getBookedSlots", it); onResult(emptySet()) }
    }

    /**
     * Creates the booking AND locks every 30-min slot it covers in ONE transaction,
     * so two customers can never book the same time.
     */
    fun createBooking(
        customerId: String, customerName: String,
        staff: StaffModel, service: ServiceModel,
        date: String, time: String,
        status: String = "pending",
        onResult: (Boolean, String?) -> Unit
    ) {
        val slots = Slots.slotsNeeded(time, service.durationMins)
        val slotKeys = slots.map { "${staff.id}_${date}_${it.replace(":", "")}" }
        val bookingRef = db.collection("bookings").document()
        val lockRefs = slotKeys.map { db.collection("slotLocks").document(it) }

        db.runTransaction { tx ->
            for (ref in lockRefs) {
                if (tx.get(ref).exists()) {
                    throw FirebaseFirestoreException(
                        "SLOT_TAKEN", FirebaseFirestoreException.Code.ALREADY_EXISTS
                    )
                }
            }
            tx.set(bookingRef, hashMapOf(
                "customerId" to customerId,
                "customerName" to customerName,
                "staffId" to staff.id,
                "staffName" to staff.name,
                "serviceId" to service.id,
                "serviceName" to service.name,
                "durationMins" to service.durationMins,
                "price" to service.price,
                "date" to date,
                "time" to time,
                "startMillis" to Slots.parseMillis(date, time),
                "status" to status,
                "paymentStatus" to "unpaid",
                "slotKeys" to slotKeys,
                "createdAt" to FieldValue.serverTimestamp()
            ))
            slots.forEachIndexed { i, t ->
                tx.set(lockRefs[i], hashMapOf(
                    "staffId" to staff.id,
                    "date" to date,
                    "time" to t,
                    "bookingId" to bookingRef.id,
                    "customerId" to customerId
                ))
            }
            null
        }
            .addOnSuccessListener { onResult(true, null) }
            .addOnFailureListener { e ->
                val taken = (e as? FirebaseFirestoreException)?.code ==
                        FirebaseFirestoreException.Code.ALREADY_EXISTS
                onResult(false, if (taken) "That time was just taken. Please pick another slot." else e.message)
            }
    }

    fun getMyBookings(onResult: (List<BookingModel>) -> Unit) {
        val uid = currentUid ?: return onResult(emptyList())
        db.collection("bookings").whereEqualTo("customerId", uid).get()
            .addOnSuccessListener { s ->
                onResult(s.documents.map { it.toBooking() }.sortedByDescending { it.startMillis })
            }
            .addOnFailureListener { Log.e(TAG, "getMyBookings", it); onResult(emptyList()) }
    }

    // Staff view. staffId = null means "all barbers" (admin).
    fun getBookingsForDate(date: String, staffId: String?, onResult: (List<BookingModel>) -> Unit) {
        var q: Query = db.collection("bookings").whereEqualTo("date", date)
        if (staffId != null) q = q.whereEqualTo("staffId", staffId)
        q.get()
            .addOnSuccessListener { s -> onResult(s.documents.map { it.toBooking() }.sortedBy { it.time }) }
            .addOnFailureListener { Log.e(TAG, "getBookingsForDate", it); onResult(emptyList()) }
    }

    fun cancelBooking(b: BookingModel, onResult: (Boolean) -> Unit) {
        val batch = db.batch()
        batch.update(db.collection("bookings").document(b.id), "status", "cancelled")
        b.slotKeys.forEach { batch.delete(db.collection("slotLocks").document(it)) }
        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.e(TAG, "cancelBooking", it); onResult(false) }
    }

    fun setStatus(bookingId: String, status: String, onResult: (Boolean) -> Unit) {
        db.collection("bookings").document(bookingId).update("status", status)
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.e(TAG, "setStatus", it); onResult(false) }
    }

    /** Staff marks the cut as done and records how the customer paid. */
    fun completeBooking(b: BookingModel, method: String, onResult: (Boolean) -> Unit) {
        val batch = db.batch()
        batch.update(
            db.collection("bookings").document(b.id),
            mapOf("status" to "completed", "paymentStatus" to "paid")
        )
        batch.set(db.collection("payments").document(), hashMapOf(
            "bookingId" to b.id,
            "customerId" to b.customerId,
            "method" to method,
            "amount" to b.price,
            "status" to "paid",
            "type" to "booking",
            "paidAt" to FieldValue.serverTimestamp()
        ))
        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.e(TAG, "completeBooking", it); onResult(false) }
    }

    // ------------------------------------------------------ PAYMENTS / ORDERS
    fun getMyPayments(onResult: (List<PaymentModel>) -> Unit) {
        val uid = currentUid ?: return onResult(emptyList())
        db.collection("payments").whereEqualTo("customerId", uid).get()
            .addOnSuccessListener { s ->
                onResult(s.documents.map {
                    PaymentModel(
                        id = it.id,
                        method = it.getString("method") ?: "",
                        amount = it.getDouble("amount") ?: 0.0,
                        status = it.getString("status") ?: "",
                        type = it.getString("type") ?: "",
                        paidMillis = it.getTimestamp("paidAt")?.toDate()?.time ?: 0L
                    )
                })
            }
            .addOnFailureListener { Log.e(TAG, "getMyPayments", it); onResult(emptyList()) }
    }

    // Product purchase. method = "card" (demo, no real gateway) or "in_store"
    fun createOrder(items: List<CartItem>, total: Double, method: String, onResult: (Boolean) -> Unit) {
        val uid = currentUid ?: return onResult(false)
        val paid = method != "in_store"
        val orderRef = db.collection("orders").document()
        val lines = items.map {
            mapOf(
                "productId" to it.product.id,
                "name" to it.product.name,
                "qty" to it.quantity,
                "price" to it.product.price
            )
        }
        val batch = db.batch()
        batch.set(orderRef, hashMapOf(
            "customerId" to uid,
            "items" to lines,
            "total" to total,
            "paymentMethod" to method,
            "paymentStatus" to if (paid) "paid" else "unpaid",
            "status" to "pending",
            "createdAt" to FieldValue.serverTimestamp()
        ))
        if (paid) {
            batch.set(db.collection("payments").document(), hashMapOf(
                "orderId" to orderRef.id,
                "customerId" to uid,
                "method" to method,
                "amount" to total,
                "status" to "paid",
                "type" to "order",
                "paidAt" to FieldValue.serverTimestamp()
            ))
        }
        batch.commit()
            .addOnSuccessListener { onResult(true) }
            .addOnFailureListener { Log.e(TAG, "createOrder", it); onResult(false) }
    }
}