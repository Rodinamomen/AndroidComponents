package com.android.androidcomponents.broadcastreciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log

/**
 * AirPlaneModeReceiver — BroadcastReceiver
 * ══════════════════════════════════════════
 *
 * A BroadcastReceiver listens for system-wide events (broadcasts)
 * sent by Android or other apps and reacts to them.
 *
 * There are two ways to register a BroadcastReceiver:
 *
 * 1. DYNAMIC REGISTRATION (Programmatic)
 *    Registered in code (Activity/Service) at runtime.
 *
 *    registerReceiver(
 *        airPlaneModeReceiver,
 *        IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED)
 *    )
 *
 *    - Only active while the component is alive
 *    - Must unregister in onDestroy() to avoid memory leak
 *    - Best for receivers tied to UI/Activity lifecycle
 *    - Stops working when Activity is destroyed
 *
 *
 * 2. STATIC REGISTRATION (Manifest)
 *    Declared in AndroidManifest.xml.
 *
 *    <receiver android:name=".AirPlaneModeReceiver"
 *        android:exported="true">
 *        <intent-filter>
 *            <action android:name=
 *              "android.intent.action.AIRPLANE_MODE"/>
 *        </intent-filter>
 *    </receiver>
 *
 *    - Works even when the app is not running
 *    - No need to manually register/unregister
 *    - Many system broadcasts are NOT allowed statically
 *      since Android 8+ (Oreo) — ACTION_AIRPLANE_MODE_CHANGED
 *      is one of them, so dynamic is required here
 *
 *
 * Why Dynamic for ACTION_AIRPLANE_MODE_CHANGED?
 *  Since Android 8.0 (API 26), most implicit broadcasts
 *  cannot be received via static registration for battery
 *  and performance reasons. ACTION_AIRPLANE_MODE_CHANGED
 *  is one of them — dynamic registration is the only option.
 */
class AirPlaneModeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_AIRPLANE_MODE_CHANGED) {
            val isTurnedOn = Settings.Global.getInt(
                context?.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON
            ) != 0
            Log.d("AirPlaneModeReceiver", "is AirPlane Mode Enabled? $isTurnedOn")
        }
    }
}