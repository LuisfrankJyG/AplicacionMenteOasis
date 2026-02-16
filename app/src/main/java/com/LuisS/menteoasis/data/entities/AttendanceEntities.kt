package com.LuisS.menteoasis.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val role: String, // Cargo: "Gerente", "Vendedor", etc.
    val isActive: Boolean = true
)

@Entity(tableName = "attendance_records")
data class AttendanceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val employeeId: Int, // Foreign Key to EmployeeEntity
    val timestamp: Long,
    val type: String // "ENTRADA" or "SALIDA"
)
