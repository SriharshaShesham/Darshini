package tv.darshini.domain.model

enum class AppTopLevelDestination(
    val storageValue: String,
    val landingDestination: AppLandingDestination? = null,
    val isRequired: Boolean = false
) {
    HOME("home", AppLandingDestination.HOME, isRequired = true),
    LIVE_TV("live_tv", AppLandingDestination.LIVE_TV),
    MOVIES("movies", AppLandingDestination.MOVIES),
    SERIES("series", AppLandingDestination.SERIES),
    DOWNLOADS("downloads", AppLandingDestination.DOWNLOADS),
    GUIDE("guide", AppLandingDestination.GUIDE),
    SEARCH("search"),
    PLUGINS("plugins", AppLandingDestination.PLUGINS),
    SETTINGS("settings", AppLandingDestination.SETTINGS, isRequired = true);

    companion object {
        val defaultOrder: List<AppTopLevelDestination> = listOf(
            HOME,
            LIVE_TV,
            MOVIES,
            SERIES,
            DOWNLOADS,
            GUIDE,
            SEARCH,
            SETTINGS
        )

        fun fromStorage(value: String?): AppTopLevelDestination? =
            entries.firstOrNull { it.storageValue.equals(value, ignoreCase = true) }

        fun normalizeForStorage(destinations: List<AppTopLevelDestination>): List<AppTopLevelDestination> {
            val filtered = destinations.filter { it != PLUGINS }.distinct()
            val middle = filtered.filter { it != HOME && it != SEARCH && it != SETTINGS }
            val hasSearch = SEARCH in filtered
            return buildList {
                add(HOME)
                addAll(middle)
                if (hasSearch) add(SEARCH)
                add(SETTINGS)
            }
        }

        fun availableLandingDestinations(
            destinations: List<AppTopLevelDestination>
        ): List<AppLandingDestination> = normalizeForStorage(destinations)
            .mapNotNull { it.landingDestination }
            .filter { it != AppLandingDestination.PLUGINS }
            .distinct()

        fun resolveLandingDestination(
            preferred: AppLandingDestination,
            destinations: List<AppTopLevelDestination>
        ): AppLandingDestination {
            val available = availableLandingDestinations(destinations)
            return if (preferred in available) {
                preferred
            } else {
                available.firstOrNull() ?: AppLandingDestination.SETTINGS
            }
        }
    }
}
