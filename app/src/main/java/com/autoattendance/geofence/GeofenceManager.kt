package com.autoattendance.geofence

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.autoattendance.model.OfficeLocation
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices

class GeofenceManager(private val context: Context) {

    private val TAG = "GeofenceManager"

    private val geofencingClient: GeofencingClient =
        LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    fun registerGeofences(locations: List<OfficeLocation>) {
        if (locations.isEmpty()) return

        val geofenceList = locations.map { loc ->
            Geofence.Builder()
                .setRequestId(loc.id.toString())
                .setCircularRegion(loc.latitude, loc.longitude, loc.radiusMeters)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(
                    Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT
                )
                .build()
        }

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent).run {
            addOnCompleteListener {
                geofencingClient.addGeofences(request, geofencePendingIntent)
                    .addOnSuccessListener { Log.d(TAG, "Geofences registered: ${locations.size}") }
                    .addOnFailureListener { e -> Log.e(TAG, "Geofence registration failed: ${e.message}") }
            }
        }
    }

    fun removeAllGeofences() {
        geofencingClient.removeGeofences(geofencePendingIntent)
    }

    companion object {
        const val ACTION_GEOFENCE_EVENT = "com.autoattendance.ACTION_GEOFENCE_EVENT"
    }
}
