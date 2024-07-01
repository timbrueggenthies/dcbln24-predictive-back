package demo.dcbln.predictiveback.ui.samples

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.ImageCell
import demo.dcbln.predictiveback.ui.core.ListScreenScaffold
import org.intellij.lang.annotations.Language

@Composable
fun PageTurningShader() {
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
    val runtimeShader = remember { RuntimeShader(PAGE_TURNING_SHADER) }

    var size: IntSize by remember { mutableStateOf(IntSize.Zero) }
    var touch: Offset by remember { mutableStateOf(Offset.Zero) }
    val progress = remember { Animatable(0f) }

    PredictiveBackHandler { events ->
        progress.snapTo(0f)
        try {
            events.collect { event ->
                progress.snapTo(event.progress)
                touch = Offset(event.touchX, event.touchY)
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
                        setFloatUniform("progress", progress.value)
                        setFloatUniform("touch", touch.x, touch.y)
                    }
                    renderEffect = RenderEffect.createRuntimeShaderEffect(runtimeShader, "content").asComposeRenderEffect()
                }
        )
}

@Language(value = "AGSL")
private const val PAGE_TURNING_SHADER = """
// https://www.shadertoy.com/view/lstGWN

uniform float2 resolution;
uniform shader content;
uniform float progress;
uniform float2 touch;

half4 main(float2 fragCoord) {
    float pi = 3.14159265359;
    float radius = .1;

    float aspect = resolution.x / resolution.y;
    float2 dragOrigin = float2(resolution.x, resolution.y * 0.5);
    float2 iMouse = touch;
    vec2 uv = fragCoord * float2(aspect, 1.) / resolution.xy;
    
    vec2 mouse = iMouse.xy * vec2(aspect, 1.) / resolution.xy;
    vec2 mouseDir = normalize(iMouse - dragOrigin);
    vec2 origin = clamp(mouse - mouseDir * mouse.x / mouseDir.x, 0., 1.);
    
    float mouseDist = clamp(length(mouse - origin), 0., aspect / mouseDir.x);
    
    if (mouseDir.x < 0.)
    {
        mouseDist = distance(mouse, origin);
    }

    float proj = dot(uv - origin, mouseDir);
    float dist = proj - mouseDist;
    
    vec2 linePoint = uv - dist * mouseDir;
    
    half4 fragColor;
    
    if (dist > radius) 
    {
        fragColor = half4(0.0);
        fragColor.rgb *= pow(clamp(dist - radius, 0., 1.) * 1.5, .2);
    }
    else if (dist >= 0.)
    {
        // map to cylinder point
        float theta = asin(dist / radius);
        vec2 p2 = linePoint + mouseDir * (pi - theta) * radius;
        vec2 p1 = linePoint + mouseDir * theta * radius;
        uv = (p2.x <= aspect && p2.y <= 1. && p2.x > 0. && p2.y > 0.) ? p2 : p1;
        fragColor = content.eval(uv * vec2(1. / aspect, 1.) * resolution);
        fragColor.rgb *= pow(clamp((radius - dist) / radius, 0., 1.), .2);
    }
    else 
    {
        vec2 p = linePoint + mouseDir * (abs(dist) + pi * radius);
        uv = (p.x <= aspect && p.y <= 1. && p.x > 0. && p.y > 0.) ? p : uv;
        fragColor = content.eval(uv * vec2(1. / aspect, 1.) * resolution);
    }
    
    return fragColor;
}
"""
