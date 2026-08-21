package tv.darshini.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction

/**
 * Keeps `favorites` / `playback_history` bound to the catalog across reinstalls and catalog wipes.
 *
 * Both tables reference content by `content_id`, which is a local autoincrement row id. Those ids
 * are stable while a provider is only ever re-synced in place, but they are re-issued from scratch
 * whenever the catalog rows are deleted and re-inserted — a restore, a provider re-add, a fresh
 * install. `source_id` mirrors the provider-side id (`channels.stream_id`, `movies.stream_id`,
 * `series.series_id`, `episodes.episode_id`), which is stable everywhere, so a backup can bind
 * through it.
 *
 * [rebind] runs both directions and is safe to call repeatedly:
 *  - forward  — fills `source_id` for rows that have a resolvable `content_id` (new favourites)
 *  - backward — fills `content_id` from `source_id` (rows that arrived from a backup)
 *
 * Restored rows that cannot bind yet (catalog not synced) are parked at `content_id = -source_id`:
 * negative, so they stay unique against the table's unique indices, never collide with a real row,
 * and are skipped by the "content is missing" maintenance sweeps instead of being deleted.
 */
@Dao
abstract class ContentBindingDao {

    @Query(
        """
        UPDATE favorites SET source_id = COALESCE(
            (SELECT c.stream_id FROM channels c
              WHERE c.id = favorites.content_id AND c.provider_id = favorites.provider_id
                AND favorites.content_type = 'LIVE'),
            (SELECT m.stream_id FROM movies m
              WHERE m.id = favorites.content_id AND m.provider_id = favorites.provider_id
                AND favorites.content_type = 'MOVIE'),
            (SELECT s.series_id FROM series s
              WHERE s.id = favorites.content_id AND s.provider_id = favorites.provider_id
                AND favorites.content_type = 'SERIES'),
            source_id
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id > 0
        """
    )
    abstract suspend fun fillFavoriteSourceIds(providerId: Long)

    @Query(
        """
        UPDATE OR IGNORE favorites SET content_id = COALESCE(
            (SELECT c.id FROM channels c
              WHERE c.stream_id = favorites.source_id AND c.provider_id = favorites.provider_id
                AND favorites.content_type = 'LIVE'),
            (SELECT m.id FROM movies m
              WHERE m.stream_id = favorites.source_id AND m.provider_id = favorites.provider_id
                AND favorites.content_type = 'MOVIE'),
            (SELECT s.id FROM series s
              WHERE s.series_id = favorites.source_id AND s.provider_id = favorites.provider_id
                AND favorites.content_type = 'SERIES'),
            content_id
        )
        WHERE provider_id = :providerId AND source_id != 0
        """
    )
    abstract suspend fun fillFavoriteContentIds(providerId: Long)

    /** Drops parked rows whose content already has a bound favourite (import collided with a local one). */
    @Query(
        """
        DELETE FROM favorites
        WHERE provider_id = :providerId AND content_id < 0
          AND EXISTS (
              SELECT 1 FROM favorites other
              WHERE other.provider_id = favorites.provider_id
                AND other.content_type = favorites.content_type
                AND other.source_id = favorites.source_id
                AND other.group_key = favorites.group_key
                AND other.content_id > 0
          )
        """
    )
    abstract suspend fun dropParkedFavoriteDuplicates(providerId: Long)

    @Query(
        """
        UPDATE playback_history SET source_id = COALESCE(
            (SELECT c.stream_id FROM channels c
              WHERE c.id = playback_history.content_id AND c.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'LIVE'),
            (SELECT m.stream_id FROM movies m
              WHERE m.id = playback_history.content_id AND m.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'MOVIE'),
            (SELECT s.series_id FROM series s
              WHERE s.id = playback_history.content_id AND s.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'SERIES'),
            (SELECT e.episode_id FROM episodes e
              WHERE e.id = playback_history.content_id AND e.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'SERIES_EPISODE'),
            source_id
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id > 0
        """
    )
    abstract suspend fun fillHistorySourceIds(providerId: Long)

    @Query(
        """
        UPDATE OR IGNORE playback_history SET content_id = COALESCE(
            (SELECT c.id FROM channels c
              WHERE c.stream_id = playback_history.source_id AND c.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'LIVE'),
            (SELECT m.id FROM movies m
              WHERE m.stream_id = playback_history.source_id AND m.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'MOVIE'),
            (SELECT s.id FROM series s
              WHERE s.series_id = playback_history.source_id AND s.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'SERIES'),
            (SELECT e.id FROM episodes e
              WHERE e.episode_id = playback_history.source_id AND e.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'SERIES_EPISODE'),
            content_id
        )
        WHERE provider_id = :providerId AND source_id != 0
        """
    )
    abstract suspend fun fillHistoryContentIds(providerId: Long)

    /**
     * `series_id` is a local row id too, so a restored episode row points at the wrong series until
     * it is re-derived from the (now bound) episode.
     */
    @Query(
        """
        UPDATE playback_history SET series_id = (
            SELECT e.series_id FROM episodes e
             WHERE e.id = playback_history.content_id AND e.provider_id = playback_history.provider_id
        )
        WHERE provider_id = :providerId AND content_type = 'SERIES_EPISODE' AND content_id > 0
          AND EXISTS (
              SELECT 1 FROM episodes e
               WHERE e.id = playback_history.content_id AND e.provider_id = playback_history.provider_id
          )
        """
    )
    abstract suspend fun refreshHistorySeriesIds(providerId: Long)

    @Query(
        """
        DELETE FROM playback_history
        WHERE provider_id = :providerId AND content_id < 0
          AND EXISTS (
              SELECT 1 FROM playback_history other
              WHERE other.provider_id = playback_history.provider_id
                AND other.content_type = playback_history.content_type
                AND other.source_id = playback_history.source_id
                AND other.content_id > 0
          )
        """
    )
    abstract suspend fun dropParkedHistoryDuplicates(providerId: Long)

    @Transaction
    open suspend fun rebind(providerId: Long) {
        fillFavoriteSourceIds(providerId)
        fillFavoriteContentIds(providerId)
        dropParkedFavoriteDuplicates(providerId)
        fillHistorySourceIds(providerId)
        fillHistoryContentIds(providerId)
        refreshHistorySeriesIds(providerId)
        dropParkedHistoryDuplicates(providerId)
    }
}
