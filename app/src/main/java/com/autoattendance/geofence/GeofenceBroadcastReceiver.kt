package com.autoattendance.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autoattendance.data.repository.AttendanceRepository
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceReceiver"

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: return

        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofence error: $errorMessage")
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences ?: return

        val repository = AttendanceRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            val employees = repository.getAllEmployeesSync()

            for (geofence in triggeringGeofences) {
                val locationId = geofence.requestId.toIntOrNull() ?: continue
                val location = repository.getLocationById(locationId) ?: continue

                when (transition) {
                    Geofence.GEOFENCE_TRANSITION_ENTER -> {
                        Log.d(TAG, "ENTER geofence: ${location.name}")
                        // Check in all active employees (single-device kiosk mode)
                        // In a real multi-user scenario, only check in the device owner
                        employees.forEach { employee ->
                            repository.checkIn(employee, location)
                        }
                        NotificationHelper.showCheckInNotification(context, location.name)
                    }

                    Geofence.GEOFENCE_TRANSITION_EXIT -> {
                        Log.d(TAG, "EXIT geofence: ${location.name}")
                        employees.forEach { employee ->
                            repository.checkOut(employee)
                        }
                        NotificationHelper.showCheckOutNotification(context, location.name)
                    }
                }
            }
        }
    }
}
