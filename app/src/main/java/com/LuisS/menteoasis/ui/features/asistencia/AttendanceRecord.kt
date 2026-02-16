package com.LuisS.menteoasis.ui.features.asistencia

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class RecordType(val displayName: String) {
    ENTRADA("Entrada"),
    SALIDA("Salida")
}

data class AttendanceRecord(
    val id: Int,
    val nombre: String,
    val carrera: String,
    val timestamp: Long,
    val type: RecordType
) {
    fun getFormattedTimestamp(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}