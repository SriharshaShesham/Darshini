package tv.darshini.app.ui.model

import tv.darshini.domain.model.Category
import tv.darshini.domain.model.CategorySortMode

fun applyProviderCategoryDisplayPreferences(
    categories: List<Category>,
    hiddenCategoryIds: Set<Long>,
    sortMode: CategorySortMode,
    priorityKeywords: List<String> = emptyList()
): List<Category> {
    val visible = categories.filterNot { it.id in hiddenCategoryIds }
    val sorted = when (sortMode) {
        CategorySortMode.DEFAULT -> visible
        CategorySortMode.TITLE_ASC -> visible.sortedBy { it.name.lowercase() }
        CategorySortMode.TITLE_DESC -> visible.sortedByDescending { it.name.lowercase() }
        CategorySortMode.COUNT_DESC -> visible.sortedWith(
            compareByDescending<Category> { it.count }.thenBy { it.name.lowercase() }
        )
        CategorySortMode.COUNT_ASC -> visible.sortedWith(
            compareBy<Category> { it.count }.thenBy { it.name.lowercase() }
        )
        CategorySortMode.CUSTOM -> {
            visible.sortedBy { category ->
                val index = priorityKeywords.indexOfFirst { keyword ->
                    keyword.equals(category.name, ignoreCase = true)
                }
                if (index == -1) Int.MAX_VALUE else index
            }
        }
    }

    if (sortMode == CategorySortMode.CUSTOM) {
        return sorted
    }

    return sorted.sortedBy { category ->
        val index = priorityKeywords.indexOfFirst { keyword ->
            category.name.lowercase().contains(keyword.lowercase())
        }
        if (index == -1) Int.MAX_VALUE else index
    }
}
