package com.oedinn.fleetify.service

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    // Fungsi untuk memformat tanggal sesuai kebutuhan
    fun formatDate(dateString: String): String? {
        return try {
            // Format asli dari API (sesuaikan dengan format yang diterima dari API)
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

            // Format yang diinginkan: "Senin, 1 Jan - 10:30"
            val desiredFormat = SimpleDateFormat("EEEE, d MMM - HH:mm", Locale("id", "ID"))

            // Parse string ke dalam Date object
            val date: Date? = originalFormat.parse(dateString)

            // Format kembali ke dalam string sesuai format yang diinginkan
            date?.let { desiredFormat.format(it) }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
