package com.devhomc.videoplayersample.ui.player

import android.content.ComponentName
import android.graphics.Rect
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.concurrent.futures.await
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.devhomc.videoplayersample.service.PlayerService
import com.devhomc.videoplayersample.ui.theme.VideoPlayerSampleTheme
import com.devhomc.videoplayersample.util.DevicePosture
import kotlinx.coroutines.flow.StateFlow

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    devicePosture: StateFlow<DevicePosture>,
    onPlayWhenReadyChanged: (Boolean) -> Unit,
    onVideoSizeChanged: (VideoSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val devicePostureValue by devicePosture.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    PlayerScreen(
        uiState = uiState,
        devicePosture = devicePostureValue,
        onPlayWhenReadyChanged = onPlayWhenReadyChanged,
        onVideoSizeChanged = onVideoSizeChanged,
        modifier = modifier
    )
}

@Composable
fun PlayerScreen(
    uiState: PlayerUiState,
    devicePosture: DevicePosture,
    onPlayWhenReadyChanged: (Boolean) -> Unit,
    onVideoSizeChanged: (VideoSize) -> Unit,
    modifier: Modifier = Modifier
) {
    val playerModifier = when (devicePosture) {
        is DevicePosture.TableTop -> {
            val playerHeight = with(LocalDensity.current) {
                (devicePosture.hingePosition.top - WindowInsets.systemBars.getTop(this)).toDp()
            }

            Modifier
                .fillMaxWidth()
                .height(playerHeight)
        }
        is DevicePosture.Book -> {
            val playerWidth = with(LocalDensity.current) { devicePosture.hingePosition.left.toDp() }

            Modifier
                .width(playerWidth)
                .fillMaxHeight()
        }
        else -> Modifier.fillMaxSize()
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        VideoPlayer(
            useController = !uiState.isInPictureInPictureMode,
            onPlayWhenReadyChanged = onPlayWhenReadyChanged,
            onVideoSizeChanged = onVideoSizeChanged,
            mediaItem = uiState.mediaItem,
            modifier = playerModifier.background(Color.Black)
        )
    }
}

@Composable
fun VideoPlayer(
    useController: Boolean,
    onPlayWhenReadyChanged: (Boolean) -> Unit,
    onVideoSizeChanged: (VideoSize) -> Unit,
    mediaItem: MediaItem?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val playerView = remember {
        PlayerView(context)
    }

    LaunchedEffect(useController) {
        playerView.useController = useController
    }

    var mediaController by remember {
        mutableStateOf<MediaController?>(null)
    }

    LaunchedEffect(mediaItem) {
        if (mediaItem != null) {
            if (mediaController == null) {
                val sessionToken = SessionToken(context, ComponentName(context, PlayerService::class.java))
                mediaController = MediaController.Builder(context, sessionToken)
                    .buildAsync()
                    .await()

                mediaController?.addListener(object : Player.Listener {
                    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
                        onPlayWhenReadyChanged(playWhenReady)
                    }

                    override fun onVideoSizeChanged(videoSize: VideoSize) {
                        onVideoSizeChanged(videoSize)
                    }
                })

                playerView.player = mediaController
                mediaController?.playWhenReady = true
                mediaController?.prepare()
            }
            if (mediaController?.currentMediaItem != mediaItem) {
                mediaController?.setMediaItem(mediaItem)
            }
        }
    }

    DisposableEffect(
        AndroidView(
            factory = { playerView },
            modifier = modifier
        )
    ) {
        onDispose {
            mediaController?.release()
        }
    }
}

@Preview
@Composable
private fun PlayerScreenScreenPreview() {
    VideoPlayerSampleTheme {
        PlayerScreen(
            uiState = PlayerUiState(),
            devicePosture = DevicePosture.Normal,
            onPlayWhenReadyChanged = {},
            onVideoSizeChanged = {}
        )
    }
}

@Preview(widthDp = 500, heightDp = 500)
@Composable
private fun TableTopPosturePlayerScreenScreenPreview() {
    VideoPlayerSampleTheme {
        val hingePosition = with(LocalDensity.current) {
            Rect(0, 250.dp.roundToPx(), 500.dp.roundToPx(), 250.dp.roundToPx())
        }
        PlayerScreen(
            uiState = PlayerUiState(),
            devicePosture = DevicePosture.TableTop(hingePosition),
            onPlayWhenReadyChanged = {},
            onVideoSizeChanged = {}
        )
    }
}

@Preview(widthDp = 500, heightDp = 500)
@Composable
private fun BookPosturePlayerScreenScreenPreview() {
    VideoPlayerSampleTheme {
        val hingePosition = with(LocalDensity.current) {
            Rect(250.dp.roundToPx(), 0, 250.dp.roundToPx(), 500.dp.roundToPx())
        }
        PlayerScreen(
            uiState = PlayerUiState(),
            devicePosture = DevicePosture.Book(hingePosition),
            onPlayWhenReadyChanged = {},
            onVideoSizeChanged = {}
        )
    }
}
