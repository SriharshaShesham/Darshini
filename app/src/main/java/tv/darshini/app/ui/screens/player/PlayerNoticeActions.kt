package tv.darshini.app.ui.screens.player

import android.util.Log
import androidx.lifecycle.viewModelScope
import tv.darshini.app.ui.model.isArchivePlayable
import tv.darshini.domain.model.ContentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun PlayerViewModel.dismissPlayerNotice() {
    playerNoticeHideJob?.cancel()
    _playerNotice.value = null
}

internal fun PlayerViewModel.dismissRecoveredNoticeIfPresent() {
    val notice = _playerNotice.value ?: return
    if (notice.isRetryNotice || notice.recoveryType != PlayerRecoveryType.UNKNOWN) {
        dismissPlayerNotice()
    }
}

fun PlayerViewModel.runPlayerNoticeAction(action: PlayerNoticeAction) {
    when (action) {
        PlayerNoticeAction.RETRY -> {
            appendRecoveryAction("Manual retry")
            retryStream(currentStreamUrl, currentChannelFlow.value?.epgChannelId)
        }
        PlayerNoticeAction.LAST_CHANNEL -> {
            appendRecoveryAction("Returned to last channel")
            zapToLastChannel()
        }
        PlayerNoticeAction.ALTERNATE_STREAM -> {
            appendRecoveryAction("Manual alternate stream")
            tryAlternateStream()
        }
        PlayerNoticeAction.OPEN_GUIDE -> {
            appendRecoveryAction("Opened guide from recovery")
            openEpgOverlay()
        }
    }
    dismissPlayerNotice()
}

internal fun PlayerViewModel.showPlayerNotice(
    message: String,
    recoveryType: PlayerRecoveryType = PlayerRecoveryType.UNKNOWN,
    actions: List<PlayerNoticeAction> = emptyList(),
    durationMs: Long = playerNoticeTimeoutMs,
    isRetryNotice: Boolean = false
) {
    // Error/recovery notices are logged rather than shown. On a TV they cover the picture during
    // the exact moment playback is already struggling, and the viewer can do nothing useful with
    // them — recovery is automatic either way. Deliberate informational notices (sleep timer,
    // catch-up availability, blocked notifications) carry no recovery type and still surface.
    if (isRetryNotice || recoveryType != PlayerRecoveryType.UNKNOWN || actions.isNotEmpty()) {
        Log.w(
            "PlayerNotice",
            "suppressed type=$recoveryType retry=$isRetryNotice actions=${actions.joinToString(",")} message=$message"
        )
        return
    }
    playerNoticeHideJob?.cancel()
    _playerNotice.value = PlayerNoticeState(
        message = message,
        recoveryType = recoveryType,
        actions = actions.distinct(),
        isRetryNotice = isRetryNotice
    )
    playerNoticeHideJob = viewModelScope.launch {
        delay(durationMs)
        if (_playerNotice.value?.message == message) {
            _playerNotice.value = null
        }
    }
}

internal fun PlayerViewModel.showRetryNotice(status: tv.darshini.player.PlayerRetryStatus) {
    val formatLabel = resolvePlaybackFormatLabel(
        currentResolvedPlaybackUrl = currentResolvedPlaybackUrl,
        currentStreamUrl = currentStreamUrl
    )
    val message = buildRetryNoticeMessage(formatLabel, status)
    showPlayerNotice(
        message = message,
        recoveryType = PlayerRecoveryType.NETWORK,
        durationMs = maxOf(playerNoticeTimeoutMs, status.delayMs + 1500L),
        isRetryNotice = true
    )
}

internal fun buildRetryNoticeMessage(
    formatLabel: String,
    status: tv.darshini.player.PlayerRetryStatus
): String {
    val retryLabel = "Retrying $formatLabel ${status.attempt}/${status.maxAttempts}"
    val delaySeconds = status.delayMs / 1_000L
    return if (delaySeconds >= 1L) {
        "$retryLabel in ${delaySeconds}s..."
    } else {
        "$retryLabel..."
    }
}

fun PlayerViewModel.restartCurrentProgram() {
    val program = currentProgram.value ?: return
    val channel = currentChannelFlow.value ?: return
    if (channel.isArchivePlayable(program)) {
        playCatchUp(program)
    }
}

fun PlayerViewModel.retryStream(streamUrl: String, epgChannelId: String?) {
    if (isCatchUpPlayback.value) {
        val requestVersion = beginPlaybackSession()
        viewModelScope.launch {
            val streamInfo = resolvePlaybackStreamInfo(streamUrl, currentContentId, currentProviderId, currentContentType)
                ?: return@launch
            if (!isActivePlaybackSession(requestVersion, streamUrl)) return@launch
            if (!preparePlayer(streamInfo.copy(title = streamInfo.title ?: currentTitle), requestVersion)) return@launch
            playerEngine.play()
        }
        return
    }
    val currentId = if (currentChannelIndex != -1 && channelList.isNotEmpty()) channelList[currentChannelIndex].id else -1L
    prepare(
        streamUrl = streamUrl,
        epgChannelId = epgChannelId,
        internalChannelId = currentId,
        categoryId = currentCategoryId,
        providerId = currentProviderId,
        isVirtual = isVirtualCategory,
        combinedProfileId = currentCombinedProfileId,
        combinedSourceFilterProviderId = currentCombinedSourceFilterProviderId,
        contentType = currentContentType.name,
        title = currentTitle
    )
}
