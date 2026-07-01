package com.autoattendance.geofence

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.autoattendance.R

object NotificationHelper {

    const val CHANNEL_ID = "attendance_channel"
    private const val CHANNEL_NAME = "Attendance Alerts"

    fun createNotificationChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Auto attendance check-in/out notifications"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    fun showCheckInNotification(context: Context, locationName: String, locationId: Int) {
        showNotification(
            context,
            id = 2000 + locationId,   // unique per location
            title = "Checked In — $locationName",
            message = "Auto attendance recorded at $locationName"
        )
    }

    fun showCheckOutNotification(context: Context, locationName: String, locationId: Int) {
        showNotification(
            context,
            id = 3000 + locationId,   // unique per location
            title = "Checked Out — $locationName",
            message = "Departure recorded from $locationName"
        )
    }

    private fun showNotification(context: Context, id: Int, title: String, message: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(id, notification)
    }
}
