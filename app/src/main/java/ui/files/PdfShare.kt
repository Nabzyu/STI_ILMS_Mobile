package com.example.stismartlib.ui.files

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

/** Saves [bytes] to the app cache and opens the share sheet (open, save, or send the PDF). */
fun sharePdf(context: Context, bytes: ByteArray, fileName: String, chooserTitle: String) {
    val file = File(context.cacheDir, fileName)
    file.writeBytes(bytes)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val share = Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(share, chooserTitle))
}
