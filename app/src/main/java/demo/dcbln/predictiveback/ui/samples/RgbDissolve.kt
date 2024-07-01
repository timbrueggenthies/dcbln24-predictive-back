package demo.dcbln.predictiveback.ui.samples

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.ImageCell
import demo.dcbln.predictiveback.ui.core.ListScreenScaffold
import org.intellij.lang.annotations.Language

@Composable
fun RgbDissolve() {
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
    val runtimeShader = remember { RuntimeShader(RGB_DISSOLVE_SHADER) }

    var size: IntSize by remember { mutableStateOf(IntSize.Zero) }
    var y: Float by remember { mutableFloatStateOf(-1f) }
    val progress = remember { Animatable(0f) }

    PredictiveBackHandler { events ->
        progress.snapTo(0f)
        val velocityTracker = VelocityTracker()
        try {
            events.collect { event ->
                progress.snapTo(event.progress * 0.3f)
                y = event.touchY
                velocityTracker.addPosition(System.currentTimeMillis(), Offset(event.touchX, event.touchY))
            }
            progress.animateTo(1f, tween(1000), initialVelocity = velocityTracker.calculateVelocity().x)
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
                    compositingStrategy = CompositingStrategy.Offscreen
                    renderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content").asComposeRenderEffect()
                }
        )
}

@Language(value = "AGSL")
private const val RGB_DISSOLVE_SHADER = """
    uniform shader content;
    uniform float progress;
    uniform float2 resolution;
    uniform float y;
    
    float calculateAlpha(float2 fragCoord) {
        float maxDimension = max(resolution.x, resolution.y);
        float alphaInterpolationWidth = maxDimension * 0.3;
        
        // Calculate the current position of the window
        float windowStart = mix(-alphaInterpolationWidth, maxDimension + alphaInterpolationWidth, progress);
        float windowEnd = windowStart + alphaInterpolationWidth;
        
        float dist = length(fragCoord - float2(0, y));

        // Check if the value is below, within, or above the window
        if (dist < windowStart) {
            return 0.0;
        } else if (dist > windowEnd) {
            return 1.0;
        } else {
            // Linearly interpolate within the window
            return (dist - windowStart) / alphaInterpolationWidth;
        }
    }

    half4 main(in float2 fragCoord) {
        // Normalized pixel coordinates (from 0 to 1)
        float2 uv = fragCoord/resolution.xy;
        float maxDimension = max(resolution.x, resolution.y) * progress;
        
        float alphaInterpolationWidth = maxDimension * 0.1;
        float maxAlphaInterpolation = maxDimension + alphaInterpolationWidth * 2;
      
        float dist = length(fragCoord - float2(0, y)) / maxDimension;
        
        float factor = 1.0 - clamp(dist, 0.0, 1.0);
        
        float w = factor * 0.05;
        
        float d1 = w*sin(2.1*progress+17.0*uv.xy.y);
        float d2 = w*sin(2.4*progress+21.0*uv.xy.y+0.7);
        
        float r = content.eval(float2(uv.xy.x+d2,uv.xy.y) * resolution).r;
        float g = content.eval(float2(uv.xy.x,uv.xy.y+d1) * resolution).g;
        float b = content.eval(float2(uv.xy.x,uv.xy.y) * resolution).b;
        
        float alpha = calculateAlpha(fragCoord);
 
        return float4(r * alpha, g * alpha, b * alpha, alpha);
    }
"""
