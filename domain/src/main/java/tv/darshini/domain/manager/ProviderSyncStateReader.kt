package tv.darshini.domain.manager

import tv.darshini.domain.model.SyncState
import kotlinx.coroutines.flow.Flow

interface ProviderSyncStateReader {
    fun currentSyncState(providerId: Long): SyncState
    fun observeBackgroundIndexingActive(providerId: Long): Flow<Boolean>
}
