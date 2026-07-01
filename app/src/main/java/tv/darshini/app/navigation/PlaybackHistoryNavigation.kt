package tv.darshini.app.navigation

import tv.darshini.domain.model.PlaybackHistory

internal fun PlaybackHistory.toPlayerNavigationRequest(): PlayerNavigationRequest =
    PlayerNavigationRequest(
        streamUrl = streamUrl,
        title = title,
        internalId = contentId,
        providerId = providerId,
        contentType = contentType.name,
        artworkUrl = posterUrl,
        seriesId = seriesId,
        seasonNumber = seasonNumber,
        episodeNumber = episodeNumber
    )