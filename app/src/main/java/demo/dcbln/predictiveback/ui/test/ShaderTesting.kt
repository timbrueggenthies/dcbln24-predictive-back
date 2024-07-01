package demo.dcbln.predictiveback.ui.test

import android.annotation.SuppressLint
import android.content.pm.PackageInstaller.PreapprovalDetails
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.CancellationException
import org.intellij.lang.annotations.Language

@SuppressLint("NewApi")
@Composable
fun ShaderTesting() {
    Box {
        val runtimeShader = remember { RuntimeShader(shader) }
        var progress by remember { mutableStateOf(0f) }
        var fingerPos by remember { mutableStateOf(Offset.Zero) }
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Blue)
        )
        val shaderBrush = remember(runtimeShader) { ShaderBrush(runtimeShader) }

        PredictiveBackHandler { events ->
            try {
                events.collect {
                    if (fingerPos == Offset.Zero) {
                        fingerPos = Offset(it.touchX, it.touchY)
                    }
                    progress = it.progress
                }
            } finally {
                fingerPos = Offset.Zero
                progress = 0f
            }
        }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawBehind {
                    runtimeShader.setFloatUniform("fingerY", fingerPos.y)
                    runtimeShader.setFloatUniform("progress", progress)
                    drawRect(shaderBrush)
                }
                .onSizeChanged { size ->
                    runtimeShader.setFloatUniform(
                        "resolution",
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                }
/*
                .graphicsLayer {
                    if (setShader) {
                        renderEffect = RenderEffect.createRuntimeShaderEffect(
                            runtimeShader, // The RuntimeShader
                            "content" // The name of the uniform for the RenderNode content
                        ).asComposeRenderEffect()
                    }
                }
                 */
        )
    }
}

@Language(value = "AGSL")
val shader = """
  uniform float2 resolution;
  uniform shader content;
  uniform float progress;
  uniform float fingerY;
  
  half4 main(float2 fragCoord) {
      // Normalized pixel coordinates (from 0 to 1)
      float2 uv = fragCoord/resolution.xy;
      float minDimension = min(resolution.x, resolution.y) * progress;
      
      float dist = length(fragCoord - float2(0, fingerY)) / minDimension;

      // Time varying pixel color
      // half4 col = content.eval(fragCoord);

      // Output to screen
      return half4(1.0, 1.0, 1.0, clamp(dist, 0.0, 1.0));
  }
"""