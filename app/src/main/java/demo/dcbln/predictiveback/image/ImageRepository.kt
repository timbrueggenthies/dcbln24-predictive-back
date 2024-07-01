package demo.dcbln.predictiveback.image

import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import kotlinx.coroutines.flow.Flow

interface ImageRepository {

    fun getPagedImages(): PagingSource<Int, Image>

    suspend fun getImage(id: String): Image
}