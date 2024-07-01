package demo.dcbln.predictiveback.image

import android.content.Context
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class AndroidAssetImageRepository(
    private val context: Context
): ImageRepository {

    override fun getPagedImages(): PagingSource<Int, Image> {
        return AssetPagingSource(context)
    }

    override suspend fun getImage(id: String): Image {
        return getImageFromAssets(id, context)
    }


}

private class AssetPagingSource(
    private val context: Context
) : PagingSource<Int, Image>() {
    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        val images = context.assets.list(DIRECTORY_IMAGES).orEmpty().map {
            getImageFromAssets(it, context)
        }
        return LoadResult.Page(images, null, null)
    }
}

private fun getImageFromAssets(file: String, context: Context): Image {
    val asset = "file:///android_asset/$DIRECTORY_IMAGES/$file"

    val exifInterface = ExifInterface(context.assets.open("$DIRECTORY_IMAGES/$file"))
    val width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)?.toIntOrNull()
    val height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)?.toIntOrNull()

    val dateTaken = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL)?.let {
        val formatter = DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss")
        LocalDateTime.parse(it, formatter).toInstant(ZoneOffset.UTC)
    } ?: Instant.EPOCH

    val dimensions = if (width != null && height != null) { ImageDimensions(width, height) } else null

    val location = exifInterface.latLong?.let { ImageLocation(it[0], it[1]) }

    return Image(file, dateTaken, dimensions, location, asset)
}

private const val DIRECTORY_IMAGES = "dummy"
