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

    data class LaunchByCallback(
        val callback: (context: Context) -> Unit
    ) : AppActions

    data class LaunchWithResultLauncher(
        val callback: (
            context: Context,
            resultActivityLauncher: ActivityResultLauncher<Intent>,
            permissionLauncher: ActivityResultLauncher<Array<String>>
        ) -> Unit
    ) : AppActions


}