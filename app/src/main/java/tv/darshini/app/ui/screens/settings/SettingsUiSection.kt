package tv.darshini.app.ui.screens.settings

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import tv.darshini.app.R
import tv.darshini.app.ui.interaction.TvClickableSurface
import tv.darshini.app.ui.theme.Primary
import tv.darshini.app.ui.theme.SettingsCardBackground
import tv.darshini.domain.model.CategorySortMode
import tv.darshini.domain.model.ContentType

internal fun LazyListScope.settingsUiSection(
    uiState: SettingsUiState,
    viewModel: SettingsViewModel,
    context: Context,
    appThemeLabel: String,
    onShowThemeDialogChange: (Boolean) -> Unit,
    onCategorySortDialogTypeChange: (String?) -> Unit,
    onNavigateToCategoryControl: (() -> Unit)? = null
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

        // Live Channels Order
        ClickableSettingsRow(
            label = stringResource(R.string.settings_category_sort_live),
            value = formatCategorySortModeLabel(uiState.categorySortModes[ContentType.LIVE] ?: CategorySortMode.DEFAULT, context),
            onClick = { onCategorySortDialogTypeChange(ContentType.LIVE.name) }
        )

        // Movies Order
        ClickableSettingsRow(
            label = stringResource(R.string.settings_category_sort_movies),
            value = formatCategorySortModeLabel(uiState.categorySortModes[ContentType.MOVIE] ?: CategorySortMode.DEFAULT, context),
            onClick = { onCategorySortDialogTypeChange(ContentType.MOVIE.name) }
        )

        // Series Order
        ClickableSettingsRow(
            label = stringResource(R.string.settings_category_sort_series),
            value = formatCategorySortModeLabel(uiState.categorySortModes[ContentType.SERIES] ?: CategorySortMode.DEFAULT, context),
            onClick = { onCategorySortDialogTypeChange(ContentType.SERIES.name) }
        )

        // Category Control
        if (onNavigateToCategoryControl != null) {
            ClickableSettingsRow(
                label = stringResource(R.string.settings_provider_category_controls_action),
                value = "",
                onClick = onNavigateToCategoryControl
            )
        }
    }
}
