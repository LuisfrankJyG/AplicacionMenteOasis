package com.LuisS.menteoasis.ui.features.cumpleanos

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.LuisS.menteoasis.data.MenteOasisRepository
import com.LuisS.menteoasis.data.entities.BirthdayEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class CumpleanosUiState(
    val birthdays: List<BirthdayEntity> = emptyList()
)

class CumpleanosViewModel(
    private val repository: MenteOasisRepository,
    application: Application
) : ViewModel() {

    private val workManager = WorkManager.getInstance(application)

    val uiState: StateFlow<CumpleanosUiState> = repository.allBirthdays
        .map { list -> 
            // Sort by upcoming Date
            // Simple sort: calculate days until next birthday and sort by that
            val sorted = list.sortedBy { getDaysUntilBirthday(it) }
            CumpleanosUiState(sorted)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CumpleanosUiState()
        )

    fun deleteBirthday(birthday: BirthdayEntity) {
        viewModelScope.launch {
            repository.deleteBirthday(birthday)
            // Cancel all stages
            workManager.cancelUniqueWork("birthday_${birthday.id}_stage_7")
            workManager.cancelUniqueWork("birthday_${birthday.id}_stage_1")
            workManager.cancelUniqueWork("birthday_${birthday.id}_stage_0")
        }
    }

    private fun scheduleBirthdayReminders(birthday: BirthdayEntity) {
        // Schedule 3 stages: 7 days before, 1 day before, and on the day
        scheduleStage(birthday, 7, "Falta 1 semana para el cumple de ${birthday.name}!")
        scheduleStage(birthday, 1, "¡El cumpleaños de ${birthday.name} es mañana!")
        scheduleStage(birthday, 0, "¡Hoy es el cumpleaños de ${birthday.name}! ¡No olvides felicitarle!")
    }

    private fun scheduleStage(birthday: BirthdayEntity, daysBefore: Int, message: String) {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthday.month - 1)
            set(Calendar.DAY_OF_MONTH, birthday.day)
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            add(Calendar.DAY_OF_YEAR, -daysBefore)
            
            if (before(now)) {
                add(Calendar.YEAR, 1) 
            }
        }

        val delay = target.timeInMillis - now.timeInMillis
        val data = Data.Builder()
            .putString("nombre", birthday.name)
            .putInt("id", birthday.id)
            .putString("mensaje", message)
            .build()
            
        val request = OneTimeWorkRequestBuilder<BirthdayReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .addTag("birthday_${birthday.id}")
            .build()
            
        // Use unique work with stage suffix to avoid collisions
        workManager.enqueueUniqueWork(
            "birthday_${birthday.id}_stage_$daysBefore",
            androidx.work.ExistingWorkPolicy.REPLACE,
            request
        )
    }

    // Update addBirthday to call the new mult-stage logic
    fun addBirthday(name: String, day: Int, month: Int, year: Int?) {
        viewModelScope.launch {
            val birthday = BirthdayEntity(
                name = name,
                day = day,
                month = month,
                year = year,
                reminderConfig = 1 
            )
            val id = repository.insertBirthday(birthday)
            scheduleBirthdayReminders(birthday.copy(id = id.toInt()))
        }
    }
    
    fun getDaysUntilBirthday(birthday: BirthdayEntity): Int {
        val today = Calendar.getInstance()
        val nextBirthday = Calendar.getInstance().apply {
            set(Calendar.MONTH, birthday.month - 1)
            set(Calendar.DAY_OF_MONTH, birthday.day)
            set(Calendar.HOUR_OF_DAY, 8)
            if (before(today)) {
                add(Calendar.YEAR, 1)
            }
        }
        val diff = nextBirthday.timeInMillis - today.timeInMillis
        return TimeUnit.MILLISECONDS.toDays(diff).toInt()
    }
}
