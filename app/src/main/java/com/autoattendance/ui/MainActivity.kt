package com.autoattendance.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.autoattendance.databinding.ActivityMainBinding
import com.autoattendance.geofence.GeofenceManager
import com.autoattendance.geofence.LocationForegroundService
import com.autoattendance.geofence.NotificationHelper
import com.autoattendance.ui.admin.AdminSettingsActivity
import com.autoattendance.ui.attendance.AttendanceHistoryActivity
import com.autoattendance.ui.employee.EmployeeListActivity
import com.autoattendance.viewmodel.AttendanceViewModel
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AttendanceViewModel by viewModels()
    private var geofenceManager: GeofenceManager? = null

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) requestBackgroundLocationPermission()
        else Toast.makeText(this, "Location permission required for auto attendance", Toast.LENGTH_LONG).show()
    }

    private val backgroundLocationRequest = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) startGeofencing()
        else {
            binding.tvGeofenceStatus.text = "Background location denied — auto check-in disabled"
            Toast.makeText(this, "Background location needed for auto check-in/out", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationHelper.createNotificationChannel(this)

        if (isPlayServicesAvailable()) {
            geofenceManager = GeofenceManager(this)
        } else {
            binding.tvGeofenceStatus.text = "Google Play Services not available"
        }

        setupUI()
        observeData()
        checkAndRequestPermissions()
    }

    private fun isPlayServicesAvailable(): Boolean {
        val result = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
        return result == ConnectionResult.SUCCESS
    }

    private fun setupUI() {
        val today = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        binding.tvDate.text = today
        binding.tvGeofenceStatus.text = "Initializing..."

        binding.cardEmployees.setOnClickListener {
            startActivity(Intent(this, EmployeeListActivity::class.java))
        }
        binding.cardAttendance.setOnClickListener {
            startActivity(Intent(this, AttendanceHistoryActivity::class.java))
        }
        binding.cardSettings.setOnClickListener {
            startActivity(Intent(this, AdminSettingsActivity::class.java))
        }
        binding.btnRefresh.setOnClickListener {
            viewModel.refreshTodayCount()
        }
    }

    private fun observeData() {
        viewModel.todayCount.observe(this) { count ->
            binding.tvTodayCount.text = count.toString()
        }
        viewModel.allEmployees.observe(this) { employees ->
            binding.tvTotalEmployees.text = employees.size.toString()
        }
    }

    private fun checkAndRequestPermissions() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineGranted) {
            locationPermissionRequest.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            requestBackgroundLocationPermission()
        }
    }

    private fun requestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val bgGranted = ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!bgGranted) {
                backgroundLocationRequest.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                return
            }
        }
        startGeofencing()
    }

    private fun startGeofencing() {
        val geoManager = geofenceManager ?: run {
            binding.tvGeofenceStatus.text = "Location services unavailable"
            return
        }

        try {
            val intent = Intent(this, LocationForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to start foreground service: ${e.message}")
        }

        viewModel.allLocations.observe(this) { locations ->
            val active = locations.filter { it.isActive }
            if (active.isNotEmpty()) {
                try {
                    geoManager.registerGeofences(active)
                    binding.tvGeofenceStatus.text = "Monitoring ${active.size} location(s)"
                } catch (e: Exception) {
                    Log.e("MainActivity", "Geofence registration failed: ${e.message}")
                    binding.tvGeofenceStatus.text = "Geofence error — check permissions"
                }
            } else {
                binding.tvGeofenceStatus.text = "No office locations configured"
            }
        }
    }
}
