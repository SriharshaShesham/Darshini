package tv.darshini.data.sync

import tv.darshini.data.local.dao.CategoryDao
import tv.darshini.data.local.dao.ChannelDao
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.repository.SyncMetadataRepository
import tv.darshini.domain.model.ProviderType
import kotlinx.coroutines.flow.first

internal suspend fun hasUsableLiveCatalogForActivation(
    providerId: Long,
    providerType: ProviderType,
    channelDao: ChannelDao,
    categoryDao: CategoryDao,
    syncMetadataRepository: SyncMetadataRepository
): Boolean {
    if (providerType != ProviderType.XTREAM_CODES && providerType != ProviderType.STALKER_PORTAL) {
        return true
    }

    if (channelDao.getCount(providerId).first() > 0) {
        return true
    }

    val metadata = syncMetadataRepository.getMetadata(providerId)
    if ((metadata?.movieCount ?: 0) > 0 || (metadata?.seriesCount ?: 0) > 0) {
        return true
    }

    return categoryDao.getByProviderAndTypeSync(providerId, ContentType.MOVIE.name).isNotEmpty() ||
        categoryDao.getByProviderAndTypeSync(providerId, ContentType.SERIES.name).isNotEmpty()
}
