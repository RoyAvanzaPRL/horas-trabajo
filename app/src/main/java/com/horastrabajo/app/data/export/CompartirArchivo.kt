package com.horastrabajo.app.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import java.io.File

fun directorioExports(context: Context): File =
    File(context.cacheDir, "exports").apply { mkdirs() }

fun compartirArchivo(context: Context, archivo: File, mimeType: String) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Compartir"))
}
