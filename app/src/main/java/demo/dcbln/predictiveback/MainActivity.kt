package demo.dcbln.predictiveback

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import demo.dcbln.predictiveback.ui.samples.Sample
import demo.dcbln.predictiveback.ui.theme.PredictiveBackGestureTheme
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PredictiveBackGestureTheme {
                var selectedSample: Sample? by remember { mutableStateOf(null) }
                BackHandler(enabled = selectedSample != null) { selectedSample = null }
                AnimatedContent(targetState = selectedSample) { sample ->
                    Surface(modifier = Modifier.fillMaxSize()) {
                        if (sample == null) {
                            SampleList { selectedSample = it }
                        } else {
                            sample.content()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SampleList(onSampleSelect: (Sample) -> Unit) {
    Scaffold(
        topBar = { PredictiveBackAppBar() },
    ) { padding ->
        LazyVerticalGrid(
            contentPadding = WindowInsets.systemBars.asPaddingValues(),
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
        ) {
            Sample.entries.groupBy { it.category }.forEach { (category, samples) ->

                item(span = { GridItemSpan(2) }) {
                    Text(
                        text = category.title,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                items(samples) { sample ->
                    ElevatedCard(
                        onClick = { onSampleSelect(sample) },
                        modifier = Modifier,
                    ) {
                        Text(
                            text = sample.title,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(1f)
                                    .padding(16.dp)
                                    .wrapContentSize(Alignment.CenterStart),
                        )
                    }
                }
            }

            item(span = { GridItemSpan(2) }) { UseGalleryToggle() }
        }
    }
}

@Composable
fun UseGalleryToggle() {
    val context = LocalContext.current
    val dataStore = remember(context) { context.dataStore }
    val gallerySwitchChecked by dataStore.data.map { it[useGallery] ?: false }.collectAsState(initial = false)
    val coroutineScope = rememberCoroutineScope()
    val permissionRequest =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                coroutineScope.launch {
                    dataStore.edit { settings ->
                        settings[useGallery] = true
                    }
                }
            }
        }

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .toggleable(
                    value = gallerySwitchChecked,
                    onValueChange = { checked ->
                        if (checked) {
                            permissionRequest.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        } else {
                            coroutineScope.launch {
                                dataStore.edit { settings ->
                                    settings[useGallery] = false
                                }
                            }
                        }
                    },
                )
                .padding(16.dp),
    ) {
        Text("Use the images from your gallery", modifier = Modifier.weight(1f))
        Switch(checked = gallerySwitchChecked, onCheckedChange = null)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictiveBackAppBar() {
    TopAppBar(
        title = { Text("Predictive Back Samples") },
    )
}
