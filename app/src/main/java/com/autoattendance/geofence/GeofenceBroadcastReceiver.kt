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
        val pendingResult = goAsync()  // keeps receiver alive until coroutine finishes

        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null || geofencingEvent.hasError()) {
            val code = geofencingEvent?.errorCode ?: -1
            Log.e(TAG, "Geofence error: ${GeofenceStatusCodes.getStatusCodeString(code)}")
            pendingResult.finish()
            return
        }

        val transition = geofencingEvent.geofenceTransition
        val triggeringGeofences = geofencingEvent.triggeringGeofences
        if (triggeringGeofences.isNullOrEmpty()) {
            pendingResult.finish()
            return
        }

        val repository = AttendanceRepository(context)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val employees = repository.getAllEmployeesSync()

                for (geofence in triggeringGeofences) {
                    val locationId = geofence.requestId.toIntOrNull() ?: continue
                    val location = repository.getLocationById(locationId) ?: continue

                    when (transition) {
                        Geofence.GEOFENCE_TRANSITION_ENTER -> {
                            Log.d(TAG, "ENTER geofence: ${location.name}")
                            employees.forEach { employee ->
                                repository.checkIn(employee, location)
                            }
                            NotificationHelper.showCheckInNotification(context, location.name, locationId)
                        }

                        Geofence.GEOFENCE_TRANSITION_EXIT -> {
                            Log.d(TAG, "EXIT geofence: ${location.name}")
                            employees.forEach { employee ->
                                repository.checkOut(employee, location.name)
                            }
                            NotificationHelper.showCheckOutNotification(context, location.name, locationId)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing geofence event: ${e.message}")
            } finally {
                pendingResult.finish()  // release the receiver when done
            }
        }
    }
}
