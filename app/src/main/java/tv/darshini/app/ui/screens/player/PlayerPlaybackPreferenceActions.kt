package tv.darshini.app.ui.screens.player

import androidx.lifecycle.viewModelScope
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.model.DecoderMode
import tv.darshini.domain.model.LiveChannelObservedQuality
import tv.darshini.domain.model.VodVariantObservation
import tv.darshini.domain.model.VideoFormat
import tv.darshini.player.AUDIO_VIDEO_OFFSET_MAX_MS
import tv.darshini.player.AUDIO_VIDEO_OFFSET_MIN_MS
import tv.darshini.player.PlaybackState
import tv.darshini.player.PlayerError
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

fun PlayerViewModel.selectAudioTrack(trackId: String) {
    playerEngine.selectAudioTrack(trackId)
}

fun PlayerViewModel.selectSubtitleTrack(trackId: String?) {
    playerEngine.selectSubtitleTrack(trackId)
}

fun PlayerViewModel.selectVideoQuality(trackId: String) {
    playerEngine.selectVideoTrack(trackId)
}

fun PlayerViewModel.selectLiveVariant(rawChannelId: Long) {
    val currentChannel = currentChannelFlow.value?.sanitizedForPlayer() ?: return
    val updatedChannel = currentChannel.withSelectedVariant(rawChannelId)?.sanitizedForPlayer() ?: return
    if (updatedChannel.selectedVariantId == currentChannel.selectedVariantId) return

    val requestVersion = beginPlaybackSession()
    triedAlternativeStreams.clear()
    currentContentId = updatedChannel.id
    currentStreamUrl = updatedChannel.streamUrl
    currentTitle = updatedChannel.currentVariant?.originalName ?: updatedChannel.name
    playbackTitleFlow.value = currentTitle
    currentChannelFlow.value = updatedChannel
    if (currentChannelIndex in channelList.indices) {
        channelList = channelList.mapIndexed { index, existing ->
            if (index == currentChannelIndex || existing.logicalGroupId == updatedChannel.logicalGroupId) {
                updatedChannel
            } else {
                existing
            }
        }
        currentChannelFlowList.value = channelList
    }
    if (currentChannelIndex >= 0) {
        displayChannelNumberFlow.value = resolveChannelNumber(updatedChannel, currentChannelIndex)
    }
    refreshCurrentChannelRecording()
    updateChannelDiagnostics(updatedChannel)
    updateStreamClass("Variant")
    viewModelScope.launch {
        preferencesRepository.setPreferredLiveVariant(
            providerId = updatedChannel.providerId,
            logicalGroupId = updatedChannel.logicalGroupId,
            rawChannelId = rawChannelId
        )
        val streamInfo = resolvePlaybackStreamInfo(
            logicalUrl = updatedChannel.streamUrl,
            internalContentId = updatedChannel.id,
            providerId = updatedChannel.providerId,
            contentType = ContentType.LIVE
        ) ?: return@launch
        if (!isActivePlaybackSession(requestVersion, updatedChannel.streamUrl)) return@launch
        if (currentContentType == ContentType.LIVE) {
            requestEpg(
                providerId = updatedChannel.providerId,
                epgChannelId = updatedChannel.epgChannelId,
                streamId = updatedChannel.streamId,
                internalChannelId = updatedChannel.id
            )
        }
        if (!preparePlayer(streamInfo.copy(title = streamInfo.title ?: currentTitle), requestVersion)) return@launch
        playerEngine.play()
    }
}

/**
 * Switches to a different stream format (e.g. HLS vs MPEG-TS) for the current live channel.
 * The [formatUrl] is one of the [tv.darshini.domain.model.ChannelQualityOption.url] values
 * from the channel's [tv.darshini.domain.model.Channel.qualityOptions].
 */
fun PlayerViewModel.selectStreamFormat(formatUrl: String) {
    val channel = currentChannelFlow.value?.sanitizedForPlayer() ?: return
    if (formatUrl == currentStreamUrl) return

    val requestVersion = beginPlaybackSession()
    triedAlternativeStreams.add(formatUrl)
    currentStreamUrl = formatUrl
    updateStreamClass("Format")
    viewModelScope.launch {
        val streamInfo = resolvePlaybackStreamInfo(
            logicalUrl = formatUrl,
            internalContentId = channel.id,
            providerId = channel.providerId,
            contentType = ContentType.LIVE
        ) ?: return@launch
        if (!isActivePlaybackSession(requestVersion, formatUrl)) return@launch
        if (!preparePlayer(streamInfo.copy(title = streamInfo.title ?: currentTitle), requestVersion)) return@launch
        playerEngine.play()
    }
}

