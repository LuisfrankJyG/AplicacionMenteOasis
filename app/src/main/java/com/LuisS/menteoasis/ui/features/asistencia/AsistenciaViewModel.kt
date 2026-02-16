package com.LuisS.menteoasis.ui.features.asistencia

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.LuisS.menteoasis.data.MenteOasisRepository
import com.LuisS.menteoasis.data.entities.AttendanceRecordEntity
import com.LuisS.menteoasis.data.entities.EmployeeEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
    val dailyRecords: List<DailyAttendance> = emptyList(),
    val totalPeriodHours: String = "0h 0m",
    val selectedEmployeeId: Int? = null,
    val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR)
)

class AsistenciaViewModel(
    private val repository: MenteOasisRepository
) : ViewModel() {

    private val _selectedEmployeeId = MutableStateFlow<Int?>(null)
    private val _selectedMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH))
    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    val uiState: StateFlow<AsistenciaUiState> = combine(
        repository.allEmployees,
        repository.getAllRecords(),
        _selectedEmployeeId,
        _selectedMonth,
        _selectedYear
    ) { employees, allRecords, selEmployee, selMonth, selYear ->
        
        // 1. Calculate TOTAL HOURS for the selected period (selMonth/selYear)
        val targetEmployees = if (selEmployee == null) employees else employees.filter { it.id == selEmployee }
        var totalPeriodMillis = 0L
        val now = System.currentTimeMillis()

        targetEmployees.forEach { employee ->
            val empRecords = allRecords.filter { it.employeeId == employee.id }.sortedBy { it.timestamp }
            var lastEntryTime: Long? = null
            
            empRecords.forEach { record ->
                if (record.type == "ENTRADA") {
                    lastEntryTime = record.timestamp
                } else if (record.type == "SALIDA" && lastEntryTime != null) {
                    val duration = record.timestamp - lastEntryTime!!
                    
                    val cal = Calendar.getInstance()
                    cal.timeInMillis = record.timestamp
                    if (cal.get(Calendar.MONTH) == selMonth && cal.get(Calendar.YEAR) == selYear) {
                        totalPeriodMillis += duration
                    }
                    lastEntryTime = null
                }
            }
            
            // If they are STILL clocked in, add the active time to the current month if applicable
            if (lastEntryTime != null) {
                val calNow = Calendar.getInstance()
                if (calNow.get(Calendar.MONTH) == selMonth && calNow.get(Calendar.YEAR) == selYear) {
                    totalPeriodMillis += (now - lastEntryTime!!)
                }
            }
        }

        // 2. Prepare DISPLAY records for the timeline (filtered by Month/Year/Employee)
        val cal = Calendar.getInstance()
        val filteredRecords = allRecords.filter { record ->
            cal.timeInMillis = record.timestamp
            val matchMonth = cal.get(Calendar.MONTH) == selMonth
            val matchYear = cal.get(Calendar.YEAR) == selYear
            val matchEmployee = selEmployee == null || record.employeeId == selEmployee
            matchMonth && matchYear && matchEmployee
        }

        val enrichedRecords = filteredRecords.map { record ->
            val employee = employees.find { it.id == record.employeeId }
            AttendanceRecordWithEmployee(
                record = record,
                employeeName = employee?.name ?: "Desconocido",
                employeeRole = employee?.role ?: ""
            )
        }

        // Group by day for the timeline
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
        val groupedByDay = enrichedRecords.groupBy { record: AttendanceRecordWithEmployee -> 
            sdf.format(Date(record.record.timestamp))
        }.map { (date, dailyList) ->
            // For daily total, we use the matched duration logic too
            val dayTotal = calculateDailyMatchedTotal(dailyList, allRecords, now)
            DailyAttendance(date, dailyList, formatMillis(dayTotal))
        }

        AsistenciaUiState(
            employees = employees,
            dailyRecords = groupedByDay.sortedByDescending { 
                it.records.firstOrNull()?.record?.timestamp ?: 0L
            },
            totalPeriodHours = formatMillis(totalPeriodMillis) ?: "0h 0m",
            selectedEmployeeId = selEmployee,
            selectedMonth = selMonth,
            selectedYear = selYear
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AsistenciaUiState()
    )

    private fun calculateDailyMatchedTotal(
        dailyRecords: List<AttendanceRecordWithEmployee>, 
        allRecords: List<AttendanceRecordEntity>,
        now: Long
    ): Long {
        var dayTotal = 0L
        
        dailyRecords.forEach { item ->
            if (item.record.type == "SALIDA") {
                // Find matching entry (the most recent one before this exit)
                val matchingEntry = allRecords.filter { 
                    it.employeeId == item.record.employeeId && 
                    it.type == "ENTRADA" && 
                    it.timestamp < item.record.timestamp 
                }.maxByOrNull { it.timestamp }
                
                if (matchingEntry != null) {
                    dayTotal += (item.record.timestamp - matchingEntry.timestamp)
                }
            } else if (item.record.type == "ENTRADA") {
                // Check if this specific ENTRADA is still open (no SALIDA after it for this employee)
                val hasSubsequentSalida = allRecords.any { 
                    it.employeeId == item.record.employeeId && 
                    it.type == "SALIDA" && 
                    it.timestamp > item.record.timestamp 
                }
                
                if (!hasSubsequentSalida) {
                    dayTotal += (now - item.record.timestamp)
                }
            }
        }
        return dayTotal
    }

    private fun formatMillis(millis: Long): String? {
        if (millis <= 0) return null
        val hours = millis / 3600000
        val mins = (millis % 3600000) / 60000
        // Show units properly even if small
        return if (hours > 0 || mins > 0) String.format("%dh %dm", hours, mins) else "0h 0m"
    }

    fun setEmployeeFilter(employeeId: Int?) {
        _selectedEmployeeId.value = employeeId
    }

    fun setMonthFilter(month: Int) {
        _selectedMonth.value = month
    }

    fun setYearFilter(year: Int) {
        _selectedYear.value = year
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
