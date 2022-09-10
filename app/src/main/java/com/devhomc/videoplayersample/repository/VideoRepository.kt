package com.devhomc.videoplayersample.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.METADATA_KEY_ARTIST
import android.media.MediaMetadataRetriever.METADATA_KEY_TITLE
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class VideoRepository @Inject constructor(@ApplicationContext private val context: Context) {

    fun getVideo(fileName: String): MediaItem {
        val uri = Uri.parse("asset:///${fileName}")

        val requestMetadata = MediaItem.RequestMetadata.Builder()
            .setMediaUri(uri)
            .build()

        val assetFileDescriptor = context.assets.openFd(fileName)
        val metadataRetriever = MediaMetadataRetriever()
        metadataRetriever.setDataSource(
            assetFileDescriptor.fileDescriptor,
            assetFileDescriptor.startOffset,
            assetFileDescriptor.length
        )

        val mediaMetadata = MediaMetadata.Builder()
            .setTitle(metadataRetriever.extractMetadata(METADATA_KEY_TITLE))
            .setArtist(metadataRetriever.extractMetadata(METADATA_KEY_ARTIST))
            .build()

        return MediaItem.Builder()
            .setRequestMetadata(requestMetadata)
            .setMediaMetadata(mediaMetadata)
            .build()
    }
}
