package tv.darshini.app.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import kotlinx.coroutines.launch
import tv.darshini.app.R
import tv.darshini.app.ui.design.requestFocusWithRetries
import tv.darshini.app.ui.screens.settings.isRemoteVersionNewer
import tv.darshini.app.ui.theme.OnSurface
import tv.darshini.app.ui.theme.OnSurfaceDim
import tv.darshini.app.ui.theme.SurfaceElevated
import tv.darshini.data.preferences.PreferencesRepository
import tv.darshini.domain.model.Result

/**
 * Startup prompt for a newer release. Reads the release cached by the launch check in
 * StreamVaultApp; "Remind me next launch" only hides it for this process.
 */
@Composable
fun AppUpdateDialogHost(
    preferencesRepository: PreferencesRepository,
    appUpdateInstaller: AppUpdateInstaller
) {
    val versionName by preferencesRepository.cachedAppUpdateVersionName.collectAsState(initial = null)
    val versionCode by preferencesRepository.cachedAppUpdateVersionCode.collectAsState(initial = null)
    val downloadUrl by preferencesRepository.cachedAppUpdateDownloadUrl.collectAsState(initial = null)
    val publishedAt by preferencesRepository.cachedAppUpdatePublishedAt.collectAsState(initial = null)
    val releaseUrl by preferencesRepository.cachedAppUpdateReleaseUrl.collectAsState(initial = null)
    val downloadState by appUpdateInstaller.downloadState.collectAsState()
    var dismissed by rememberSaveable { mutableStateOf(false) }
    var downloadStartedHere by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    val latest = versionName
    if (dismissed || latest == null || downloadUrl == null) return
    if (!isRemoteVersionNewer(versionCode, latest, publishedAt)) return

    val status = if (downloadState.versionName == latest) downloadState.status else AppUpdateDownloadStatus.Idle
    val install: () -> Unit = {
        scope.launch {
            errorMessage = (appUpdateInstaller.installDownloadedUpdate() as? Result.Error)?.message
        }
    }

    // Open the installer as soon as a download started from this dialog finishes.
    LaunchedEffect(status) {
        if (status == AppUpdateDownloadStatus.Downloaded && downloadStartedHere) {
            downloadStartedHere = false
            install()
        }
    }

    val primaryFocus = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        primaryFocus.requestFocusWithRetries(tag = "AppUpdateDialog", target = "Update button")
    }

    Dialog(onDismissRequest = { dismissed = true }) {
        Surface(
            modifier = Modifier.widthIn(max = 560.dp),
            shape = RoundedCornerShape(24.dp),
            colors = SurfaceDefaults.colors(containerColor = SurfaceElevated)
        ) {
            Column(
                modifier = Modifier.padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_update_available_message, latest),
                    style = MaterialTheme.typography.headlineSmall,
                    color = OnSurface
                )
                if (status == AppUpdateDownloadStatus.Downloading) {
                    Text(
                        text = stringResource(R.string.settings_update_download_in_progress),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceDim
                    )
                }
                errorMessage?.let {
                    Text(text = it, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceDim)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    when (status) {
                        AppUpdateDownloadStatus.Downloaded -> Button(
                            onClick = install,
                            modifier = Modifier.focusRequester(primaryFocus)
                        ) { Text(stringResource(R.string.settings_update_install_action)) }

                        AppUpdateDownloadStatus.Downloading -> Button(
                            // Stays enabled so D-pad focus isn't dropped while downloading.
                            onClick = {},
                            modifier = Modifier.focusRequester(primaryFocus)
                        ) { Text(stringResource(R.string.settings_update_download_in_progress)) }

                        AppUpdateDownloadStatus.Idle,
                        AppUpdateDownloadStatus.Failed -> Button(
                            onClick = {
                                scope.launch {
                                    val release = GitHubReleaseInfo(
                                        versionName = latest,
                                        versionCode = versionCode,
                                        releaseUrl = releaseUrl.orEmpty(),
                                        downloadUrl = downloadUrl,
                                        releaseNotes = "",
                                        publishedAt = publishedAt
                                    )
                                    when (val result = appUpdateInstaller.startDownload(release)) {
                                        is Result.Error -> errorMessage = result.message
                                        else -> {
                                            errorMessage = null
                                            downloadStartedHere = true
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.focusRequester(primaryFocus)
                        ) { Text(stringResource(R.string.settings_update_download_action)) }
                    }
                    Button(onClick = { dismissed = true }) {
                        Text(stringResource(R.string.app_update_remind_next_launch))
                    }
                }
            }
        }
    }
}
