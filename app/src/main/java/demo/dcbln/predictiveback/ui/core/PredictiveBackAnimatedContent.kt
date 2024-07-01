package demo.dcbln.predictiveback.ui.core

import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.CancellationException

/*

@Composable
fun <T> PredictiveBackAnimatedContent(
    key: T,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    getNextItem: () -> T,
    backCommited: () -> Unit,
    content: @Composable (T) -> Unit
) {
    var inPredictiveBack by remember { mutableStateOf(false) }

    val transitionState = remember { SeekableTransitionState(key) }
    val transition = rememberTransition(transitionState = transitionState)

    PredictiveBackHandler(enabled = enabled) { events ->
        val newItem = getNextItem()
        transitionState.seekTo(0f, newItem)
        try {
            events.collect {
                inPredictiveBack = true
                transitionState.seekTo(it.progress * 0.3f, newItem)
            }
            transitionState.animateTo(newItem)
            navigator.pop()
            inPredictiveBack = false
        } catch (e: CancellationException) {
            transitionState.animateTo(navigator.lastItem)
            inPredictiveBack = false
        }
    }

    if (!inPredictiveBack) {
        LaunchedEffect(navigator.lastItem) {
            transitionState.animateTo(navigator.lastItem)
        }
    }

    transition.AnimatedContent(
        transitionSpec = {
            val contentTransform = fadeIn() togetherWith fadeOut()

            val stackSize = navigator.size
            contentTransform.targetContentZIndex = when (navigator.lastEvent) {
                StackEvent.Pop -> stackSize - 1
                else -> stackSize
            }.toFloat()

            contentTransform
        },
        modifier = modifier
    ) { screen ->
        CompositionLocalProvider(LocalAnimatedVisibilityScope provides this) {
            navigator.saveableState("transition", screen) {
                screen.Content()
            }
        }
    }
}

*/
