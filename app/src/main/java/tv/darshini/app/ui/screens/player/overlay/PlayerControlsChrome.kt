package tv.darshini.app.ui.screens.player.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Hd
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import tv.darshini.app.R
import tv.darshini.app.device.rememberIsTelevisionDevice
import tv.darshini.app.ui.components.rememberCrossfadeImageModel
import tv.darshini.app.ui.screens.player.NumericChannelInputState
import tv.darshini.app.ui.screens.player.PlayerTimeshiftUiState
import tv.darshini.app.ui.screens.player.SeekPreviewState
import tv.darshini.app.ui.screens.player.SleepTimerUiState
import tv.darshini.app.ui.time.LocalAppTimeFormat
import tv.darshini.app.ui.time.createTimeFormat
import tv.darshini.app.ui.theme.ErrorColor
import tv.darshini.app.ui.theme.Primary
import tv.darshini.domain.model.Program
import tv.darshini.domain.model.RecordingStatus
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Date
import java.util.Locale
import tv.darshini.app.ui.interaction.TvClickableSurface
import tv.darshini.app.ui.interaction.TvButton
import tv.darshini.app.ui.interaction.TvIconButton

private data class PlayerActionSpec(
    val label: String,
    val onClick: () -> Unit
)

@Composable
fun PlayerControlsOverlay(
    visible: Boolean,
    title: String,
    contentType: String,
    isCatchUpPlayback: Boolean = false,
    isPlaying: Boolean,
    currentProgram: Program?,
    currentChannelName: String?,
    displayChannelNumber: Int,
    currentPosition: Long,
    duration: Long,
    aspectRatioLabel: String,
    subtitleTrackCount: Int,
    liveTranslationAvailable: Boolean = false,
    audioTrackCount: Int,
    videoQualityCount: Int,
    currentRecordingStatus: RecordingStatus?,
    isMuted: Boolean,
    playbackSpeed: Float = 1f,
    mediaTitle: String?,
    sleepTimerUiState: SleepTimerUiState = SleepTimerUiState(),
    timeshiftUiState: PlayerTimeshiftUiState = PlayerTimeshiftUiState(),
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester = FocusRequester(),
    onClose: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onRestartProgram: () -> Unit,
    onOpenArchive: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onScheduleRecording: () -> Unit,
    onScheduleDailyRecording: () -> Unit = {},
    onScheduleWeeklyRecording: () -> Unit = {},
    onToggleAspectRatio: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit = {},
    onOpenStopPlaybackTimer: () -> Unit = {},
    onOpenIdleStandbyTimer: () -> Unit = {},
    onOpenAudioVideoSync: () -> Unit = {},
    audioVideoSyncEnabled: Boolean = false,
    showEpisodesAction: Boolean = false,
    onOpenEpisodes: () -> Unit = {},
    onOpenSplitScreen: () -> Unit,
    onEnterPictureInPicture: () -> Unit = {},
    onToggleMute: () -> Unit,
    isCastConnected: Boolean = false,
    onCast: () -> Unit = {},
    onStopCasting: () -> Unit = {},
    onSeekToLiveEdge: () -> Unit = {},
    onSeekToPosition: (Long) -> Unit = {},
    onSetScrubbingMode: (Boolean) -> Unit = {},
    showExternalPlayerAction: Boolean = false,
    onOpenExternalPlayer: () -> Unit = {},
    seekPreview: SeekPreviewState = SeekPreviewState(),
    onSeekPreviewPositionChanged: (Long?) -> Unit = {},
    clockLabelOverride: String? = null,
    onUserInteraction: () -> Unit = {},
    hasNextEpisode: Boolean = false,
    onPlayNextEpisode: () -> Unit = {},
    selectedAudioTrackName: String? = null,
    selectedSubtitleTrackName: String? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.nativeKeyEvent.action != android.view.KeyEvent.ACTION_DOWN) return@onPreviewKeyEvent false
                    when (event.nativeKeyEvent.keyCode) {
                        android.view.KeyEvent.KEYCODE_DPAD_UP,
                        android.view.KeyEvent.KEYCODE_DPAD_DOWN,
                        android.view.KeyEvent.KEYCODE_DPAD_LEFT,
                        android.view.KeyEvent.KEYCODE_DPAD_RIGHT,
                        android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                        android.view.KeyEvent.KEYCODE_ENTER,
                        android.view.KeyEvent.KEYCODE_NUMPAD_ENTER -> onUserInteraction()
                    }
                    false
                }
        ) {
            PlayerTopBar(
                title = title,
                contentType = contentType,
                clockLabelOverride = clockLabelOverride,
                onClose = onClose,
                modifier = Modifier.align(Alignment.TopCenter)
            )

            PlayerBottomBar(
                title = title,
                contentType = contentType,
                isCatchUpPlayback = isCatchUpPlayback,
                isPlaying = isPlaying,
                currentProgram = currentProgram,
                currentChannelName = currentChannelName,
                displayChannelNumber = displayChannelNumber,
                currentPosition = currentPosition,
                duration = duration,
                aspectRatioLabel = aspectRatioLabel,
                subtitleTrackCount = subtitleTrackCount,
                liveTranslationAvailable = liveTranslationAvailable,
                audioTrackCount = audioTrackCount,
                videoQualityCount = videoQualityCount,
                currentRecordingStatus = currentRecordingStatus,
                isMuted = isMuted,
                playbackSpeed = playbackSpeed,
                mediaTitle = mediaTitle,
                sleepTimerUiState = sleepTimerUiState,
                timeshiftUiState = timeshiftUiState,
                playButtonFocusRequester = playButtonFocusRequester,
                quickActionsFocusRequester = quickActionsFocusRequester,
                modifier = Modifier.align(Alignment.BottomCenter),
                onRestartProgram = onRestartProgram,
                onOpenArchive = onOpenArchive,
                onStartRecording = onStartRecording,
                onStopRecording = onStopRecording,
                onScheduleRecording = onScheduleRecording,
                onScheduleDailyRecording = onScheduleDailyRecording,
                onScheduleWeeklyRecording = onScheduleWeeklyRecording,
                onToggleAspectRatio = onToggleAspectRatio,
                onOpenSubtitleTracks = onOpenSubtitleTracks,
                onOpenAudioTracks = onOpenAudioTracks,
                onOpenVideoTracks = onOpenVideoTracks,
                onOpenPlaybackSpeed = onOpenPlaybackSpeed,
                onOpenStopPlaybackTimer = onOpenStopPlaybackTimer,
                onOpenIdleStandbyTimer = onOpenIdleStandbyTimer,
                onOpenAudioVideoSync = onOpenAudioVideoSync,
                audioVideoSyncEnabled = audioVideoSyncEnabled,
                showEpisodesAction = showEpisodesAction,
                onOpenEpisodes = onOpenEpisodes,
                onOpenSplitScreen = onOpenSplitScreen,
                onEnterPictureInPicture = onEnterPictureInPicture,
                onToggleMute = onToggleMute,
                isCastConnected = isCastConnected,
                onCast = onCast,
                onStopCasting = onStopCasting,
                onSeekToLiveEdge = onSeekToLiveEdge,
                onTogglePlayPause = onTogglePlayPause,
                onSeekBackward = onSeekBackward,
                onSeekForward = onSeekForward,
                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                seekPreview = seekPreview,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged,
                showExternalPlayerAction = showExternalPlayerAction,
                onOpenExternalPlayer = onOpenExternalPlayer,
                hasNextEpisode = hasNextEpisode,
                onPlayNextEpisode = onPlayNextEpisode,
                selectedAudioTrackName = selectedAudioTrackName,
                selectedSubtitleTrackName = selectedSubtitleTrackName
            )
        }
    }
}

