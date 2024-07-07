package com.healbe.complex_example.dashboard

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class Exporter(val context: Context) {

    fun createFile(fileName: String, content: String): File? {
        val path = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        val file = File(path, fileName)
        return try {
            FileOutputStream(file).use { outputStream ->
                outputStream.write(content.toByteArray())
            }
            file
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share data via"))
    }
}