fun PlayerViewModel.recordLiveVariantObservation(playbackState: PlaybackState, videoFormat: VideoFormat) {
    if (currentContentType != ContentType.LIVE || playbackState != PlaybackState.READY || videoFormat.isEmpty) {
        return
    }
    val channel = currentChannelFlow.value?.sanitizedForPlayer() ?: return
    val rawChannelId = channel.selectedVariantId.takeIf { it > 0 } ?: channel.id
    if (rawChannelId <= 0L) return
    val signature = buildString {
        append(rawChannelId)
        append('|')
        append(videoFormat.width)
        append('|')
        append(videoFormat.height)
        append('|')
        append(videoFormat.bitrate)
        append('|')
        append(videoFormat.frameRate)
    }
    if (signature == lastRecordedVariantObservationSignature) return
    lastRecordedVariantObservationSignature = signature

    viewModelScope.launch {
        val existing = preferencesRepository.liveVariantObservations.first()[rawChannelId]
        preferencesRepository.recordLiveVariantObservation(
            rawChannelId = rawChannelId,
            observedQuality = LiveChannelObservedQuality(
                lastObservedWidth = videoFormat.width,
                lastObservedHeight = videoFormat.height,
                lastObservedBitrate = videoFormat.bitrate,
                lastObservedFrameRate = videoFormat.frameRate,
                successCount = (existing?.successCount ?: 0) + 1,
                lastSuccessfulAt = System.currentTimeMillis()
            )
        )
    }
}

fun PlayerViewModel.recordMovieVariantSuccessObservation() {
    if (currentContentType != ContentType.MOVIE) return
    val rawMovieId = currentContentId.takeIf { it > 0L } ?: return
    val signature = "success|$prepareRequestVersion|$rawMovieId"
    if (signature == lastRecordedVodVariantObservationSignature) return
    lastRecordedVodVariantObservationSignature = signature

    viewModelScope.launch {
        val existing = preferencesRepository.vodVariantObservations.first()[rawMovieId]
        preferencesRepository.recordVodVariantObservation(
            rawItemId = rawMovieId,
            observation = VodVariantObservation(
                successCount = (existing?.successCount ?: 0) + 1,
                failureCount = existing?.failureCount ?: 0,
                lastSuccessfulAt = System.currentTimeMillis(),
                lastFailedAt = existing?.lastFailedAt ?: 0L
            )
        )
    }
}

fun PlayerViewModel.recordMovieVariantFailureObservation(error: PlayerError) {
    if (currentContentType != ContentType.MOVIE) return
    val rawMovieId = currentContentId.takeIf { it > 0L } ?: return
    val signature = buildString {
        append("failure|")
        append(prepareRequestVersion)
        append('|')
        append(rawMovieId)
        append('|')
        append(error::class.java.simpleName)
        append('|')
        append(error.message)
    }
    if (signature == lastRecordedVodVariantObservationSignature) return
    lastRecordedVodVariantObservationSignature = signature

    viewModelScope.launch {
        val existing = preferencesRepository.vodVariantObservations.first()[rawMovieId]
        preferencesRepository.recordVodVariantObservation(
            rawItemId = rawMovieId,
            observation = VodVariantObservation(
                successCount = existing?.successCount ?: 0,
                failureCount = (existing?.failureCount ?: 0) + 1,
                lastSuccessfulAt = existing?.lastSuccessfulAt ?: 0L,
                lastFailedAt = System.currentTimeMillis()
            )
        )
    }
}

fun PlayerViewModel.cycleDecoderMode() {
    val next = when (playerDiagnostics.value.decoderMode) {
        DecoderMode.AUTO -> DecoderMode.HARDWARE
        DecoderMode.HARDWARE -> DecoderMode.SOFTWARE
        DecoderMode.SOFTWARE -> DecoderMode.COMPATIBILITY
        DecoderMode.COMPATIBILITY -> DecoderMode.AUTO
    }
    // Manual choice overrides any automatic software retry made this session.
    hasRetriedWithSoftwareDecoder = false
    playerEngine.setDecoderMode(next)
    updateDecoderMode(next)
    viewModelScope.launch {
        preferencesRepository.setPlayerDecoderMode(next)
    }
}

fun PlayerViewModel.setPlaybackSpeed(speed: Float) {
    val normalizedSpeed = speed.coerceIn(0.5f, 2f)
    playerEngine.setPlaybackSpeed(normalizedSpeed)
    viewModelScope.launch {
        preferencesRepository.setPlayerPlaybackSpeed(normalizedSpeed)
    }
}

