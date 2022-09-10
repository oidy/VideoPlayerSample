package com.devhomc.videoplayersample.ui.player

import androidx.lifecycle.ViewModel
import com.devhomc.videoplayersample.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(repository: VideoRepository) : ViewModel() {

    val uiState = MutableStateFlow(PlayerUiState())

    init {
        uiState.value = uiState.value.copy(
            mediaItem = repository.getVideo("BigBuckBunny.mp4")
        )
    }

    fun setIsInPictureInPictureMode(isInPictureInPictureMode: Boolean) {
        uiState.value = uiState.value.copy(
            isInPictureInPictureMode = isInPictureInPictureMode
        )
    }

    fun setIsPictureInPictureEnabled(isEnabled: Boolean) {
        uiState.value = uiState.value.copy(
            isPictureInPictureEnabled = isEnabled
        )
    }
}
