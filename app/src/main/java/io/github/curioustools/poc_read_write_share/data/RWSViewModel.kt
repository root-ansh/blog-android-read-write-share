package io.github.curioustools.poc_read_write_share.data

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.curioustools.poc_read_write_share.data.RWSMediaType.*
import io.github.curioustools.poc_read_write_share.screens.FileInfo
import io.github.curioustools.poc_read_write_share.screens.FileInfo.Companion.toLocalFile
import io.github.curioustools.poc_read_write_share.screens.XMLActivity
import io.github.curioustools.poc_read_write_share.screens.openSystemViewerForSAFUri
import io.github.curioustools.poc_read_write_share.screens.saveBitmapToUserMemory
import io.github.curioustools.poc_read_write_share.screens.saveBitmapToUserSelectedPath
import io.github.curioustools.poc_read_write_share.screens.saveFileToUserMemory
import io.github.curioustools.poc_read_write_share.screens.saveFileToUserSelectedPath
import io.github.curioustools.poc_read_write_share.screens.shareLocalFile
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RWSViewModel @Inject constructor() : ViewModel() {
    private val _appActions = Channel<AppActions>(Channel.BUFFERED)
    val appActions = _appActions.receiveAsFlow()

    private val _homeScreenState = MutableStateFlow(HomeScreenState())
    val homeScreenState = _homeScreenState.asStateFlow()

    fun handleIntent(intent: HomeScreenIntents){
        when(intent){
            HomeScreenIntents.OpenXMLClick -> {
                emitAction(
                    AppActions.LaunchByCallback{ context ->
                        context.startActivity(Intent(context, XMLActivity::class.java))
                    }
                )

            }
            is HomeScreenIntents.ChangePDFPageClick -> {
                val curPage = _homeScreenState.value.pdfFileCurrPage
                _homeScreenState.update { it.copy(pdfFileCurrPage = curPage +(if(intent.isForward) 1 else -1)) }
            }
            is HomeScreenIntents.DownloadMediaDirectClick -> {
                viewModelScope.launch {
                    val context = intent.context
                    val bitmap = intent.bitmap
                    val pdfFile = _homeScreenState.value.pdfFile
                    val mediaFile = _homeScreenState.value.mediaFile
                    val uri = when(intent.mediaType){
                        IMAGE -> context.saveBitmapToUserMemory(bitmap!!)
                        PDF -> context.saveFileToUserMemory(pdfFile!!)
                        FILE ->  context.saveFileToUserMemory(mediaFile!!)
                    }
                    emitAction(
                        AppActions.ShowSnackBar(
                            SnackBarConfig(
                                str = "Saved to downloads",
                                onclick = {context.openSystemViewerForSAFUri(uri)}
                            )
                        )
                    )
                }
            }
            is HomeScreenIntents.DownloadMediaSAFClick -> {
                viewModelScope.launch {
                    _homeScreenState.update {
                        it.copy(
                            bitmapToBeSaved = intent.bitmap,
                            fileToBeSaved = if(intent.mediaType==PDF) it.pdfFile else it.mediaFile
                        )
                    }
                }
                emitAction(
                    AppActions.LaunchWithResultLauncher{ context: Context, getFileFromSystem, getPDFFromSystem, getImageFromSystem, saveFileToSystemSAF, savePDFToSystemSAF, saveImageToSystemSAF, ->
                        when(intent.mediaType){
                            IMAGE -> saveImageToSystemSAF.launch(_homeScreenState.value.imageFile?.name.orEmpty())
                            PDF ->  savePDFToSystemSAF.launch(_homeScreenState.value.pdfFile?.name.orEmpty())
                            FILE ->  saveFileToSystemSAF.launch(_homeScreenState.value.mediaFile?.name.orEmpty())
                        }
                    }
                )

            }
            is HomeScreenIntents.ReadMediaFromSystemClick -> {
                emitAction(
                    AppActions.LaunchWithResultLauncher{ context: Context, getFileFromSystem, getPDFFromSystem, getImageFromSystem, saveFileToSystemSAF, savePDFToSystemSAF, saveImageToSystemSAF, ->
                        when(intent.mediaType){
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
                )
            }
            is HomeScreenIntents.ShareMediaClick -> {
                val context = intent.context
                when(intent.mediaType){
                    IMAGE -> {
                        viewModelScope.launch {
                            val cachePath = File(context.cacheDir, "images")
                            cachePath.mkdirs()
                            val file = File(cachePath, "${System.currentTimeMillis()}.png")
                            val bitmap = intent.bitmap!!
                            context.saveBitmapToUserSelectedPath(Uri.fromFile(file),bitmap)
                            context.shareLocalFile(file)
                        }
                    }
                    PDF ,FILE -> {
                        val file: File = if(intent.mediaType==PDF) _homeScreenState.value.pdfFile!! else _homeScreenState.value.mediaFile!!
                        context.shareLocalFile(file)
                    }
                }
            }
        }
    }

    private fun emitAction(action: AppActions) {
        viewModelScope.launch {
            _appActions.send(action)
        }

    }

    fun onGetFromSystemResult(result: ActivityResult, mediaType: RWSMediaType, context: Context) {
        viewModelScope.launch {
            if (result.resultCode == RESULT_OK) {
                val uri = result.data?.data
                val file = uri?.toLocalFile(context)
                _homeScreenState.update {
                    when(mediaType){
                        IMAGE -> HomeScreenState(
                            imageFile = file,
                            imageFileInfo = FileInfo.fromSAFUri(context,uri)
                        )
                        PDF -> HomeScreenState(
                            pdfFile = file,
                            pdfFileInfo = FileInfo.fromSAFUri(context,uri)
                        )
                        FILE -> HomeScreenState(
                            mediaFile = file,
                            mediaFileInfo = FileInfo.fromSAFUri(context,uri)
                        )
                    }

                }

            }
        }
    }

    fun onSaveToSystemSAFResult(result: Uri?, mediaType: RWSMediaType,context: Context) {
        result?:return
        viewModelScope.launch {
            when(mediaType){
                IMAGE -> context.saveBitmapToUserSelectedPath(result,_homeScreenState.value.bitmapToBeSaved!!)
                PDF,FILE -> {
                    context.saveFileToUserSelectedPath(result,_homeScreenState.value.fileToBeSaved!!)
                }
            }
            emitAction(
                AppActions.ShowSnackBar(
                    SnackBarConfig(
                        str = "Saved file  to user selected path",
                        onclick = {context.openSystemViewerForSAFUri(result)}
                    )
                )
            )
        }


    }

}


