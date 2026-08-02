package tv.darshini.domain.sync

/**
 * Process-global flag: true while a stream is actively loaded in the player.
 *
 * Background catalog sync workers (Xtream/Stalker index, EPG) defer while this is set so they
 * don't steal bandwidth and connections from playback — the sync and the video stream usually
 * hit the same origin server, and a mid-playback catalog sweep starves the player's buffer.
 *
 * Set by the player engine on prepare/stop/release; read by the workers alongside the low-memory
 * guard. All workers run in the app process, so a plain @Volatile is enough.
 */
object PlaybackActivitySignal {
    @Volatile
    var isActive: Boolean = false
}
