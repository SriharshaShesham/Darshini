package tv.darshini.app.diagnostics

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import tv.darshini.domain.model.Result
import tv.darshini.domain.repository.ChannelRepository
import tv.darshini.domain.repository.MovieRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ExternalPlayerProbeActivity : ComponentActivity() {
    @Inject
    lateinit var channelRepository: ChannelRepository

    @Inject
    lateinit var movieRepository: MovieRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            launchExternalPlayer()
            finish()
        }
    }

    private suspend fun launchExternalPlayer() {
        // Resolve the stream URL
        val streamUrl = resolveStreamUrl()
        if (streamUrl == null) {
            Log.w(TAG, "external-player-probe unable to resolve stream URL")
            return
        }

        val url = if (intent.getBooleanExtra(EXTRA_FORCE_TS, false)) {
            toMpegTsUrl(streamUrl)
        } else {
            streamUrl
        }
        val playerPackage = intent.getStringExtra(EXTRA_PLAYER_PACKAGE)
            ?.takeIf(String::isNotBlank)
        val showChooser = intent.getBooleanExtra(EXTRA_SHOW_CHOOSER, false)
        val launchIntent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(url.toUri(), "video/*")
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (playerPackage != null) {
            launchIntent.setPackage(playerPackage)
        }

        val finalIntent = if (showChooser) {
            Intent.createChooser(launchIntent, "Open with")
        } else {
            launchIntent
        }

        runCatching { startActivity(finalIntent) }
            .onSuccess {
                Log.i(
                    TAG,
                    "external-player-probe launched streamUrl=$streamUrl " +
                        "playerPackage=$playerPackage showChooser=$showChooser"
                )
            }
            .onFailure { error ->
                Log.w(TAG, "external-player-probe launch failed error=${error::class.java.simpleName} url=$streamUrl")
                if (!showChooser) {
                    Log.i(TAG, "Launch failed. Attempting fallback by showing chooser.")
                    val chooserIntent = Intent.createChooser(launchIntent, "Open with")
                    runCatching { startActivity(chooserIntent) }
                        .onFailure { chooserError ->
                            Log.e(TAG, "All fallback attempts failed", chooserError)
                        }
                }
            }
    }

    private suspend fun resolveStreamUrl(): String? {
        // 1. Direct stream URL — if already an HTTP(S) URL, use as-is
        intent.getStringExtra(EXTRA_STREAM_URL)?.takeIf(String::isNotBlank)?.let { url ->
            if (!url.startsWith("xtream://")) {
                Log.d(TAG, "resolved direct URL: $url")
                return url
            }
            Log.d(TAG, "xtream URL needs resolution via repository: $url")
            // If xtream://, fall through to try repository resolution with IDs
        }

        // 2. Movie ID — resolve via MovieRepository
        val movieId = intent.getLongExtra(EXTRA_MOVIE_ID, -1L)
        if (movieId > 0L) {
            return resolveMovieUrl(movieId)
        }

        // 3. Channel ID — resolve via ChannelRepository
        val channelId = intent.getLongExtra(EXTRA_CHANNEL_ID, -1L)
        if (channelId > 0L) {
            return resolveChannelUrl(channelId)
        }

        Log.w(TAG, "no resolvable content identifier found")
        return null
    }

    private suspend fun resolveMovieUrl(movieId: Long): String? {
        val movie = movieRepository.getMovie(movieId) ?: run {
            Log.w(TAG, "movie not found movieId=$movieId")
            return null
        }
        val streamInfo = when (val result = movieRepository.getStreamInfo(movie)) {
            is Result.Success -> result.data
            is Result.Error -> {
                Log.w(TAG, "movie stream resolve failed movieId=$movieId")
                return null
            }
            Result.Loading -> return null
        }
        Log.d(TAG, "resolved movie URL: ${streamInfo.url}")
        return streamInfo.url
    }

    private suspend fun resolveChannelUrl(channelId: Long): String? {

        val channel = channelRepository.getChannel(channelId)
        if (channel == null) {
            Log.w(TAG, "external-player-probe channel not found channelId=$channelId")
            return null
        }

        val streamInfo = when (val result = channelRepository.getStreamInfo(channel)) {
            is Result.Success -> result.data
            is Result.Error -> {
                Log.w(TAG, "external-player-probe resolve failed channelId=$channelId")
                return null
            }
            Result.Loading -> return null
        }
        return streamInfo.url
    }

    private fun toMpegTsUrl(url: String): String {
        val queryStart = url.indexOf('?')
        val base = if (queryStart >= 0) url.substring(0, queryStart) else url
        val query = if (queryStart >= 0) url.substring(queryStart) else ""
        return if (base.endsWith(HLS_EXTENSION, ignoreCase = true)) {
            base.dropLast(HLS_EXTENSION.length) + MPEG_TS_EXTENSION + query
        } else {
            url
        }
    }

    companion object {
        const val EXTRA_CHANNEL_ID = "channel_id"
        const val EXTRA_MOVIE_ID = "movie_id"
        const val EXTRA_STREAM_URL = "stream_url"
        const val EXTRA_PLAYER_PACKAGE = "player_package"
        const val EXTRA_FORCE_TS = "force_ts"
        const val EXTRA_SHOW_CHOOSER = "show_chooser"
        private const val DEFAULT_PLAYER_PACKAGE = "org.videolan.vlc"
        private const val HLS_EXTENSION = ".m3u8"
        private const val MPEG_TS_EXTENSION = ".ts"
        private const val TAG = "ExternalPlayerProbe"
    }
}
