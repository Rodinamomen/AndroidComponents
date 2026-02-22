package com.android.androidcomponents.services.background

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.os.Handler
import android.widget.Toast
/**
 * BackgroundService — Android Service
 * ═════════════════════════════════════
 *
 * A Service is an Android component that runs operations
 * in the background WITHOUT a UI. It keeps running even
 * when the user navigates away from the app.
 *
 * There are two types of Services:
 *
 * 1. STARTED SERVICE (what this is)
 *    Started by startService() / startForegroundService()
 *    - Runs independently in the background
 *    - Keeps running until stopSelf() or stopService() is called
 *    - Does NOT communicate back to the caller directly
 *
 * 2. BOUND SERVICE
 *    Started by bindService()
 *    - Allows components to bind and interact with the service
 *    - Destroyed when all clients unbind
 *    - Requires onBind() to return an IBinder — null here since
 *      this is a Started Service, not a Bound Service
 *
 * Lifecycle:
 *  startService() → onCreate() → onStartCommand() → running
 *  stopService()  → onDestroy() → destroyed
 *
 * Note:
 *  This is a background service. On Android 8+ (Oreo), background
 *  services are killed when the app goes to background.
 *  Use Foreground Service with a notification for long-running tasks.
 */

class BackGroundService : Service() {
    /**
     * Called when a client tries to bind to this service.
     * Returns null because this is a Started Service — binding is not supported.
     * Override and return an IBinder only if you need a Bound Service.
     */
    override fun onBind(p0: Intent?): IBinder? = null

    /**
     * Called once when the service is first created.
     * Use for one-time setup (initialize resources, threads, etc.)
     * Called before onStartCommand().
     */
    override fun onCreate() {
        super.onCreate()
    }
    /**
     * Called every time startService() is invoked.
     * This is where the service's main work begins.
     *
     * Return values:
     *  - START_STICKY      → system restarts service if killed, intent = null
     *  - START_NOT_STICKY  → system does NOT restart service if killed
     *  - START_REDELIVER_INTENT → restarts and re-delivers the last intent
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        showMessage()
        return super.onStartCommand(intent, flags, startId)
    }
    /**
     * Called when the service is about to be destroyed.
     * Use to clean up resources (stop threads, unregister receivers, etc.)
     */
    override fun onDestroy() {
        super.onDestroy()
    }
    fun showMessage(){
        Handler(Looper.getMainLooper()).postDelayed(
            {
                Toast.makeText(applicationContext, "Hello from service", Toast.LENGTH_SHORT).show()
            },
            5000
        )
    }
}