package com.example.bonial.brochures.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.memory.MemoryCache.Builder

private const val MEMORY_SIZE_PERCENT = .25

@Composable
internal fun rememberBrochureListImageLoader(
    key: Any? = null
): ImageLoader {
    val context = LocalContext.current
    return remember(key) {
        ImageLoader.Builder(context)
            .memoryCache { Builder(context).maxSizePercent(MEMORY_SIZE_PERCENT).build() }
            .build()
    }
}
