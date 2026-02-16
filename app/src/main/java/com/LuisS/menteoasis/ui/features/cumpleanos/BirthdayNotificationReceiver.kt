package com.LuisS.menteoasis.ui.features.cumpleanos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.LuisS.menteoasis.data.entities.BirthdayEntity

class BirthdayNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            val nombre = it.getStringExtra("nombre") ?: return
            val mensaje = it.getStringExtra("mensaje") ?: "¡Recordatorio de cumpleaños!"
            
            // Create a temporary entity for the notification system
            val birthday = BirthdayEntity(
                id = 0,
                name = nombre,
                day = 0,
                month = 0
            )

            createNotificationChannel(context)
            showBirthdayNotification(context, birthday, mensaje)
        }
    }
}