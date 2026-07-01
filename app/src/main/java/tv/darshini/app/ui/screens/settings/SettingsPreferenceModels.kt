package tv.darshini.app.ui.screens.settings

import android.app.Application
import tv.darshini.app.R
import tv.darshini.app.ui.model.LiveTvChannelMode
import tv.darshini.app.ui.model.LiveTvQuickFilterVisibilityMode
import tv.darshini.app.ui.model.VodViewMode
import tv.darshini.domain.model.AppTimeFormat
import tv.darshini.domain.model.AppHomeDashboardShelf
import tv.darshini.domain.model.AppLandingDestination
import tv.darshini.domain.model.AppTopLevelDestination
import tv.darshini.domain.model.AudioOutputPreference
import tv.darshini.domain.model.Category
import tv.darshini.domain.model.SyncCadence
import tv.darshini.domain.model.ExternalPlaybackMode
import tv.darshini.domain.model.ChannelNumberingMode
import tv.darshini.domain.model.DecoderMode
import tv.darshini.domain.model.GroupedChannelLabelMode
import tv.darshini.domain.model.LiveChannelGroupingMode
import tv.darshini.domain.model.LiveVariantPreferenceMode
import tv.darshini.domain.model.PlaybackBufferMode
import tv.darshini.domain.model.VodDuplicateHandlingMode
import tv.darshini.domain.model.VodHttpProtocolMode
import tv.darshini.domain.model.VodVariantPreferenceMode
import tv.darshini.domain.model.PlayerSurfaceMode
import tv.darshini.domain.model.Provider
import tv.darshini.domain.model.RemoteShortcutPreferences

enum class ProviderWarningAction {
    EPG,
    MOVIES,
    SERIES
}

enum class ProviderSyncSelection {
    SYNC_NOW,
    REBUILD_INDEX,
    TV,
    MOVIES,
    SERIES,
    EPG
}

internal data class SettingsPreferenceSnapshot(
    val providers: List<Provider>,
    val activeProviderId: Long?,
    val parentalControlLevel: Int,
    val hasParentalPin: Boolean,
    val appLanguage: String,
    val appTheme: String,
    val appLandingDestination: AppLandingDestination,
    val appTopLevelDestinations: List<AppTopLevelDestination>,
    val appHomeDashboardShelves: List<AppHomeDashboardShelf>,
    val appTimeFormat: AppTimeFormat,
    val preferredAudioLanguage: String,
    val playerMediaSessionEnabled: Boolean,
    val playerFastRetryOnTransientFailures: Boolean,
    val playerDecoderMode: DecoderMode,
    val playerPlaybackBufferMode: PlaybackBufferMode,
    val playerAudioOutputPreference: AudioOutputPreference,
    val playerCompatibilityMemoryEnabled: Boolean,
    val playerSurfaceMode: PlayerSurfaceMode,
    val playerVodHttpProtocolMode: VodHttpProtocolMode,
    val playerPlaybackSpeed: Float,
    val playerExternalPlaybackMode: ExternalPlaybackMode,
    val playerAudioVideoSyncEnabled: Boolean,
    val playerAudioVideoOffsetMs: Int,
    val centerTwoSlotMultiviewLayout: Boolean,
    val multiViewRespectProviderConnectionLimit: Boolean,
    val playerControlsTimeoutSeconds: Int,
    val playerLiveOverlayTimeoutSeconds: Int,
    val playerNoticeTimeoutSeconds: Int,
    val playerDiagnosticsTimeoutSeconds: Int,
    val subtitleTextScale: Float,
    val subtitleTextColor: Int,
    val subtitleBackgroundColor: Int,
    val playerLiveTranslationEnabled: Boolean,
    val playerLiveTranslationEndpoint: String,
    val wifiMaxVideoHeight: Int?,
    val ethernetMaxVideoHeight: Int?,
    val playerTimeshiftEnabled: Boolean,
    val playerTimeshiftDepthMinutes: Int,
    val defaultStopPlaybackTimerMinutes: Int,
    val defaultIdleStandbyTimerMinutes: Int,
    val lastSpeedTestMegabits: Double?,
    val lastSpeedTestTimestamp: Long?,
    val lastSpeedTestTransport: String?,
    val lastSpeedTestRecommendedHeight: Int?,
    val lastSpeedTestEstimated: Boolean,
    val isIncognitoMode: Boolean,
    val useXtreamTextClassification: Boolean,
    val xtreamBase64TextCompatibility: Boolean,
    val liveTvChannelMode: LiveTvChannelMode,
    val showLiveSourceSwitcher: Boolean,
    val useSideNavigation: Boolean,
    val showAllChannelsCategory: Boolean,
    val showRecentChannelsCategory: Boolean,
    val remoteShortcutPreferences: RemoteShortcutPreferences,
    val liveTvCategoryFilters: List<String>,
    val liveTvQuickFilterVisibilityMode: LiveTvQuickFilterVisibilityMode,
    val hideDecorativeLiveRows: Boolean,
    val liveChannelNumberingMode: ChannelNumberingMode,
    val liveChannelGroupingMode: LiveChannelGroupingMode,
    val groupedChannelLabelMode: GroupedChannelLabelMode,
    val liveVariantPreferenceMode: LiveVariantPreferenceMode,
    val vodViewMode: VodViewMode,
    val vodInfiniteScroll: Boolean,
    val vodDuplicateHandlingMode: VodDuplicateHandlingMode,
    val vodVariantPreferenceMode: VodVariantPreferenceMode,
    val guideDefaultCategoryId: Long,
    val guideDefaultCategoryOptions: List<Category>,
    val preventStandbyDuringPlayback: Boolean,
    val zapAutoRevert: Boolean,
    val autoPlayNextEpisode: Boolean,
    val autoCheckAppUpdates: Boolean,
    val autoDownloadAppUpdates: Boolean,
    val providerSyncCadence: SyncCadence,
    val lastAppUpdateCheckAt: Long?,
    val cachedAppUpdateVersionName: String?,
    val cachedAppUpdateVersionCode: Int?,
    val cachedAppUpdateReleaseUrl: String?,
    val cachedAppUpdateDownloadUrl: String?,
    val cachedAppUpdateReleaseNotes: String,
    val cachedAppUpdatePublishedAt: String?,
    val categoryLanguagePriority: List<String>
)

internal fun ProviderSyncSelection.label(application: Application): String = when (this) {
    ProviderSyncSelection.SYNC_NOW -> application.getString(R.string.settings_sync_option_sync_now)
    ProviderSyncSelection.REBUILD_INDEX -> application.getString(R.string.settings_sync_option_rebuild_index)
    ProviderSyncSelection.TV -> application.getString(R.string.settings_sync_option_tv)
    ProviderSyncSelection.MOVIES -> application.getString(R.string.settings_sync_option_movies)
    ProviderSyncSelection.SERIES -> application.getString(R.string.settings_sync_option_series)
    ProviderSyncSelection.EPG -> application.getString(R.string.settings_sync_option_epg)
}
