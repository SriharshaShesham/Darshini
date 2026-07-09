package tv.darshini.app

import tv.darshini.data.sync.SyncRepairSection
import tv.darshini.domain.model.AppTopLevelDestination

/**
 * Maps the user's visible top-level destinations onto the sync sections that are eligible to sync.
 * A section that has no visible destination is never synced (and its launch checkbox is disabled).
 * EPG feeds both the Live guide and the standalone Guide screen, so it stays eligible if either is visible.
 */
fun isSyncSectionVisible(
    section: SyncRepairSection,
    destinations: List<AppTopLevelDestination>
): Boolean = when (section) {
    SyncRepairSection.MOVIES -> AppTopLevelDestination.MOVIES in destinations
    SyncRepairSection.SERIES -> AppTopLevelDestination.SERIES in destinations
    SyncRepairSection.LIVE -> AppTopLevelDestination.LIVE_TV in destinations
    SyncRepairSection.EPG ->
        AppTopLevelDestination.LIVE_TV in destinations || AppTopLevelDestination.GUIDE in destinations
}

fun visibleSyncSections(destinations: List<AppTopLevelDestination>): Set<SyncRepairSection> =
    SyncRepairSection.values().filterTo(mutableSetOf()) { isSyncSectionVisible(it, destinations) }
