package io.github.curioustools.poc_read_write_share.screens

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.CreateDocument
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle.State.STARTED
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import coil3.Bitmap
import com.google.android.material.snackbar.Snackbar
import io.github.curioustools.poc_read_write_share.data.AppActions
import io.github.curioustools.poc_read_write_share.data.HomeScreenIntents
import io.github.curioustools.poc_read_write_share.data.HomeScreenState
import io.github.curioustools.poc_read_write_share.R
import io.github.curioustools.poc_read_write_share.data.RWSMediaType
import io.github.curioustools.poc_read_write_share.data.RWSViewModel
import io.github.curioustools.poc_read_write_share.data.SnackBarConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun HomeScreen() {
    val rwsViewModel = hiltViewModel<RWSViewModel>()
    val screenOwner = LocalLifecycleOwner.current
    val screenContext = LocalContext.current
    val getFileFromSystem= rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        rwsViewModel.onGetFromSystemResult(result, RWSMediaType.FILE,screenContext)
    }
    val getPDFFromSystem= rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        rwsViewModel.onGetFromSystemResult(result, RWSMediaType.PDF,screenContext)
    }
    val getImageFromSystem= rememberLauncherForActivityResult(StartActivityForResult()) { result ->
        rwsViewModel.onGetFromSystemResult(result, RWSMediaType.IMAGE,screenContext)
    }

    val saveFileToSystemSAF=rememberLauncherForActivityResult(CreateDocument("*/*")) { result ->
        rwsViewModel.onSaveToSystemSAFResult(result, RWSMediaType.FILE,screenContext)
    }
    val savePDFToSystemSAF=rememberLauncherForActivityResult(CreateDocument("application/pdf")) { result ->
        rwsViewModel.onSaveToSystemSAFResult(result, RWSMediaType.PDF,screenContext)
    }
    val saveImageToSystemSAF=rememberLauncherForActivityResult(CreateDocument("image/png")) { result ->
        rwsViewModel.onSaveToSystemSAFResult(result, RWSMediaType.IMAGE,screenContext)
    }

    var snackBarConfig by remember { mutableStateOf<SnackBarConfig?>(null) }


    val state by rwsViewModel.homeScreenState.collectAsStateWithLifecycle()
    val appActions = rwsViewModel.appActions
    LaunchedEffect(Unit) {
        appActions.flowWithLifecycle(screenOwner.lifecycle, STARTED).collect { action ->
            when(action){
                AppActions.DoNothing -> {}
                is AppActions.LaunchByCallback -> action.callback.invoke(screenContext)
                is AppActions.LaunchWithResultLauncher -> action.callback(
                      screenContext,
                      getFileFromSystem,
                      getPDFFromSystem,
                      getImageFromSystem,
                      saveFileToSystemSAF,
                      savePDFToSystemSAF,
                      saveImageToSystemSAF
                )
                is AppActions.ShowSnackBar -> snackBarConfig = action.config
                is AppActions.ShowToast -> Toast.makeText(screenContext, action.str, action.duration).show()
            }
        }
    }
    Box(Modifier.fillMaxSize()) {
        HomeScreenUI(state) { rwsViewModel.handleIntent(it) }
        AnimatedSnackBarHost(snackBarConfig) { snackBarConfig = null }
    }

}
@Composable
fun HomeScreenUI(
    state: HomeScreenState,
    onClick:(HomeScreenIntents)-> Unit
) {
    Column(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(8.dp)
    )
    {
        var selectedIndex by remember { mutableIntStateOf(-1) }
        val options = listOf("Choose Image" to RWSMediaType.IMAGE, "Choose PDF" to RWSMediaType.PDF, "Choose any File" to RWSMediaType.FILE)

        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = {
                        selectedIndex = index
                        onClick.invoke(HomeScreenIntents.ReadMediaFromSystemClick(label.second))
                              },
                    selected = index == selectedIndex
                ) {
                    Text(label.first)
                }
            }
        }
        state.mediaFile?.let {
            FilePreview(state,onClick)
            Spacer(Modifier.size(12.dp))
        }
        state.pdfFile?.let {
            PDFPreview(state,onClick)
            Spacer(Modifier.size(12.dp))
        }
        state.imageFile?.let {
            ImagePreview(state,onClick)
        }
        Button(
            modifier = Modifier
                .padding(0.dp)
                .fillMaxWidth()
                .defaultMinSize(1.dp, 1.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RectangleShape,
            onClick = {onClick.invoke(HomeScreenIntents.OpenXMLClick)}
        ) {
            Text(
                text = "Click Here for XML Version",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            )
        }


    }
}


