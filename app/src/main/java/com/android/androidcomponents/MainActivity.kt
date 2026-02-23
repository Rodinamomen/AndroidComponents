package com.android.androidcomponents

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.IntentFilter
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.KeyboardType.Companion.Uri
import androidx.core.app.ActivityCompat
import androidx.work.Constraints
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import coil.compose.AsyncImage
import com.android.androidcomponents.broadcastreciever.AirPlaneModeReceiver
import com.android.androidcomponents.intent.SecondActivity
import com.android.androidcomponents.services.background.BackGroundService
import com.android.androidcomponents.services.foreground.ForeGroundService
import com.android.androidcomponents.ui.theme.AndroidComponentsTheme
import com.android.androidcomponents.workmanager.PhotoCompressionWorker
import com.android.androidcomponents.workmanager.PhotoViewModel

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
    private lateinit var workManager: WorkManager
    private val viewModel by viewModels<PhotoViewModel>()
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
        workManager = WorkManager.getInstance(applicationContext)
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
                    }) {
                        Text(text = "Send Foreground service")
                    }
                    Button(onClick = {
                        Intent(applicationContext, ForeGroundService::class.java).also {
                            it.action = ForeGroundService.Actions.STOP.toString()
                            startService(it)
                        }
                    }) {
                        Text(text = "Stop Foreground service")
                    }
                    // Observe the WorkManager task status by its unique ID stored in the ViewModel.
                    // getWorkInfoByIdLiveData() returns a LiveData that emits updates every time the
                    // worker's state changes (ENQUEUED → RUNNING → SUCCEEDED / FAILED).
                    // observeAsState() bridges the LiveData into Compose state, triggering recomposition
                    // automatically. If no work ID exists yet, workerResult will be null.
                    val workerResult = viewModel.workId?.let { id ->
                        workManager.getWorkInfoByIdLiveData(id).observeAsState().value
                    }

                    // React to changes in the worker's output data.
                    // LaunchedEffect re-runs its block whenever outputData changes, ensuring we only
                    // process the result once it is actually available (i.e., after the worker succeeds).
                    LaunchedEffect(workerResult?.outputData) {
                        if (workerResult?.outputData != null) {

                            // Read the compressed image file path from the worker's output data bundle.
                            val filePath =
                                workerResult?.outputData?.getString(PhotoCompressionWorker.KEY_RESULT_PATH)

                            filePath?.let {
                                // Decode the file at the given path into a Bitmap and push it to the ViewModel
                                // so the UI can display the compressed result.
                                val bitMap = BitmapFactory.decodeFile(it)
                                viewModel.updateCompressedBitmap(bitMap)
                            }
                        }
                    }

                    // Display the original uncompressed image if one has been selected.
                    // AsyncImage loads the image from the URI asynchronously,
                    // handling caching and decoding off the main thread automatically.
                    viewModel.unCompressedUri?.let {
                        Text("unCompressedUri")
                        AsyncImage(model = it, contentDescription = null)
                    }

                    // Display the compressed image once the worker has finished and the ViewModel
                    // has been updated with the result Bitmap.
                    viewModel.compressedBitmap?.let {
                        Text("compressedBitmap")
                        Image(bitmap = it.asImageBitmap(), contentDescription = null)
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(airPlaneModeReceiver)
    }

    /**
     * Called when the activity is already running and a new intent is delivered to it.
     * This happens when another app shares an image to this activity via the Android share sheet.
     *
     * In this function we extract the shared image URI from the intent, build a WorkManager
     * request to compress it in the background, and enqueue it for execution.
     *
     * @param intent The new intent that was delivered to the activity, containing the shared data.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Extract the shared image URI from the intent.
        // Android 13 (Tiramisu) introduced a type-safe version of getParcelableExtra() that
        // requires the expected class to be passed explicitly, replacing the deprecated version
        // used on older API levels. Both approaches return the same Uri, just via different APIs.
        val uri: Uri = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            intent?.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) as? Uri // 👈 explicit cast
        } ?: return) as Uri // If no URI was shared, there is nothing to compress — exit early.
        viewModel.updateUnCompressedUri(uri)
        // Build a one-time WorkRequest targeting our PhotoCompressionWorker.
        // OneTimeWorkRequest means this task will run once and not repeat.
        // Use PeriodicWorkRequest instead if the task should repeat on a schedule.
        val request = OneTimeWorkRequestBuilder<PhotoCompressionWorker>()

            // Pass input data to the worker as key-value pairs.
            // The worker will read these values inside doWork() via params.inputData.
            .setInputData(
                workDataOf(
                    // The URI of the image shared by the other app, converted to a String
                    // because WorkManager's Data class does not support Uri directly.
                    PhotoCompressionWorker.KEY_CONTENT_URI to uri.toString(),

                    // The maximum allowed output file size: 20 KB (1024 * 20 bytes).
                    // The worker will keep reducing JPEG quality until it hits this threshold.
                    PhotoCompressionWorker.KEY_COMPRESSION_THRESHOLD to 1024 * 20L
                )
            )

            // Set constraints that must be satisfied before WorkManager executes this task.
            // requiresStorageNotLow = true means the task will be delayed if the device
            // is reporting low storage, preventing the worker from writing to disk unsafely.
            .setConstraints(
                Constraints(requiresStorageNotLow = true)
            )
            .build()
        viewModel.updateWorkId(request.id)
        // Hand the request off to WorkManager. It will schedule and execute the worker
        // in the background, honouring the constraints and surviving app process death.
        workManager.enqueue(request)
    }
}