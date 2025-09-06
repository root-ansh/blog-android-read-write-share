package io.github.curioustools.poc_read_write_share.screens

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.Formatter
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.graphics.createBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.jvm.Throws

data class FileInfo(
    val nameWithExtension: String,
    val extension: String?,
    val mimeType: String?,
    val size: Long?,
    val sizeFormatted: String?,
    val createdOn: Long?,
    val modifiedOn: Long?,
    val displayPath: String?
){
    fun prettyString(): String{
        return buildString {
            appendLine("Name:${nameWithExtension}")
            appendLine("MimeType:${mimeType}")
            appendLine("Path:${displayPath}")
            appendLine("Size:${sizeFormatted}")
        }
    }
    companion object{
        // gets file info for any system uri(content/file). added @SuppressLint("Range") To avoid warnings for column access
        @SuppressLint("Range")
        fun fromSAFUri(context: Context, uri: Uri?): FileInfo? {
            uri?:return null
            val contentResolver = context.contentResolver

            var name: String = "Unknown"
            var size: Long? = null
            var createdOn: Long? = null
            var modifiedOn: Long? = null
            val mimeType: String? = contentResolver.getType(uri)
            var displayPath: String? = null

            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
                    val dateAddedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED)
                    val dateModifiedIndex = it.getColumnIndex(MediaStore.MediaColumns.DATE_MODIFIED)

                    if (nameIndex != -1) name = it.getString(nameIndex)
                    if (sizeIndex != -1) size = it.getLong(sizeIndex)
                    if (dateAddedIndex != -1) createdOn = it.getLong(dateAddedIndex) * 1000 // convert to ms
                    if (dateModifiedIndex != -1) modifiedOn = it.getLong(dateModifiedIndex) * 1000
                }
            }

            val extension = name.substringAfterLast('.', "")

            val sizeFormatted = size?.let { Formatter.formatFileSize(context, it) }

            displayPath = when (uri.scheme) {
                "content" -> "content://${uri.authority}${uri.path ?: ""}"
                "file" -> uri.path
                else -> uri.toString()
            }

            return FileInfo(
                nameWithExtension = name,
                extension = extension.ifEmpty { null },
                mimeType = mimeType,
                size = size,
                sizeFormatted = sizeFormatted,
                createdOn = createdOn,
                modifiedOn = modifiedOn,
                displayPath = displayPath
            )
        }


        // converts any uri(content/file) to a local temp file first in cache directory for easy read/write
        suspend fun Uri.toLocalFile(context: Context,localFileLocation:File = context.cacheDir): File? {
            val uri = this
            return withContext(Dispatchers.IO) {
                try {
                    val fileInfo = FileInfo.fromSAFUri(context,uri)
                    val fileName = fileInfo?.nameWithExtension?: "${System.currentTimeMillis()}.bin"

                    val tempFile = File(localFileLocation, fileName)

                    context.contentResolver.openInputStream(uri)?.use { inputStream ->
                        FileOutputStream(tempFile).use { outputStream ->
                            val buffer = ByteArray(8 * 1024)
                            var bytesRead: Int
                            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                outputStream.write(buffer, 0, bytesRead)
                            }
                        }
                    }
                    tempFile
                } catch (e: Exception) {
                    e.printStackTrace()
                    null
                }
            }
        }

    }
}

fun File.pdfFileToBitmap(index: Int = 0): Bitmap? {
    val localNonSAFFile = this
    if (!localNonSAFFile.exists()) return null
    try {
        val fileDescriptor = ParcelFileDescriptor.open(localNonSAFFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)
        val pageIndex = when{
            index<=0 -> 0
            index>= renderer.pageCount -> renderer.pageCount
            else -> index
        }
        val page = renderer.openPage(pageIndex)

        val bitmap = createBitmap(page.width, page.height)

        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

        page.close()
        renderer.close()
        fileDescriptor.close()
        return bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }


}

fun File.imageFileToBitmap(): Bitmap? {
    val localNonSAFFile = this

    return if (localNonSAFFile.exists()) {
        BitmapFactory.decodeFile(localNonSAFFile.absolutePath)
    } else {
        null
    }
}


@Throws
suspend fun Context.saveFileToUserSelectedPath(uri: Uri, sourceFile: File): Uri {
    return withContext(Dispatchers.IO) {
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            sourceFile.inputStream().use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        uri
    }
}

@Throws
suspend fun Context.saveBitmapToUserSelectedPath(uri: Uri, bitmap: Bitmap): Uri {
    return withContext(Dispatchers.IO){
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            outputStream.flush()
        }
        uri
    }
}

@Throws
suspend fun Context.saveFileToUserMemory(
    sourceFile: File,
    targetDirectory: String = Environment.DIRECTORY_DOWNLOADS
): Uri {
    return withContext(Dispatchers.IO){
        if (!sourceFile.exists()) error("Source file does not exist")
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) error("Saving to public directories is only supported on Android Q and above")
        val extension = sourceFile.extension.lowercase()
        val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, sourceFile.name)
            put(MediaStore.Downloads.MIME_TYPE, mimeType)
            put(MediaStore.Downloads.RELATIVE_PATH, targetDirectory)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
        saveFileToUserSelectedPath(uri,sourceFile)
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    }
}


suspend fun Context.saveBitmapToUserMemory(
    bitmap: Bitmap,
    targetDirectory: String = Environment.DIRECTORY_DOWNLOADS,
): Uri {
    return withContext(Dispatchers.IO){
        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.Q) error("Saving to public directories is only supported on Android Q and above")
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, "${System.currentTimeMillis()}.png")
            put(MediaStore.Downloads.MIME_TYPE, "image/png")
            put(MediaStore.Downloads.RELATIVE_PATH, targetDirectory)
            put(MediaStore.Downloads.IS_PENDING, 1)
        }
        val resolver = contentResolver
        val uri: Uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)!!
        saveBitmapToUserSelectedPath(uri,bitmap)
        values.clear()
        values.put(MediaStore.Downloads.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        uri
    }
}

fun Context.shareLocalFile(file: File) {
    val context = this
    val mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(file.extension.lowercase()) ?: "application/octet-stream"
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(shareIntent, "Share file via")
    )
}
fun Context.openSystemViewerForSAFUri(uri: Uri?) {
    uri?:return
    val mimeType1 = FileInfo.fromSAFUri(this,uri)?.mimeType
    val extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
    val mimeType2 =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
    val mimeType3 = "*/*"
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, mimeType1?:mimeType2?:mimeType3)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    try {
        startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(this, "No app found to open this file type", Toast.LENGTH_SHORT).show()
    }
}