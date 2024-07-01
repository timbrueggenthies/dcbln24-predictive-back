package demo.dcbln.predictiveback.ui.samples

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.ImageCell
import demo.dcbln.predictiveback.ui.core.ListScreenScaffold
import org.intellij.lang.annotations.Language
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect

@Composable
fun CircularReveal() {
    var selectedImageId: String? by remember { mutableStateOf(null) }
    Box {
        ListScreen(onImageClick = { selectedImageId = it })
        selectedImageId?.let {
            DetailScreen(imageId = it, onScreenFinished = { selectedImageId = null })
        }
    }
}

@Composable
private fun ListScreen(
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ListScreenScaffold(
        imageCell = { image -> ImageCell(image, onClick = { onImageClick(image.id) }) },
        modifier = modifier,
    )
}

@SuppressLint("NewApi")
@Composable
private fun DetailScreen(
    imageId: String,
    onScreenFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    val runtimeShader = remember { RuntimeShader(CIRCULAR_REVEAL_SHADER) }
    fun createRenderEffect(): ComposeRenderEffect {
        return RenderEffect.createRuntimeShaderEffect(runtimeShader, "content").asComposeRenderEffect()
    }

    var effect by remember(runtimeShader) { mutableStateOf(createRenderEffect()) }

    var size: IntSize by remember { mutableStateOf(IntSize.Zero) }

    val radiusAnimation = remember { Animatable(0f) }

    val progress = remember { Animatable(0f) }
    var fingerY: Float by remember { mutableFloatStateOf(-1f) }
    var edge: Int by remember { mutableIntStateOf(BackEventCompat.EDGE_LEFT) }

    LaunchedEffect(radiusAnimation.value) {
        runtimeShader.setFloatUniform("radius", radiusAnimation.value)
        effect = createRenderEffect()
    }

    PredictiveBackHandler { events ->
        progress.snapTo(0f)
        fingerY = -1f
        try {
            events.collect { event ->
                edge = event.swipeEdge
                progress.snapTo(event.progress)
                if (fingerY == -1f) fingerY = event.touchY
                runtimeShader.setFloatUniform("fingerY", event.touchY)
                radiusAnimation.snapTo(size.width * event.progress)
                effect = createRenderEffect()
            }
            progress.animateTo(1f)
            //radiusAnimation.animateTo(size.height.toFloat())
            onScreenFinished()
        } finally {
            progress.snapTo(0f)
            fingerY = -1f
        }
    }

    Box(modifier = Modifier.drawWithContent {
        drawContent()
        if (fingerY != -1f) {
            drawPath(
                path = createDropPath(
                    fingerY,
                    edge,
                    progress.value,
                    Size(size.width.toFloat(), size.height.toFloat()),
                    layoutDirection,
                    this,
                    inclusive = false
                ),
                color = Color(0x605352ED)
            )
        }
    }
    ) {
        BasicDetailScreen(imageId,
            modifier
                .onSizeChanged { size = it }
                .graphicsLayer {
                    if (fingerY != -1f) {
                        clip = true
                        shape = CustomShape(fingerY, edge, progress.value)
                    } else {
                        clip = false
                        shape = RectangleShape
                    }
                }
            //.graphicsLayer { renderEffect = effect }
        )
    }
}

class CustomShape(val y: Float, val edge: Int, val progress: Float) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        return Outline.Generic(createDropPath(y, edge, progress, size, layoutDirection, density))
    }
}

private fun createDropPath(
    y: Float,
    edge: Int,
    progress: Float,
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
    inclusive: Boolean = true,
): Path {
    val rightEdge = ((4 * progress).coerceAtMost(1f) * with(density) { 64.dp.toPx() }).coerceAtLeast(2f)
    val dropHeight = size.height / 6f
    val dropCenter = y

    val vertices = if (edge == BackEventCompat.EDGE_RIGHT) {
        with(size) {
            floatArrayOf(
                if (inclusive) 0f else width, 0f,
                if (inclusive) 0f else width, height,
                width - rightEdge, height,
                width - rightEdge, dropCenter + dropHeight / 2f,
                width - rightEdge - progress * 400f, dropCenter,
                width - rightEdge, dropCenter - dropHeight / 2f,
                width - rightEdge, 0f,
            )
        }
    } else {
        with(size) {
            floatArrayOf(
                if (inclusive) width else 0f, 0f,
                if (inclusive) width else 0f, height,
                rightEdge, height,
                rightEdge, dropCenter + dropHeight / 2f,
                rightEdge + progress * 400f, dropCenter,
                rightEdge, dropCenter - dropHeight / 2f,
                rightEdge, 0f,
            )
        }
    }
    val perVertexRounding = listOf(
        CornerRounding(radius = 0f, smoothing = 0f),
        CornerRounding(radius = 0f, smoothing = 0f),
        CornerRounding(radius = 0f, smoothing = 0f),
        CornerRounding(radius = 1000f, smoothing = 1f),
        CornerRounding(radius = 200f, smoothing = 1f),
        CornerRounding(radius = 1000f, smoothing = 1f),
        CornerRounding(radius = 0f, smoothing = 0f),
    )

    val polygon = RoundedPolygon(
        vertices = vertices,
        centerX = size.width / 2f,
        centerY = size.height / 2f,
        perVertexRounding = perVertexRounding,
    )

    return polygon.toPath().asComposePath()
}

@Language(value = "AGSL")
private const val CIRCULAR_REVEAL_SHADER = """
  //uniform float2 resolution;
  uniform shader content;
  uniform float radius;
  uniform float fingerY;
  
  half4 main(float2 fragCoord) {
      // Normalized pixel coordinates (from 0 to 1)
      //float2 uv = fragCoord/resolution.xy;
      float minDimension = radius;
      
      float dist = length(fragCoord - float2(0, fingerY));
      float alpha = 0.0;
      
      if (dist > radius) {
        return content.eval(fragCoord);
      } else {
        return half4(0.0, 0.0, 0.0, 0.0);
      }
  }
"""
