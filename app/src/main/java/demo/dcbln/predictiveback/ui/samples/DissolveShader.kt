package demo.dcbln.predictiveback.ui.samples

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.ImageCell
import demo.dcbln.predictiveback.ui.core.ListScreenScaffold
import org.intellij.lang.annotations.Language

@Composable
fun DissolveShader() {
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
    val runtimeShader = remember { RuntimeShader(DISSOLVE_SHADER) }

    var size: IntSize by remember { mutableStateOf(IntSize.Zero) }
    var y: Float by remember { mutableFloatStateOf(-1f) }
    val progress = remember { Animatable(0f) }

    PredictiveBackHandler { events ->
        progress.snapTo(0f)
        try {
            events.collect { event ->
                progress.snapTo(event.progress)
                y = event.touchY
            }
            progress.animateTo(1f)
            onScreenFinished()
        } finally {
            progress.animateTo(0f)
        }
    }


        BasicDetailScreen(imageId,
            modifier
                .onSizeChanged { size = it }
                .graphicsLayer {
                    runtimeShader.apply {
                        setFloatUniform("resolution", size.width.toFloat(), size.height.toFloat())
                        setFloatUniform("y", y)
                        setFloatUniform("progress", progress.value)
                    }
                    renderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content").asComposeRenderEffect()
                }
        )
}

@Language(value = "AGSL")
private const val DISSOLVE_SHADER = """
    uniform shader content;
    uniform float progress;
    uniform float2 resolution;
    uniform float y;

    half4 main(in float2 fragCoord) {
        float strength;
        
        if (fragCoord.y < y) {
            strength = fragCoord.y / y;
        } else {
            strength = 1.0 - fragCoord.y - y / resolution.y - y;
        }
        float2 uv = fragCoord.xy / resolution.xy;
        float particleSize = 1.0 - (progress * strength);
        float particleAmount = 10;
        float fade = 1.0 - progress;

        half4 color = half4(0.0);
        float x = mod(uv.x * particleAmount, 1.0);
        float y = mod(uv.y * particleAmount, 1.0);
        if (x < particleSize && y < particleSize) {
            color = content.eval(fragCoord);
        }

        return color;
    }
"""
