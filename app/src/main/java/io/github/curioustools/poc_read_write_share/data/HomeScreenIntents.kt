package io.github.curioustools.poc_read_write_share.data

import android.content.Context
import coil3.Bitmap

sealed interface HomeScreenIntents {
    data object OpenXMLClick: HomeScreenIntents

    data class ChangePDFPageClick(val isForward: Boolean): HomeScreenIntents

    data class ReadMediaFromSystemClick(
        val mediaType: RWSMediaType
    ) : HomeScreenIntents

    data class ShareMediaClick(
        val mediaType: RWSMediaType,
        val bitmap: Bitmap?,
        val context: Context
    ) : HomeScreenIntents

    data class DownloadMediaSAFClick(
        val mediaType: RWSMediaType,
        val bitmap: Bitmap?,
    ) : HomeScreenIntents
    data class DownloadMediaDirectClick(
        val mediaType: RWSMediaType,
        val bitmap: Bitmap?,
        val context: Context
    ) : HomeScreenIntents

}