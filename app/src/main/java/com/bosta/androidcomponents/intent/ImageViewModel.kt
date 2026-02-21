package com.bosta.androidcomponents.intent

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {
    val imageUri = mutableStateOf<Uri?>(null)
    fun updateUrl(uri: Uri?) {
        imageUri.value = uri
    }

}