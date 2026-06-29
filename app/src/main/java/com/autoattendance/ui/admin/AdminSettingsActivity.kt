package com.autoattendance.ui.admin

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.autoattendance.databinding.ActivityAdminSettingsBinding
import com.autoattendance.databinding.DialogAddLocationBinding
import com.autoattendance.geofence.GeofenceManager
import com.autoattendance.model.OfficeLocation
import com.autoattendance.viewmodel.AttendanceViewModel

class AdminSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminSettingsBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private lateinit var adapter: LocationAdapter
    private lateinit var geofenceManager: GeofenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.apply {
            title = "Admin Settings"
            setDisplayHomeAsUpEnabled(true)
        }

        geofenceManager = GeofenceManager(this)

        adapter = LocationAdapter(
            onDelete = { location ->
                AlertDialog.Builder(this)
                    .setTitle("Remove Location")
                    .setMessage("Remove ${location.name} from geofence monitoring?")
                    .setPositiveButton("Remove") { _, _ ->
                        viewModel.deleteLocation(location)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            },
            onToggle = { location ->
                viewModel.updateLocation(location.copy(isActive = !location.isActive))
            }
        )

        binding.rvLocations.layoutManager = LinearLayoutManager(this)
        binding.rvLocations.adapter = adapter

        binding.fabAddLocation.setOnClickListener { showAddLocationDialog() }

        viewModel.allLocations.observe(this) { locations ->
            adapter.submitList(locations)
            binding.tvEmpty.visibility =
                if (locations.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE

            // Re-register geofences whenever location list changes
            val active = locations.filter { it.isActive }
            geofenceManager.registerGeofences(active)
        }
    }

    private fun showAddLocationDialog() {
        val dialogBinding = DialogAddLocationBinding.inflate(layoutInflater)

        AlertDialog.Builder(this)
            .setTitle("Add Office Location")
            .setView(dialogBinding.root)
            .setPositiveButton("Add") { _, _ ->
                val name = dialogBinding.etLocationName.text.toString().trim()
                val lat = dialogBinding.etLatitude.text.toString().toDoubleOrNull()
                val lng = dialogBinding.etLongitude.text.toString().toDoubleOrNull()
                val radius = dialogBinding.etRadius.text.toString().toFloatOrNull() ?: 100f

                if (name.isNotEmpty() && lat != null && lng != null) {
                    viewModel.addLocation(
                        OfficeLocation(
                            name = name,
                            latitude = lat,
                            longitude = lng,
                            radiusMeters = radius
                        )
                    )
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }
}
