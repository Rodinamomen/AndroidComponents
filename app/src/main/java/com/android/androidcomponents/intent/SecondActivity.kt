package com.android.androidcomponents.intent

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage

/*
 * IMPLICIT INTENT RECEIVER — Receiving Shared Image
 * ══════════════════════════════════════════════════
 *
 * This Activity acts as a TARGET for implicit intents.
 * It receives shared images from other apps (Chrome, Gallery, etc.)
 * via ACTION_SEND with mimeType = "image/*" declared in the manifest.
 *
 * Flow:
 *  Other App (e.g. Chrome)
 *      → shares image
 *      → Android finds this Activity via intent-filter
 *      → SecondActivity launched with image URI in EXTRA_STREAM
 *      → Image is displayed using Coil's AsyncImage
**/*/
class SecondActivity : ComponentActivity() {
    private val viewModel: ImageViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onNewIntent(intent)
        setContent {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = "Second Activity", color = Color.Red)
                AsyncImage(
                    modifier = Modifier.fillMaxSize(),
                    model = viewModel.imageUri.value,
                    contentDescription = null,
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val uri = intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        viewModel.updateUrl(uri)
    }
}