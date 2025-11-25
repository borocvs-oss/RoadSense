package com.example.roadsenseedge

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var locationManager: LocationManager
    private lateinit var dbHelper: BrakeDatabaseHelper

    private var accelX = 0f
    private var accelY = 0f
    private var accelZ = 0f
    private var speed = 0f
    private var lastSpeed = 0f

    private var suddenBrakeDetected by mutableStateOf(false)
    private var brakeList by mutableStateOf(listOf<BrakeRecord>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        dbHelper = BrakeDatabaseHelper(this)

        setContent {
            RoadSenseEdgeUI()
        }
    }

    @Composable
    fun RoadSenseEdgeUI() {
        var sensorData by remember { mutableStateOf("Sensors not running") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Button(onClick = {
                requestPermissions {
                    startSensors { data ->
                        sensorData = data
                        // učitamo poslednja nagla kočenja
                        brakeList = dbHelper.getAllBrakes()
                    }
                }
            }) {
                Text("Start")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = sensorData, fontSize = 20.sp)

            Spacer(modifier = Modifier.height(20.dp))

            if (suddenBrakeDetected) {
                Text(
                    text = "⚠️ Naglo kočenje detektovano!",
                    color = Color.Red,
                    fontSize = 24.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFCDD2))
                        .padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(text = "Poslednja nagla kočenja:", fontSize = 18.sp)

            LazyColumn(modifier = Modifier.fillMaxHeight()) {
                items(brakeList) { brake ->
                    Text(
                        text = "Vreme: ${brake.timestamp}, Brzina: ${"%.2f".format(brake.speed)} m/s, Accel: x=${"%.2f".format(brake.accelX)} y=${"%.2f".format(brake.accelY)} z=${"%.2f".format(brake.accelZ)}",
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }

    private fun startSensors(updateUI: (String) -> Unit) {
        // GPS
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                0f
            ) { location: Location ->
                lastSpeed = speed
                speed = location.speed
                checkSuddenBrake()
                updateUI(getSensorText())
            }
        }

        // Accelerometer
        val accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL)
        updateUI(getSensorText())
    }

    private fun checkSuddenBrake() {
        val speedDrop = lastSpeed - speed
        suddenBrakeDetected =
            speedDrop > 5 || accelX < -15 || accelY < -15

        if (suddenBrakeDetected) {
            val timestamp = System.currentTimeMillis()
            dbHelper.insertBrake(timestamp, speed, accelX, accelY, accelZ)
        }
    }

    private fun getSensorText(): String {
        return "Speed: %.2f m/s\nAccel: x=%.2f y=%.2f z=%.2f".format(speed, accelX, accelY, accelZ)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            accelX = event.values[0]
            accelY = event.values[1]
            accelZ = event.values[2]
            checkSuddenBrake()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun requestPermissions(onGranted: () -> Unit) {
        val permissionsList = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissionsList.add(Manifest.permission.ACTIVITY_RECOGNITION)
        }

        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            val granted = permissions.all { it.value }
            if (granted) onGranted()
        }

        launcher.launch(permissionsList.toTypedArray())
    }
}
