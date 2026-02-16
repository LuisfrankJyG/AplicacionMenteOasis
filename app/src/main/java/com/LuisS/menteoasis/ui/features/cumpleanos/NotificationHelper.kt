package com.LuisS.menteoasis.ui.features.cumpleanos

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.LuisS.menteoasis.R

const val NOTIFICATION_ID = 1
const val CHANNEL_ID = "birthday_channel"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val name = "Birthday Reminders"
        val descriptionText = "Notifications for upcoming birthdays"
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}

fun showBirthdayNotification(context: Context, birthday: Birthday, message: String) {
    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground) // Reemplazar con un ícono apropiado
        .setContentTitle("MenteOasis: Cumpleaños")
        .setContentText(message)
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    // Use birthday ID for multiple notifications at once
    notificationManager.notify(birthday.id, builder.build())
}