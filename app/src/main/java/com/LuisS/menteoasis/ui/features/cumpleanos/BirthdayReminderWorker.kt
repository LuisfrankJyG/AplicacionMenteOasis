package com.LuisS.menteoasis.ui.features.cumpleanos

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.LuisS.menteoasis.data.entities.BirthdayEntity

class BirthdayReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val nombre = inputData.getString("nombre") ?: return Result.failure()
        val mensaje = inputData.getString("mensaje") ?: "¡Recordatorio de cumpleaños!"

        // Show Notification
        createNotificationChannel(applicationContext)
        
        val birthday = BirthdayEntity(
            id = 0,
            name = nombre,
            day = 0,
            month = 0
        )
        
        showBirthdayNotification(applicationContext, birthday, mensaje)

        return Result.success()
    }
}