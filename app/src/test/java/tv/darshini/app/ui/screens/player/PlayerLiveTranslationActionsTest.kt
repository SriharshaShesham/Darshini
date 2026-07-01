package tv.darshini.app.ui.screens.player

import com.google.common.truth.Truth.assertThat
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.model.ProviderType
import org.junit.Test

class PlayerLiveTranslationActionsTest {

    @Test
    fun shouldEnableLiveTranslationSession_requiresEnabledSupportedProviderAndLiveContent() {
        assertThat(
            shouldEnableLiveTranslationSession(
                enabledPreference = true,
                contentType = ContentType.LIVE,
                providerType = ProviderType.XTREAM_CODES
            )
        ).isTrue()

        assertThat(
            shouldEnableLiveTranslationSession(
                enabledPreference = false,
                contentType = ContentType.LIVE,
                providerType = ProviderType.XTREAM_CODES
            )
        ).isFalse()

        assertThat(
            shouldEnableLiveTranslationSession(
                enabledPreference = true,
                contentType = ContentType.MOVIE,
                providerType = ProviderType.XTREAM_CODES
            )
        ).isFalse()

        assertThat(
            shouldEnableLiveTranslationSession(
                enabledPreference = true,
                contentType = ContentType.LIVE,
                providerType = ProviderType.STALKER_PORTAL
            )
        ).isFalse()

        assertThat(
            shouldEnableLiveTranslationSession(
                enabledPreference = true,
                contentType = ContentType.LIVE,
                providerType = ProviderType.M3U
            )
        ).isTrue()
    }
}
