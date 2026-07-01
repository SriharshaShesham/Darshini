package tv.darshini.app.ui.screens.settings

import android.content.ActivityNotFoundException
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.tv.material3.*
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import tv.darshini.app.backup.BackupFileBridge
import tv.darshini.app.device.isFireTvDevice
import tv.darshini.app.device.removableAppStorageDirs
import java.io.File
import tv.darshini.app.diagnostics.CrashReportStore
import tv.darshini.app.util.OfficialBuildVerifier
import tv.darshini.app.ui.components.shell.AppTopBarCloseAction
import tv.darshini.app.ui.components.shell.AppNavigationChrome
import tv.darshini.app.ui.components.shell.AppScreenScaffold
import tv.darshini.app.ui.theme.*
import tv.darshini.domain.model.Provider
import androidx.compose.ui.res.stringResource
import tv.darshini.app.R
import tv.darshini.app.ui.design.requestFocusSafely
import tv.darshini.app.ui.design.AppColors
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val backupFileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")

private fun buildBackupFileName(): String =
    "streamvault_backup_${LocalDateTime.now().format(backupFileNameFormatter)}.json"

// Fire OS strips the AOSP DocumentsUI, so ACTION_CREATE_DOCUMENT and
// ACTION_OPEN_DOCUMENT have no handler and throw ActivityNotFoundException.
// ACTION_OPEN_DOCUMENT_TREE is handled by Amazon's storage UI, so on Fire TV
// we route backup export/import through a folder picker instead.
private fun Context.isFireTv(): Boolean =
    packageManager.hasSystemFeature("amazon.hardware.fire_tv")


