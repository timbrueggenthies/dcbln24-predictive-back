@file:OptIn(ExperimentalSharedTransitionApi::class)

package demo.dcbln.predictiveback.ui.core.voyager

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.SeekableTransitionState
import androidx.compose.animation.core.rememberTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.isActive
import java.io.Serializable

@Composable
fun PredictiveBackScreenTransition(
    navigator: Navigator,
    modifier: Modifier = Modifier,
    defaultTransition: ScreenTransition = FadeScreenTransition
) {
    var inPredictiveBack by remember { mutableStateOf(false) }
    var transitionHandledByScreen by remember { mutableStateOf(false) }

    val transitionState = remember(navigator) { SeekableTransitionState(navigator.lastItem) }
    val transition = rememberTransition(transitionState = transitionState)

    PredictiveBackHandler(enabled = navigator.canPop) { events ->
        val newItem = navigator.items.asReversed()[1]
        val currentItem = transitionState.currentState
        if (currentItem is PredictiveBackAware) {
            transitionHandledByScreen = true
            inPredictiveBack = true
            transitionState.seekTo(0f, newItem)
            currentItem.onBack(events)
            if (currentCoroutineContext().isActive) {
                transitionState.animateTo(newItem)
                navigator.pop()
            }
            transitionHandledByScreen = false
            inPredictiveBack = false
        } else {
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
    }

    if (!inPredictiveBack) {
        LaunchedEffect(navigator.lastItem) {
            transitionState.animateTo(navigator.lastItem)
        }
    }

    SharedTransitionLayout(modifier = modifier) {
        transition.AnimatedContent(
            transitionSpec = {
                val contentTransform: ContentTransform = when {
                    transitionHandledByScreen -> EnterTransition.None togetherWith ExitTransition.None
                    else -> {
                        val transitionSource = when (navigator.lastEvent) {
                            StackEvent.Pop -> initialState
                            else -> targetState
                        }

                        (transitionSource as? ScreenTransition ?: defaultTransition).contentTransform(navigator)
                    }
                }

                val stackSize = navigator.size
                contentTransform.targetContentZIndex = when {
                    transitionHandledByScreen && targetState == transition.currentState -> stackSize
                    transitionHandledByScreen && targetState == transition.targetState -> stackSize - 1
                    navigator.lastEvent == StackEvent.Pop -> stackSize - 1
                    else -> stackSize
                }.toFloat()

                contentTransform
            }
        ) { screen ->
            navigator.saveableState("transition", screen) {
                if (screen is TransitionAware) {
                    screen.Content(this@SharedTransitionLayout, this)
                } else {
                    screen.Content()
                }
            }
        }
    }
}

interface PredictiveBackAware : Serializable {

    suspend fun onBack(progress: Flow<BackEventCompat>)
}


