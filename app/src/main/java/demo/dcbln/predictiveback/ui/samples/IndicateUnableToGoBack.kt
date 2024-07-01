package demo.dcbln.predictiveback.ui.samples

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.BasicListScreen
import demo.dcbln.predictiveback.ui.core.voyager.PredictiveBackAware
import demo.dcbln.predictiveback.ui.core.voyager.PredictiveBackScreenTransition
import demo.dcbln.predictiveback.ui.core.voyager.ScreenTransition
import demo.dcbln.predictiveback.ui.core.voyager.SlideOverScreenTransition
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import java.time.format.DateTimeFormatter

@Composable
fun IndicateUnableToGoBack() {
    var selectedImage: String? by remember { mutableStateOf(null) }

    Box {
        ListScreen(onImageClick = { selectedImage = it })
        selectedImage?.let {
            DetailScreen(it, onClose = { selectedImage = null })
        }
    }
}

@Composable
private fun ListScreen(onImageClick: (String) -> Unit) {
    BasicListScreen(
        onImageClick = { onImageClick(it) }
    )
}

@Composable
private fun DetailScreen(imageId: String, onClose: () -> Unit) {
    var showIndication by remember { mutableStateOf(false) }

    val scale = remember { Animatable(1f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    PredictiveBackHandler { events ->
        try {
            events.collect { event ->
                swipeEdge = event.swipeEdge
                scale.snapTo(1f + (event.progress * 0.1f))
            }
            showIndication = true
            scale.animateTo(1f)
        } catch (e: CancellationException) {
            scale.animateTo(1f)
        }
    }

    if (showIndication) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text("You really wanna quit?") },
            confirmButton = { TextButton(onClose) { Text("Yes, quit") } },
            dismissButton = { TextButton(onClick = { showIndication = false }) { Text("Stay here") } })
    }

    BasicDetailScreen(
        imageId = imageId,
        modifier = Modifier
            .graphicsLayer {
                scaleX = scale.value
                transformOrigin = when (swipeEdge) {
                    BackEventCompat.EDGE_LEFT -> TransformOrigin(1f, 0.5f)
                    else -> TransformOrigin(0f, 0.5f)
                }
            }
    )
}