@Composable
fun PDFPreview(state: HomeScreenState, onClick: (HomeScreenIntents) -> Unit) {
    val bitmap = remember(state.pdfFileCurrPage) {
        state.pdfFile?.pdfFileToBitmap(state.pdfFileCurrPage)
    }
    Card(Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .height(600.dp)) {
        Box(Modifier.fillMaxSize()){
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f)) {
                    if (bitmap!=null){
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "folder"
                        )
                    }else{
                        Image(
                            modifier = Modifier.fillMaxSize(),
                            painter = painterResource(id = R.drawable.image_placeholder),
                            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                            contentDescription = "folder"
                        )
                    }
                }
                ActionsRow(modifier = Modifier.padding(horizontal = 8.dp), RWSMediaType.PDF,{null},onClick)
            }

            Button(
                modifier = Modifier
                    .padding(0.dp)
                    .defaultMinSize(1.dp, 1.dp)
                    .align(Alignment.CenterStart),
                contentPadding = PaddingValues(0.dp),
                onClick = {onClick.invoke(HomeScreenIntents.ChangePDFPageClick(false))}
            ) {
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.back),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }


            Button(
                modifier = Modifier
                    .padding(0.dp)
                    .defaultMinSize(1.dp, 1.dp)
                    .align(Alignment.CenterEnd),
                contentPadding = PaddingValues(0.dp),
                onClick = {onClick.invoke(HomeScreenIntents.ChangePDFPageClick(true))}
            ) {
                Image(
                    modifier = Modifier
                        .size(40.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.forward),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }



        }

    }
}

@Composable
fun ImagePreview(state: HomeScreenState, onClick: (HomeScreenIntents) -> Unit) {
    val bitmap = remember(state.imageFile) {
        state.imageFile?.imageFileToBitmap()
    }

    Card(Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .height(600.dp)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f)) {
                if (bitmap!=null){
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "folder"
                    )
                }else{
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.image_placeholder),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                        contentDescription = "folder"
                    )
                }
            }
            ActionsRow(modifier = Modifier.padding(horizontal = 8.dp),  RWSMediaType.IMAGE,{bitmap},onClick)
        }
    }
}

@Composable
fun FilePreview(state: HomeScreenState, onClick: (HomeScreenIntents) -> Unit) {
    Card(Modifier
        .fillMaxWidth()
        .padding(8.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(
                modifier = Modifier
                    .size(48.dp)
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.file),
                contentDescription = "folder",
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),

                )
            Column(Modifier
                .weight(1f)
                .padding(8.dp)) {
                Text(state.mediaFileInfo?.prettyString().orEmpty())
                ActionsRow(modifier = Modifier,  RWSMediaType.FILE,{null},onClick)
            }

        }
    }

}

@Composable
fun ActionsRow(modifier: Modifier = Modifier, mediaType: RWSMediaType,
               requestBitMap:()-> Bitmap?,
               onClick: (HomeScreenIntents) -> Unit
) {
    val context = LocalContext.current

    Row (modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val text = when(mediaType) {
            RWSMediaType.IMAGE -> "Image"
            RWSMediaType.PDF -> "PDF"
            RWSMediaType.FILE -> "File"
        }

        Button(
            modifier = Modifier
                .padding(0.dp)
                .defaultMinSize(1.dp, 1.dp),
            contentPadding = PaddingValues(0.dp),
            
            onClick = {onClick.invoke(HomeScreenIntents.ShareMediaClick(mediaType,requestBitMap(),context))}
        ) {
            Text(
                text = "Share this $text",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
        }

        Button(
            modifier = Modifier
                .padding(0.dp)
                .defaultMinSize(1.dp, 1.dp),
            contentPadding = PaddingValues(0.dp),
            
            onClick = {onClick.invoke(HomeScreenIntents.DownloadMediaSAFClick(mediaType,requestBitMap()))}
        ) {
            Text(
                text = "Download this $text (SAF)",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
        }


        Button(
            modifier = Modifier
                .padding(0.dp)
                .defaultMinSize(1.dp, 1.dp),
            contentPadding = PaddingValues(0.dp),
            
            onClick = {onClick.invoke(HomeScreenIntents.DownloadMediaDirectClick(mediaType,requestBitMap(),context))}
        ) {
            Text(
                text = "Download this $text (Direct)",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
        }





    }
}

@Preview
@Composable
fun AnimatedSnackBarHost(
    config: SnackBarConfig? = SnackBarConfig("Hello", {}),
    onDismiss:()-> Unit = {}
    ) {
    val isVisible = config!=null

    Box(modifier = Modifier.Companion.fillMaxSize()) {
        AnimatedVisibility(
            visible = isVisible,
            enter = slideInVertically(
                initialOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { fullHeight -> fullHeight },
                animationSpec = tween(durationMillis = 300)
            ),
            modifier = Modifier.Companion
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
                .align(Alignment.Companion.BottomCenter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier
                .fillMaxWidth()
                .background(
                    Color.DarkGray, RoundedCornerShape(12)
                )
                .clip(RoundedCornerShape(12))
                .padding(horizontal = 8.dp)) {

                Text(modifier = Modifier.weight(1f), text = config?.str.orEmpty(), color = Color.White)
                config?.onclick?.let {
                    Button(
                        modifier = Modifier
                            .padding(0.dp)
                            .defaultMinSize(1.dp, 1.dp),
                        contentPadding = PaddingValues(0.dp),
                        onClick = it
                    ) {
                        Text(
                            text = "View",
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                            fontSize = 10.sp
                        )
                    }

                }

            }
        }
    }

    LaunchedEffect(config) {
        if (isVisible) {
            delay(3000)
            onDismiss.invoke()
        }
    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

    HomeScreenUI(
        state = HomeScreenState(
            imageFile = File(""),
            pdfFile = File(""),
            mediaFile = File(""),
            pdfFileCurrPage = 0
        )
    ){}
}
