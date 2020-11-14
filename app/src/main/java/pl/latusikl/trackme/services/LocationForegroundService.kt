package pl.latusikl.trackme.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.location.*
import pl.latusikl.trackme.MainActivity
import pl.latusikl.trackme.R
import pl.latusikl.trackme.util.SharedPreferenceUtil
import pl.latusikl.trackme.util.toText
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class LocationForegroundService : Service() {

    private var stateChange = false

    private var serviceRunningInForeground = false

    private val localBinder = LocalBinder()
    private lateinit var notificationManager: NotificationManager
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private var locationCallback: LocationCallback? = null
    private var serverConnector : ServerConnector? = null

    private lateinit var currentLocation: String

    override fun onCreate() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = prepareLocationRequest()
        currentLocation =  getString(R.string.location_unknown)

    }

    private fun prepareLocationRequest(): LocationRequest {
        return LocationRequest().apply {
            interval = TimeUnit.SECONDS.toMillis(10)
            fastestInterval = TimeUnit.SECONDS.toMillis(5)
            maxWaitTime = TimeUnit.MINUTES.toMillis(2)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    private fun prepareLocationCallback(): LocationCallback {
        return object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)

                currentLocation = if (locationResult?.lastLocation != null) {
                    locationResult.lastLocation.toText()

                } else {
                    getString(R.string.location_unknown)
                }

                val intent = Intent(ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST)
                saveDataToSharedPreferences()
                LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

                if (serviceRunningInForeground) {
                    notificationManager.notify(
                        NOTIFICATION_ID,
                        generateNotification(currentLocation)
                    )
                }
            }
        }
    }

    private fun saveDataToSharedPreferences(){
        SharedPreferenceUtil.saveLastLocationValue(this, currentLocation)
        SharedPreferenceUtil.saveLocationTimeStamp(
            this,
            SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Date())
        )
    }

override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    val cancelLocationTrackingFromNotification =
        intent.getBooleanExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, false)

    if (cancelLocationTrackingFromNotification) {
        unsubscribeToLocationUpdates()
        stopSelf()
    }
    return START_NOT_STICKY
}

override fun onBind(intent: Intent): IBinder? {
    goInBackground()
    return localBinder
}

override fun onRebind(intent: Intent) {
    goInBackground()
    super.onRebind(intent)
}

private fun goInBackground() {
    stopForeground(true)
    serviceRunningInForeground = false
    stateChange = false
}

override fun onUnbind(intent: Intent): Boolean {
    if (!stateChange && SharedPreferenceUtil.getLocationTrackingPref(this)) {
        val notification = generateNotification(currentLocation)
        startForeground(NOTIFICATION_ID, notification)
        serviceRunningInForeground = true
    }
    return true
}

override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    stateChange = true
}

fun subscribeToLocationUpdates() {
    SharedPreferenceUtil.saveLocationTrackingPref(this, true)

    startService(Intent(applicationContext, LocationForegroundService::class.java))

    try {
        this.locationCallback = prepareLocationCallback()
        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest, locationCallback, Looper.myLooper()
        )
    } catch (exception: SecurityException) {
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
    }
}

fun unsubscribeToLocationUpdates() {
    if (locationCallback != null) {
        try {
            val removeTask = fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            removeTask.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    stopSelf()
                }
            }
            SharedPreferenceUtil.saveLocationTrackingPref(this, false)

        } catch (exception: SecurityException) {
            SharedPreferenceUtil.saveLocationTrackingPref(this, true)
        }
    }
    else{
        SharedPreferenceUtil.saveLocationTrackingPref(this, false)
    }
}

private fun generateNotification(location: String): Notification {

    val titleText = getString(R.string.app_name)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

        val notificationChannel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID, titleText, NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(notificationChannel)
    }

    val bigTextStyle = NotificationCompat.BigTextStyle()
        .bigText(location)
        .setBigContentTitle(titleText)

    val launchActivityIntent = Intent(this, MainActivity::class.java)

    val cancelIntent = Intent(this, LocationForegroundService::class.java)
    cancelIntent.putExtra(EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION, true)

    val servicePendingIntent = PendingIntent.getService(
        this, 0, cancelIntent, PendingIntent.FLAG_UPDATE_CURRENT
    )

    val activityPendingIntent = PendingIntent.getActivity(
        this, 0, launchActivityIntent, 0
    )

    val notificationCompatBuilder =
        NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)

    return notificationCompatBuilder
        .setStyle(bigTextStyle)
        .setContentTitle(titleText)
        .setContentText(location)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setDefaults(NotificationCompat.DEFAULT_ALL)
        .setOngoing(true)
        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        .addAction(
            R.drawable.ic_baseline_play_circle_outline_24, getString(R.string.back_to_app),
            activityPendingIntent
        )
        .addAction(
            R.drawable.ic_baseline_play_circle_outline_24,
            getString(R.string.stop_track_location_button_text),
            servicePendingIntent
        )
        .build()
}


inner class LocalBinder : Binder() {
    internal val onlyLocationForegroundService: LocationForegroundService
        get() = this@LocationForegroundService
}

companion object {

    private const val PACKAGE_NAME = "pl.latusikl.trackme"
    internal const val ACTION_FOREGROUND_ONLY_LOCATION_BROADCAST =
        "$PACKAGE_NAME.action.FOREGROUND_ONLY_LOCATION_BROADCAST"
    internal const val EXTRA_LOCATION = "$PACKAGE_NAME.extra.LOCATION"
    private const val EXTRA_CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION =
        "$PACKAGE_NAME.extra.CANCEL_LOCATION_TRACKING_FROM_NOTIFICATION"
    private const val NOTIFICATION_ID = 1
    private const val NOTIFICATION_CHANNEL_ID = "track_me_channel_01"
}

}
