package tv.darshini.player.playback

import androidx.media3.extractor.ts.TsExtractor
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PlayerMediaSourceFactoryTest {

    @Test
    fun `live mpeg ts extractor stays off hls mode`() {
        // Live channels are a continuous raw MPEG-TS feed driven by a ProgressiveMediaSource.
        // MODE_HLS needs an HLS-supplied timestamp adjuster and throws IllegalStateException
        // without one, which took out every live channel. The exact fallback mode is Media3's
        // default and not the point - staying off MODE_HLS is.
        val factory = liveMpegTsExtractorsFactory()
        val modeField = factory::class.java.getDeclaredField("tsMode").apply {
            isAccessible = true
        }

        assertThat(modeField.getInt(factory)).isNotEqualTo(TsExtractor.MODE_HLS)
    }
}
