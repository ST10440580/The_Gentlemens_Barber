package com.example.thegentlemensbar

import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors


object HolidayApi {
    private val cache = mutableMapOf<Int, Map<String, String>>()
    private val executor = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())

    // Returns a map of "yyyy-MM-dd" as well as the holiday name.
    fun getHolidays(year: Int, onResult: (Map<String, String>) -> Unit) {
        cache[year]?.let { onResult(it); return }

        executor.execute {
            val result: Map<String, String>? = try {
                val conn = URL("https://date.nager.at/api/v3/PublicHolidays/$year/ZA")
                    .openConnection() as HttpURLConnection
                conn.connectTimeout = 8000
                conn.readTimeout = 8000
                val text = try {
                    conn.inputStream.bufferedReader().use { it.readText() }
                } finally {
                    conn.disconnect()
                }
                val arr = JSONArray(text)
                val map = mutableMapOf<String, String>()
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    map[o.getString("date")] = o.getString("name")
                }
                map
            } catch (e: Exception) {
                Log.w("HolidayApi", "Holiday lookup failed", e)
                null
            }
            main.post {
                if (result != null) {
                    cache[year] = result
                    onResult(result)
                } else {
                    onResult(emptyMap()) // fail open: booking still works without the API
                }
            }
        }
    }
}