fun PlayerViewModel.previewAudioVideoOffset(offsetMs: Int) {
    audioVideoOffsetPreviewMs.value = offsetMs.coerceIn(AUDIO_VIDEO_OFFSET_MIN_MS, AUDIO_VIDEO_OFFSET_MAX_MS)
}

fun PlayerViewModel.adjustAudioVideoOffset(deltaMs: Int) {
    val current = audioVideoOffsetPreviewMs.value ?: _audioVideoOffsetUiState.value.effectiveOffsetMs
    previewAudioVideoOffset(current + deltaMs)
}

fun PlayerViewModel.resetAudioVideoOffsetPreview() {
    previewAudioVideoOffset(0)
}

fun PlayerViewModel.dismissAudioVideoOffsetPreview() {
    audioVideoOffsetPreviewMs.value = null
}

fun PlayerViewModel.saveAudioVideoOffsetForChannel() {
    val channelId = currentChannelFlow.value?.id?.takeIf { it > 0L } ?: return
    val offsetMs = _audioVideoOffsetUiState.value.effectiveOffsetMs
    viewModelScope.launch {
        preferencesRepository.setAudioVideoOffsetForChannel(channelId, offsetMs)
        audioVideoOffsetPreviewMs.value = null
    }
}

fun PlayerViewModel.saveAudioVideoOffsetAsGlobal() {
    val offsetMs = _audioVideoOffsetUiState.value.effectiveOffsetMs
    val channelId = currentChannelFlow.value?.id?.takeIf { it > 0L }
    viewModelScope.launch {
        preferencesRepository.setPlayerAudioVideoOffsetMs(offsetMs)
        if (channelId != null) {
            preferencesRepository.clearAudioVideoOffsetForChannel(channelId)
        }
        audioVideoOffsetPreviewMs.value = null
    }
}

fun PlayerViewModel.useGlobalAudioVideoOffset() {
    val channelId = currentChannelFlow.value?.id?.takeIf { it > 0L } ?: return
    viewModelScope.launch {
        preferencesRepository.clearAudioVideoOffsetForChannel(channelId)
        audioVideoOffsetPreviewMs.value = null
    }
}

fun PlayerViewModel.seekTo(positionMs: Long) {
    notifyUserActivity()
    playerEngine.seekTo(positionMs)
    clearSeekPreview()
}

fun PlayerViewModel.setScrubbingMode(enabled: Boolean) {
    playerEngine.setScrubbingMode(enabled)
    if (!enabled) {
        clearSeekPreview()
    }
}

fun PlayerViewModel.updateSeekPreview(positionMs: Long?) {
    if (positionMs == null || currentContentType == ContentType.LIVE) {
        clearSeekPreview()
        return
    }

    val previewPositionMs = positionMs.coerceAtLeast(0L)
    val previewUrl = currentResolvedPlaybackUrl.ifBlank { currentStreamUrl }
    val canExtractFrame = previewUrl.isNotBlank() && seekThumbnailProvider.supportsFrameExtraction(previewUrl)

    _seekPreview.update { current ->
        current.copy(
            visible = true,
            positionMs = previewPositionMs,
            artworkUrl = currentArtworkUrl,
            title = currentTitle,
            isLoading = canExtractFrame,
            frameBitmap = if (canExtractFrame) current.frameBitmap else null
        )
    }

    seekPreviewJob?.cancel()
    if (!canExtractFrame) {
        return
    }

    val requestVersion = ++seekPreviewRequestVersion
    seekPreviewJob = viewModelScope.launch {
        delay(120)
        val bitmap = seekThumbnailProvider.loadFrame(previewUrl, previewPositionMs)
        if (requestVersion != seekPreviewRequestVersion) return@launch

        _seekPreview.update { current ->
            if (!current.visible || current.positionMs != previewPositionMs) {
                current
            } else {
                current.copy(
                    frameBitmap = bitmap,
                    artworkUrl = currentArtworkUrl,
                    title = currentTitle,
                    isLoading = false
                )
            }
        }
    }
}

// Background thumbnail preloading was removed: MediaMetadataRetriever opens its OWN HTTP
// connection to the stream, and IPTV providers cap concurrent connections per account (often 1).
// The preload's connection made the provider drop ExoPlayer's, killing playback with
// "ProtocolException: unexpected end of stream". Seek previews are now fetched only while the
// user is actually scrubbing — see updateSeekPreview().

internal fun PlayerViewModel.clearSeekPreview() {
    seekPreviewJob?.cancel()
    seekPreviewRequestVersion++
    _seekPreview.value = SeekPreviewState()
}