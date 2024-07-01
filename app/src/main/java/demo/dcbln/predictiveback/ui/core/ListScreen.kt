package demo.dcbln.predictiveback.ui.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.insertSeparators
import androidx.paging.map
import demo.dcbln.predictiveback.image.Image
import demo.dcbln.predictiveback.image.ImageRepository
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@Composable
fun BasicListScreen(
    onImageClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        ListScreenScaffold(
            imageCell = { image -> ImageCell(image, onClick = { onImageClick(image.id) }) },
        )
    }
}

@Composable
fun ListScreenScaffold(
    imageCell: @Composable (image: Image) -> Unit,
    modifier: Modifier = Modifier,
    monthRow: @Composable (date: ZonedDateTime) -> Unit = { BasicMonthRow(it) },
) {
    val imageRepository: ImageRepository = koinInject()
    val imagesPaged =
        remember(imageRepository) {
            Pager(PagingConfig(pageSize = 30)) { imageRepository.getPagedImages() }
                .flow
                .map { images ->
                    images
                        .map { ImageListItem.ActualImage(it) }
                        .insertSeparators { imageA: ImageListItem.ActualImage?, imageB: ImageListItem.ActualImage? ->
                            val imageATime = imageA?.image?.takenOn?.atZone(ZoneId.systemDefault()) ?: return@insertSeparators null
                            val imageAMonth = "${imageATime.year}:${imageATime.month}"
                            val imageBTime = imageB?.image?.takenOn?.atZone(ZoneId.systemDefault()) ?: return@insertSeparators null
                            val imageBMonth = "${imageBTime.year}:${imageBTime.month}"

                            if (imageAMonth != imageBMonth) {
                                ImageListItem.MonthSeparator(imageBTime)
                            } else {
                                null
                            }
                        }
                }
        }

    val images = imagesPaged.collectAsLazyPagingItems()

    LazyVerticalGrid(
        contentPadding = WindowInsets.systemBars.asPaddingValues(),
        columns = GridCells.Fixed(2),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        items(
            count = images.itemCount,
            /*
            key = { index ->
                val listItem = images[index]
                when (listItem) {
                    is ImageListItem.ActualImage -> listItem.image.id
                    is ImageListItem.MonthSeparator -> listItem.time.toEpochSecond()
                    else -> index
                }
            },
             */
            span = { index ->
                val listItem = images[index]
                when (listItem) {
                    is ImageListItem.ActualImage -> GridItemSpan(1)
                    is ImageListItem.MonthSeparator -> GridItemSpan(2)
                    else -> GridItemSpan(1)
                }
            },
            itemContent = { index ->
                val listItem = images[index]
                when (listItem) {
                    is ImageListItem.ActualImage -> imageCell(listItem.image)
                    is ImageListItem.MonthSeparator -> monthRow(listItem.time)
                    else -> Unit
                }
            },
        )
    }
}

sealed class ImageListItem {
    data class ActualImage(
        val image: Image,
    ) : ImageListItem()

    data class MonthSeparator(
        val time: ZonedDateTime,
    ) : ImageListItem()
}

@Composable
fun ImageCell(
    image: Image,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        modifier = modifier,
    ) {
        ImagePreview(
            image,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
        )
    }
}

@Composable
fun BasicMonthRow(
    date: ZonedDateTime,
    modifier: Modifier = Modifier,
) {
    val month =
        remember(date) {
            val formatter = "MMMM"
            date.format(DateTimeFormatter.ofPattern(formatter))
        }
    Text(text = month, modifier = modifier)
}
