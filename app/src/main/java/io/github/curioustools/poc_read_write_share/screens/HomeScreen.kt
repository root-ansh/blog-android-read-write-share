package io.github.curioustools.poc_read_write_share.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.compose.foundation.Image
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
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
import io.github.curioustools.poc_read_write_share.data.AppActions
import io.github.curioustools.poc_read_write_share.data.HomeScreenIntents
import io.github.curioustools.poc_read_write_share.data.HomeScreenState
import io.github.curioustools.poc_read_write_share.R
import io.github.curioustools.poc_read_write_share.data.RWSMediaType
import io.github.curioustools.poc_read_write_share.data.RWSViewModel
import java.io.File

@Composable
fun HomeScreen() {
    val rwsViewModel = hiltViewModel<RWSViewModel>()
    val screenOwner = LocalLifecycleOwner.current
    val screenContext = LocalContext.current
    val screenActivityLauncher = rememberLauncherForActivityResult(StartActivityForResult()) { onStartActivityForResultCallBack(it)}
    val screenPermissionLauncher = rememberLauncherForActivityResult(RequestMultiplePermissions()) { onSystemPermissionPopupCallback(it) }

    val state by rwsViewModel.homeScreenState.collectAsStateWithLifecycle()
    val appActions = rwsViewModel.appActions
    LaunchedEffect(Unit) {
        appActions.flowWithLifecycle(screenOwner.lifecycle, STARTED).collect { action ->
            when(action){
                AppActions.DoNothing -> {}
                is AppActions.LaunchByCallback -> action.callback.invoke(screenContext)
                is AppActions.LaunchWithResultLauncher -> action.callback.invoke(screenContext,screenActivityLauncher,screenPermissionLauncher)
                is AppActions.ShowToast -> Toast.makeText(screenContext, action.str, action.duration).show()
            }
        }
    }
    HomeScreenUI(state) { rwsViewModel.handleIntent(it) }

}

fun onSystemPermissionPopupCallback(it: Map<String, Boolean>) {
}
fun onStartActivityForResultCallBack(it: ActivityResult) {
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
    Card(Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .height(600.dp)) {
        Box(Modifier.fillMaxSize()){
            Column(Modifier.fillMaxSize()) {
                Column(Modifier.weight(1f)) {
                    Image(
                        modifier = Modifier.fillMaxSize(),
                        painter = painterResource(id = R.drawable.file),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                        contentDescription = "folder"
                    )
                }
                ActionsRow(modifier = Modifier.padding(horizontal = 8.dp), RWSMediaType.PDF,onClick)
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
    Card(Modifier
        .padding(8.dp)
        .fillMaxWidth()
        .height(600.dp)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f)) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.image_placeholder),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }
            ActionsRow(modifier = Modifier.padding(horizontal = 8.dp),  RWSMediaType.IMAGE,onClick)
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
                Text("Your Selected File Information will come here. Choose a file using top buttons")
                ActionsRow(modifier = Modifier,  RWSMediaType.FILE,onClick)
            }

        }
    }

}

@Composable
fun ActionsRow(modifier: Modifier = Modifier, mediaType: RWSMediaType, onClick: (HomeScreenIntents) -> Unit) {
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
            enabled = false,
            onClick = {onClick.invoke(HomeScreenIntents.ShareMediaClick(mediaType))}
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
            enabled = false,
            onClick = {onClick.invoke(HomeScreenIntents.DownloadMediaSAFClick(mediaType))}
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
            enabled = false,
            onClick = {onClick.invoke(HomeScreenIntents.DownloadMediaDirectClick(mediaType))}
        ) {
            Text(
                text = "Download this $text (Direct)",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
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
