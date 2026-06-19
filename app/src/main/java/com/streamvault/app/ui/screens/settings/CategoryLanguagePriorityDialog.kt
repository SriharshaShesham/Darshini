package com.streamvault.app.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.streamvault.app.R
import com.streamvault.app.ui.components.dialogs.PremiumDialog
import com.streamvault.app.ui.components.dialogs.PremiumDialogFooterButton
import com.streamvault.app.ui.interaction.TvButton
import com.streamvault.app.ui.theme.OnSurface
import com.streamvault.app.ui.theme.OnSurfaceDim
import com.streamvault.app.ui.theme.Primary
import com.streamvault.domain.model.Category

@Composable
internal fun CategoryLanguagePriorityDialog(
    title: String,
    currentPriority: List<String>,
    availableCategories: List<Category>,
    hiddenIds: Set<Long>,
    onToggleVisibility: (Long) -> Unit,
    onDismiss: () -> Unit,
    onSave: (List<String>) -> Unit
) {
    var draftCategories by remember(availableCategories, currentPriority) {
        val sortedByPriority = availableCategories.sortedBy { category ->
            val index = currentPriority.indexOfFirst { it.equals(category.name, ignoreCase = true) }
            if (index == -1) Int.MAX_VALUE else index
        }
        mutableStateOf(sortedByPriority)
    }

    PremiumDialog(
        title = title,
        subtitle = "Arrange category order and toggle visibility (hide/show) using the eye icon.",
        onDismissRequest = onDismiss,
        widthFraction = 0.68f,
        bodyHeightFraction = 0.78f,
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                draftCategories.forEachIndexed { index, category ->
                    val isHidden = hiddenIds.contains(category.id)
                    CategoryPriorityRow(
                        category = category,
                        isHidden = isHidden,
                        canMoveUp = index > 0,
                        canMoveDown = index < draftCategories.lastIndex,
                        onToggleVisibility = { onToggleVisibility(category.id) },
                        onMoveUp = {
                            val reordered = draftCategories.toMutableList().also { items ->
                                items[index] = items[index - 1]
                                items[index - 1] = category
                            }
                            draftCategories = reordered
                        },
                        onMoveDown = {
                            val reordered = draftCategories.toMutableList().also { items ->
                                items[index] = items[index + 1]
                                items[index + 1] = category
                            }
                            draftCategories = reordered
                        },
                        onMoveToTop = {
                            val reordered = draftCategories.toMutableList().also { items ->
                                items.removeAt(index)
                                items.add(0, category)
                            }
                            draftCategories = reordered
                        }
                    )
                }
            }
        },
        footer = {
            PremiumDialogFooterButton(
                label = stringResource(R.string.settings_cancel),
                onClick = onDismiss
            )
            PremiumDialogFooterButton(
                label = stringResource(R.string.action_save_order),
                emphasized = true,
                onClick = {
                    onSave(draftCategories.map { it.name })
                }
            )
        }
    )
}

@Composable
private fun CategoryPriorityRow(
    category: Category,
    isHidden: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onToggleVisibility: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onMoveToTop: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = if (isHidden) 0.02f else 0.05f), RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isHidden) OnSurfaceDim else OnSurface
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TvButton(
                    onClick = onToggleVisibility
                ) {
                    Icon(
                        imageVector = if (isHidden) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = if (isHidden) Color.White.copy(alpha = 0.4f) else Primary
                    )
                }
                TvButton(
                    enabled = canMoveUp,
                    onClick = onMoveToTop
                ) {
                    Icon(
                        imageVector = Icons.Filled.VerticalAlignTop,
                        contentDescription = "Move to Top"
                    )
                }
                TvButton(
                    enabled = canMoveUp,
                    onClick = onMoveUp
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowUp,
                        contentDescription = "Move Up"
                    )
                }
                TvButton(
                    enabled = canMoveDown,
                    onClick = onMoveDown
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowDown,
                        contentDescription = "Move Down"
                    )
                }
            }
        }
    }
}

