package tv.darshini.domain.model

/**
 * How often the app uploads its backup to Google Drive in the background.
 *
 * Push only — a scheduled *download* would silently overwrite local favourites, watch progress and
 * settings with another device's copy, and the import path has no merge, only wholesale
 * keep-existing / replace-existing. Restoring stays a deliberate, confirmed action.
 */
enum class DriveSyncCadence(val intervalHours: Long) {
    EVERY_6_HOURS(6),
    EVERY_12_HOURS(12),
    EVERY_1_DAY(24),
    EVERY_7_DAYS(24 * 7),
    MANUAL(0);

    companion object {
        val DEFAULT = EVERY_1_DAY

        fun fromName(value: String?): DriveSyncCadence =
            value?.let { name -> entries.firstOrNull { it.name == name } } ?: DEFAULT
    }
}
