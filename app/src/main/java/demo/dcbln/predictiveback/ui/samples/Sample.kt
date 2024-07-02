package demo.dcbln.predictiveback.ui.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import demo.dcbln.predictiveback.ui.core.DrawDebugBackGesture
import demo.dcbln.predictiveback.ui.samples.jetpack.AndroidxNavigationSample
import demo.dcbln.predictiveback.ui.samples.voyager.VoyagerNavigationSample
import demo.dcbln.predictiveback.ui.test.ShaderTesting

enum class Sample(val title: String, val category: Category, val content: @Composable () -> Unit) {
    DebugPredictiveBack("Demo", Category.Basics, { DebugSample() }),
    // TODO ShaderTesting("Shader Testing", Category.Basics, { ShaderTesting() }),
    BottomSheet("Bottom Sheet", Category.Basics, { BottomSheetSample() }),
    SharedElement("Shared Element", Category.GraphicalEffects, { SharedElement() }),
    Droplet("Droplet", Category.GraphicalEffects, { Droplet() }),
    ScaleContentDown("Scale Content Down", Category.GraphicalEffects, { ScaleContentDown() }),
    PreventBack("Prevent Back", Category.GraphicalEffects, { IndicateUnableToGoBack() }),
    // TODO DissolveShader("Dissolve Shader", Category.GraphicalEffects, { DissolveShader() }),
    RgbDissolve("RGB Dissolve", Category.GraphicalEffects, { RgbDissolve() }),
    // TODO PageTurning("Page Turning", Category.GraphicalEffects, { PageTurningShader() }),
    AndroidxNavigation("Androidx Navigation", Category.Integration, { AndroidxNavigationSample() }),
    VoyagerIntegration("Voyager Integration", Category.Integration, { VoyagerNavigationSample() }),
}

enum class Category(val title: String) {
    Basics("Basics"),
    Integration("Integration"),
    GraphicalEffects("Graphical effects")
}

@Composable
fun DebugSample(modifier: Modifier = Modifier) {
    Surface(modifier = modifier) {
        var enabled by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize()) {
            Switch(checked = enabled, onCheckedChange = { enabled = it }, modifier = Modifier.align(Alignment.Center))
            if (enabled) {
                DrawDebugBackGesture(modifier = Modifier.fillMaxSize())
            }
        }
    }
}