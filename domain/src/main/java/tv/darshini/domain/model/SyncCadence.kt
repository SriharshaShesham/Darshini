package tv.darshini.domain.model

/**
 * How often the app should run a full provider catalog refresh on launch.
 * A refresh re-fetches the catalog (picking up newly-added items and dropping removed ones).
 */
enum class SyncCadence {
    EVERY_LAUNCH,
    EVERY_1_DAY,
    EVERY_2_DAYS,
    EVERY_3_DAYS,
    MANUAL;

    /** Whether a launch-time refresh is due, given the last refresh time. */
    fun isLaunchSyncDue(lastSyncAtMillis: Long, nowMillis: Long): Boolean {
        val elapsed = nowMillis - lastSyncAtMillis
        return when (this) {
            EVERY_LAUNCH -> true
            EVERY_1_DAY -> elapsed >= DAY_MILLIS
            EVERY_2_DAYS -> elapsed >= 2 * DAY_MILLIS
            EVERY_3_DAYS -> elapsed >= 3 * DAY_MILLIS
            MANUAL -> false
        }
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60 * 60 * 1000

        val DEFAULT = EVERY_1_DAY

        fun fromName(value: String?): SyncCadence =
            value?.let { name -> entries.firstOrNull { it.name == name } } ?: DEFAULT
    }
}
