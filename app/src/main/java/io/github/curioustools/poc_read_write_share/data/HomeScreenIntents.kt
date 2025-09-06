package io.github.curioustools.poc_read_write_share.data

sealed interface HomeScreenIntents {
    data object OpenXMLClick: HomeScreenIntents

    data class ChangePDFPageClick(val isForward: Boolean): HomeScreenIntents

    data class ReadMediaFromSystemClick(
        val mediaType: RWSMediaType
    ) : HomeScreenIntents

    data class ShareMediaClick(
        val mediaType: RWSMediaType
    ) : HomeScreenIntents

    data class DownloadMediaSAFClick(
        val mediaType: RWSMediaType
    ) : HomeScreenIntents
    data class DownloadMediaDirectClick(
        val mediaType: RWSMediaType
    ) : HomeScreenIntents

}