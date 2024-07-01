package demo.dcbln.predictiveback.image

import java.time.Instant

data class Image(
    val id: String,
    val takenOn: Instant,
    val dimensions: ImageDimensions?,
    val location: ImageLocation?,
    val fullSizeUri: String,
)

data class ImageDimensions(val width: Int, val height: Int)

data class ImageLocation(val lat: Double, val long: Double)
