package demo.dcbln.predictiveback.ui.samples

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.BasicListScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSample() {
    var selectedImageId: String? by remember { mutableStateOf(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        BasicListScreen(onImageClick = { selectedImageId = it })
    }
    selectedImageId?.let {
        ModalBottomSheet(onDismissRequest = { selectedImageId = null }) {
            BasicDetailScreen(it)
        }
    }
}
