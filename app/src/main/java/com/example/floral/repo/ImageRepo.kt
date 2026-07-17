package com.example.floral.repo

import android.content.Context
import android.net.Uri

interface ImageRepo {
    fun uploadImage(
        context: Context,
        imageUri: Uri,
        callback: (String?) -> Unit
    )

    fun getFileNameFromUri(
        context: Context,
        imageUri: Uri
    ): String?
}
