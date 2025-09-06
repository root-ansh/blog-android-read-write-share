package io.github.curioustools.poc_read_write_share.data

import android.graphics.Bitmap
import io.github.curioustools.poc_read_write_share.screens.FileInfo
import java.io.File

data class HomeScreenState(
    val imageFile: File? = null,
    val pdfFile: File? = null,
    val mediaFile: File? = null,
    val imageFileInfo: FileInfo? = null,
    val pdfFileInfo: FileInfo? = null,
    val mediaFileInfo: FileInfo? = null,
    val pdfFileCurrPage: Int = 0,

    val bitmapToBeSaved: Bitmap? = null,
    val fileToBeSaved: File? = null
)