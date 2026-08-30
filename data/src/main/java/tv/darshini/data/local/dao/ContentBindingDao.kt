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
 * Restored rows are parked at a negative `content_id` (`-source_id`, or `-content_id` for pre-v8
 * backups that carry no source id): unique against the table's unique indices, never colliding with
 * a real row, skipped by the "content is missing" sweeps instead of being deleted, and — the point —
 * excluded from the forward fill, which would otherwise read the source device's row id as a local
 * one and bind the row to unrelated content.
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
        WHERE provider_id = :providerId AND content_id < 0 AND source_id != 0
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
        WHERE provider_id = :providerId AND content_id < 0 AND source_id != 0
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

    /**
     * Binds restored rows that carry no `source_id` (pre-v8 backups) by **title**.
     *
     * These rows have no provider-side id anywhere in the backup, and their `content_id` is the
     * source device's row id, so resolving through it lands on unrelated content — that is the
     * "Continue Watching opens a different series" bug. `title` is the only key the payload
     * actually carries that means the same thing on both devices.
     *
     * Binds only when the title matches **exactly one** catalog row: two series sharing a name is
     * precisely how a wrong tile comes back. Ambiguous rows stay parked and invisible.
     *
     * `series` is used rather than `episodes` on purpose — episodes hydrate on demand, so after a
     * fresh restore the episode rows usually do not exist yet, while the series always does.
     */
    @Query(
        """
        UPDATE playback_history SET source_id = (
            SELECT s.series_id FROM series s
             WHERE s.provider_id = playback_history.provider_id AND s.name = playback_history.title
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND content_type = 'SERIES' AND title != ''
          AND (SELECT COUNT(*) FROM series s2
                WHERE s2.provider_id = playback_history.provider_id
                  AND s2.name = playback_history.title) = 1
        """
    )
    abstract suspend fun bindHistorySeriesByTitle(providerId: Long)

    @Query(
        """
        UPDATE playback_history SET source_id = (
            SELECT m.stream_id FROM movies m
             WHERE m.provider_id = playback_history.provider_id AND m.name = playback_history.title
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND content_type = 'MOVIE' AND title != ''
          AND (SELECT COUNT(*) FROM movies m2
                WHERE m2.provider_id = playback_history.provider_id
                  AND m2.name = playback_history.title) = 1
        """
    )
    abstract suspend fun bindHistoryMovieByTitle(providerId: Long)

    @Query(
        """
        UPDATE playback_history SET source_id = (
            SELECT c.stream_id FROM channels c
             WHERE c.provider_id = playback_history.provider_id AND c.name = playback_history.title
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND content_type = 'LIVE' AND title != ''
          AND (SELECT COUNT(*) FROM channels c2
                WHERE c2.provider_id = playback_history.provider_id
                  AND c2.name = playback_history.title) = 1
        """
    )
    abstract suspend fun bindHistoryChannelByTitle(providerId: Long)

    /**
     * Episode rows: the stored `title` is the *series* name, so resolve the series by title and
     * then pin the episode with the season/episode numbers the row already carries.
     *
     * Sets `series_id` even when the episode itself has not hydrated yet — that alone is enough for
     * the tile to open the right series, and `content_id` binds on a later pass once the episodes
     * arrive.
     */
    @Query(
        """
        UPDATE playback_history SET series_id = (
            SELECT s.id FROM series s
             WHERE s.provider_id = playback_history.provider_id AND s.name = playback_history.title
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND content_type = 'SERIES_EPISODE' AND title != ''
          AND (SELECT COUNT(*) FROM series s2
                WHERE s2.provider_id = playback_history.provider_id
                  AND s2.name = playback_history.title) = 1
        """
    )
    abstract suspend fun bindHistoryEpisodeSeriesByTitle(providerId: Long)

    @Query(
        """
        UPDATE playback_history SET source_id = (
            SELECT e.episode_id FROM episodes e
             WHERE e.provider_id = playback_history.provider_id
               AND e.series_id = playback_history.series_id
               AND e.season_number = playback_history.season_number
               AND e.episode_number = playback_history.episode_number
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND content_type = 'SERIES_EPISODE'
          AND series_id IS NOT NULL AND season_number IS NOT NULL AND episode_number IS NOT NULL
          AND (SELECT COUNT(*) FROM episodes e2
                WHERE e2.provider_id = playback_history.provider_id
                  AND e2.series_id = playback_history.series_id
                  AND e2.season_number = playback_history.season_number
                  AND e2.episode_number = playback_history.episode_number) = 1
        """
    )
    abstract suspend fun bindHistoryEpisodeBySeasonEpisode(providerId: Long)

    /**
     * Tiebreaker for rows the title could not place — an exact `stream_url` match. Weaker than
     * title because Xtream URLs embed the credentials, so a password change invalidates every
     * stored URL, but it does resolve duplicate titles.
     */
    @Query(
        """
        UPDATE playback_history SET source_id = COALESCE(
            (SELECT c.stream_id FROM channels c
              WHERE c.stream_url = playback_history.stream_url
                AND c.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'LIVE'),
            (SELECT m.stream_id FROM movies m
              WHERE m.stream_url = playback_history.stream_url
                AND m.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'MOVIE'),
            (SELECT e.episode_id FROM episodes e
              WHERE e.stream_url = playback_history.stream_url
                AND e.provider_id = playback_history.provider_id
                AND playback_history.content_type = 'SERIES_EPISODE'),
            source_id
        )
        WHERE provider_id = :providerId AND source_id = 0 AND content_id < 0
          AND stream_url != ''
        """
    )
    abstract suspend fun bindHistoryByStreamUrl(providerId: Long)

    @Transaction
    open suspend fun rebind(providerId: Long) {
        fillFavoriteSourceIds(providerId)
        fillFavoriteContentIds(providerId)
        dropParkedFavoriteDuplicates(providerId)

        // Title/stream-url passes first: they give a parked pre-v8 row the source_id it never had,
        // so the fills below have something real to bind through. Order matters for episodes —
        // the series must resolve before the season/episode lookup can use it.
        bindHistorySeriesByTitle(providerId)
        bindHistoryMovieByTitle(providerId)
        bindHistoryChannelByTitle(providerId)
        bindHistoryEpisodeSeriesByTitle(providerId)
        bindHistoryEpisodeBySeasonEpisode(providerId)
        bindHistoryByStreamUrl(providerId)

        fillHistorySourceIds(providerId)
        fillHistoryContentIds(providerId)
        refreshHistorySeriesIds(providerId)
        dropParkedHistoryDuplicates(providerId)
    }
}
