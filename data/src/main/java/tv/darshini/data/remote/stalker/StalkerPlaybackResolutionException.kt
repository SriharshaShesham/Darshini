package tv.darshini.data.remote.stalker

import tv.darshini.domain.model.StalkerBootstrapRecipe
import tv.darshini.domain.model.StalkerCookieMode
import tv.darshini.domain.model.StalkerEndpointPreference
import tv.darshini.domain.model.StalkerMagPreset
import tv.darshini.domain.model.StalkerPlaybackBackendHint
import tv.darshini.domain.model.StalkerPortalFingerprint
import java.io.IOException

class StalkerPlaybackResolutionException(
    message: String,
    cause: Throwable? = null,
    val streamKind: StalkerStreamKind = StalkerStreamKind.LIVE,
    val portalFingerprint: StalkerPortalFingerprint? = null,
    val magPreset: StalkerMagPreset? = null,
    val bootstrapRecipe: StalkerBootstrapRecipe? = null,
    val endpointPreference: StalkerEndpointPreference = StalkerEndpointPreference.AUTO,
    val cookieMode: StalkerCookieMode = StalkerCookieMode.NONE,
    val playbackBackendHint: StalkerPlaybackBackendHint = StalkerPlaybackBackendHint.AUTO,
    val fallbackRecipeUsed: Boolean = false,
    val rediscoveryAttempted: Boolean = false
) : IOException(message, cause)
