package demo.dcbln.predictiveback.ui.core

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import demo.dcbln.predictiveback.image.Image
import demo.dcbln.predictiveback.image.ImageLocation
import demo.dcbln.predictiveback.image.ImageRepository
import org.koin.compose.koinInject
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun BasicDetailScreen(
    imageId: String,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier) {
        DetailsScreenScaffold(imageId) { image ->
            ImagePreview(
                image = image,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            )

            image?.let {
                ImageDetails(it)
            }

            ImageMap(image?.location)
        }
    }
}

@Composable
fun DetailsScreenScaffold(imageId: String, modifier: Modifier = Modifier, content: @Composable ColumnScope.(image: Image?) -> Unit) {
    val imageRepository: ImageRepository = koinInject()
    val image: Image? by produceState<Image?>(initialValue = null, key1 = imageId) {
        value = imageRepository.getImage(imageId)
    }

    val scrollState = rememberScrollState()
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        content(image)
    }
}

@Composable
fun ImageDetails(image: Image, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Image(imageVector = Icons.Default.DateRange, contentDescription = null)
                Spacer(modifier = Modifier.width(16.dp))
                val dateTimeString = remember(image) {
                    val formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
                    val localDateTime = image.takenOn.atZone(ZoneId.systemDefault())
                    localDateTime.format(formatter)
                }
                Text(dateTimeString)
            }
            image.dimensions?.let {
                Row {
                    Image(imageVector = Icons.Default.Done, contentDescription = null)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("${it.width}x${it.height}")
                }
            }
        }
    }
}

@Composable
fun ImageMap(location: ImageLocation?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        if (location == null) {
            OutlinedCard {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Text(text = "No location available", modifier = Modifier.align(Alignment.Center))
                }
            }
        } else {
            val latlng = LatLng(location.lat, location.long)
            val camera = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(latlng, 13f)
            }
            Card {
                GoogleMap(
                    cameraPositionState = camera,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(500.dp),
                    googleMapOptionsFactory = {
                        GoogleMapOptions()
                            .zoomControlsEnabled(false)
                            .zoomGesturesEnabled(false)
                    }
                )
            }
        }
    }
}