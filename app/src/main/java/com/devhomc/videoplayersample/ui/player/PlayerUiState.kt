package com.devhomc.videoplayersample.ui.player

import androidx.media3.common.MediaItem

data class PlayerUiState(
    val isPictureInPictureEnabled: Boolean = false,
    val isInPictureInPictureMode: Boolean = false,
    val mediaItem: MediaItem? = null
)
