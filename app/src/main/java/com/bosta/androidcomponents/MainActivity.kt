package com.bosta.androidcomponents

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.bosta.androidcomponents.intent.SecondActivity
import com.bosta.androidcomponents.ui.theme.AndroidComponentsTheme

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
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
                }
            }
        }
    }
}