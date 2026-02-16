package com.LuisS.menteoasis.data

import com.LuisS.menteoasis.data.dao.AttendanceDao
import com.LuisS.menteoasis.data.dao.BirthdayDao
import com.LuisS.menteoasis.data.dao.NoteDao
import com.LuisS.menteoasis.data.entities.*
import kotlinx.coroutines.flow.Flow

class MenteOasisRepository(
    private val noteDao: NoteDao,
    private val attendanceDao: AttendanceDao,
    private val birthdayDao: BirthdayDao
) {
    // --- Notes & Checklists ---
    val allNotes: Flow<List<NoteWithChecklist>> = noteDao.getAllNotes()
    val archivedNotes: Flow<List<NoteWithChecklist>> = noteDao.getArchivedNotes()

    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(note: NoteEntity) = noteDao.deleteNote(note)
    
    suspend fun insertChecklistItem(item: ChecklistItemEntity) = noteDao.insertChecklistItem(item)
    suspend fun updateChecklistItem(item: ChecklistItemEntity) = noteDao.updateChecklistItem(item)
    suspend fun deleteChecklistItem(item: ChecklistItemEntity) = noteDao.deleteChecklistItem(item)
    suspend fun deleteChecklistItemsForNote(noteId: Int) = noteDao.deleteChecklistItemsForNote(noteId)

    // --- Attendance ---
    val allEmployees: Flow<List<EmployeeEntity>> = attendanceDao.getAllEmployees()
    
    suspend fun insertEmployee(employee: EmployeeEntity) = attendanceDao.insertEmployee(employee)
    suspend fun insertRecord(record: AttendanceRecordEntity) = attendanceDao.insertRecord(record)
    fun getRecordsForEmployee(employeeId: Int) = attendanceDao.getRecordsForEmployee(employeeId)
    fun getRecordsByDateRange(start: Long, end: Long) = attendanceDao.getRecordsByDateRange(start, end)

    // --- Birthdays ---
    val allBirthdays: Flow<List<BirthdayEntity>> = birthdayDao.getAllBirthdays()
    suspend fun insertBirthday(birthday: BirthdayEntity) = birthdayDao.insertBirthday(birthday)
    suspend fun deleteBirthday(birthday: BirthdayEntity) = birthdayDao.deleteBirthday(birthday)
}
