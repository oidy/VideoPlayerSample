package com.devhomc.videoplayersample.ui.player

import android.os.Bundle
import android.util.Rational
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.PictureInPictureModeChangedInfo
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.VideoSize
import androidx.window.layout.FoldingFeature
import androidx.window.layout.FoldingFeature.Orientation.Companion.HORIZONTAL
import androidx.window.layout.FoldingFeature.Orientation.Companion.VERTICAL
import androidx.window.layout.FoldingFeature.State.Companion.HALF_OPENED
import androidx.window.layout.WindowInfoTracker
import com.devhomc.videoplayersample.ui.theme.VideoPlayerSampleTheme
import com.devhomc.videoplayersample.util.DevicePosture
import com.devhomc.videoplayersample.util.enterPictureInPicture
import com.devhomc.videoplayersample.util.updatePictureInPictureParams
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@AndroidEntryPoint
class PlayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModels()

    private val pictureInPictureModeListener = Consumer<PictureInPictureModeChangedInfo> { info ->
        viewModel.setIsInPictureInPictureMode(info.isInPictureInPictureMode)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        addOnPictureInPictureModeChangedListener(pictureInPictureModeListener)

        val devicePosture = WindowInfoTracker.getOrCreate(this)
            .windowLayoutInfo(this)
            .flowWithLifecycle(lifecycle)
            .map { layoutInfo ->
                val foldingFeature = layoutInfo.displayFeatures
                    .filterIsInstance<FoldingFeature>()
                    .firstOrNull()

                foldingFeature?.toDevicePosture() ?: DevicePosture.Normal
            }
            .stateIn(
                scope = lifecycleScope,
                started = SharingStarted.Eagerly,
                initialValue = DevicePosture.Normal
            )

        setContent {
            VideoPlayerSampleTheme {
                PlayerScreen(
                    viewModel = viewModel,
                    devicePosture = devicePosture,
                    onPlayWhenReadyChanged = ::updatePictureInPictureEnabled,
                    onVideoSizeChanged = ::updatePictureInPictureAspectRatio
                )
            }
        }
    }

    override fun onDestroy() {
        removeOnPictureInPictureModeChangedListener(pictureInPictureModeListener)
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        if (viewModel.uiState.value.isPictureInPictureEnabled) {
            enterPictureInPicture()
        }
    }

    private fun updatePictureInPictureEnabled(isEnabled: Boolean) {
        viewModel.setIsPictureInPictureEnabled(isEnabled)
        updatePictureInPictureParams(isEnabled = isEnabled)
    }

    private fun updatePictureInPictureAspectRatio(videoSize: VideoSize) =
        updatePictureInPictureParams(aspectRatio = Rational(videoSize.width, videoSize.height))

    private fun FoldingFeature.toDevicePosture(): DevicePosture {
        return when {
            state == HALF_OPENED && orientation == HORIZONTAL -> DevicePosture.TableTop(bounds)
            state == HALF_OPENED && orientation == VERTICAL -> DevicePosture.Book(bounds)
            else -> DevicePosture.Normal
        }
    }
}
