package com.example.vehicleqtt

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient
import com.hivemq.client.mqtt.mqtt3.message.connect.connack.Mqtt3ConnAck
import com.hivemq.client.mqtt.datatypes.MqttQos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var locationMarker: Marker? = null
    private var locationCallback: LocationCallback? = null

    // ✅ HiveMQ Async Client (recommended for Android)
    private var mqttClient: Mqtt3AsyncClient? = null
    private var isMqttConnected = false

    // ✅ HiveMQ Cloud Configuration
    private val hiveMqHost = "80dddc8bfc79414d9548ca4b93d807a7.s1.eu.hivemq.cloud"
    private val hiveMqPort = 8883
    private val mqttUsername = "Ambulance"
    private val mqttPassword = "Pass@123"
    private val mqttTopic = "emergency/location"
    private val clientId = "AndroidVehicle_${System.currentTimeMillis()}"

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "HiveMQ_VehicleTracker"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Google Maps
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize location services
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Initialize HiveMQ MQTT client
        initializeHiveMqClient()

        // Check permissions and start location tracking
        checkPermissionsAndStartLocationUpdates()
    }

    private fun initializeHiveMqClient() {
        try {
            // ✅ Create HiveMQ Async Client (best for Android)
            mqttClient = MqttClient.builder()
                .useMqttVersion3()
                .identifier(clientId)
                .serverHost(hiveMqHost)
                .serverPort(hiveMqPort)
                .sslWithDefaultConfig() // Enable SSL for HiveMQ Cloud
                .buildAsync()

            connectToHiveMq()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to initialize HiveMQ client: ${e.message}", e)
            Toast.makeText(this, "MQTT Client initialization failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun connectToHiveMq() {
        mqttClient?.let { client ->
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    Log.d(TAG, "🔄 Connecting to HiveMQ Cloud...")

                    val connectFuture: CompletableFuture<Mqtt3ConnAck> = client.connectWith()
                        .simpleAuth()
                        .username(mqttUsername)
                        .password(mqttPassword.toByteArray(StandardCharsets.UTF_8))
                        .applySimpleAuth()
                        .keepAlive(60) // Keep connection alive
                        .cleanSession(true)
                        .send()

                    // Handle connection result
                    connectFuture.whenComplete { connAck, throwable ->
                        if (throwable != null) {
                            Log.e(TAG, "❌ HiveMQ connection failed: ${throwable.message}", throwable)
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "MQTT Connection Failed: ${throwable.message}", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            isMqttConnected = true
                            Log.d(TAG, "✅ Connected to HiveMQ Cloud! Return Code: ${connAck.returnCode}")
                            runOnUiThread {
                                Toast.makeText(this@MainActivity, "HiveMQ Connected Successfully!", Toast.LENGTH_SHORT).show()
                            }

                            // ✅ Subscribe to response topic (optional)
                            subscribeToResponseTopic()
                        }
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Connection exception: ${e.message}", e)
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Connection Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun subscribeToResponseTopic() {
        mqttClient?.let { client ->
            try {
                val responseTopic = "emergency/response"
                client.subscribeWith()
                    .topicFilter(responseTopic)
                    .qos(MqttQos.AT_LEAST_ONCE)
                    .callback { publish ->
                        val message = String(publish.payloadAsBytes, StandardCharsets.UTF_8)
                        Log.d(TAG, "📨 Received response: $message")
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, "Traffic Light Response: $message", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .send()
                    .whenComplete { _, throwable ->
                        if (throwable != null) {
                            Log.e(TAG, "❌ Subscription failed: ${throwable.message}")
                        } else {
                            Log.d(TAG, "✅ Subscribed to response topic: $responseTopic")
                        }
                    }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Subscribe error: ${e.message}", e)
            }
        }
    }

    private fun checkPermissionsAndStartLocationUpdates() {
        when {
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }
            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),
                    LOCATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                    Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Location permission required for vehicle tracking", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(3000) // 3 seconds - good for vehicle tracking
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMinUpdateIntervalMillis(1000) // Minimum 1 second
            .setMaxUpdateDelayMillis(5000) // Maximum 5 seconds delay
            .build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    Log.d(TAG, "📍 New location: ${location.latitude}, ${location.longitude}")
                    updateMapLocation(location)
                    publishLocationToHiveMq(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                super.onLocationAvailability(locationAvailability)
                if (!locationAvailability.isLocationAvailable) {
                    Log.w(TAG, "⚠️ GPS signal lost")
                    Toast.makeText(this@MainActivity, "GPS signal weak", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, mainLooper)
        Log.d(TAG, "🚀 Location updates started")
    }

    private fun updateMapLocation(location: Location) {
        if (!::googleMap.isInitialized) return

        val latLng = LatLng(location.latitude, location.longitude)

        if (locationMarker == null) {
            locationMarker = googleMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Emergency Vehicle")
                    .snippet("Speed: ${location.speed} m/s | Accuracy: ${location.accuracy}m")
            )
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
        } else {
            locationMarker!!.position = latLng
            locationMarker!!.snippet = "Speed: ${location.speed} m/s | Accuracy: ${location.accuracy}m"
            googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng))
        }
    }

    private fun publishLocationToHiveMq(location: Location) {
        if (!isMqttConnected || mqttClient == null) {
            Log.w(TAG, "⚠️ HiveMQ not connected, skipping publish")
            return
        }

        try {
            // ✅ Enhanced JSON payload for traffic light system
            val locationData = """
                {
                    "vehicleID": "Ambulance-01",
                    "vehicleType": "emergency",
                    "location": {
                        "lat": ${location.latitude},
                        "lon": ${location.longitude}
                    },
                    "movement": {
                        "speed": ${location.speed},
                        "bearing": ${location.bearing},
                        "accuracy": ${location.accuracy}
                    },
                    "timestamp": ${System.currentTimeMillis()},
                    "priority": "high",
                    "eta": "2min"
                }
            """.trimIndent()

            mqttClient?.publishWith()
                ?.topic(mqttTopic)
                ?.payload(locationData.toByteArray(StandardCharsets.UTF_8))
                ?.qos(MqttQos.AT_LEAST_ONCE)
                ?.retain(false)
                ?.send()
                ?.whenComplete { _, throwable ->
                    if (throwable != null) {
                        Log.e(TAG, "❌ Failed to publish to HiveMQ: ${throwable.message}", throwable)
                        // Try to reconnect if connection lost
                        if (throwable.message?.contains("not connected", true) == true) {
                            isMqttConnected = false
                            connectToHiveMq()
                        }
                    } else {
                        Log.d(TAG, "✅ Location published to HiveMQ successfully")
                    }
                }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Publish exception: ${e.message}", e)
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        Log.d(TAG, "🗺️ Google Map ready")

        // ✅ Customize map for vehicle tracking
        googleMap.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = false
            uiSettings.isCompassEnabled = true
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🔄 Cleaning up resources...")

        // Stop location updates
        locationCallback?.let { callback ->
            fusedLocationClient.removeLocationUpdates(callback)
            Log.d(TAG, "📍 Location updates stopped")
        }

        // Disconnect from HiveMQ
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (isMqttConnected && mqttClient != null) {
                    mqttClient?.disconnect()?.get() // Wait for disconnect
                    Log.d(TAG, "✅ Disconnected from HiveMQ")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error disconnecting from HiveMQ: ${e.message}")
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "⏸️ App paused - continuing background tracking")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "▶️ App resumed")

        // Check if MQTT connection is still alive
        if (!isMqttConnected) {
            connectToHiveMq()
        }
    }
}