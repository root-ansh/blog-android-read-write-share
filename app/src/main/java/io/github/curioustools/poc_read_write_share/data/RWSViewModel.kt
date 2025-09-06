package io.github.curioustools.poc_read_write_share.data

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.curioustools.poc_read_write_share.screens.XMLActivity
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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
            }
            is HomeScreenIntents.DownloadMediaDirectClick -> {
                emitAction(
                    AppActions.ShowToast("download ${intent.mediaType} direct click")
                )

            }
            is HomeScreenIntents.DownloadMediaSAFClick -> {
                emitAction(
                    AppActions.ShowToast("download ${intent.mediaType} SAF click")
                )
            }
            is HomeScreenIntents.ReadMediaFromSystemClick -> {
                emitAction(
                    AppActions.ShowToast("Read ${intent.mediaType}  click")
                )
            }
            is HomeScreenIntents.ShareMediaClick -> {
                emitAction(
                    AppActions.ShowToast("Sager ${intent.mediaType}  click")
                )
            }
        }
    }

    private fun emitAction(action: AppActions) {
        viewModelScope.launch {
            _appActions.send(action)
        }

    }

}


