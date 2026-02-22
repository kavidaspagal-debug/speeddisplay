package com.speeddisplay

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.speeddisplay.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private lateinit var prefs: SharedPreferences
    private lateinit var gestureDetector: GestureDetector

    private var isMirrored = false
    private var selectedColor = COLOR_GREEN

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1001
        const val PREF_COLOR = "pref_color"
        const val PREF_MIRRORED = "pref_mirrored"
        const val COLOR_GREEN = 0
        const val COLOR_BLUE = 1
        const val COLOR_RED = 2
        const val COLOR_YELLOW = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Full screen immersive
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )

        prefs = getSharedPreferences("SpeedDisplayPrefs", Context.MODE_PRIVATE)
        selectedColor = prefs.getInt(PREF_COLOR, COLOR_GREEN)
        isMirrored = prefs.getBoolean(PREF_MIRRORED, false)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupGestureDetector()
        applyColor()
        applyMirror()
        updateSpeedDisplay(0f)

        requestLocationPermission()
    }

    private fun setupGestureDetector() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
                toggleMirror()
                return true
            }

            override fun onLongPress(e: MotionEvent) {
                showSettingsDialog()
            }
        })

        binding.root.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    private fun toggleMirror() {
        isMirrored = !isMirrored
        prefs.edit().putBoolean(PREF_MIRRORED, isMirrored).apply()
        applyMirror()
    }

    private fun applyMirror() {
        binding.speedText.scaleX = if (isMirrored) -1f else 1f
    }

    private fun applyColor() {
        val color = when (selectedColor) {
            COLOR_GREEN -> android.graphics.Color.parseColor("#00FF00")
            COLOR_BLUE -> android.graphics.Color.parseColor("#00AAFF")
            COLOR_RED -> android.graphics.Color.parseColor("#FF2200")
            COLOR_YELLOW -> android.graphics.Color.parseColor("#FFEE00")
            else -> android.graphics.Color.parseColor("#00FF00")
        }
        binding.speedText.setTextColor(color)
        binding.unitText.setTextColor(color)
    }

    private fun showSettingsDialog() {
        val dialog = SettingsDialog(this, selectedColor) { newColor ->
            selectedColor = newColor
            prefs.edit().putInt(PREF_COLOR, selectedColor).apply()
            applyColor()
        }
        dialog.show()
    }

    private fun updateSpeedDisplay(speedKmh: Float) {
        val speedInt = speedKmh.toInt()
        binding.speedText.text = speedInt.toString()
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST
            )
        } else {
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            try {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    500L,
                    0f,
                    this
                )
            } catch (e: Exception) {
                // GPS not available
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates()
        }
    }

    override fun onLocationChanged(location: Location) {
        if (location.hasSpeed()) {
            val speedKmh = location.speed * 3.6f
            updateSpeedDisplay(speedKmh)
        }
    }

    override fun onResume() {
        super.onResume()
        window.decorView.systemUiVisibility = (
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        )
        startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        locationManager.removeUpdates(this)
    }

    // Legacy LocationListener methods
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
