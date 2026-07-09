package tv.darshini.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.darshini.app.R
import tv.darshini.app.isSyncSectionVisible
import tv.darshini.app.ui.interaction.TvClickableSurface
import tv.darshini.app.ui.theme.OnSurfaceDim
import tv.darshini.app.ui.theme.Primary
import tv.darshini.app.ui.theme.SettingsCardBackground
import tv.darshini.data.sync.SyncRepairSection
import tv.darshini.domain.model.SyncCadence

internal fun LazyListScope.settingsUiSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    context: Context,
    appThemeLabel: String,
    onShowThemeDialogChange: (Boolean) -> Unit,
    onNavigateToCategoryControl: (() -> Unit)? = null,
    onNavigateToSectionVisibility: (() -> Unit)? = null
) {
    item {
        // App Theme Selector
        TvClickableSurface(
            onClick = { onShowThemeDialogChange(true) },
            shape = ClickableSurfaceDefaults.shape(RoundedCornerShape(12.dp)),
            colors = ClickableSurfaceDefaults.colors(
                containerColor = SettingsCardBackground,
                focusedContainerColor = Primary.copy(alpha = 0.15f)
            ),
            scale = ClickableSurfaceDefaults.scale(focusedScale = 1f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.settings_app_theme), style = MaterialTheme.typography.bodyMedium, color = tv.darshini.app.ui.theme.OnSurface)
                Text(text = appThemeLabel, style = MaterialTheme.typography.bodyMedium, color = Primary)
            }
        }
    }

    // Category Control
    if (onNavigateToCategoryControl != null) {
        item {
            ClickableSettingsRow(
                label = stringResource(R.string.settings_provider_category_controls_action),
                value = "",
                onClick = onNavigateToCategoryControl
            )
        }
    }

    // Section Visibility
    if (onNavigateToSectionVisibility != null) {
        item {
            ClickableSettingsRow(
                label = stringResource(R.string.settings_section_visibility),
                value = "",
                onClick = onNavigateToSectionVisibility
            )
        }
    }

    // Automatic sync — Sync Content cadence + (for Every launch) per-section selection.
    item {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.settings_sync_cadence_title),
            style = MaterialTheme.typography.titleSmall,
            color = OnSurfaceDim,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        var showCadenceDialog by rememberSaveable { mutableStateOf(false) }
        ClickableSettingsRow(
            label = stringResource(R.string.settings_sync_content_label),
            value = stringResource(syncCadenceLabelRes(uiState.providerSyncCadence)),
            onClick = { showCadenceDialog = true }
        )
        Text(
            text = stringResource(R.string.settings_sync_content_description),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim,
            modifier = Modifier.padding(start = 12.dp, top = 6.dp, end = 12.dp)
        )
        if (showCadenceDialog) {
            PremiumSelectionDialog(
                title = stringResource(R.string.settings_sync_content_label),
                onDismiss = { showCadenceDialog = false }
            ) {
                SyncCadence.entries.forEach { cadence ->
                    LevelOption(
                        level = cadence.ordinal,
                        text = stringResource(syncCadenceLabelRes(cadence)),
                        currentLevel = uiState.providerSyncCadence.ordinal,
                        onSelect = {
                            viewModel.setProviderSyncCadence(cadence)
                            showCadenceDialog = false
                        }
                    )
                }
            }
        }
    }

    if (uiState.providerSyncCadence == SyncCadence.EVERY_LAUNCH) {
        item {
            SyncOnStartSectionRows(
                selected = uiState.syncOnStartSections,
                visibleDestinations = uiState.appTopLevelDestinations,
                onToggle = { section, isChecked ->
                    val updated = if (isChecked) {
                        uiState.syncOnStartSections + section
                    } else {
                        uiState.syncOnStartSections - section
                    }
                    viewModel.setSyncOnStartSections(updated)
                }
            )
        }
    }
}

private fun syncCadenceLabelRes(cadence: SyncCadence): Int = when (cadence) {
    SyncCadence.EVERY_LAUNCH -> R.string.settings_sync_cadence_every_launch
    SyncCadence.EVERY_1_DAY -> R.string.settings_sync_cadence_1_day
    SyncCadence.EVERY_2_DAYS -> R.string.settings_sync_cadence_2_days
    SyncCadence.EVERY_3_DAYS -> R.string.settings_sync_cadence_3_days
    SyncCadence.MANUAL -> R.string.settings_sync_cadence_manual
}

private val SYNC_ON_START_SECTION_ORDER = listOf(
    SyncRepairSection.MOVIES,
    SyncRepairSection.SERIES,
    SyncRepairSection.LIVE,
    SyncRepairSection.EPG
)

@Composable
private fun syncOnStartSectionLabel(section: SyncRepairSection): String = when (section) {
    SyncRepairSection.MOVIES -> stringResource(R.string.settings_sync_section_movies)
    SyncRepairSection.SERIES -> stringResource(R.string.settings_sync_section_series)
    SyncRepairSection.LIVE -> stringResource(R.string.settings_sync_section_live)
    SyncRepairSection.EPG -> stringResource(R.string.settings_sync_section_epg)
}

@Composable
private fun SyncOnStartSectionRows(
    selected: Set<SyncRepairSection>,
    visibleDestinations: List<tv.darshini.domain.model.AppTopLevelDestination>,
    onToggle: (SyncRepairSection, Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 12.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.settings_sync_content_checkbox_hint),
            style = MaterialTheme.typography.bodySmall,
            color = OnSurfaceDim,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        SYNC_ON_START_SECTION_ORDER.forEach { section ->
            val isVisible = isSyncSectionVisible(section, visibleDestinations)
            val isChecked = isVisible && section in selected
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(
                            if (isVisible) Modifier.clickable { onToggle(section, !isChecked) }
                            else Modifier
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = isChecked,
                        enabled = isVisible,
                        onCheckedChange = { onToggle(section, it) }
                    )
                    Text(
                        text = syncOnStartSectionLabel(section),
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isVisible) tv.darshini.app.ui.theme.OnSurface else OnSurfaceDim
                    )
                }
                if (!isVisible) {
                    Text(
                        text = stringResource(R.string.settings_sync_section_hidden_note),
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceDim,
                        modifier = Modifier.padding(start = 48.dp, bottom = 4.dp)
                    )
                }
            }
        }
    }
}
