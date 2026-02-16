package com.LuisS.menteoasis.ui.features.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LuisS.menteoasis.data.MenteOasisRepository
import com.LuisS.menteoasis.data.entities.AttendanceRecordEntity
import com.LuisS.menteoasis.data.entities.EmployeeEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



data class AttendanceRecordWithEmployee(
    val record: AttendanceRecordEntity,
    val employeeName: String,
    val employeeRole: String
)

data class DailyAttendance(
    val date: String,
    val records: List<AttendanceRecordWithEmployee>,
    val totalHours: String? = null
)

data class AsistenciaUiState(
    val employees: List<EmployeeEntity> = emptyList(),
    val dailyRecords: List<DailyAttendance> = emptyList()
)

class AsistenciaViewModel(
    private val repository: MenteOasisRepository
) : ViewModel() {

    // Combine employees and records to show a rich history
    // For simplicity, we'll just fetch all employees and maybe filtered records?
    // Let's correct the Dao to return a Flow of records joined with employees or handle it in VM.
    // Dao currently returns Flow<List<AttendanceRecordEntity>> for a specific employee or range.
    // Let's start simple: Get employees to select, and get records for *today* or just *recent* globally?
    // The previous implementation showed a list. Let's show recent records for the Selected Employee or All?
    // Let's show All employees for selection, and All recent records.
    
    // We need a flow for ALL records to show the history list.
    // I didn't add "getAllRecords" to Repository/Dao. Let's add it or use "getRecordsByDateRange" with a wide range.
    
    val uiState: StateFlow<AsistenciaUiState> = combine(
        repository.allEmployees,
        repository.getRecordsByDateRange(System.currentTimeMillis() - 86400000 * 7, System.currentTimeMillis() + 86400000)
    ) { employees, records ->
        val enrichedRecords = records.map { record ->
            val employee = employees.find { it.id == record.employeeId }
            AttendanceRecordWithEmployee(
                record = record,
                employeeName = employee?.name ?: "Unknown",
                employeeRole = employee?.role ?: ""
            )
        }

        // Group by day
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val grouped = enrichedRecords.groupBy { record: AttendanceRecordWithEmployee -> 
            sdf.format(Date(record.record.timestamp))
        }.map { (date, dailyList) ->
            // Simple hour calc for same-employee pairs in one day
            // This is basic; a more robust one would match exact pairs
            val totalMillis = calculateTotalMillis(dailyList)
            val hoursStr = if (totalMillis > 0) {
                val hours = totalMillis / 3600000
                val mins = (totalMillis % 3600000) / 60000
                String.format("%dh %dm", hours, mins)
            } else null

            DailyAttendance(date, dailyList, hoursStr)
        }.sortedByDescending { it.records.firstOrNull()?.record?.timestamp ?: 0L }

        AsistenciaUiState(employees, grouped)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AsistenciaUiState()
    )

    private fun calculateTotalMillis(records: List<AttendanceRecordWithEmployee>): Long {
        var total = 0L
        val employeeRecords = records.groupBy { it.record.employeeId }
        employeeRecords.forEach { (_, logs) ->
            val sortedLogs = logs.sortedBy { it.record.timestamp }
            var entryTime: Long? = null
            sortedLogs.forEach { log ->
                if (log.record.type == "ENTRADA") {
                    entryTime = log.record.timestamp
                } else if (log.record.type == "SALIDA" && entryTime != null) {
                    total += (log.record.timestamp - entryTime!!)
                    entryTime = null
                }
            }
        }
        return total
    }

    fun addEmployee(name: String, role: String) {
        viewModelScope.launch {
            repository.insertEmployee(EmployeeEntity(name = name, role = role))
        }
    }

    fun addRecord(employeeId: Int, type: RecordType) {
        viewModelScope.launch {
            repository.insertRecord(
                AttendanceRecordEntity(
                    employeeId = employeeId,
                    timestamp = System.currentTimeMillis(),
                    type = type.name
                )
            )
        }
    }
}