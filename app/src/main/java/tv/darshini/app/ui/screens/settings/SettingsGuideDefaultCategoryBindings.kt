package tv.darshini.app.ui.screens.settings

import tv.darshini.app.ui.model.applyProviderCategoryDisplayPreferences
import tv.darshini.data.preferences.PreferencesRepository
import tv.darshini.domain.model.ActiveLiveSource
import tv.darshini.domain.model.Category
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.model.VirtualCategoryIds
import tv.darshini.domain.repository.ChannelRepository
import tv.darshini.domain.repository.CombinedM3uRepository
import tv.darshini.domain.usecase.GetCustomCategories
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalCoroutinesApi::class)
internal fun observeGuideDefaultCategoryOptions(
    combinedM3uRepository: CombinedM3uRepository,
    channelRepository: ChannelRepository,
    preferencesRepository: PreferencesRepository,
    getCustomCategories: GetCustomCategories
): Flow<List<Category>> {
    return combinedM3uRepository.getActiveLiveSource().flatMapLatest { activeSource ->
        when (activeSource) {
            is ActiveLiveSource.CombinedM3uSource -> {
                combine(
                    combinedM3uRepository.getCombinedCategories(activeSource.profileId),
                    flow {
                        emit(combinedM3uRepository.getProfile(activeSource.profileId)?.members.orEmpty())
                    }.flatMapLatest { members ->
                        getCustomCategories(
                            members.filter { it.enabled }.map { it.providerId },
                            ContentType.LIVE
                        )
                    }
                ) { combinedCategories, customCategories ->
                    buildGuideDefaultCategoryOptions(
                        physicalCategories = combinedCategories.map { it.category },
                        customCategories = customCategories
                    )
                }
            }

            is ActiveLiveSource.ProviderSource -> {
                combine(
                    channelRepository.getCategories(activeSource.providerId),
                    getCustomCategories(activeSource.providerId, ContentType.LIVE),
                    preferencesRepository.getHiddenCategoryIds(activeSource.providerId, ContentType.LIVE),
                    preferencesRepository.getCategorySortMode(activeSource.providerId, ContentType.LIVE),
                    preferencesRepository.getCategoryLanguagePriority(activeSource.providerId, ContentType.LIVE)
                ) { categories, customCategories, hiddenCategoryIds, sortMode, priorityKeywords ->
                    val visibleProviderCategories = applyProviderCategoryDisplayPreferences(
                        categories = categories.filter { it.id != ChannelRepository.ALL_CHANNELS_ID },
                        hiddenCategoryIds = hiddenCategoryIds,
                        sortMode = sortMode,
                        priorityKeywords = priorityKeywords
                    )
                    buildGuideDefaultCategoryOptions(
                        physicalCategories = visibleProviderCategories,
                        customCategories = customCategories
                    )
                }
            }

            null -> flowOf(
                listOf(
                    Category(
                        id = VirtualCategoryIds.FAVORITES,
                        name = "Favorites",
                        type = ContentType.LIVE,
                        isVirtual = true
                    ),
                    Category(
                        id = ChannelRepository.ALL_CHANNELS_ID,
                        name = "All Channels",
                        type = ContentType.LIVE
                    )
                )
            )
        }
    }
}

internal fun buildGuideDefaultCategoryOptions(
    physicalCategories: List<Category>,
    customCategories: List<Category>
): List<Category> {
    val favorites = customCategories.find { it.id == VirtualCategoryIds.FAVORITES }
    return buildList {
        if (favorites != null) {
            add(favorites)
        }
        addAll(customCategories.filter { it.id != VirtualCategoryIds.FAVORITES })
        add(
            Category(
                id = ChannelRepository.ALL_CHANNELS_ID,
                name = "All Channels",
                type = ContentType.LIVE,
                count = physicalCategories.sumOf(Category::count)
            )
        )
        addAll(physicalCategories)
    }
}