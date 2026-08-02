package tv.darshini.player.playback

import tv.darshini.player.PlaybackState

internal fun shouldRecoverReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    true

internal fun shouldRecoverPositionAdvancingReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    !resolvedStreamType.isLiveForStallRecovery

internal fun shouldRecoverFrameSilentReadyStalls(resolvedStreamType: ResolvedStreamType): Boolean =
    // Live was already covered; progressive VOD needs it too — a hardware decoder can freeze
    // (video frames stop while the audio clock keeps advancing) and only escalating recovery
    // (reprepare → software-decoder fallback) unfreezes it.
    true

internal fun shouldReconnectLiveStall(
    playbackState: PlaybackState,
    resolvedStreamType: ResolvedStreamType,
    recoveryAttempt: Int
): Boolean =
    recoveryAttempt == 1 &&
        (
            playbackState == PlaybackState.BUFFERING && resolvedStreamType.isLiveForStallRecovery ||
                playbackState == PlaybackState.READY && resolvedStreamType.isLiveForStallRecovery
        )

private val ResolvedStreamType.isLiveForStallRecovery: Boolean
    get() = this == ResolvedStreamType.HLS ||
        this == ResolvedStreamType.SMOOTH_STREAMING ||
        this == ResolvedStreamType.MPEG_TS_LIVE ||
        this == ResolvedStreamType.RTSP