@Composable
fun SettingsScreen(
    onNavigate: (String) -> Unit,
    onAddProvider: () -> Unit = {},
    onEditProvider: (Provider) -> Unit = {},
    onNavigateToParentalControl: (Long) -> Unit = {},
    currentRoute: String,
    initialBackupImportUri: String? = null,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val settingsNavFocusRequester = remember { FocusRequester() }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current
    val mainActivity = context.findMainActivity()
    val officialBuildVerification = remember(context.packageName) { OfficialBuildVerifier.verify(context) }
    val screenLabels = rememberSettingsScreenLabels(
        uiState = uiState,
        context = context,
        officialBuildStatus = officialBuildVerification.status
    )
    val dialogState = rememberSettingsScreenDialogState()
    val providerState = rememberSettingsProviderSectionState(dialogState)
    var handledInitialBackupImportUri by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportConfig(it.toString()) }
    }

    var pendingExportProviderId by remember { mutableStateOf<Long?>(null) }
    val exportProviderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val providerId = pendingExportProviderId
        if (uri != null && providerId != null) {
            viewModel.exportProvider(providerId, uri.toString())
        }
        pendingExportProviderId = null
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.inspectBackup(it.toString()) }
    }

    var pendingImportCandidates by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val exportTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
        }
        val folder = DocumentFile.fromTreeUri(context, treeUri)
        if (folder == null || !folder.canWrite()) {
            viewModel.showUserMessage(context.getString(R.string.settings_backup_folder_create_failed))
            return@rememberLauncherForActivityResult
        }
        val newFile = folder.createFile(BackupFileBridge.MIME_TYPE_JSON, buildBackupFileName())
        if (newFile == null) {
            viewModel.showUserMessage(context.getString(R.string.settings_backup_folder_create_failed))
            return@rememberLauncherForActivityResult
        }
        viewModel.exportConfig(newFile.uri.toString())
    }

    val importTreeLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        treeUri ?: return@rememberLauncherForActivityResult
        runCatching {
            context.contentResolver.takePersistableUriPermission(
                treeUri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        }
        val folder = DocumentFile.fromTreeUri(context, treeUri)
        if (folder == null) {
            viewModel.showUserMessage(context.getString(R.string.settings_backup_folder_read_failed))
            return@rememberLauncherForActivityResult
        }
        val candidates = folder.listFiles()
            .filter { it.isFile && it.name?.endsWith(".json", ignoreCase = true) == true }
            .sortedByDescending { it.lastModified() }
            .map { (it.name ?: "backup.json") to it.uri.toString() }
        when {
            candidates.isEmpty() ->
                viewModel.showUserMessage(context.getString(R.string.settings_backup_no_files_in_folder))
            candidates.size == 1 -> viewModel.inspectBackup(candidates.first().second)
            else -> pendingImportCandidates = candidates
        }
    }

    fun shareBackup() {
        val file = runCatching { BackupFileBridge.createExportFile(context) }.getOrNull()
        if (file == null) {
            viewModel.showUserMessage(context.getString(R.string.settings_backup_share_prepare_failed))
            return
        }
        val uri = BackupFileBridge.providerUriForFile(context, file)
        viewModel.exportConfig(uri.toString()) {
            runCatching { context.startActivity(BackupFileBridge.buildShareIntent(uri)) }
                .onFailure { viewModel.showUserMessage(context.getString(R.string.settings_backup_share_failed)) }
        }
    }

    // Fire-Stick-only: app-private folder on a plugged-in USB OTG drive. Null on every other device
    // and when no removable drive is attached, which keeps all USB controls hidden elsewhere.
    val usbStorageDir: File? = remember(context) {
        if (context.isFireTvDevice()) context.removableAppStorageDirs().firstOrNull() else null
    }

    fun createBackupToUsb() {
        val dir = usbStorageDir ?: return
        val file = runCatching { BackupFileBridge.createExportFile(dir) }.getOrNull()
        if (file == null) {
            viewModel.showUserMessage(context.getString(R.string.settings_backup_usb_failed))
            return
        }
        viewModel.exportConfig(Uri.fromFile(file).toString())
    }

    fun restoreBackupFromUsb() {
        val dir = usbStorageDir ?: return
        val candidates = BackupFileBridge.listBackupFiles(dir)
            .map { (it.name) to Uri.fromFile(it).toString() }
        when {
            candidates.isEmpty() ->
                viewModel.showUserMessage(context.getString(R.string.settings_backup_no_files_in_folder))
            candidates.size == 1 -> viewModel.inspectBackup(candidates.first().second)
            else -> pendingImportCandidates = candidates
        }
    }

    fun shareCrashReport() {
        val file = CrashReportStore.latestReportFile(context)
        if (!file.isFile || file.length() <= 0L) {
            viewModel.showUserMessage(context.getString(R.string.settings_crash_report_missing))
            viewModel.refreshCrashReport()
            return
        }
        val uri = CrashReportStore.providerUriForFile(context, file)
        runCatching { context.startActivity(CrashReportStore.buildShareIntent(uri)) }
            .onFailure { viewModel.showUserMessage(context.getString(R.string.settings_crash_report_share_failed)) }
    }

    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.completeDriveSignIn(result.data)
    }

    val recordingFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val displayName = DocumentFile.fromTreeUri(context, it)?.name
            viewModel.updateRecordingFolder(it.toString(), displayName)
        }
    }

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(uiState.userMessage) {
        uiState.userMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.userMessageShown()
        }
    }

    LaunchedEffect(uiState.recordingItems) {
        dialogState.selectedRecordingId = when {
            uiState.recordingItems.isEmpty() -> null
            dialogState.selectedRecordingId == null -> uiState.recordingItems.first().id
            uiState.recordingItems.any { item -> item.id == dialogState.selectedRecordingId } -> dialogState.selectedRecordingId
            else -> uiState.recordingItems.first().id
        }
    }

    LaunchedEffect(initialBackupImportUri) {
        val uri = initialBackupImportUri?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (handledInitialBackupImportUri == uri) return@LaunchedEffect
        handledInitialBackupImportUri = uri
        dialogState.selectedCategory = 6
        viewModel.inspectBackup(uri)
    }

    LaunchedEffect(currentRoute, dialogState.selectedCategory) {
        delay(80)
        settingsNavFocusRequester.requestFocusSafely(tag = "SettingsScreen", target = "Selected settings section")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AppScreenScaffold(
            currentRoute = currentRoute,
            onNavigate = { if (!uiState.isSyncing) onNavigate(it) },
            title = stringResource(R.string.settings_title),
            subtitle = stringResource(R.string.settings_providers_subtitle),
            navigationChrome = AppNavigationChrome.TopBar,
            compactHeader = true,
            showScreenHeader = false,
            topBarActions = {
                AppTopBarCloseAction(
                    onClick = { mainActivity?.finishAffinity() }
                )
            }
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                SettingsNavigationRail(
                    selectedCategory = dialogState.selectedCategory,
                    focusRequester = settingsNavFocusRequester,
                    onCategorySelected = { dialogState.selectedCategory = it }
                )

                // Thin vertical separator
                Box(
                    Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(AppColors.Divider)
                )

                SettingsContentPane(
                    uiState = uiState,
                    viewModel = viewModel,
                    context = context,
                    screenLabels = screenLabels,
                    dialogState = dialogState,
                    providerState = providerState,
                    onAddProvider = onAddProvider,
                    onEditProvider = onEditProvider,
                    onExportProvider = { provider ->
                        val defaultFileName = "${provider.name.lowercase().replace(Regex("[^a-zA-Z0-9_]"), "_")}_backup.json"
                        val isTv = context.packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_LEANBACK)

                        if (isTv) {
                            val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
                            val targetFile = java.io.File(downloadDir, defaultFileName)
                            var success = false
                            try {
                                downloadDir.mkdirs()
                                if (targetFile.exists()) targetFile.delete()
                                targetFile.createNewFile()
                                viewModel.exportProvider(provider.id, android.net.Uri.fromFile(targetFile).toString()) {
                                    viewModel.showUserMessage("Exported to Download folder: $defaultFileName")
                                }
                                success = true
                            } catch (e: Exception) {
                                // fallback to app private storage
                            }

                            if (!success) {
                                val appDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
                                val backupsDir = java.io.File(appDir, "Backups").apply { mkdirs() }
                                val fallbackFile = java.io.File(backupsDir, defaultFileName).apply {
                                    if (exists()) delete()
                                    createNewFile()
                                }
                                viewModel.exportProvider(provider.id, android.net.Uri.fromFile(fallbackFile).toString()) {
                                    viewModel.showUserMessage("Exported to: ${fallbackFile.absolutePath}")
                                }
                            }
                        } else {
                            val shareSingleProvider = {
                                val documentsDir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
                                val backupsDir = java.io.File(documentsDir, "Backups").apply { mkdirs() }
                                val file = java.io.File(backupsDir, defaultFileName).apply {
                                    if (exists()) delete()
                                    createNewFile()
                                }
                                val uri = BackupFileBridge.providerUriForFile(context, file)
                                viewModel.exportProvider(provider.id, uri.toString()) {
                                    runCatching {
                                        val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                            type = "application/json"
                                            putExtra(android.content.Intent.EXTRA_STREAM, uri)
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        val chooser = android.content.Intent.createChooser(sendIntent, "Share Provider Backup").apply {
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(chooser)
                                    }.onFailure {
                                        viewModel.showUserMessage(context.getString(R.string.settings_backup_share_failed))
                                    }
                                }
                            }

                            val intent = android.content.Intent(android.content.Intent.ACTION_CREATE_DOCUMENT).apply {
                                addCategory(android.content.Intent.CATEGORY_OPENABLE)
                                type = "application/json"
                            }
                            val canCreateDocument = context.packageManager.resolveActivity(intent, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY) != null

                            if (canCreateDocument) {
                                pendingExportProviderId = provider.id
                                try {
                                    exportProviderLauncher.launch(defaultFileName)
                                } catch (e: android.content.ActivityNotFoundException) {
                                    shareSingleProvider()
                                }
                            } else {
                                shareSingleProvider()
                            }
                        }
                    },
                    onNavigateToParentalControl = onNavigateToParentalControl,
                    onChooseRecordingFolder = {
                        try {
                            recordingFolderLauncher.launch(null)
                        } catch (e: ActivityNotFoundException) {
                            viewModel.showUserMessage(
                                context.getString(R.string.settings_backup_folder_picker_unavailable)
                            )
                        }
                    },
                    onUseUsbRecordingStorage = usbStorageDir?.let { dir ->
                        { viewModel.useUsbRecordingStorage(File(dir, "recordings").absolutePath) }
                    },
                    onCreateBackupUsb = usbStorageDir?.let { { createBackupToUsb() } },
                    onRestoreBackupUsb = usbStorageDir?.let { { restoreBackupFromUsb() } },
                    onCreateBackup = {
                        val onFireTv = context.isFireTv()
                        val primary: () -> Unit = if (onFireTv) {
                            { exportTreeLauncher.launch(null) }
                        } else {
                            { createDocumentLauncher.launch("streamvault_backup.json") }
                        }
                        val fallback: () -> Unit = if (onFireTv) {
                            { createDocumentLauncher.launch("streamvault_backup.json") }
                        } else {
                            { exportTreeLauncher.launch(null) }
                        }
                        try {
                            primary()
                        } catch (e: ActivityNotFoundException) {
                            try {
                                fallback()
                            } catch (e2: ActivityNotFoundException) {
                                viewModel.showUserMessage(
                                    context.getString(R.string.settings_backup_folder_picker_unavailable)
                                )
                            }
                        }
                    },
                    onShareBackup = ::shareBackup,
                    onViewCrashReport = viewModel::viewCrashReport,
                    onShareCrashReport = ::shareCrashReport,
                    onDeleteCrashReport = viewModel::deleteCrashReport,
                    onRestoreBackup = {
                        val candidates = scanLocalBackupCandidates(context)
                        pendingImportCandidates = candidates + ("Browse other folders (System Picker)" to "__system_picker__")
                    },
                    onDriveSignIn = { viewModel.beginDriveSignIn(driveSignInLauncher) },
                    onDriveSignOut = viewModel::signOutDrive,
                    onDrivePush = viewModel::pushToDrive,
                    onDrivePull = viewModel::pullFromDrive,
                    onOpenUri = uriHandler::openUri,
                    modifier = Modifier.weight(1f)
                )
            }
        }

    SettingsScreenOverlays(
        snackbarHostState = snackbarHostState,
        uiState = uiState,
        viewModel = viewModel,
        context = context,
        scope = scope,
        dialogState = dialogState,
        mainActivity = mainActivity,
        currentRoute = currentRoute,
        modifier = Modifier
    )

    if (pendingImportCandidates.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { pendingImportCandidates = emptyList() },
            title = {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.settings_backup_choose_file_title)
                )
            },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    pendingImportCandidates.forEach { (name, uri) ->
                        TextButton(
                            onClick = {
                                val selectedUri = uri
                                pendingImportCandidates = emptyList()
                                if (selectedUri == "__system_picker__") {
                                    val onFireTv = context.isFireTv()
                                    val primary: () -> Unit = if (onFireTv) {
                                        { importTreeLauncher.launch(null) }
                                    } else {
                                        {
                                            openDocumentLauncher.launch("*/*")
                                        }
                                    }
                                    val fallback: () -> Unit = if (onFireTv) {
                                        {
                                            openDocumentLauncher.launch("*/*")
                                        }
                                    } else {
                                        { importTreeLauncher.launch(null) }
                                    }
                                    try {
                                        primary()
                                    } catch (e: android.content.ActivityNotFoundException) {
                                        try {
                                            fallback()
                                        } catch (e2: android.content.ActivityNotFoundException) {
                                            viewModel.showUserMessage(
                                                context.getString(R.string.settings_backup_folder_picker_unavailable)
                                            )
                                        }
                                    }
                                } else {
                                    viewModel.inspectBackup(selectedUri)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            androidx.compose.material3.Text(text = name, color = OnSurface)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pendingImportCandidates = emptyList() }) {
                    androidx.compose.material3.Text(
                        text = stringResource(R.string.settings_cancel),
                        color = OnSurface
                    )
                }
            },
            containerColor = SurfaceElevated,
            titleContentColor = OnSurface,
            textContentColor = TextSecondary
        )
    }
}
}

private fun scanLocalBackupCandidates(context: Context): List<Pair<String, String>> {
    val paths = mutableListOf<File>()
    context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOCUMENTS)?.let { docDir ->
        paths.add(File(docDir, "Backups"))
        paths.add(docDir)
    }
    paths.add(File(context.filesDir, "Backups"))
    paths.add(context.filesDir)
    
    val downloadDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
    if (downloadDir != null) {
        paths.add(downloadDir)
    }
    
    if (context.isFireTvDevice()) {
        context.removableAppStorageDirs().forEach { dir ->
            paths.add(File(dir, "Backups"))
            paths.add(dir)
        }
    }
    
    val files = paths.distinct()
        .flatMap { dir ->
            if (dir.isDirectory) {
                dir.listFiles()?.filter { it.isFile && it.name.endsWith(".json", ignoreCase = true) } ?: emptyList()
            } else {
                emptyList()
            }
        }
        .distinctBy { it.absolutePath }
        .sortedByDescending { it.lastModified() }
        
    return files.map { file ->
        file.name to Uri.fromFile(file).toString()
    }
}
