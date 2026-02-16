package com.LuisS.menteoasis.data

import com.LuisS.menteoasis.data.dao.AttendanceDao
import com.LuisS.menteoasis.data.dao.BirthdayDao
import com.LuisS.menteoasis.data.entities.*
import kotlinx.coroutines.flow.Flow

class MenteOasisRepository(
    private val attendanceDao: AttendanceDao,
    private val birthdayDao: BirthdayDao
) {
    // --- Attendance ---
    val allEmployees: Flow<List<EmployeeEntity>> = attendanceDao.getAllEmployees()
    
    suspend fun insertEmployee(employee: EmployeeEntity) = attendanceDao.insertEmployee(employee)
    suspend fun insertRecord(record: AttendanceRecordEntity) = attendanceDao.insertRecord(record)
    fun getRecordsForEmployee(employeeId: Int) = attendanceDao.getRecordsForEmployee(employeeId)
    fun getRecordsByDateRange(start: Long, end: Long) = attendanceDao.getRecordsByDateRange(start, end)
    fun getAllRecords() = attendanceDao.getAllRecords()

    // --- Birthdays ---
    val allBirthdays: Flow<List<BirthdayEntity>> = birthdayDao.getAllBirthdays()
    suspend fun insertBirthday(birthday: BirthdayEntity) = birthdayDao.insertBirthday(birthday)
    suspend fun deleteBirthday(birthday: BirthdayEntity) = birthdayDao.deleteBirthday(birthday)
}
