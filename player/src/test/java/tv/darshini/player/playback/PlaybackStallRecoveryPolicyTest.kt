package tv.darshini.player.playback

import com.google.common.truth.Truth.assertThat
import tv.darshini.player.PlaybackState
import org.junit.Test

class PlaybackStallRecoveryPolicyTest {
    @Test
    fun `ready stalls are recovered for live transport streams`() {
        assertThat(shouldRecoverReadyStalls(ResolvedStreamType.MPEG_TS_LIVE)).isTrue()
    }

    @Test
    fun `position advancing ready stalls are not recovered for live streams`() {
        assertThat(shouldRecoverPositionAdvancingReadyStalls(ResolvedStreamType.MPEG_TS_LIVE)).isFalse()
        assertThat(shouldRecoverPositionAdvancingReadyStalls(ResolvedStreamType.HLS)).isFalse()
        assertThat(shouldRecoverPositionAdvancingReadyStalls(ResolvedStreamType.PROGRESSIVE)).isTrue()
    }

    @Test
    fun `frame silent ready stalls are recovered for every stream type`() {
        // Progressive VOD was added: a hardware decoder can freeze (video frames stop while the
        // audio clock keeps advancing) and only escalating recovery unfreezes it.
        assertThat(shouldRecoverFrameSilentReadyStalls(ResolvedStreamType.MPEG_TS_LIVE)).isTrue()
        assertThat(shouldRecoverFrameSilentReadyStalls(ResolvedStreamType.HLS)).isTrue()
        assertThat(shouldRecoverFrameSilentReadyStalls(ResolvedStreamType.PROGRESSIVE)).isTrue()
    }

    @Test
    fun `live ready stalls reconnect the current stream`() {
        assertThat(
            shouldReconnectLiveStall(
                playbackState = PlaybackState.READY,
                resolvedStreamType = ResolvedStreamType.MPEG_TS_LIVE,
                recoveryAttempt = 1
            )
        ).isTrue()
    }

    @Test
    fun `live ready stalls stop reconnecting after first recovery attempt`() {
        assertThat(
            shouldReconnectLiveStall(
                playbackState = PlaybackState.READY,
                resolvedStreamType = ResolvedStreamType.MPEG_TS_LIVE,
                recoveryAttempt = 2
            )
        ).isFalse()
    }

    @Test
    fun `vod ready stalls do not reconnect as live streams`() {
        assertThat(
            shouldReconnectLiveStall(
                playbackState = PlaybackState.READY,
                resolvedStreamType = ResolvedStreamType.PROGRESSIVE,
                recoveryAttempt = 1
            )
        ).isFalse()
    }
}
