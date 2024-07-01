package demo.dcbln.predictiveback.image

import android.provider.MediaStore
import androidx.paging.PagingSource
import androidx.paging.PagingState


class GalleryImageRepository(
    private val imageMediaStore: ImageMediaStore
) : ImageRepository {

    override fun getPagedImages(): PagingSource<Int, Image> {
        return GalleryPagingSource(imageMediaStore)
    }

    override suspend fun getImage(id: String): Image {
        val images = imageMediaStore.getImagesWithQuery("${MediaStore.Images.ImageColumns._ID} = ?", arrayOf(id))
        return images.single()
    }
}

class GalleryPagingSource(
    private val imageMediaStore: ImageMediaStore
) : PagingSource<Int, Image>() {
    override fun getRefreshKey(state: PagingState<Int, Image>): Int? {
        return null
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Image> {
        val pageSize = params.loadSize
        val offset = params.key ?: 0
        val images = imageMediaStore
            .getImagesWithQuery {
                encodedQuery("limit=$offset,$pageSize")
            }
            .take(pageSize)
            .toList()
        val prevOffset = null
        val nextOffset = if (images.isEmpty()) null else offset + pageSize
        return LoadResult.Page(images.take(pageSize).toList(), prevOffset, nextOffset)
    }
}


