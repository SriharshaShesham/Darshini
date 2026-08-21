package tv.darshini.data.preferences

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

/**
 * Whole-DataStore backup encoding.
 *
 * The previous backup hand-listed every preference worth saving, so every setting added since then
 * was silently absent from backups. This dumps the store instead and excludes what must *not*
 * travel: state about this install (update downloads, maintenance counters, speed tests), values
 * that only mean something on this device (storage tree URIs), and per-channel-row-id keys that
 * cannot survive a catalog re-issue.
 */
internal object PreferencesBackup {

    /** Exact keys never written to a backup. */
    private val EXCLUDED_KEYS = setOf(
        "download_tree_uri",
        "last_launch_sync_timestamp",
        "last_app_update_check_timestamp",
        "xtream_text_import_generation",
        "is_incognito_mode",
        "player_muted",
        // Drive sync bookkeeping — describes this device's sync state, not the user's config.
        "drive_last_push_at",
        "drive_last_pull_at",
        "drive_last_error",
    )

    /** Key prefixes never written to a backup. */
    private val EXCLUDED_PREFIXES = listOf(
        "app_update_",
        "last_maintenance_",
        "last_speed_test_",
        // Observations are re-learned by playing; selections (the user's picks) are kept.
        "live_variant_observations",
        "vod_variant_observations",
        // Keyed by channel row id, which is re-issued on a catalog rebuild.
        "aspect_ratio_",
    )

    /**
     * Keys that embed a provider row id, as (prefix, suffix-after-id). The id is rewritten to the
     * restoring device's row id — without this the whole per-provider block (hidden categories,
     * pinned, sort mode, custom order) lands under a key nothing reads, which is exactly the
     * "Settings shows it, the browse screens ignore it" symptom.
     */
    private val PROVIDER_SCOPED_PREFIXES = listOf(
        "hidden_categories_",
        "pinned_categories_",
        "category_sort_",
        "category_priority_",
        "hidden_channels_",
        "last_live_category_id_",
    )

    /** ASCII unit separator - cannot occur in the string-set values these preferences hold. */
    private const val SET_SEPARATOR = "\u001F"

    fun shouldBackUp(keyName: String): Boolean =
        keyName !in EXCLUDED_KEYS && EXCLUDED_PREFIXES.none { keyName.startsWith(it) }

    fun encode(value: Any?): String? = when (value) {
        is Boolean -> "b:$value"
        is Int -> "i:$value"
        is Long -> "l:$value"
        is Float -> "f:$value"
        is Double -> "d:$value"
        is String -> "s:$value"
        is Set<*> -> "ss:" + value.filterIsInstance<String>().joinToString(SET_SEPARATOR)
        else -> null
    }

    /** Applies one encoded entry to [prefs]. Unparseable entries are skipped, never fatal. */
    fun apply(prefs: MutablePreferences, keyName: String, encoded: String) {
        val separator = encoded.indexOf(':')
        if (separator <= 0) return
        val raw = encoded.substring(separator + 1)
        when (encoded.substring(0, separator)) {
            "b" -> raw.toBooleanStrictOrNull()?.let { prefs[booleanPreferencesKey(keyName)] = it }
            "i" -> raw.toIntOrNull()?.let { prefs[intPreferencesKey(keyName)] = it }
            "l" -> raw.toLongOrNull()?.let { prefs[longPreferencesKey(keyName)] = it }
            "f" -> raw.toFloatOrNull()?.let { prefs[floatPreferencesKey(keyName)] = it }
            "d" -> raw.toDoubleOrNull()?.let { prefs[doublePreferencesKey(keyName)] = it }
            "s" -> prefs[stringPreferencesKey(keyName)] = raw
            "ss" -> prefs[stringSetPreferencesKey(keyName)] =
                if (raw.isEmpty()) emptySet() else raw.split(SET_SEPARATOR).toSet()
        }
    }

    /**
     * Rewrites a backed-up key so its embedded provider id points at the restoring device's row.
     * Returns null when the key belongs to a provider that was not restored — writing it would
     * leave dead state behind.
     */
    fun remapKey(keyName: String, providerIdMap: Map<Long, Long>): String? {
        val prefix = PROVIDER_SCOPED_PREFIXES.firstOrNull { keyName.startsWith(it) } ?: return keyName
        val rest = keyName.removePrefix(prefix)
        val idText = rest.substringBefore('_')
        val backupProviderId = idText.toLongOrNull() ?: return keyName
        val resolved = providerIdMap[backupProviderId] ?: return null
        return prefix + resolved + rest.removePrefix(idText)
    }

    /**
     * Values that are themselves provider ids: `last_active_provider_id` (a bare id) and
     * `epg_time_shift_by_provider` (an `id:minutes` list).
     */
    fun remapValue(keyName: String, encoded: String, providerIdMap: Map<Long, Long>): String? =
        when (keyName) {
            "last_active_provider_id" -> encoded.substringAfter("l:").toLongOrNull()
                ?.let { providerIdMap[it] }
                ?.let { "l:$it" }
            "epg_time_shift_by_provider" -> encoded.substringAfter("s:")
                .split(",")
                .mapNotNull { entry ->
                    val id = entry.substringBefore(':').toLongOrNull() ?: return@mapNotNull null
                    val minutes = entry.substringAfter(':', "").toIntOrNull() ?: return@mapNotNull null
                    providerIdMap[id]?.let { "$it:$minutes" }
                }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(",")
                ?.let { "s:$it" }
            else -> encoded
        }
}
