package com.android.androidcomponents.services.foreground

import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.android.androidcomponents.R

/**
 *  FOREGROUND SERVICE — ForeGroundService
 *  What is a Foreground Service?
 *  A Foreground Service is a type of Android Service that performs
 *  operations the user is actively aware of. Unlike background services,
 *  the system will NOT kill a foreground service when memory is low.
 *
 *  It MUST display a persistent notification as long as it is running,
 *  so the user always knows the app is doing work (e.g. playing music,
 *  tracking location, syncing data).
 *
 *  When to use a Foreground Service?
 *  Music / podcast playback
 *   GPS / location tracking
 *  File upload or download
 *   Fitness tracking (step counter, workout timer)
 *  Any long-running task that the user should be aware of
 *
 *  Foreground Service vs Background Service
 *  │ Feature              │ Foreground      │ Background       │
 *  │ User awareness       │ Yes (notification) │ No            │
 *  │ Killed by system     │ Rarely          │ Yes (low memory) │
 *  │ Notification needed  │ Yes (required)  │ No               │
 *  │ API restrictions     │ Less restricted │ Very restricted  │
 *
 *  How to build a Foreground Service (Step by Step)
 *  Step 1: Add permissions in AndroidManifest.xml
 *      <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
 *      <uses-permission android:name="android.permission.FOREGROUND_SERVICE_DATA_SYNC" />
 *
 *  Step 2: Register the service in AndroidManifest.xml
 *      <service
 *          android:name=".ForeGroundService"
 *          android:foregroundServiceType="dataSync" />
 *
 *  Step 3: Create a Notification Channel (required for API 26+)
 *      Done in the Application class (RunningApp.kt)
 *
 *  Step 4: Create a class that extends Service
 *      Override onBind() and onStartCommand()
 *
 *  Step 5: Call startForeground(id, notification) inside the service
 *      This promotes the service to foreground and shows the notification
 *
 *  Step 6: Start the service from an Activity using an Intent
 *      Intent(this, ForeGroundService::class.java).also {
 *          it.action = ForeGroundService.Actions.START.toString()
 *          startService(it)
 *      }
 *
 *  Step 7: Stop the service
 *      Call stopSelf() from inside the service, or
 *      stopService(intent) from outside
 */
class ForeGroundService : Service() {

    /**
     * onBind() is required to override from Service.
     *
     * Returning null means this is a STARTED service, not a BOUND service.
     *
     * Started Service  → launched with startService(), runs independently
     * Bound Service    → launched with bindService(), tied to a component's lifecycle
     *
     * Since we return null here, no component can bind to this service.
     */
    override fun onBind(p0: Intent?): IBinder? = null

    /**
     * onStartCommand() is called every time a component sends an Intent to this service.
     *
     * If the service is already running, this method is called again (it does NOT restart).
     * If the service is NOT running, Android creates it first, then calls this method.
     *
     * @param intent  The intent sent by the caller — carries the action (START or STOP)
     * @param flags   Delivery info (usually 0)
     * @param startId Unique ID for this start request — useful if handling multiple starts
     *
     * @return Int — tells Android what to do if the service is killed:
     *   START_STICKY     → Restart the service, but don't re-deliver the intent
     *   START_NOT_STICKY → Don't restart the service automatically
     *   START_REDELIVER_INTENT → Restart and re-deliver the last intent
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Actions.START.toString() -> start()  // Promote to foreground
            Actions.STOP.toString() -> stopSelf() // Stop the service entirely
        }
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * Promotes this service to the foreground by:
     *   1. Building a visible notification (required by Android)
     *   2. Calling startForeground() with the notification
     *
     * Without calling startForeground(), Android will kill the service
     * shortly after it starts (especially on API 26+).
     *
     * Note: The channel_id here must match the channel created in RunningApp.
     *
     * On API 29+ (Android 10), a foregroundServiceType must also be passed
     * to startForeground() to match what's declared in the manifest.
     */
    private fun start() {
        val notification = NotificationCompat.Builder(this, "channel_id")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Required — shown in status bar
            .setContentTitle("ForeGround Service")           // Bold title in notification
            .setContentText("Service is running")            // Description text
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // API 29+ requires passing the foreground service type explicitly
            // This type must match android:foregroundServiceType in the manifest
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            // Older devices don't need a service type
            startForeground(1, notification)
        }
    }

    /**
     * Actions — defines the commands this service understands.
     *
     * These are passed as the action of an Intent from the caller:
     *
     *   Intent(context, ForeGroundService::class.java).also {
     *       it.action = Actions.START.toString()
     *       startService(it)
     *   }
     *
     * Using an enum ensures type safety and avoids raw string mistakes.
     */
    enum class Actions {
        START,  // Start the foreground service
        STOP    // Stop the foreground service
    }
}