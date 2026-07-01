package tv.darshini.app.ui.screens.settings

import tv.darshini.app.ui.model.LiveTvChannelMode
import tv.darshini.app.ui.model.LiveTvQuickFilterVisibilityMode
import tv.darshini.app.ui.model.VodViewMode
import tv.darshini.domain.manager.BackupImportPlan
import tv.darshini.domain.manager.BackupPreview
import tv.darshini.domain.manager.DriveAuthState
import tv.darshini.domain.manager.DriveSignInRequest
import tv.darshini.domain.manager.DriveSyncStatus
import tv.darshini.domain.manager.ProviderCredentials
import tv.darshini.domain.model.ActiveLiveSource
import tv.darshini.domain.model.AppHomeDashboardShelf
import tv.darshini.domain.model.AppLandingDestination
import tv.darshini.domain.model.AppTopLevelDestination
import tv.darshini.domain.model.AppTimeFormat
import tv.darshini.domain.model.AudioOutputPreference
import tv.darshini.domain.model.Category
import tv.darshini.domain.model.CategorySortMode
import tv.darshini.domain.model.ChannelNumberingMode
import tv.darshini.domain.model.SyncCadence
import tv.darshini.domain.model.CombinedM3uProfile
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.model.DecoderMode
import tv.darshini.domain.model.EpgResolutionSummary
import tv.darshini.domain.model.GroupedChannelLabelMode
import tv.darshini.domain.model.LiveChannelGroupingMode
import tv.darshini.domain.model.LiveVariantPreferenceMode
import tv.darshini.domain.model.PlaybackBufferMode
import tv.darshini.domain.model.VodDuplicateHandlingMode
import tv.darshini.domain.model.VodHttpProtocolMode
import tv.darshini.domain.model.ExternalPlaybackMode
import tv.darshini.domain.model.PlayerSurfaceMode
import tv.darshini.domain.model.Provider
import tv.darshini.domain.model.RecordingItem
import tv.darshini.domain.model.RecordingStorageState
import tv.darshini.domain.model.RemoteShortcutPreferences
import tv.darshini.domain.model.VodVariantPreferenceMode

data class CrashReportUiModel(
    val timestamp: String = "",
    val exception: String = "",
    val fileName: String = "",
    val content: String = ""
) {
    val hasReport: Boolean
        get() = content.isNotBlank()
}

