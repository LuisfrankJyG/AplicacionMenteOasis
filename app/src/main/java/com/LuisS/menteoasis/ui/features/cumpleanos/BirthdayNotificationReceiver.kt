package com.LuisS.menteoasis.ui.features.cumpleanos

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BirthdayNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        intent?.let {
            val nombre = it.getStringExtra("nombre") ?: return
            val fecha = it.getStringExtra("fecha") ?: return
            val mensaje = it.getStringExtra("mensaje") ?: "¡Recordatorio de cumpleaños!"
            val birthday = Birthday(0, nombre, fecha)

            createNotificationChannel(context)
            showBirthdayNotification(context, birthday, mensaje)
        }
    }
}