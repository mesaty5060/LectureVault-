package com.lecturevault.app.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun notesDir(context: Context): File {
        val dir = File(context.filesDir, "notes")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    fun newImageFile(context: Context): File {
        val name = "img_${System.currentTimeMillis()}.jpg"
        return File(notesDir(context), name)
    }

    fun copyUriToInternal(context: Context, src: Uri): String {
        val target = newImageFile(context)
        context.contentResolver.openInputStream(src)?.use { input: InputStream ->
            FileOutputStream(target).use { out -> input.copyTo(out) }
        }
        return target.absolutePath
    }
}

object PdfExporter {
    fun exportNoteToPdf(context: Context, name: String, imagePaths: List<String>): Uri? {
        if (imagePaths.isEmpty()) return null
        val doc = PdfDocument()
        imagePaths.forEachIndexed { index, path ->
            val bitmap: Bitmap = BitmapFactory.decodeFile(path) ?: return@forEachIndexed
            val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
            val page = doc.startPage(pageInfo)
            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            doc.finishPage(page)
            bitmap.recycle()
        }
        val out = File(FileUtils.notesDir(context), "$name.pdf")
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", out)
    }
}
