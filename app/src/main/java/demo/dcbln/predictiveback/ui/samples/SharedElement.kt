@file:OptIn(ExperimentalSharedTransitionApi::class)

package demo.dcbln.predictiveback.ui.samples

import androidx.activity.compose.BackHandler
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.Transition
import androidx.compose.animation.core.TransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import demo.dcbln.predictiveback.ui.core.DetailsScreenScaffold
import demo.dcbln.predictiveback.ui.core.ImageDetails
import demo.dcbln.predictiveback.ui.core.ImageMap
import demo.dcbln.predictiveback.ui.core.ImagePreview
import demo.dcbln.predictiveback.ui.core.ListScreenScaffold
import demo.dcbln.predictiveback.ui.core.LocalAnimatedVisibilityScope
import demo.dcbln.predictiveback.ui.core.LocalSharedTransitionScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun SharedElement() {
    SharedTransitionLayout {
        var selectedImageId by remember { mutableStateOf<String?>(null) }

        var inPredictiveBack by remember { mutableStateOf(false) }
        val transitionState = remember { SeekableTransitionState(selectedImageId) }
        val transition = rememberTransition(transitionState = transitionState)

        PredictiveBackHandler(enabled = selectedImageId != null) { events ->
            transitionState.seekTo(0f, null)
            try {
                events.collect {
                    inPredictiveBack = true
                    transitionState.seekTo(it.progress * 0.3f, null)
                }
                transitionState.animateTo(null)
                selectedImageId = null
                inPredictiveBack = false
            } catch (e: CancellationException) {
                transitionState.animateTo(selectedImageId)
                inPredictiveBack = false
            }
        }

        val coroutineScope = rememberCoroutineScope()

        transition.AnimatedContent(
            transitionSpec = {
                val contentTransform = fadeIn() togetherWith fadeOut()
                contentTransform.targetContentZIndex = if (targetState == null) 0f else 1f
                contentTransform
            },
        ) { imageId ->
            if (imageId == null) {
                ListScreen(
                    animatedVisibilityScope = this,
                    onImageClick = {
                        coroutineScope.launch {
                            selectedImageId = it
                            transitionState.animateTo(it)
                        }
                    })
            } else {
                DetailScreen(this, imageId)
            }
        }
    }
}

@Composable
private fun SharedTransitionScope.ListScreen(animatedVisibilityScope: AnimatedVisibilityScope, onImageClick: (imageId: String) -> Unit) {
    ListScreenScaffold(
        imageCell = { image ->
            val clipShape = RoundedCornerShape(16.dp)
            ElevatedCard(
                shape = clipShape,
                onClick = { onImageClick(image.id) },
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .sharedBounds(
                        sharedContentState = rememberSharedContentState(key = "Item ${image.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
                    )
            ) {
                ImagePreview(
                    image = image,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .sharedElement(
                            state = rememberSharedContentState(key = "Image ${image.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            clipInOverlayDuringTransition = OverlayClip(clipShape)
                        )
                )
            }
        }
    )
}

@Composable
private fun SharedTransitionScope.DetailScreen(animatedVisibilityScope: AnimatedVisibilityScope, imageId: String) {
    Surface(
        modifier = Modifier.sharedBounds(
            sharedContentState = rememberSharedContentState(key = "Item $imageId"),
            animatedVisibilityScope = animatedVisibilityScope,
            resizeMode = SharedTransitionScope.ResizeMode.RemeasureToBounds,
            enter = fadeIn(),
            exit = fadeOut(),
        )
    ) {
        DetailsScreenScaffold(imageId) { image ->
            ImagePreview(
                image = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .sharedElement(
                        state = rememberSharedContentState(key = "Image $imageId"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
            )

            image?.let {
                ImageDetails(image)
            }

            ImageMap(image?.location)
        }
    }
}