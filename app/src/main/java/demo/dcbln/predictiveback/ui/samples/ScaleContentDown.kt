package demo.dcbln.predictiveback.ui.samples

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun ScaleContentDown() {
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

    var progress by remember { mutableFloatStateOf(0f) }
    val scale = remember { Animatable(1f) }
    var swipeEdge by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }
    var pivot by remember { mutableStateOf(Offset.Unspecified) }

    PredictiveBackHandler { events ->
        try {
            events.collect { event ->
                progress = event.progress
                pivot = Offset(event.touchX, event.touchY)
                swipeEdge = event.swipeEdge
                scale.snapTo(1f - (event.progress * 0.3f))
            }
            progress = 0f
            scale.animateTo(0f)
            onClose()
        } catch (e: CancellationException) {
            scale.animateTo(1f)
        }
    }

    BasicDetailScreen(
        imageId = imageId,
        modifier = Modifier
            .graphicsLayer {
                clip = true
                shape = if (scale.value == 0f) RectangleShape else RoundedCornerShape(32.dp)
                scaleX = scale.value
                scaleY = scale.value
                val pivotX = if (swipeEdge == BackEventCompat.EDGE_LEFT) 0.8f else 0.2f
                transformOrigin = TransformOrigin(pivotX, pivot.y / size.height)
            }
    )
}
