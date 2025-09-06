package io.github.curioustools.poc_read_write_share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen() {
    Column(Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(8.dp)
    ) {


        var selectedIndex by remember { mutableIntStateOf(-1) }
        val options = listOf("Choose Image", "Choose PDF", "Choose any File")

        Button(
            modifier = Modifier.padding(0.dp).fillMaxWidth().defaultMinSize(1.dp,1.dp),
            contentPadding = PaddingValues(0.dp),
            shape = RectangleShape,
            onClick = {}
        ) {
            Text(
                text = "Click Here for XML Version",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp),
            )
        }


        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                    onClick = { selectedIndex = index },
                    selected = index == selectedIndex
                ) {
                    Text(label)
                }
            }
        }
        FilePreview()
        Spacer(Modifier.size(12.dp))
        PDFPreview()
        Spacer(Modifier.size(12.dp))
        ImagePreview()

    }
}


@Composable
fun PDFPreview() {
    Card(Modifier.padding(8.dp).fillMaxWidth().height(600.dp)) {
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
                ActionsRow(modifier = Modifier.padding(horizontal = 8.dp), "PDF")
            }

            Button(
                modifier = Modifier.padding(0.dp).defaultMinSize(1.dp,1.dp).align(Alignment.CenterStart),
                contentPadding = PaddingValues(0.dp),
                onClick = {}
            ) {
                Image(
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    painter = painterResource(id = R.drawable.back),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }


            Button(
                modifier = Modifier.padding(0.dp).defaultMinSize(1.dp,1.dp).align(Alignment.CenterEnd),
                contentPadding = PaddingValues(0.dp),
                onClick = {}
            ) {
                Image(
                    modifier = Modifier.size(40.dp).padding(8.dp),
                    painter = painterResource(id = R.drawable.forward),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }



        }

    }
}

@Composable
fun ImagePreview() {
    Card(Modifier.padding(8.dp).fillMaxWidth().height(600.dp)) {
        Column(Modifier.fillMaxSize()) {
            Column(Modifier.weight(1f)) {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.image_placeholder),
                    colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.surface),
                    contentDescription = "folder"
                )
            }
            ActionsRow(modifier = Modifier.padding(horizontal = 8.dp), "Image")
        }
    }
}

@Composable
fun FilePreview() {
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
                ActionsRow( text = "File")
            }

        }
    }

}

@Composable
fun ActionsRow(modifier: Modifier = Modifier, text:String) {
    Row (modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Button(
            modifier = Modifier.padding(0.dp).defaultMinSize(1.dp,1.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = false,
            onClick = {}
        ) {
            Text(
                text = "Share this $text",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
        }

        Button(
            modifier = Modifier.padding(0.dp).defaultMinSize(1.dp,1.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = false,
            onClick = {}
        ) {
            Text(
                text = "Download this $text (SAF)",
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
                fontSize = 10.sp
            )
        }


        Button(
            modifier = Modifier.padding(0.dp).defaultMinSize(1.dp,1.dp),
            contentPadding = PaddingValues(0.dp),
            enabled = false,
            onClick = {}
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
    HomeScreen()
}