@Composable
fun PlayerZapOverlay(
    visible: Boolean,
    displayChannelNumber: Int,
    channelName: String?,
    programTitle: String?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally(),
        exit = fadeOut() + slideOutHorizontally(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .padding(32.dp)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.84f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(18.dp)
                .widthIn(min = 320.dp, max = 460.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (displayChannelNumber > 0) {
                    Text(
                        text = displayChannelNumber.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = Primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
                Column {
                    Text(
                        text = channelName.orEmpty(),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!programTitle.isNullOrBlank()) {
                        Text(
                            text = programTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.78f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PlayerNumericInputOverlay(
    state: NumericChannelInputState?,
    visible: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && state != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it / 2 }),
        modifier = modifier
    ) {
        val inputState = state ?: return@AnimatedVisibility
        Box(
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.82f), RoundedCornerShape(14.dp))
                .padding(horizontal = 22.dp, vertical = 12.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = inputState.input,
                    style = MaterialTheme.typography.headlineMedium,
                    color = if (inputState.invalid) ErrorColor else Primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when {
                        inputState.invalid -> stringResource(R.string.player_channel_not_found)
                        !inputState.matchedChannelName.isNullOrBlank() -> inputState.matchedChannelName
                        else -> stringResource(R.string.player_type_channel_number)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun PlayerAspectRatioToast(
    aspectRatioLabel: String,
    controlsVisible: Boolean,
    modifier: Modifier = Modifier
) {
    var show by remember { mutableStateOf(false) }
    LaunchedEffect(aspectRatioLabel) {
        show = true
        delay(2000)
        show = false
    }

    AnimatedVisibility(
        visible = show && !controlsVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .background(Primary.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            Text(
                text = stringResource(R.string.player_aspect_ratio_label, aspectRatioLabel),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PlayerResolutionBadge(
    visible: Boolean,
    resolutionLabel: String,
    modifier: Modifier = Modifier
) {
    if (!visible) return

    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = resolutionLabel,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PlayerTopBar(
    title: String,
    contentType: String,
    clockLabelOverride: String?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val appTimeFormat = LocalAppTimeFormat.current
    val timeFormat = remember(appTimeFormat) { appTimeFormat.createTimeFormat() }
    val topBarHeight = when {
        screenWidth < 700.dp -> 100.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 116.dp
        else -> 132.dp
    }
    val horizontalPadding = when {
        screenWidth < 700.dp -> 18.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 24.dp
        else -> 32.dp
    }
    val verticalPadding = when {
        screenWidth < 700.dp -> 16.dp
        !isTelevisionDevice && screenWidth < 1280.dp -> 20.dp
        else -> 24.dp
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                )
            )
            .padding(horizontal = horizontalPadding, vertical = verticalPadding)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                PlayerMetaPill(
                    text = when (contentType) {
                        "LIVE" -> stringResource(R.string.nav_live_tv)
                        "MOVIE" -> stringResource(R.string.player_type_movie)
                        else -> stringResource(R.string.player_type_series)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (contentType != "LIVE") {
                    Text(
                        text = if (contentType == "MOVIE") {
                            stringResource(R.string.player_type_movie)
                        } else {
                            stringResource(R.string.player_type_series)
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val currentTime = remember(clockLabelOverride, timeFormat) {
                    mutableStateOf(
                        clockLabelOverride ?: timeFormat.format(Date())
                    )
                }
                LaunchedEffect(clockLabelOverride, timeFormat) {
                    if (clockLabelOverride == null) {
                        while (true) {
                            currentTime.value = timeFormat.format(Date())
                            delay(10_000)
                        }
                    } else {
                        currentTime.value = clockLabelOverride
                    }
                }
                Text(
                    text = currentTime.value,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.padding(end = 16.dp)
                )

                TvClickableSurface(
                    onClick = onClose,
                    shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(999.dp)),
                    colors = ClickableSurfaceDefaults.colors(
                        containerColor = Color.White.copy(alpha = 0.12f),
                        focusedContainerColor = Primary.copy(alpha = 0.9f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.player_close),
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerBottomBar(
    title: String,
    contentType: String,
    isCatchUpPlayback: Boolean = false,
    isPlaying: Boolean,
    currentProgram: Program?,
    currentChannelName: String?,
    displayChannelNumber: Int,
    currentPosition: Long,
    duration: Long,
    aspectRatioLabel: String,
    subtitleTrackCount: Int,
    liveTranslationAvailable: Boolean = false,
    audioTrackCount: Int,
    videoQualityCount: Int,
    currentRecordingStatus: RecordingStatus?,
    isMuted: Boolean,
    playbackSpeed: Float,
    mediaTitle: String?,
    sleepTimerUiState: SleepTimerUiState,
    timeshiftUiState: PlayerTimeshiftUiState,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    onRestartProgram: () -> Unit,
    onOpenArchive: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onScheduleRecording: () -> Unit,
    onScheduleDailyRecording: () -> Unit,
    onScheduleWeeklyRecording: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onOpenStopPlaybackTimer: () -> Unit,
    onOpenIdleStandbyTimer: () -> Unit,
    onOpenAudioVideoSync: () -> Unit,
    audioVideoSyncEnabled: Boolean,
    showEpisodesAction: Boolean,
    onOpenEpisodes: () -> Unit,
    onOpenSplitScreen: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onToggleMute: () -> Unit,
    isCastConnected: Boolean,
    onCast: () -> Unit,
    onStopCasting: () -> Unit,
    onSeekToLiveEdge: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    seekPreview: SeekPreviewState,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    showExternalPlayerAction: Boolean,
    onOpenExternalPlayer: () -> Unit,
    hasNextEpisode: Boolean = false,
    onPlayNextEpisode: () -> Unit = {},
    selectedAudioTrackName: String? = null,
    selectedSubtitleTrackName: String? = null,
    modifier: Modifier = Modifier
) {
    val isVod = contentType != "LIVE" || isCatchUpPlayback
    if (isVod) {
        // ponytail: VOD = no card, full-bleed progress bar anchored to bottom edge
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.92f))
                    )
                )
                .padding(top = 56.dp, bottom = 14.dp)
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            PlayerVodInfo(
                title = title,
                contentType = contentType,
                isPlaying = isPlaying,
                currentPosition = currentPosition,
                duration = duration,
                aspectRatioLabel = aspectRatioLabel,
                subtitleTrackCount = subtitleTrackCount,
                audioTrackCount = audioTrackCount,
                videoQualityCount = videoQualityCount,
                isMuted = isMuted,
                playbackSpeed = playbackSpeed,
                sleepTimerUiState = sleepTimerUiState,
                playButtonFocusRequester = playButtonFocusRequester,
                quickActionsFocusRequester = quickActionsFocusRequester,
                onSeekToPosition = onSeekToPosition,
                onSetScrubbingMode = onSetScrubbingMode,
                onToggleAspectRatio = onToggleAspectRatio,
                onOpenSubtitleTracks = onOpenSubtitleTracks,
                onOpenAudioTracks = onOpenAudioTracks,
                onOpenVideoTracks = onOpenVideoTracks,
                onOpenPlaybackSpeed = onOpenPlaybackSpeed,
                onOpenStopPlaybackTimer = onOpenStopPlaybackTimer,
                onOpenIdleStandbyTimer = onOpenIdleStandbyTimer,
                onOpenAudioVideoSync = onOpenAudioVideoSync,
                audioVideoSyncEnabled = audioVideoSyncEnabled,
                showEpisodesAction = showEpisodesAction,
                onOpenEpisodes = onOpenEpisodes,
                onEnterPictureInPicture = onEnterPictureInPicture,
                onToggleMute = onToggleMute,
                isCastConnected = isCastConnected,
                onCast = onCast,
                onStopCasting = onStopCasting,
                onTogglePlayPause = onTogglePlayPause,
                onSeekBackward = onSeekBackward,
                onSeekForward = onSeekForward,
                seekPreview = seekPreview,
                onSeekPreviewPositionChanged = onSeekPreviewPositionChanged,
                showExternalPlayerAction = showExternalPlayerAction,
                onOpenExternalPlayer = onOpenExternalPlayer,
                hasNextEpisode = hasNextEpisode,
                onPlayNextEpisode = onPlayNextEpisode,
                selectedAudioTrackName = selectedAudioTrackName,
                selectedSubtitleTrackName = selectedSubtitleTrackName
            )
          }
        }
        return
    }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.84f))
                )
            )
            .padding(horizontal = 32.dp, vertical = 24.dp)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = SurfaceDefaults.colors(containerColor = Color(0xFF0C1624).copy(alpha = 0.92f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Primary.copy(alpha = 0.10f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 22.dp)
            ) {
                run {
                    PlayerLiveInfo(
                        currentProgram = currentProgram,
                        currentChannelName = currentChannelName,
                        displayChannelNumber = displayChannelNumber,
                        aspectRatioLabel = aspectRatioLabel,
                        subtitleTrackCount = subtitleTrackCount,
                        liveTranslationAvailable = liveTranslationAvailable,
                        audioTrackCount = audioTrackCount,
                        videoQualityCount = videoQualityCount,
                        currentRecordingStatus = currentRecordingStatus,
                        isMuted = isMuted,
                        mediaTitle = mediaTitle,
                        sleepTimerUiState = sleepTimerUiState,
                        timeshiftUiState = timeshiftUiState,
                        playButtonFocusRequester = playButtonFocusRequester,
                        quickActionsFocusRequester = quickActionsFocusRequester,
                        onRestartProgram = onRestartProgram,
                        onOpenArchive = onOpenArchive,
                        onStartRecording = onStartRecording,
                        onStopRecording = onStopRecording,
                        onScheduleRecording = onScheduleRecording,
                        onScheduleDailyRecording = onScheduleDailyRecording,
                        onScheduleWeeklyRecording = onScheduleWeeklyRecording,
                        onToggleAspectRatio = onToggleAspectRatio,
                        onOpenSubtitleTracks = onOpenSubtitleTracks,
                        onOpenAudioTracks = onOpenAudioTracks,
                        onOpenVideoTracks = onOpenVideoTracks,
                        onOpenStopPlaybackTimer = onOpenStopPlaybackTimer,
                        onOpenIdleStandbyTimer = onOpenIdleStandbyTimer,
                        onOpenAudioVideoSync = onOpenAudioVideoSync,
                        audioVideoSyncEnabled = audioVideoSyncEnabled,
                        onOpenSplitScreen = onOpenSplitScreen,
                        onEnterPictureInPicture = onEnterPictureInPicture,
                        onToggleMute = onToggleMute,
                        isCastConnected = isCastConnected,
                        onCast = onCast,
                        onStopCasting = onStopCasting
                        ,
                        isPlaying = isPlaying,
                        onTogglePlayPause = onTogglePlayPause,
                        onSeekBackward = onSeekBackward,
                        onSeekForward = onSeekForward,
                        onSeekToLiveEdge = onSeekToLiveEdge,
                        onSeekToPosition = onSeekToPosition,
                        onSetScrubbingMode = onSetScrubbingMode,
                        showExternalPlayerAction = showExternalPlayerAction,
                        onOpenExternalPlayer = onOpenExternalPlayer
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayerLiveInfo(
    currentProgram: Program?,
    currentChannelName: String?,
    displayChannelNumber: Int,
    aspectRatioLabel: String,
    subtitleTrackCount: Int,
    liveTranslationAvailable: Boolean,
    audioTrackCount: Int,
    videoQualityCount: Int,
    currentRecordingStatus: RecordingStatus?,
    isMuted: Boolean,
    mediaTitle: String?,
    sleepTimerUiState: SleepTimerUiState,
    timeshiftUiState: PlayerTimeshiftUiState,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    onRestartProgram: () -> Unit,
    onOpenArchive: () -> Unit,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onScheduleRecording: () -> Unit,
    onScheduleDailyRecording: () -> Unit,
    onScheduleWeeklyRecording: () -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenStopPlaybackTimer: () -> Unit,
    onOpenIdleStandbyTimer: () -> Unit,
    onOpenAudioVideoSync: () -> Unit,
    audioVideoSyncEnabled: Boolean,
    onOpenSplitScreen: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onToggleMute: () -> Unit,
    isCastConnected: Boolean,
    onCast: () -> Unit,
    onStopCasting: () -> Unit,
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekToLiveEdge: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    showExternalPlayerAction: Boolean,
    onOpenExternalPlayer: () -> Unit
) {
    val showTimeshiftControls = timeshiftUiState.available && !isCastConnected
    val appTimeFormat = LocalAppTimeFormat.current
    val timeFormat = remember(appTimeFormat) { appTimeFormat.createTimeFormat() }
    val primaryActions = buildList {
        if (showTimeshiftControls) {
            add(PlayerActionSpec(stringResource(R.string.player_jump_to_live), onSeekToLiveEdge))
        }
        add(PlayerActionSpec(
            stringResource(if (isMuted) R.string.player_unmute else R.string.player_mute),
            onToggleMute
        ))
        add(PlayerActionSpec(
            stringResource(if (isCastConnected) R.string.player_stop_casting else R.string.player_cast),
            if (isCastConnected) onStopCasting else onCast
        ))
        add(PlayerActionSpec(
            sleepTimerActionLabel(
                title = stringResource(R.string.player_stop_playback_after),
                activeLabel = stringResource(
                    R.string.player_stop_timer_status,
                    formatTimerRemaining(sleepTimerUiState.stopRemainingMs)
                ),
                active = sleepTimerUiState.stopTimerActive
            ),
            onOpenStopPlaybackTimer
        ))
        add(PlayerActionSpec(
            sleepTimerActionLabel(
                title = stringResource(R.string.player_idle_standby_after),
                activeLabel = stringResource(
                    R.string.player_idle_timer_status,
                    formatTimerRemaining(sleepTimerUiState.idleRemainingMs)
                ),
                active = sleepTimerUiState.idleTimerActive
            ),
            onOpenIdleStandbyTimer
        ))
        add(PlayerActionSpec(stringResource(R.string.player_picture_in_picture), onEnterPictureInPicture))
        if (showExternalPlayerAction) {
            add(PlayerActionSpec(stringResource(R.string.player_open_in_external_player), onOpenExternalPlayer))
        }
        if (currentProgram?.hasArchive == true) {
            add(PlayerActionSpec(stringResource(R.string.player_restart), onRestartProgram))
            add(PlayerActionSpec(stringResource(R.string.player_archive), onOpenArchive))
        }
        if (currentRecordingStatus == RecordingStatus.RECORDING) {
            add(PlayerActionSpec(stringResource(R.string.player_stop_recording), onStopRecording))
        } else {
            add(PlayerActionSpec(stringResource(R.string.player_record), onStartRecording))
            add(PlayerActionSpec(stringResource(R.string.player_schedule_recording), onScheduleRecording))
            add(PlayerActionSpec(stringResource(R.string.player_schedule_daily_recording), onScheduleDailyRecording))
            add(PlayerActionSpec(stringResource(R.string.player_schedule_weekly_recording), onScheduleWeeklyRecording))
        }
    }
    val secondaryActions = buildList {
        add(PlayerActionSpec(stringResource(R.string.player_aspect_ratio_label, aspectRatioLabel), onToggleAspectRatio))
        if (subtitleTrackCount > 0 || liveTranslationAvailable) {
            add(PlayerActionSpec(stringResource(R.string.player_subs), onOpenSubtitleTracks))
        }
        if (audioTrackCount > 0) {
            add(PlayerActionSpec(stringResource(R.string.player_audio), onOpenAudioTracks))
        }
        if (videoQualityCount > 0) {
            add(PlayerActionSpec(stringResource(R.string.player_video_quality), onOpenVideoTracks))
        }
        if (audioVideoSyncEnabled && !isCastConnected) {
            add(PlayerActionSpec(stringResource(R.string.player_av_sync_short), onOpenAudioVideoSync))
        }
        add(PlayerActionSpec(stringResource(R.string.multiview_nav), onOpenSplitScreen))
    }

    Row(verticalAlignment = Alignment.Top) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PlayerMetaPill(
                    text = if (showTimeshiftControls) {
                        stringResource(R.string.player_live_rewind_badge)
                    } else {
                        stringResource(R.string.player_live_now)
                    },
                    accent = true
                )
                if (displayChannelNumber > 0) {
                    PlayerMetaPill(text = stringResource(R.string.player_live_channel, displayChannelNumber))
                }
                if (currentProgram?.hasArchive == true) {
                    PlayerMetaPill(text = stringResource(R.string.player_archive_badge))
                }
                if (isMuted) {
                    PlayerMetaPill(text = stringResource(R.string.player_muted_badge))
                }
                if (sleepTimerUiState.stopTimerActive) {
                    PlayerMetaPill(text = stringResource(R.string.player_stop_timer_status, formatTimerRemaining(sleepTimerUiState.stopRemainingMs)))
                }
                if (sleepTimerUiState.idleTimerActive) {
                    PlayerMetaPill(text = stringResource(R.string.player_idle_timer_status, formatTimerRemaining(sleepTimerUiState.idleRemainingMs)))
                }
                if (showTimeshiftControls) {
                    PlayerMetaPill(
                        text = if (timeshiftUiState.bufferedBehindLiveMs > 1_000L) {
                            stringResource(
                                R.string.player_live_offset,
                                formatDuration(timeshiftUiState.bufferedBehindLiveMs)
                            )
                        } else {
                            stringResource(R.string.player_live_ready)
                        }
                    )
                }
            }
            Text(
                text = currentProgram?.title ?: mediaTitle ?: currentChannelName.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = if (currentProgram != null) {
                    if (displayChannelNumber > 0) {
                        stringResource(R.string.channel_number_name_format, displayChannelNumber, currentChannelName.orEmpty())
                    } else {
                        currentChannelName.orEmpty()
                    }
                } else {
                    stringResource(R.string.player_no_guide_data)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.68f)
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (showTimeshiftControls) {
        LiveTimeshiftScrubber(
            timeshiftUiState = timeshiftUiState,
            isPlaying = isPlaying,
            playButtonFocusRequester = playButtonFocusRequester,
            quickActionsFocusRequester = quickActionsFocusRequester,
            onTogglePlayPause = onTogglePlayPause,
            onSeekBackward = onSeekBackward,
            onSeekForward = onSeekForward,
            onSeekToPosition = onSeekToPosition,
            onSeekToLiveEdge = onSeekToLiveEdge,
            onSetScrubbingMode = onSetScrubbingMode
        )
        Spacer(modifier = Modifier.height(16.dp))
    }

    val start = currentProgram?.startTime ?: 0L
    val end = currentProgram?.endTime ?: 0L
    if (start > 0 && end > 0) {
        val now = System.currentTimeMillis()
        val progress = (now - start).toFloat() / (end - start)
        LinearProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
            color = Primary,
            trackColor = Color.White.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timeFormat.format(Date(start)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = timeFormat.format(Date(end)),
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    } else {
        Text(
            text = currentChannelName?.let { stringResource(R.string.channel_number_name_format, displayChannelNumber, it) }.orEmpty(),
            style = MaterialTheme.typography.labelLarge,
            color = Color.White.copy(alpha = 0.82f)
        )
        Spacer(modifier = Modifier.height(10.dp))
    }

    PlayerQuickActionRows(
        primaryActions = primaryActions,
        secondaryActions = secondaryActions,
        firstActionFocusRequester = quickActionsFocusRequester,
        primaryActionsUpFocusRequester = playButtonFocusRequester
    )
}

@Composable
private fun PlayerVodInfo(
    title: String,
    contentType: String,
    isPlaying: Boolean,
    currentPosition: Long,
    duration: Long,
    aspectRatioLabel: String,
    subtitleTrackCount: Int,
    audioTrackCount: Int,
    videoQualityCount: Int,
    isMuted: Boolean,
    playbackSpeed: Float,
    sleepTimerUiState: SleepTimerUiState,
    audioVideoSyncEnabled: Boolean,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    onSeekToPosition: (Long) -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    onToggleAspectRatio: () -> Unit,
    onOpenSubtitleTracks: () -> Unit,
    onOpenAudioTracks: () -> Unit,
    onOpenVideoTracks: () -> Unit,
    onOpenPlaybackSpeed: () -> Unit,
    onOpenStopPlaybackTimer: () -> Unit,
    onOpenIdleStandbyTimer: () -> Unit,
    onOpenAudioVideoSync: () -> Unit,
    showEpisodesAction: Boolean,
    onOpenEpisodes: () -> Unit,
    onEnterPictureInPicture: () -> Unit,
    onToggleMute: () -> Unit,
    isCastConnected: Boolean,
    onCast: () -> Unit,
    onStopCasting: () -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    seekPreview: SeekPreviewState,
    onSeekPreviewPositionChanged: (Long?) -> Unit,
    showExternalPlayerAction: Boolean,
    onOpenExternalPlayer: () -> Unit,
    hasNextEpisode: Boolean = false,
    onPlayNextEpisode: () -> Unit = {},
    selectedAudioTrackName: String? = null,
    selectedSubtitleTrackName: String? = null
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val isTelevisionDevice = rememberIsTelevisionDevice()
    val compactControls = screenWidth < 700.dp
    val tabletControls = !isTelevisionDevice && screenWidth >= 700.dp && screenWidth < 1280.dp

    val seekPreviewWidth = when {
        compactControls -> 148.dp
        tabletControls -> 168.dp
        else -> 188.dp
    }

    val playbackLabel = stringResource(R.string.player_playback_label)

    var sliderValue by remember(duration, currentPosition) {
        mutableStateOf(if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f)
    }
    var isScrubbing by remember { mutableStateOf(false) }
    val latestSeekCallback by rememberUpdatedState(onSeekToPosition)
    val latestScrubbingCallback by rememberUpdatedState(onSetScrubbingMode)
    val latestSeekPreviewPositionChanged by rememberUpdatedState(onSeekPreviewPositionChanged)

    LaunchedEffect(duration, currentPosition, isScrubbing) {
        if (!isScrubbing) {
            sliderValue = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f
        }
    }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Full-width progress bar (edge-to-edge; thumb kept off the screen edge with a tiny inset)
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
        // Seek preview card \u2014 floats at current slider position
        AnimatedVisibility(visible = seekPreview.visible) {
            val fraction = sliderValue.coerceIn(0f, 1f)
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(modifier = Modifier.fillMaxWidth(fraction)) {
                    PlayerSeekPreviewCard(
                        preview = seekPreview,
                        previewHeight = when {
                            compactControls -> 96.dp
                            tabletControls -> 106.dp
                            else -> 118.dp
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .width(seekPreviewWidth)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Full-width slider
        Slider(
            value = sliderValue,
            onValueChange = { newValue ->
                val clamped = newValue.coerceIn(0f, 1f)
                if (!isScrubbing) {
                    isScrubbing = true
                    latestScrubbingCallback(true)
                }
                sliderValue = clamped
                if (duration > 0) {
                    latestSeekPreviewPositionChanged((clamped * duration).toLong())
                }
            },
            onValueChangeFinished = {
                if (duration > 0) {
                    latestSeekCallback((sliderValue.coerceIn(0f, 1f) * duration).toLong())
                }
                if (isScrubbing) {
                    latestScrubbingCallback(false)
                    isScrubbing = false
                }
                latestSeekPreviewPositionChanged(null)
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(playButtonFocusRequester)
                .focusProperties { down = quickActionsFocusRequester }
                .semantics { contentDescription = playbackLabel },
            enabled = duration > 0,
            colors = SliderDefaults.colors(
                activeTrackColor = Primary,
                thumbColor = Color.White,
                inactiveTrackColor = Color.White.copy(alpha = 0.22f)
            )
        )
    }

    // ONE control row: [currentTime] [5 icon buttons] [totalDuration]
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 20.dp, end = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatDuration(
                if (isScrubbing && duration > 0) (sliderValue * duration).toLong()
                else currentPosition
            ),
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            PlayerBarIconButton(
                icon = Icons.Default.ClosedCaption,
                contentDescription = stringResource(R.string.player_subs),
                onClick = onOpenSubtitleTracks,
                modifier = if (audioTrackCount == 0) Modifier.focusRequester(quickActionsFocusRequester) else Modifier
            )
            PlayerBarIconButton(
                icon = Icons.Default.Audiotrack,
                contentDescription = stringResource(R.string.player_audio),
                onClick = onOpenAudioTracks,
                modifier = if (audioTrackCount > 0) Modifier.focusRequester(quickActionsFocusRequester) else Modifier
            )
            PlayerBarIconButton(
                icon = Icons.Default.Hd,
                contentDescription = stringResource(R.string.player_video_quality),
                onClick = onOpenVideoTracks
            )
            PlayerBarIconButton(
                icon = Icons.Default.AspectRatio,
                contentDescription = stringResource(R.string.player_aspect_ratio_label, aspectRatioLabel),
                onClick = onToggleAspectRatio
            )
            PlayerBarIconButton(
                icon = Icons.Default.Settings,
                contentDescription = stringResource(R.string.player_more_controls),
                onClick = { showSettingsMenu = true }
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (duration > 0) {
            Text(
                text = formatDuration(duration),
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.55f)
            )
        }
    }

    // Settings popup \u2014 gear icon reveals secondary options
    if (showSettingsMenu) {
        Popup(
            alignment = Alignment.BottomCenter,
            onDismissRequest = { showSettingsMenu = false },
            properties = PopupProperties(focusable = true)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                colors = SurfaceDefaults.colors(containerColor = Color(0xFF0C1624).copy(alpha = 0.96f)),
                modifier = Modifier
                    .padding(16.dp)
                    .widthIn(min = 240.dp, max = 380.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PlayerQuickSettingsButton(
                        text = stringResource(R.string.player_playback_speed_value, formatPlaybackSpeedLabel(playbackSpeed)),
                        onClick = { showSettingsMenu = false; onOpenPlaybackSpeed() }
                    )
                    PlayerQuickSettingsButton(
                        text = stringResource(if (isMuted) R.string.player_unmute else R.string.player_mute),
                        onClick = { showSettingsMenu = false; onToggleMute() }
                    )
                    if (showEpisodesAction) {
                        PlayerQuickSettingsButton(
                            text = stringResource(R.string.player_episodes),
                            onClick = { showSettingsMenu = false; onOpenEpisodes() }
                        )
                    }
                    if (audioVideoSyncEnabled) {
                        PlayerQuickSettingsButton(
                            text = stringResource(R.string.player_av_sync_short),
                            onClick = { showSettingsMenu = false; onOpenAudioVideoSync() }
                        )
                    }
                    PlayerQuickSettingsButton(
                        text = sleepTimerActionLabel(
                            title = stringResource(R.string.player_stop_playback_after),
                            activeLabel = stringResource(R.string.player_stop_timer_status, formatTimerRemaining(sleepTimerUiState.stopRemainingMs)),
                            active = sleepTimerUiState.stopTimerActive
                        ),
                        onClick = { showSettingsMenu = false; onOpenStopPlaybackTimer() }
                    )
                    PlayerQuickSettingsButton(
                        text = sleepTimerActionLabel(
                            title = stringResource(R.string.player_idle_standby_after),
                            activeLabel = stringResource(R.string.player_idle_timer_status, formatTimerRemaining(sleepTimerUiState.idleRemainingMs)),
                            active = sleepTimerUiState.idleTimerActive
                        ),
                        onClick = { showSettingsMenu = false; onOpenIdleStandbyTimer() }
                    )
                    PlayerQuickSettingsButton(
                        text = stringResource(if (isCastConnected) R.string.player_stop_casting else R.string.player_cast),
                        onClick = { showSettingsMenu = false; if (isCastConnected) onStopCasting() else onCast() }
                    )
                    PlayerQuickSettingsButton(
                        text = stringResource(R.string.player_picture_in_picture),
                        onClick = { showSettingsMenu = false; onEnterPictureInPicture() }
                    )
                    if (showExternalPlayerAction) {
                        PlayerQuickSettingsButton(
                            text = stringResource(R.string.player_open_in_external_player),
                            onClick = { showSettingsMenu = false; onOpenExternalPlayer() }
                        )
                    }
                    if (hasNextEpisode) {
                        PlayerQuickSettingsButton(
                            text = stringResource(R.string.player_next_episode),
                            onClick = { showSettingsMenu = false; onPlayNextEpisode() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerSeekPreviewCard(
    preview: SeekPreviewState,
    previewHeight: androidx.compose.ui.unit.Dp = 118.dp,
    modifier: Modifier = Modifier
) {
    val artworkModel = rememberCrossfadeImageModel(preview.artworkUrl)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.08f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(previewHeight)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.Black.copy(alpha = 0.38f)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    preview.frameBitmap != null -> {
                        Image(
                            bitmap = preview.frameBitmap.asImageBitmap(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    artworkModel != null -> {
                        AsyncImage(
                            model = artworkModel,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    else -> {
                        Text(
                            text = preview.title.ifBlank { formatDuration(preview.positionMs) },
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.72f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatDuration(preview.positionMs),
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = preview.title,
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.64f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }
        }
    }
}

private fun formatPlaybackSpeedLabel(speed: Float): String {
    return if (speed % 1f == 0f) {
        "${speed.toInt()}x"
    } else {
        "${("%.2f".format(Locale.US, speed)).trimEnd('0').trimEnd('.')}x"
    }
}

private fun sleepTimerActionLabel(title: String, activeLabel: String, active: Boolean): String =
    if (active) activeLabel else title

private fun formatTimerRemaining(ms: Long): String {
    val totalSeconds = (ms.coerceAtLeast(0L) + 999L) / 1000L
    if (totalSeconds < 60L) return "${totalSeconds}s"
    val totalMinutes = (totalSeconds + 59L) / 60L
    return if (totalMinutes < 60L) {
        "${totalMinutes}m"
    } else {
        val hours = totalMinutes / 60L
        val minutes = totalMinutes % 60L
        if (minutes == 0L) "${hours}h" else "${hours}h ${minutes}m"
    }
}

@Composable
private fun PlayerQuickSettingsButton(
    text: String,
    onClick: () -> Unit,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(8.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Primary.copy(alpha = 0.9f)
        ),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = Color.White,
            style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(
                horizontal = if (compact) 12.dp else 14.dp,
                vertical = if (compact) 7.dp else 9.dp
            )
        )
    }
}

@Composable
private fun PlayerBarIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(10.dp)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.10f),
            focusedContainerColor = Primary.copy(alpha = 0.90f)
        ),
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun PlayerTransportButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    buttonSize: androidx.compose.ui.unit.Dp = 56.dp,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val latestOnClick by rememberUpdatedState(onClick)
    var repeatJob by remember { mutableStateOf<Job?>(null) }

    fun stopRepeating() {
        repeatJob?.cancel()
        repeatJob = null
    }

    fun performSeekStep() {
        latestOnClick()
    }

    fun startRepeating() {
        if (repeatJob?.isActive == true) return
        performSeekStep()
        repeatJob = coroutineScope.launch {
            delay(350L)
            while (true) {
                performSeekStep()
                delay(180L)
            }
        }
    }

    TvClickableSurface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.White.copy(alpha = 0.1f),
            focusedContainerColor = Color.White.copy(alpha = 0.3f)
        ),
        modifier = modifier
            .size(buttonSize)
            .onPreviewKeyEvent { event ->
                when (event.nativeKeyEvent.keyCode) {
                    android.view.KeyEvent.KEYCODE_DPAD_CENTER,
                    android.view.KeyEvent.KEYCODE_ENTER,
                    android.view.KeyEvent.KEYCODE_NUMPAD_ENTER -> {
                        when (event.nativeKeyEvent.action) {
                            android.view.KeyEvent.ACTION_DOWN -> {
                                startRepeating()
                                true
                            }
                            android.view.KeyEvent.ACTION_UP -> {
                                stopRepeating()
                                true
                            }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size((buttonSize * 0.5f))
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose { stopRepeating() }
    }
}

@Composable
private fun PlayerMetaPill(
    text: String,
    accent: Boolean = false
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        colors = SurfaceDefaults.colors(
            containerColor = if (accent) Primary.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.10f)
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PlayerQuickActionRows(
    primaryActions: List<PlayerActionSpec>,
    secondaryActions: List<PlayerActionSpec>,
    firstActionFocusRequester: FocusRequester,
    primaryActionsUpFocusRequester: FocusRequester? = null,
    singleRow: Boolean = false,
    compactButtons: Boolean = false
) {
    val rows = listOf(primaryActions, secondaryActions).filter { it.isNotEmpty() }
    if (rows.isEmpty()) return

    Spacer(modifier = Modifier.height(if (compactButtons) 10.dp else 14.dp))

    if (singleRow) {
        val actions = rows.flatten()
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            itemsIndexed(actions) { actionIndex, action ->
                PlayerQuickSettingsButton(
                    text = action.label,
                    onClick = action.onClick,
                    compact = compactButtons,
                    modifier = Modifier
                        .then(
                            if (actionIndex == 0) {
                                Modifier.focusRequester(firstActionFocusRequester)
                            } else {
                                Modifier
                            }
                        )
                        .focusProperties {
                            if (actionIndex == 0 && primaryActionsUpFocusRequester != null) {
                                up = primaryActionsUpFocusRequester
                            }
                        }
                )
            }
        }
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEachIndexed { index, row ->
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (index == 1) {
                    PlayerMetaPill(text = stringResource(R.string.player_more_controls))
                }
                row.forEachIndexed { actionIndex, action ->
                    PlayerQuickSettingsButton(
                        text = action.label,
                        onClick = action.onClick,
                        compact = compactButtons,
                        modifier = Modifier
                            .then(
                                if (index == 0 && actionIndex == 0) {
                                    Modifier.focusRequester(firstActionFocusRequester)
                                } else {
                                    Modifier
                                }
                            )
                            .focusProperties {
                                if (index == 0 && actionIndex == 0 && primaryActionsUpFocusRequester != null) {
                                    up = primaryActionsUpFocusRequester
                                }
                            }
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveTimeshiftScrubber(
    timeshiftUiState: PlayerTimeshiftUiState,
    isPlaying: Boolean,
    playButtonFocusRequester: FocusRequester,
    quickActionsFocusRequester: FocusRequester,
    onTogglePlayPause: () -> Unit,
    onSeekBackward: () -> Unit,
    onSeekForward: () -> Unit,
    onSeekToPosition: (Long) -> Unit,
    onSeekToLiveEdge: () -> Unit,
    onSetScrubbingMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bufferDepthMs = timeshiftUiState.bufferDepthMs.coerceAtLeast(1L)
    val bufferedBehindLive = timeshiftUiState.bufferedBehindLiveMs
    val targetFraction = remember(bufferedBehindLive, bufferDepthMs) {
        1f - (bufferedBehindLive.toFloat() / bufferDepthMs.toFloat()).coerceIn(0f, 1f)
    }
    var scrubberFraction by remember { mutableStateOf(targetFraction) }
    var isScrubbing by remember { mutableStateOf(false) }
    val latestSeekCallback by rememberUpdatedState(onSeekToPosition)
    val latestScrubbingCallback by rememberUpdatedState(onSetScrubbingMode)
    val latestSeekBackward by rememberUpdatedState(onSeekBackward)
    val latestSeekForward by rememberUpdatedState(onSeekForward)
    val latestSeekToLiveEdge by rememberUpdatedState(onSeekToLiveEdge)

    LaunchedEffect(targetFraction, isScrubbing) {
        if (!isScrubbing) scrubberFraction = targetFraction
    }

    val engineState = timeshiftUiState.engineState
    val oldestWallMs = engineState.bufferStartMs
    val appTimeFormat = LocalAppTimeFormat.current
    val timeFormat = remember(appTimeFormat) { appTimeFormat.createTimeFormat() }
    val oldestLabel = if (oldestWallMs > 0L) {
        timeFormat.format(Date(oldestWallMs))
    } else if (bufferDepthMs > 1_000L) {
        "-${formatTimeshiftDuration(bufferDepthMs)}"
    } else {
        ""
    }

    val currentOffsetMs = if (isScrubbing) {
        ((1f - scrubberFraction) * bufferDepthMs.toFloat()).toLong()
    } else {
        bufferedBehindLive
    }

    var liveDotVisible by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(700L)
            liveDotVisible = !liveDotVisible
        }
    }

    val scrubberCd = stringResource(R.string.player_live_scrubber_cd)

    Surface(
        shape = RoundedCornerShape(20.dp),
        colors = SurfaceDefaults.colors(containerColor = Color.White.copy(alpha = 0.06f)),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transport controls pill
            Surface(
                shape = RoundedCornerShape(999.dp),
                colors = SurfaceDefaults.colors(containerColor = Color.Black.copy(alpha = 0.24f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PlayerTransportButton(
                        icon = Icons.Default.FastRewind,
                        contentDescription = stringResource(R.string.player_rewind),
                        onClick = onSeekBackward,
                        modifier = Modifier.focusProperties { down = quickActionsFocusRequester }
                    )
                    TvClickableSurface(
                        onClick = onTogglePlayPause,
                        shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(50)),
                        colors = ClickableSurfaceDefaults.colors(
                            containerColor = Primary.copy(alpha = 0.84f),
                            focusedContainerColor = Primary
                        ),
                        modifier = Modifier
                            .size(62.dp)
                            .focusRequester(playButtonFocusRequester)
                            .focusProperties { down = quickActionsFocusRequester }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = stringResource(R.string.player_play),
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                    PlayerTransportButton(
                        icon = Icons.Default.FastForward,
                        contentDescription = stringResource(R.string.player_forward),
                        onClick = onSeekForward,
                        modifier = Modifier.focusProperties { down = quickActionsFocusRequester }
                    )
                }
            }

            // Scrubber section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Labels: oldest time | current offset | LIVE badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = oldestLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.55f)
                    )
                    Text(
                        text = if (currentOffsetMs < 2_000L) {
                            stringResource(R.string.player_live_ready)
                        } else {
                            stringResource(R.string.player_live_offset, formatTimeshiftDuration(currentOffsetMs))
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (currentOffsetMs < 2_000L) FontWeight.Bold else FontWeight.Normal,
                        color = if (currentOffsetMs < 2_000L) Primary else Color.White
                    )
                    // LIVE edge badge with pulsing dot
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(
                                    color = Color.Red.copy(alpha = if (liveDotVisible) 0.95f else 0.30f),
                                    shape = RoundedCornerShape(999.dp)
                                )
                        )
                        Text(
                            text = stringResource(R.string.player_jump_to_live_short),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                // Timeline slider
                Slider(
                    value = scrubberFraction,
                    onValueChange = { newValue ->
                        val clamped = newValue.coerceIn(0f, 1f)
                        if (!isScrubbing) {
                            isScrubbing = true
                            latestScrubbingCallback(true)
                        }
                        scrubberFraction = clamped
                    },
                    onValueChangeFinished = {
                        val finalFraction = scrubberFraction.coerceIn(0f, 1f)
                        if (finalFraction >= 0.995f) {
                            latestSeekToLiveEdge()
                        } else {
                            latestSeekCallback((finalFraction * bufferDepthMs.toFloat()).toLong())
                        }
                        if (isScrubbing) {
                            latestScrubbingCallback(false)
                            isScrubbing = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusProperties {
                            down = quickActionsFocusRequester
                            up = playButtonFocusRequester
                        }
                        .onPreviewKeyEvent { event ->
                            if (event.nativeKeyEvent.action != android.view.KeyEvent.ACTION_DOWN) {
                                return@onPreviewKeyEvent false
                            }
                            when (event.nativeKeyEvent.keyCode) {
                                android.view.KeyEvent.KEYCODE_DPAD_LEFT -> { latestSeekBackward(); true }
                                android.view.KeyEvent.KEYCODE_DPAD_RIGHT -> {
                                    if (timeshiftUiState.canSeekToLive) latestSeekForward() else latestSeekToLiveEdge()
                                    true
                                }
                                else -> false
                            }
                        }
                        .semantics { contentDescription = scrubberCd },
                    enabled = bufferDepthMs > 2_000L,
                    colors = SliderDefaults.colors(
                        activeTrackColor = Primary,
                        inactiveTrackColor = Color.White.copy(alpha = 0.22f),
                        thumbColor = Primary
                    )
                )
            }
        }
    }
}

private fun formatTimeshiftDuration(ms: Long): String {
    val totalSeconds = ms / 1_000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    val hours = minutes / 60L
    val remainingMinutes = minutes % 60L
    return if (hours > 0L) {
        String.format(Locale.getDefault(), "%d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    val remainingMinutes = minutes % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, remainingMinutes, seconds)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", remainingMinutes, seconds)
    }
}
