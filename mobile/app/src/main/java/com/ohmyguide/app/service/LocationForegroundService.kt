package com.ohmyguide.app.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.ohmyguide.app.BuildConfig
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ohmyguide.app.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class LocationData(val latitude: Double, val longitude: Double)

class LocationForegroundService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) Log.d(TAG, "Service onCreate")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (BuildConfig.DEBUG) {
                        if (location != null) {
                            Log.d(TAG, "lastLocation: provider=${location.provider}, " +
                                "lat=${location.latitude}, lng=${location.longitude}, " +
                                "accuracy=${location.accuracy}m, source=${classifySource(location.accuracy)}")
                        } else {
                            Log.d(TAG, "lastLocation: null (캐시된 위치 없음)")
                        }
                    }
                    if (location != null && _locationFlow.value == null) {
                        _locationFlow.value = LocationData(location.latitude, location.longitude)
                    }
                }
                .addOnFailureListener { e ->
                    if (BuildConfig.DEBUG) Log.e(TAG, "lastLocation failed", e)
                }
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "lastLocation SecurityException", e)
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_INTERVAL_MS,
        ).setMinUpdateIntervalMillis(FASTEST_INTERVAL_MS).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "onLocationResult: provider=${location.provider}, " +
                            "lat=${location.latitude}, lng=${location.longitude}, " +
                            "accuracy=${location.accuracy}m, source=${classifySource(location.accuracy)}")
                    }
                    _locationFlow.value = LocationData(location.latitude, location.longitude)
                }
            }
        }

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper(),
            )
            if (BuildConfig.DEBUG) Log.d(TAG, "requestLocationUpdates started")
        } catch (e: SecurityException) {
            if (BuildConfig.DEBUG) Log.e(TAG, "requestLocationUpdates SecurityException", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW,
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(): Notification {
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val contentText = _naviStatus.value ?: "Tracking your location"
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Oh My Guide")
            .setContentText(contentText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    fun refreshNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun classifySource(accuracyMeters: Float): String = when {
        accuracyMeters <= 10f -> "GPS (위성)"
        accuracyMeters <= 50f -> "Wi-Fi (무선랜)"
        else -> "Cell (기지국)"
    }

    companion object {
        private const val TAG = "LocationService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_channel"
        private const val LOCATION_INTERVAL_MS = 3000L
        private const val FASTEST_INTERVAL_MS = 1000L

        private val _locationFlow = MutableStateFlow<LocationData?>(null)
        val locationFlow: StateFlow<LocationData?> = _locationFlow.asStateFlow()

        private val _naviStatus = MutableStateFlow<String?>(null)
        val naviStatus: StateFlow<String?> = _naviStatus.asStateFlow()

        fun updateNaviStatus(status: String?) {
            _naviStatus.value = status
        }
    }
}
