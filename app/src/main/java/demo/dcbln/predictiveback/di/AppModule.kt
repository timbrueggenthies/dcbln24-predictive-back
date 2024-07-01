package demo.dcbln.predictiveback.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import demo.dcbln.predictiveback.dataStore
import demo.dcbln.predictiveback.image.AndroidAssetImageRepository
import demo.dcbln.predictiveback.image.GalleryImageRepository
import demo.dcbln.predictiveback.image.ImageMediaStore
import demo.dcbln.predictiveback.image.ImageRepository
import demo.dcbln.predictiveback.useGallery
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.dsl.module

val appModule = module {
    factory<ImageRepository> {
        val dataStore = get<Context>().dataStore
        val useGallery = runBlocking { dataStore.data.first() }[useGallery] ?: false
        if (useGallery) GalleryImageRepository(get()) else AndroidAssetImageRepository(get())
    }

    single { ImageMediaStore(get()) }

    single {
        val context: Context = get()
        ImageLoader.Builder(context)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizeBytes(10_000_000)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100_000_000)
                    .build()
            }
            .build()
    }
}