data class SettingsUiState(
    val providers: List<Provider> = emptyList(),
    val combinedProfiles: List<CombinedM3uProfile> = emptyList(),
    val availableM3uProviders: List<Provider> = emptyList(),
    val activeProviderId: Long? = null,
    val activeLiveSource: ActiveLiveSource? = null,
    val isSyncing: Boolean = false,
    val syncProgress: String? = null,
    val syncingProviderName: String? = null,
    val userMessage: String? = null,
    val syncWarningsByProvider: Map<Long, List<String>> = emptyMap(),
    val xtreamLiveOnboardingPhaseByProvider: Map<Long, String> = emptyMap(),
    val xtreamLiveOnboardingByProvider: Map<Long, XtreamLiveOnboardingUiModel> = emptyMap(),
    val xtreamIndexSectionStatusByProvider: Map<Long, Map<String, ProviderCatalogCountStatus>> = emptyMap(),
    val diagnosticsByProvider: Map<Long, ProviderDiagnosticsUiModel> = emptyMap(),
    val databaseMaintenance: DatabaseMaintenanceUiModel? = null,
    val parentalControlLevel: Int = 0,
    val hasParentalPin: Boolean = false,
    val appLanguage: String = "system",
    val appTheme: String = "dark",
    val appLandingDestination: AppLandingDestination = AppLandingDestination.HOME,
    val appTopLevelDestinations: List<AppTopLevelDestination> = AppTopLevelDestination.defaultOrder,
    val appHomeDashboardShelves: List<AppHomeDashboardShelf> = AppHomeDashboardShelf.defaultOrder,
    val appTimeFormat: AppTimeFormat = AppTimeFormat.SYSTEM,
    val preferredAudioLanguage: String = "auto",
    val playerMediaSessionEnabled: Boolean = true,
    val playerFastRetryOnTransientFailures: Boolean = false,
    val playerDecoderMode: DecoderMode = DecoderMode.AUTO,
    val playerPlaybackBufferMode: PlaybackBufferMode = PlaybackBufferMode.AUTO,
    val playerAudioOutputPreference: AudioOutputPreference = AudioOutputPreference.AUTO,
    val playerCompatibilityMemoryEnabled: Boolean = true,
    val playerSurfaceMode: PlayerSurfaceMode = PlayerSurfaceMode.AUTO,
    val playerVodHttpProtocolMode: VodHttpProtocolMode = VodHttpProtocolMode.COMPATIBILITY_HTTP1,
    val playerPlaybackSpeed: Float = 1f,
    val playerExternalPlaybackMode: ExternalPlaybackMode = ExternalPlaybackMode.INTERNAL_PLAYER,
    val playerAudioVideoSyncEnabled: Boolean = false,
    val playerAudioVideoOffsetMs: Int = 0,
    val centerTwoSlotMultiviewLayout: Boolean = false,
    val multiViewRespectProviderConnectionLimit: Boolean = true,
    val playerControlsTimeoutSeconds: Int = 5,
    val playerLiveOverlayTimeoutSeconds: Int = 4,
    val playerNoticeTimeoutSeconds: Int = 6,
    val playerDiagnosticsTimeoutSeconds: Int = 15,
    val subtitleTextScale: Float = 1f,
    val subtitleTextColor: Int = 0xFFFFFFFF.toInt(),
    val subtitleBackgroundColor: Int = 0x80000000.toInt(),
    val playerLiveTranslationEnabled: Boolean = false,
    val playerLiveTranslationEndpoint: String = "http://10.0.2.2:8765",
    val wifiMaxVideoHeight: Int? = null,
    val ethernetMaxVideoHeight: Int? = null,
    val playerTimeshiftEnabled: Boolean = false,
    val playerTimeshiftDepthMinutes: Int = 30,
    val defaultStopPlaybackTimerMinutes: Int = 0,
    val defaultIdleStandbyTimerMinutes: Int = 0,
    val lastSpeedTest: InternetSpeedTestUiModel? = null,
    val isRunningInternetSpeedTest: Boolean = false,
    val isDeletingProvider: Boolean = false,
    val isImportingBackup: Boolean = false,
    val backupPreview: BackupPreview? = null,
    val pendingBackupUri: String? = null,
    val backupImportPlan: BackupImportPlan = BackupImportPlan(),
    // --- Drive sync (M2) ---
    val driveAuthState: DriveAuthState = DriveAuthState.SignedOut,
    val driveSyncStatus: DriveSyncStatus = DriveSyncStatus(),
    val driveLastPushAt: Long? = null,
    val driveLastPullAt: Long? = null,
    val drivePendingSignIn: DriveSignInRequest? = null,
    val driveIsBusy: Boolean = false,
    // M3 — credentials downloaded by pullBackup, waiting to be applied
    // to providers once the import confirm completes.
    val pendingDriveCredentials: List<ProviderCredentials>? = null,
    val recordingItems: List<RecordingItem> = emptyList(),
    val recordingStorageState: RecordingStorageState = RecordingStorageState(),
    val wifiOnlyRecording: Boolean = false,
    val recordingPaddingBeforeMinutes: Int = 0,
    val recordingPaddingAfterMinutes: Int = 0,
    val isIncognitoMode: Boolean = false,
    val useXtreamTextClassification: Boolean = true,
    val xtreamBase64TextCompatibility: Boolean = false,
    val liveTvChannelMode: LiveTvChannelMode = LiveTvChannelMode.PRO,
    val showLiveSourceSwitcher: Boolean = false,
    val useSideNavigation: Boolean = false,
    val showAllChannelsCategory: Boolean = true,
    val showRecentChannelsCategory: Boolean = true,
    val remoteShortcutPreferences: RemoteShortcutPreferences = RemoteShortcutPreferences(),
    val liveTvCategoryFilters: List<String> = emptyList(),
    val liveTvQuickFilterVisibilityMode: LiveTvQuickFilterVisibilityMode = LiveTvQuickFilterVisibilityMode.ALWAYS_VISIBLE,
    val hideDecorativeLiveRows: Boolean = true,
    val liveChannelNumberingMode: ChannelNumberingMode = ChannelNumberingMode.GROUP,
    val liveChannelGroupingMode: LiveChannelGroupingMode = LiveChannelGroupingMode.RAW_VARIANTS,
    val groupedChannelLabelMode: GroupedChannelLabelMode = GroupedChannelLabelMode.HYBRID,
    val liveVariantPreferenceMode: LiveVariantPreferenceMode = LiveVariantPreferenceMode.BALANCED,
    val vodViewMode: VodViewMode = VodViewMode.MODERN,
    val vodInfiniteScroll: Boolean = true,
    val vodDuplicateHandlingMode: VodDuplicateHandlingMode = VodDuplicateHandlingMode.SHOW_ALL,
    val vodVariantPreferenceMode: VodVariantPreferenceMode = VodVariantPreferenceMode.BALANCED,
    val guideDefaultCategoryId: Long = tv.darshini.domain.model.VirtualCategoryIds.FAVORITES,
    val guideDefaultCategoryOptions: List<Category> = emptyList(),
    val preventStandbyDuringPlayback: Boolean = true,
    val zapAutoRevert: Boolean = true,
    val autoPlayNextEpisode: Boolean = true,
    val categorySortModes: Map<ContentType, CategorySortMode> = emptyMap(),
    val hiddenCategories: List<Category> = emptyList(),
    val epgSources: List<tv.darshini.domain.model.EpgSource> = emptyList(),
    val epgSourceAssignments: Map<Long, List<tv.darshini.domain.model.ProviderEpgSourceAssignment>> = emptyMap(),
    val epgResolutionSummaries: Map<Long, EpgResolutionSummary> = emptyMap(),
    val refreshingEpgSourceIds: Set<Long> = emptySet(),
    val epgPendingDeleteSourceId: Long? = null,
    val epgTimeShiftMinutesByProvider: Map<Long, Int> = emptyMap(),
    val autoCheckAppUpdates: Boolean = true,
    val autoDownloadAppUpdates: Boolean = false,
    val providerSyncCadence: SyncCadence = SyncCadence.DEFAULT,
    val isCheckingForUpdates: Boolean = false,
    val appUpdate: AppUpdateUiModel = AppUpdateUiModel(),
    val crashReport: CrashReportUiModel = CrashReportUiModel(),
    val viewedCrashReport: CrashReportUiModel? = null,
    val categoryLanguagePriority: List<String> = emptyList(),
    val availableMovieCategories: List<Category> = emptyList(),
    val availableLiveCategories: List<Category> = emptyList(),
    val availableSeriesCategories: List<Category> = emptyList(),
    val hiddenCategoryIds: Map<ContentType, Set<Long>> = emptyMap()
)
