package com.android.androidcomponents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.android.androidcomponents.broadcastreciever.AirPlaneModeReceiver
import com.android.androidcomponents.intent.SecondActivity
import com.android.androidcomponents.services.background.BackGroundService
import com.android.androidcomponents.services.foreground.ForeGroundService
import com.android.androidcomponents.ui.theme.AndroidComponentsTheme

/**
 * -------------------------------------------------------
 * ANDROID INTENTS - EXPLICIT vs IMPLICIT
 * -------------------------------------------------------
 *
 * An Intent is a messaging object used to request an action
 * from another component (Activity, Service, BroadcastReceiver).
 * There are two types: Explicit and Implicit.
 */

class MainActivity : ComponentActivity() {
    private val airPlaneModeReceiver = AirPlaneModeReceiver()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startService(Intent(this, BackGroundService::class.java))
        enableEdgeToEdge()
        registerReceiver(airPlaneModeReceiver, IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED))
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            0
        )
        setContent {
            AndroidComponentsTheme {
                Column(modifier = Modifier.fillMaxSize()) {
                    /**
                     * EXPLICIT INTENT — Inside App
                     * ─────────────────────────────
                     * Targets a component within YOUR OWN app by specifying
                     * the exact class. Android delivers it directly with no resolution.
                     *
                     * How it works:
                     *  1. Intent is created with (context, SecondActivity::class.java)
                     *  2. Android skips resolution — target is already known
                     *  3. SecondActivity launches immediately
                     */
                    Button(onClick = {
                        Intent(applicationContext, SecondActivity::class.java).also {
                            startActivity(it)
                        }
                    }) {
                        Text(text = "Go to Second Activity")
                    }
                    /**
                     * EXPLICIT INTENT — Outside App
                     * ──────────────────────────────
                     * Targets a component in ANOTHER app by specifying its
                     * package name. Android opens that app's main entry point directly.
                     *
                     * How it works:
                     *  1. Intent is created with ACTION_MAIN (open app's main activity)
                     *  2. package is set to the target app (YouTube)
                     *  3. Android finds and launches YouTube's MainActivity directly
                     *
                     * Note:
                     *  If the app is not installed → ActivityNotFoundException is thrown.
                     *  Always handle this case in production code.
                     */
                    Button(onClick = {
                        Intent(Intent.ACTION_MAIN).also {
                            it.`package` = "com.google.android.youtube"
                            // To check first if the app is installed or not.
                            try {
                                startActivity(it)
                            } catch (e: ActivityNotFoundException) {
                                print(e.stackTrace)
                            }
                        }
                    }) {
                        Text(text = "Go to Youtube")
                    }
                    /**
                     * IMPLICIT INTENT — Share/Send Content
                     * ──────────────────────────────────────
                     * Does NOT specify a target component. Instead, declares an ACTION
                     * and lets Android find all apps that can handle it at runtime.
                     *
                     * In this case → any app that can send plain text (Email, WhatsApp, etc.)
                     *
                     * How it works:
                     *  1. Intent is created with ACTION_SEND — "I want to send something"
                     *  2. type = "text/plain" — narrows it to text-sharing apps only
                     *  3. Extras are attached (recipient, subject, body)
                     *  4. resolveActivity() checks if ANY app can handle this intent
                     *  5. If found → Android shows a chooser (Gmail, Outlook, etc.)
                     *  6. If not found → nothing happens (no crash)
                     *
                     * Intent Extras:
                     *  - EXTRA_EMAIL   → pre-fills the recipient field
                     *  - EXTRA_SUBJECT → pre-fills the subject field
                     *  - EXTRA_TEXT    → pre-fills the body content
                     *
                     * Why resolveActivity()?
                     *  Safety check — if no app can handle ACTION_SEND,
                     *  calling startActivity() directly would crash the app.
                     */
                    Button(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            // Intent Extras
                            putExtra(Intent.EXTRA_EMAIL, "test@test.com")
                            putExtra(Intent.EXTRA_SUBJECT, "Test subject")
                            putExtra(Intent.EXTRA_TEXT, "Content o f my email")
                        }
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                    ) {
                        Text(text = "Go to Email")
                    }
                    // Sending Broadcast It will be received by all apps that are registered to it
                    // If you want to send it to specific app add the package name
                    Button(onClick = {
                        sendBroadcast(Intent("TEST_ACTION"))
                    }) {
                        Text(text = "Send broadcast")
                    }

                    Button(onClick = {
                        Intent(applicationContext, ForeGroundService::class.java).also {
                            it.action = ForeGroundService.Actions.START.toString()
                            startService(it)
                        }
                    }){
                        Text(text = "Send Foreground service")
                    }
                    Button(onClick = {
                        Intent(applicationContext, ForeGroundService::class.java).also {
                            it.action = ForeGroundService.Actions.STOP.toString()
                            startService(it)
                        }
                    }){
                        Text(text = "Stop Foreground service")
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airPlaneModeReceiver)
    }
}