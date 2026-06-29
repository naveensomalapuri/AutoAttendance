package com.autoattendance.geofence

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.autoattendance.R

// Foreground service keeps the app alive for background location on Android 10+
class LocationForegroundService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = NotificationCompat.Builder(this, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_location)
            .setContentTitle("Auto Attendance Active")
            .setContentText("Monitoring location for attendance...")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        startForeground(999, notification)
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
