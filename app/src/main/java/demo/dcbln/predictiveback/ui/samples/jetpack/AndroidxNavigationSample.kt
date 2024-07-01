package demo.dcbln.predictiveback.ui.samples.jetpack

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import demo.dcbln.predictiveback.ui.core.BasicDetailScreen
import demo.dcbln.predictiveback.ui.core.BasicListScreen
import kotlinx.serialization.Serializable

@Serializable
private data object ImageList

@Serializable
private data class ImageDetail(val imageId: String)

@Composable
fun AndroidxNavigationSample() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = ImageList,
        enterTransition = { slideInHorizontally { it } },
        exitTransition = { slideOutHorizontally { -it } },
        popEnterTransition = { slideInHorizontally { -it } },
        popExitTransition = { slideOutHorizontally { it } },
    ) {
        composable<ImageList> {
            BasicListScreen(onImageClick = { navController.navigate(ImageDetail(it)) })
        }

        composable<ImageDetail> {
            val route = it.toRoute<ImageDetail>()
            BasicDetailScreen(route.imageId)
        }
    }
}
