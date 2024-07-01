package demo.dcbln.predictiveback.ui.core

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CancellationException

@Composable
fun DrawDebugBackGesture(modifier: Modifier = Modifier) {
    val points = remember { mutableStateListOf<BackEventCompat>() }
    var layoutCoordinates: LayoutCoordinates? by remember { mutableStateOf(null) }
    var gestureCompleted by remember { mutableStateOf(false) }
    Canvas(modifier.onPlaced { layoutCoordinates = it }) {
        val currentCoordinates = layoutCoordinates ?: return@Canvas
        val vertices = points.map {
            currentCoordinates.screenToLocal(Offset(it.touchX, it.touchY))
        }
        drawPoints(vertices, PointMode.Points, color = Color.Red, strokeWidth = 20f)
        val lastEvent = points.lastOrNull() ?: return@Canvas
        val color = if (gestureCompleted) Color.Green else Color.Red
        val width = 80.dp.toPx()
        when (lastEvent.swipeEdge) {
            BackEventCompat.EDGE_LEFT -> {
                val brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = lastEvent.progress), color.copy(alpha = 0f)),
                    endX = width,
                )
                drawRect(brush, size = Size(width, size.height))
            }

            BackEventCompat.EDGE_RIGHT -> {
                val brush = Brush.horizontalGradient(
                    colors = listOf(color.copy(alpha = 0f), color.copy(alpha = lastEvent.progress)),
                    endX = width,
                )
                drawRect(brush, topLeft = Offset(size.width - width, 0f))
            }
        }
    }
    PredictiveBackHandler { backEvents ->
        gestureCompleted = false
        points.clear()
        try {
            backEvents.collect { backEvent ->
                points.add(backEvent)
            }
            gestureCompleted = true
        } catch (e: CancellationException) {
            points.clear()
        }
    }
}