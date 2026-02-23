package com.android.androidcomponents.workmanager

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import java.util.UUID

class PhotoViewModel : ViewModel() {
    var unCompressedUri: Uri? by mutableStateOf(null)
        private set
    var compressedBitmap: Bitmap? by mutableStateOf(null)
        private set
    var workId: UUID? by mutableStateOf(null)
        private set

    fun updateUnCompressedUri(uri: Uri?) {
        unCompressedUri = uri
    }

    fun updateCompressedBitmap(bitmap: Bitmap?) {
        compressedBitmap = bitmap
    }

    fun updateWorkId(id: UUID?) {
        workId = id
    }
}