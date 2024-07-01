@file:OptIn(ExperimentalSharedTransitionApi::class)

package demo.dcbln.predictiveback.ui.core.voyager

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.core.stack.StackEvent
import cafe.adriel.voyager.navigator.Navigator
import java.io.Serializable

interface TransitionAware : Screen {

    @Composable
    fun Content(sharedTransitionScope: SharedTransitionScope, animatedVisibilityScope: AnimatedVisibilityScope)

    @Composable
    override fun Content(): Nothing {
        error("Use the override with receiver")
    }

}


interface ScreenTransition : Serializable {

    fun contentTransform(navigator: Navigator): ContentTransform

}

object FadeScreenTransition : ScreenTransition {

    override fun contentTransform(navigator: Navigator): ContentTransform = fadeIn(tween(durationMillis = 2000)) togetherWith fadeOut(tween(durationMillis = 2000))
}

object SlideOverScreenTransition : ScreenTransition {

    override fun contentTransform(navigator: Navigator): ContentTransform {
        return when (navigator.lastEvent) {
            StackEvent.Pop -> scaleIn(initialScale = 0.8f) + fadeIn() togetherWith slideOutHorizontally { it }
            else -> slideInHorizontally { it } togetherWith scaleOut(targetScale = 0.8f) + fadeOut()
        }
    }
}
