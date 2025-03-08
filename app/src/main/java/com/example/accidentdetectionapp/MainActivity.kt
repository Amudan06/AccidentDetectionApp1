package com.example.accidentdetectionapp

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var locationManager: LocationManager? = null
    private var latitude = 0.0
    private var longitude = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize sensors
        sensorManager = getSystemService(SensorManager::class.java)
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager?.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)

        // Initialize location
        locationManager = getSystemService(LocationManager::class.java)

        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 5000, 10f,
                object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
            )
        }

        // Button click handlers
        val btnStart = findViewById<Button>(R.id.btnStartDetection)
        val btnStop = findViewById<Button>(R.id.btnStopDetection)

        btnStart.setOnClickListener {
            Toast.makeText(this, "Accident Detection Started", Toast.LENGTH_SHORT).show()
        }

        btnStop.setOnClickListener {
            Toast.makeText(this, "Accident Detection Stopped", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            val acceleration = sqrt((x * x + y * y + z * z).toDouble())
            if (acceleration > ACCIDENT_THRESHOLD) {
                sendEmergencyAlert()
            }
        }
    }

    private fun sendEmergencyAlert() {
        AlertDialog.Builder(this)
            .setTitle("Accident Detected!")
            .setMessage("Do you want to send an emergency alert?")
            .setPositiveButton("Yes") { _, _ ->
                // Send SMS with location
                val message =
                    "Accident detected! Location: https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(EMERGENCY_CONTACT, null, message, null, null)
                Toast.makeText(this, "Emergency Alert Sent!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("No") { dialog, _ -> dialog.dismiss() } // Cancel alert
            .show()
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {
        private const val ACCIDENT_THRESHOLD = 20.0f // Adjust based on testing
        private const val EMERGENCY_CONTACT = "1234567890" // Replace with actual number
    }
}
