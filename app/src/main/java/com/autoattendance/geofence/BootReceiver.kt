package com.autoattendance.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.autoattendance.data.repository.AttendanceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Re-registers geofences after device reboot (geofences don't survive reboots)
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        CoroutineScope(Dispatchers.IO).launch {
            val repository = AttendanceRepository(context)
            val activeLocations = repository.getActiveLocations()
            GeofenceManager(context).registerGeofences(activeLocations)
        }
    }
}
