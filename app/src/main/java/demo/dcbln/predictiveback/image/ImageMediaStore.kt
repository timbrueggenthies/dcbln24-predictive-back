package demo.dcbln.predictiveback.image

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import java.time.Instant

class ImageMediaStore(
    private val context: Context,
) {
    fun getImagesWithQuery(
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        uriBuilder: Uri.Builder.() -> Unit = { },
    ): Sequence<Image> {
        val projection = arrayOf(MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATE_TAKEN)
        val sortOrder = "${MediaStore.Images.ImageColumns.DATE_TAKEN} DESC"

        val uri =
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .apply(uriBuilder)
                .build()

        val cursor =
            context.contentResolver.query(
                uri,
                projection,
                selection,
                selectionArgs,
                sortOrder,
            ) ?: return emptySequence()

        val idColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns._ID)
        val dateTakenColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATE_TAKEN)
        val indices = ColumnIndices(idColumnIndex, dateTakenColumnIndex)
        return sequence {
            while (cursor.moveToNext()) {
                try {
                    yield(cursor.getImage(indices))
                } catch (e: CancellationException) {
                    cursor.close()
                }
            }
            cursor.close()
        }.constrainOnce()
    }

    private class ColumnIndices(
        val idColumnIndex: Int,
        val dateTakenColumnIndex: Int,
    )

    private fun Cursor.getImage(indices: ColumnIndices): Image {
        val id = getLong(indices.idColumnIndex)
        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

        return context.contentResolver.openInputStream(uri).use { stream ->
            val exifInterface = ExifInterface(stream!!)
            val width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH)?.toIntOrNull()
            val height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH)?.toIntOrNull()

            val dateTaken = Instant.ofEpochMilli(getLong(indices.dateTakenColumnIndex))
            val dimensions =
                if (width != null && height != null) {
                    ImageDimensions(width, height)
                } else {
                    null
                }

            val location = exifInterface.latLong?.let { ImageLocation(it[0], it[1]) }

            Image(id.toString(), dateTaken, dimensions, location, uri.toString())
        }
    }
}
