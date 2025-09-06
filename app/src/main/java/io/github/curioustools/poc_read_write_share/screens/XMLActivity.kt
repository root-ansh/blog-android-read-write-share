package io.github.curioustools.poc_read_write_share.screens

import android.content.Intent
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import coil3.Bitmap
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import io.github.curioustools.poc_read_write_share.data.HomeScreenState
import io.github.curioustools.poc_read_write_share.data.RWSMediaType
import io.github.curioustools.poc_read_write_share.data.RWSMediaType.FILE
import io.github.curioustools.poc_read_write_share.data.RWSMediaType.IMAGE
import io.github.curioustools.poc_read_write_share.data.RWSMediaType.PDF
import io.github.curioustools.poc_read_write_share.databinding.ActivityXmlBinding
import io.github.curioustools.poc_read_write_share.screens.FileInfo.Companion.toLocalFile
import kotlinx.coroutines.launch
import java.io.File


@AndroidEntryPoint
class XMLActivity : AppCompatActivity() {
    private val binding: ActivityXmlBinding by lazy { ActivityXmlBinding.inflate(layoutInflater) }

    private val getFileFromSystem: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            lifecycleScope.launch {
                val file = uri?.toLocalFile(this@XMLActivity)
                state = HomeScreenState(
                    mediaFile = file,
                    mediaFileInfo = FileInfo.fromSAFUri(this@XMLActivity,uri)
                )
                updateUi()
            }

        }

    }
    private val getPDFFromSystem: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            lifecycleScope.launch {
                val file = uri?.toLocalFile(this@XMLActivity)
                state = HomeScreenState(
                    pdfFile = file,
                    pdfFileInfo = FileInfo.fromSAFUri(this@XMLActivity,uri)
                )
                updateUi()

            }
        }
    }
    private val getImageFromSystem: ActivityResultLauncher<Intent> = registerForActivityResult(StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val uri = result.data?.data
            lifecycleScope.launch {
                val file = uri?.toLocalFile(this@XMLActivity)
                state = HomeScreenState(
                    imageFile = file,
                    imageFileInfo = FileInfo.fromSAFUri(this@XMLActivity,uri)
                )
                updateUi()
            }
        }
    }

    private val saveFileToSystemSAF: ActivityResultLauncher<String> = registerForActivityResult(CreateDocument("*/*")) { result ->
        val uri = result
        val file = state.fileToBeSaved
        val context = this
        if (uri!=null && file!=null){
            lifecycleScope.launch {
                context.saveFileToUserSelectedPath(uri,file)
                Snackbar
                    .make(binding.root,"Saved file :${file.name} to user selected directory", Snackbar.LENGTH_SHORT)
                    .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                    .show()
            }
        }

    }
    private val savePDFToSystemSAF:ActivityResultLauncher<String> = registerForActivityResult(CreateDocument("application/pdf")) { result ->
        val uri = result
        val file = state.fileToBeSaved
        val context = this
        if (uri!=null && file!=null){
            lifecycleScope.launch {
                context.saveFileToUserSelectedPath(uri,file)
                Snackbar
                    .make(binding.root,"Saved file :${file.name} to user selected directory", Snackbar.LENGTH_SHORT)
                    .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                    .show()
            }
        }
    }
    private val saveImageToSystemSAF:ActivityResultLauncher<String> = registerForActivityResult(CreateDocument("image/png")) { result ->
        val uri = result
        val file = state.bitmapToBeSaved
        val context = this
        if (uri!=null && file!=null){
            lifecycleScope.launch {
                context.saveBitmapToUserSelectedPath(uri,file)
                Snackbar.make(binding.root,"Saved currentlyVisibleImage to user selected directory", Snackbar.LENGTH_SHORT)
                    .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                    .show()
            }
        }
    }

    private var state = HomeScreenState()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        updateUi()
    }


    private fun updateUi() {
        with(binding) {
            state.pdfFile.let {
                clPdfPreview.visibility = if(it!=null) View.VISIBLE else View.GONE
                if(it!=null){
                    pdfPreviewImage.setImageBitmap(it.pdfFileToBitmap(state.pdfFileCurrPage))
                }
            }
            state.imageFile.let {
                clImagePreview.visibility = if(it!=null) View.VISIBLE else View.GONE
                if(it!=null){
                    imagePreviewPlaceholder.setImageBitmap(it.imageFileToBitmap())
                }
            }

            state.mediaFile.let {
                clFilePreview.visibility = if(it!=null) View.VISIBLE else View.GONE
                tvFileInfo.text = state.mediaFileInfo?.prettyString()?:"No Info available"
            }

            btnPdfBack.setOnClickListener {
                state = state.copy(pdfFileCurrPage = state.pdfFileCurrPage-1)
                updateUi()
            }
            btnPdfForward.setOnClickListener {
                state = state.copy(pdfFileCurrPage = state.pdfFileCurrPage+1)
                updateUi()
                 }

            arrayOf(
                btnDownloadDirectFile to RWSMediaType.FILE,
                btnDownloadDirectPdf to RWSMediaType.PDF,
                btnDownloadDirectImage to RWSMediaType.IMAGE
            ).map { (btn,type) ->
                btn.setOnClickListener { downloadMediaToUserMemoryDirect(type)}
            }

            arrayOf(
                btnDownloadSafFile to RWSMediaType.FILE,
                btnDownloadSafPdf to RWSMediaType.PDF,
                btnDownloadSafImage to RWSMediaType.IMAGE
            ).map { (btn,type) ->
                btn.setOnClickListener { downloadMediaToUserMemorySAF(type) }
            }
            arrayOf(
                btnShareFile to RWSMediaType.FILE,
                btnSharePdf to RWSMediaType.PDF,
                btnShareImage to RWSMediaType.IMAGE
            ).map { (btn,type) ->
                btn.setOnClickListener { shareMedia(type) }
            }
            arrayOf(
                btnChooseAnyFile to RWSMediaType.FILE,
                btnChoosePdf to RWSMediaType.PDF,
                btnChooseImage to RWSMediaType.IMAGE
            ).map { (btn,type) ->
                btn.setOnClickListener {pickMediaFromUserMemory(type) }
            }


        }
    }

    private fun shareMedia(shareType: RWSMediaType) {
        val context = this
        when(shareType){
            IMAGE -> {
                lifecycleScope.launch {
                    val cachePath = File(context.cacheDir, "images")
                    cachePath.mkdirs()
                    val file = File(cachePath, "${System.currentTimeMillis()}.png")
                    val bitmap = getCurrentImageViewBitmap()
                    context.saveBitmapToUserSelectedPath(Uri.fromFile(file),bitmap)
                    context.shareLocalFile(file)
                }
            }
            PDF ,FILE -> {
                val file = if(shareType==PDF) state.pdfFile!! else state.mediaFile!!
                context.shareLocalFile(file)

            }
        }
    }

    private fun pickMediaFromUserMemory(pickerType: RWSMediaType) {
        when(pickerType){
            IMAGE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "image/*"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*")) // required in some android versions. also good if you set type as */* and want to filter mimetypes
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)// optional for our app, but required if you intend to save the  SAF content uris(temporary by nature) in databases
                }
                getImageFromSystem.launch(intent)
            }
            PDF -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("application/pdf")) // required in some android versions. also good if you set type as */* and want to filter mimetypes
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)// optional for our app, but required if you intend to save the  SAF content uris(temporary by nature) in databases
                }
                getPDFFromSystem.launch(intent)
            }
            FILE -> {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    type = "*/*"
                    addCategory(Intent.CATEGORY_OPENABLE)
                    addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION)// optional for our app, but required if you intend to save the  SAF content uris(temporary by nature) in databases
                }
                getFileFromSystem.launch(intent)
            }
        }

    }

    private fun getCurrentImageViewBitmap(): Bitmap{
        val drawable = binding.imagePreviewPlaceholder.drawable
        val bitmap = if (drawable is BitmapDrawable) {
            drawable.bitmap
        } else {
            val bitmap = createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight)
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
        return bitmap
    }

    private fun downloadMediaToUserMemoryDirect(downloadType: RWSMediaType) {
        val context =this
        when(downloadType){
            RWSMediaType.IMAGE -> {

                lifecycleScope.launch {
                    val uri = context.saveBitmapToUserMemory(getCurrentImageViewBitmap())
                    Snackbar
                        .make(binding.root,"Saved file :currentlyVisibleImage to downloads", Snackbar.LENGTH_SHORT)
                        .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                        .show()
                }

            }
            RWSMediaType.PDF -> {
                lifecycleScope.launch {
                    val file = state.pdfFile!!
                    val uri = context.saveFileToUserMemory(file)
                    Snackbar
                        .make(binding.root,"Saved file :${file.name} to downloads", Snackbar.LENGTH_SHORT)
                        .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                        .show()
                }

            }
            RWSMediaType.FILE -> {
                lifecycleScope.launch {
                    val file = state.mediaFile!!
                    val uri = context.saveFileToUserMemory(file)
                    Snackbar
                        .make(binding.root,"Saved file :${file.name} to downloads", Snackbar.LENGTH_SHORT)
                        .setAction("show"){context.openSystemViewerForSAFUri(uri)}
                        .show()
                }

            }
        }

    }

    private fun downloadMediaToUserMemorySAF(downloadType: RWSMediaType) {
        when(downloadType){
            RWSMediaType.IMAGE -> {

                state =state.copy(bitmapToBeSaved = getCurrentImageViewBitmap())

                saveImageToSystemSAF.launch(state.imageFile?.name.orEmpty())
            }
            RWSMediaType.PDF -> {
                state = state.copy(fileToBeSaved = state.pdfFile)
                savePDFToSystemSAF.launch(state.pdfFile?.name.orEmpty())
            }
            RWSMediaType.FILE -> {
                state = state.copy(fileToBeSaved = state.mediaFile)
                saveFileToSystemSAF.launch(state.mediaFile?.name.orEmpty())
            }
        }
    }
}


