package com.autoattendance.ui.admin

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoattendance.databinding.ActivityAdminSettingsBinding
import com.autoattendance.databinding.DialogAddLocationBinding
import com.autoattendance.geofence.GeofenceManager
import com.autoattendance.model.OfficeLocation
import com.autoattendance.viewmodel.AttendanceViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: LocationAdapter
    private var geofenceManager: GeofenceManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Admin Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        geofenceManager = try { GeofenceManager(this) } catch (e: Exception) { null }

        adapter = LocationAdapter(
            onDelete = { location ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Location")
                    .setMessage("Remove \"${location.name}\" from geofence monitoring?")
                    .setPositiveButton("Remove") { _, _ -> viewModel.deleteLocation(location) }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onToggle = { location ->
                viewModel.updateLocation(location.copy(isActive = !location.isActive))
            },
            onCheckIn = { location ->
                AlertDialog.Builder(this)
                    .setTitle("Manual Check-In")
                    .setMessage("Record check-in for all employees at \"${location.name}\" right now?")
                    .setPositiveButton("Check In") { _, _ ->
                        viewModel.manualCheckIn(location)
                        Toast.makeText(this, "Check-in recorded at ${location.name}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onCheckOut = { location ->
                AlertDialog.Builder(this)
                    .setTitle("Manual Check-Out")
                    .setMessage("Record check-out for all employees at \"${location.name}\" right now?")
                    .setPositiveButton("Check Out") { _, _ ->
                        viewModel.manualCheckOut(location)
                        Toast.makeText(this, "Check-out recorded from ${location.name}", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )

        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter

        binding.fabAddLocation.setOnClickListener { showAddLocationDialog() }

        viewModel.allLocations.observe(this) { locations ->
            adapter.submitList(locations)
            binding.tvEmpty.visibility = if (locations.isEmpty()) View.VISIBLE else View.GONE

            val active = locations.filter { it.isActive }
            try { geofenceManager?.registerGeofences(active) } catch (e: Exception) { /* no-op */ }
        }
    }

    private fun showAddLocationDialog() {
        val dialogBinding = DialogAddLocationBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Add Office Location")
            .setView(dialogBinding.root)
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", null)
            .create()

        dialogBinding.btnGetLocation.setOnClickListener {
            fetchCurrentLocation(dialogBinding)
        }

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val name = dialogBinding.etLocationName.text.toString().trim()
                val lat = dialogBinding.etLatitude.text.toString().toDoubleOrNull()
                val lng = dialogBinding.etLongitude.text.toString().toDoubleOrNull()
                val radius = dialogBinding.etRadius.text.toString().toFloatOrNull() ?: 100f

                when {
                    name.isEmpty() -> dialogBinding.etLocationName.error = "Required"
                    lat == null -> dialogBinding.etLatitude.error = "Enter a valid number"
                    lng == null -> dialogBinding.etLongitude.error = "Enter a valid number"
                    else -> {
                        viewModel.addLocation(OfficeLocation(
                            name = name,
                            latitude = lat,
                            longitude = lng,
                            radiusMeters = radius
                        ))
                        dialog.dismiss()
                        Toast.makeText(this, "\"$name\" added. Toggle ON to activate geofence.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        dialog.show()
    }

    @SuppressLint("MissingPermission")
    private fun fetchCurrentLocation(dialogBinding: DialogAddLocationBinding) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show()
            return
        }

        dialogBinding.btnGetLocation.isEnabled = false
        dialogBinding.tvLocationStatus.text = "Fetching location..."

        val fusedClient = LocationServices.getFusedLocationProviderClient(this)

        fusedClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                dialogBinding.etLatitude.setText("%.6f".format(location.latitude))
                dialogBinding.etLongitude.setText("%.6f".format(location.longitude))
                dialogBinding.tvLocationStatus.text = "Detected (accuracy: ${location.accuracy.toInt()}m)"
                dialogBinding.btnGetLocation.isEnabled = true
            } else {
                requestFreshLocation(dialogBinding, fusedClient)
            }
        }.addOnFailureListener {
            dialogBinding.tvLocationStatus.text = "Failed — enter manually"
            dialogBinding.btnGetLocation.isEnabled = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestFreshLocation(
        dialogBinding: DialogAddLocationBinding,
        fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    ) {
        val request = com.google.android.gms.location.CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .build()

        fusedClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                dialogBinding.btnGetLocation.isEnabled = true
                if (location != null) {
                    dialogBinding.etLatitude.setText("%.6f".format(location.latitude))
                    dialogBinding.etLongitude.setText("%.6f".format(location.longitude))
                    dialogBinding.tvLocationStatus.text = "Detected (accuracy: ${location.accuracy.toInt()}m)"
                } else {
                    dialogBinding.tvLocationStatus.text = "Could not detect — enter manually"
                }
            }
            .addOnFailureListener {
                dialogBinding.btnGetLocation.isEnabled = true
                dialogBinding.tvLocationStatus.text = "Error — enter manually"
            }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
