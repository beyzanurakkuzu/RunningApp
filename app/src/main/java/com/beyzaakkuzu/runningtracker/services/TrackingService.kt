package com.beyzaakkuzu.runningtracker.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.beyzaakkuzu.runningtracker.R
import com.beyzaakkuzu.runningtracker.other.Constants.ACTION_PAUSE_SERVICE
import com.beyzaakkuzu.runningtracker.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.beyzaakkuzu.runningtracker.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.beyzaakkuzu.runningtracker.other.Constants.ACTION_STOP_SERVICE
import com.beyzaakkuzu.runningtracker.other.Constants.FASTEST_LOCATION_INTERVAL
import com.beyzaakkuzu.runningtracker.other.Constants.LOCATION_UPDATE_INTERVAL
import com.beyzaakkuzu.runningtracker.other.Constants.NOTIFICATION_CHANNEL_ID
import com.beyzaakkuzu.runningtracker.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.beyzaakkuzu.runningtracker.other.Constants.NOTIFICATION_ID
import com.beyzaakkuzu.runningtracker.other.TrackingUtility
import com.beyzaakkuzu.runningtracker.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.model.LatLng
import timber.log.Timber

typealias Polyline=MutableList<LatLng>
typealias Polylines=MutableList<Polyline>
class TrackingService : LifecycleService() {

    var isFirstRun = true

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    companion object{
        val isTracking=MutableLiveData<Boolean>()
        val pathPoints= MutableLiveData<MutableList<MutableList<LatLng>>>()
    }
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
    }

    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient= FusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
        })
    }
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when (it.action) {
                ACTION_START_OR_RESUME_SERVICE -> {
                    if(isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming service...")
                    }
                }
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Paused service")
                }
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stopped service")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if(isTracking) {
            if(TrackingUtility.hasLocationPermission(this)) {
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        } else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback= object: LocationCallback(){
        override fun onLocationResult(p0: LocationResult?) {
            super.onLocationResult(p0)
            if (isTracking.value!!){
                p0?.locations?.let {
                    locations->
                    for (location in locations){
                        addPathPoint(location)
                        Timber.d("NEW LCOATION: ${location.latitude},${location.longitude}")
                    }
                }
            }
        }
    }
private fun addPathPoint(location: Location){
    location.let {
        val pos= LatLng(location.latitude,location.longitude)
        pathPoints.value?.apply {
            last().add(pos)
            pathPoints.postValue(this)
        }
    }
}

    private fun addEmptyPolyline()= pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))



    @RequiresApi(Build.VERSION_CODES.M)
    private fun startForegroundService() {
        addEmptyPolyline()
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel(notificationManager)
        }

        val notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setAutoCancel(false)
            .setOngoing(true)
            .setSmallIcon(R.drawable.ic_directions_run_block_24dp)
            .setContentTitle("Running App")
            .setContentText("00:00:00")
            .setContentIntent(getMainActivityPendingIntent())

        startForeground(NOTIFICATION_ID, notificationBuilder.build())
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressLint("UnspecifiedImmutableFlag")
    private fun getMainActivityPendingIntent() = PendingIntent.getActivity(
        this,
        0,
        Intent(this, MainActivity::class.java).also {
            it.action = ACTION_SHOW_TRACKING_FRAGMENT
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }
}