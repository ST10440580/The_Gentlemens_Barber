package com.example.thegentlemensbar

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper



data class Payment(
    val id: Int,
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String,
    val amount: Double
)

class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "barber_app.db"
        private const val DATABASE_VERSION = 1

        // Tables
        const val TABLE_BOOKINGS = "bookings"
        const val TABLE_PAYMENTS = "payments"
        const val TABLE_USERS = "users"

        // Booking columns
        const val COL_BOOKING_ID = "id"
        const val COL_SERVICE_NAME = "serviceName"
        const val COL_DATE_TIME = "dateTime"

        // Payment columns
        const val COL_PAYMENT_ID = "id"
        const val COL_CARD_NUMBER = "cardNumber"
        const val COL_EXPIRY_DATE = "expiryDate"
        const val COL_CVV = "cvv"
        const val COL_AMOUNT = "amount"

        // User columns
        const val COL_USER_ID = "id"
        const val COL_FULLNAME = "fullname"
        const val COL_EMAIL = "email"
        const val COL_PHONE = "phone"
        const val COL_PASSWORD = "password"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createBookings = """
            CREATE TABLE $TABLE_BOOKINGS (
                $COL_BOOKING_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_SERVICE_NAME TEXT,
                $COL_DATE_TIME TEXT
            )
        """.trimIndent()




        val createPayments = """
            CREATE TABLE $TABLE_PAYMENTS (
                $COL_PAYMENT_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_CARD_NUMBER TEXT,
                $COL_EXPIRY_DATE TEXT,
                $COL_CVV TEXT,
                $COL_AMOUNT REAL
            )
        """.trimIndent()

        val createUsers = """
            CREATE TABLE $TABLE_USERS (
                $COL_USER_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_FULLNAME TEXT,
                $COL_EMAIL TEXT UNIQUE,
                $COL_PHONE TEXT,
                $COL_PASSWORD TEXT
            )
        """.trimIndent()

        db.execSQL(createBookings)
        db.execSQL(createPayments)
        db.execSQL(createUsers)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_BOOKINGS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PAYMENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    // ------------------- BOOKINGS -------------------
    fun addBooking(serviceName: String, dateTime: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_SERVICE_NAME, serviceName)
            put(COL_DATE_TIME, dateTime)
        }
        val id = db.insert(TABLE_BOOKINGS, null, values)
        db.close()
        return id
    }

    fun getAllBookings(): List<Bookings> {
        val bookings1 = mutableListOf<Bookings>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_BOOKINGS", null)

        if (cursor.moveToFirst()) {
            do {
                bookings1.add(
                    Bookings(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_BOOKING_ID)),
                        serviceName = cursor.getString(cursor.getColumnIndexOrThrow(COL_SERVICE_NAME)),
                        dateTime = cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE_TIME)),
                        iconResId = when (cursor.getString(cursor.getColumnIndexOrThrow(COL_SERVICE_NAME))) {
                            "Haircut" -> R.drawable.haircut
                            "Shave" -> R.drawable.hottowel_shave
                            "Beard Trim" -> R.drawable.shave
                            "Facial" -> R.drawable.facial
                            else -> R.drawable.ic_booking
                        }
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return bookings1
    }


    fun deleteBooking(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_BOOKINGS, "$COL_BOOKING_ID=?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    // ------------------- USERS -------------------
    fun addUser(fullName: String, email: String, phone: String, password: String): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_FULLNAME, fullName)
            put(COL_EMAIL, email)
            put(COL_PHONE, phone)
            put(COL_PASSWORD, password)
        }
        val id = db.insert(TABLE_USERS, null, values)
        db.close()
        return id
    }

    fun checkUser(email: String, password: String): Boolean {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_USERS WHERE $COL_EMAIL=? AND $COL_PASSWORD=?",
            arrayOf(email, password)
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
    }

    fun getFullName(email: String): String {
        val db = readableDatabase
        val cursor = db.rawQuery(
            "SELECT $COL_FULLNAME FROM $TABLE_USERS WHERE $COL_EMAIL=?",
            arrayOf(email)
        )
        var name = ""
        if (cursor.moveToFirst()) {
            name = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return name
    }

    // ------------------- PAYMENTS -------------------
    fun addPayment(cardNumber: String, expiryDate: String, cvv: String, amount: Double): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CARD_NUMBER, cardNumber)
            put(COL_EXPIRY_DATE, expiryDate)
            put(COL_CVV, cvv)
            put(COL_AMOUNT, amount)
        }
        val id = db.insert(TABLE_PAYMENTS, null, values)
        db.close()
        return id
    }

    fun getAllPayments(): List<Payment> {
        val payments = mutableListOf<Payment>()
        val db = readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM $TABLE_PAYMENTS", null)

        if (cursor.moveToFirst()) {
            do {
                payments.add(
                    Payment(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_PAYMENT_ID)),
                        cardNumber = cursor.getString(cursor.getColumnIndexOrThrow(COL_CARD_NUMBER)),
                        expiryDate = cursor.getString(cursor.getColumnIndexOrThrow(COL_EXPIRY_DATE)),
                        cvv = cursor.getString(cursor.getColumnIndexOrThrow(COL_CVV)),
                        amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_AMOUNT))
                    )
                )
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return payments
    }

    fun getTotalSpend(): Double {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT SUM($COL_AMOUNT) FROM $TABLE_PAYMENTS", null)
        var total = 0.0
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return total
    }

    fun deletePayment(id: Int): Int {
        val db = writableDatabase
        val rows = db.delete(TABLE_PAYMENTS, "$COL_PAYMENT_ID=?", arrayOf(id.toString()))
        db.close()
        return rows
    }

    fun addBookingWithPayment(serviceName: String, dateTime: String, price: Double): Long {
        val db = writableDatabase

        // Insert booking
        val bookingValues = ContentValues().apply {
            put(COL_SERVICE_NAME, serviceName)
            put(COL_DATE_TIME, dateTime)
        }
        val bookingId = db.insert(TABLE_BOOKINGS, null, bookingValues)

        // Insert payment (dummy card details for now, or you can pass real ones later)
        val paymentValues = ContentValues().apply {
            put(COL_CARD_NUMBER, "N/A")
            put(COL_EXPIRY_DATE, "N/A")
            put(COL_CVV, "N/A")
            put(COL_AMOUNT, price)
        }
        db.insert(TABLE_PAYMENTS, null, paymentValues)

        db.close()
        return bookingId
    }

}
