package com.speeddisplay

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.BatteryManager
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
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
    private lateinit var scaleGestureDetector: ScaleGestureDetector

    private var isMirrored = false
    private var selectedColor = COLOR_GREEN
    private var currentScale = 1.0f
    private val minScale = 0.4f
    private val maxScale = 1.8f

    companion object {
        const val LOCATION_PERMISSION_REQUEST = 1001
        const val PREF_COLOR = "pref_color"
        const val PREF_MIRRORED = "pref_mirrored"
        const val PREF_SCALE = "pref_scale"
        const val COLOR_GREEN = 0
        const val COLOR_BLUE = 1
        const val COLOR_RED = 2
        const val COLOR_YELLOW = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

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
        currentScale = prefs.getFloat(PREF_SCALE, 1.0f)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        setupGestureDetector()
        applyColor()
        applyMirror()
        applyScale()
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

        scaleGestureDetector = ScaleGestureDetector(this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    currentScale *= detector.scaleFactor
                    currentScale = currentScale.coerceIn(minScale, maxScale)
                    applyScale()
                    return true
                }

                override fun onScaleEnd(detector: ScaleGestureDetector) {
                    prefs.edit().putFloat(PREF_SCALE, currentScale).apply()
                }
            })

        binding.root.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
            if (!scaleGestureDetector.isInProgress) {
                gestureDetector.onTouchEvent(event)
            }
            true
        }
    }

    private fun applyScale() {
        // Scale only the speed digits and unit, not the whole screen
        binding.speedContainer.scaleX = currentScale
        binding.speedContainer.scaleY = currentScale
        binding.speedContainer.pivotX = binding.speedContainer.width / 2f
        binding.speedContainer.pivotY = binding.speedContainer.height / 2f
    }

    private fun getBatteryPercentage(): Int {
        val ifilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = registerReceiver(null, ifilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) (level * 100 / scale) else 0
    }

    private fun toggleMirror() {
        isMirrored = !isMirrored
        prefs.edit().putBoolean(PREF_MIRRORED, isMirrored).apply()
        applyMirror()
    }

    private fun applyMirror() {
        binding.mirrorContainer.scaleY = if (isMirrored) -1f else 1f
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
        binding.speedText.text = speedKmh.toInt().toString()
        binding.speedBar.setSpeed(speedKmh)
        binding.batteryText.text = "${getBatteryPercentage()}%"
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
                    LocationManager.GPS_PROVIDER, 500L, 0f, this
                )
            } catch (e: Exception) {}
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
            updateSpeedDisplay(location.speed * 3.6f)
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

    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
}
