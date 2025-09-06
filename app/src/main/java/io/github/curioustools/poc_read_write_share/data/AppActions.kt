package io.github.curioustools.poc_read_write_share.data

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher

sealed interface AppActions{
    data object DoNothing : AppActions
    data class ShowToast(
        val str: String,
        val duration: Int = Toast.LENGTH_SHORT
    ) : AppActions
    data class ShowSnackBar(val config:SnackBarConfig?  = null): AppActions

    data class LaunchByCallback(
        val callback: (context: Context) -> Unit
    ) : AppActions

    data class LaunchWithResultLauncher(
        val callback: (
            context: Context,
            getFileFromSystem: ActivityResultLauncher<Intent>,
            getPDFFromSystem: ActivityResultLauncher<Intent>,
            getImageFromSystem: ActivityResultLauncher<Intent>,
            saveFileToSystemSAF: ActivityResultLauncher<String>,
            savePDFToSystemSAF: ActivityResultLauncher<String>,
            saveImageToSystemSAF: ActivityResultLauncher<String>,
        ) -> Unit
    ) : AppActions
}
data class SnackBarConfig(val str: String, val onclick:(()-> Unit)? = null)