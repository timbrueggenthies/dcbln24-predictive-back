package demo.dcbln.predictiveback.ui.core

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import demo.dcbln.predictiveback.image.Image
import org.koin.compose.koinInject

@Composable
fun ImagePreview(image: Image?, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val request: ImageRequest? = image?.let {
        ImageRequest.Builder(context)
            .data(it.fullSizeUri)
            .build()
    }
    SubcomposeAsyncImage(
        model = request,
        contentDescription = null,
        imageLoader = koinInject(),
        contentScale = ContentScale.Crop,
        loading = {
            CircularProgressIndicator(modifier = Modifier.fillMaxSize().wrapContentSize())
        },
        modifier = modifier
    )
}