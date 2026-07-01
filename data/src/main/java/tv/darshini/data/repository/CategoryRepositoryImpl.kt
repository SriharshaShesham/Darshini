package tv.darshini.data.repository

import tv.darshini.data.local.dao.*
import tv.darshini.data.local.DatabaseTransactionRunner
import tv.darshini.data.mapper.toDomain
import tv.darshini.domain.model.Category
import tv.darshini.domain.model.ContentType
import tv.darshini.domain.model.Result
import tv.darshini.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val channelDao: ChannelDao,
    private val movieDao: MovieDao,
    private val seriesDao: SeriesDao,
    private val transactionRunner: DatabaseTransactionRunner
) : CategoryRepository {

    override fun getCategories(providerId: Long): Flow<List<Category>> =
        combine(
            categoryDao.getByProviderAndType(providerId, ContentType.LIVE.name),
            categoryDao.getByProviderAndType(providerId, ContentType.MOVIE.name),
            categoryDao.getByProviderAndType(providerId, ContentType.SERIES.name)
        ) { live, movies, series ->
            (live + movies + series)
                .map { it.toDomain() }
                .sortedWith(compareBy<Category>({ it.type.ordinal }, { it.name.lowercase() }))
        }

    override suspend fun setCategoryProtection(
        providerId: Long,
        categoryId: Long,
        type: ContentType,
        isProtected: Boolean
    ): Result<Unit> = try {
        transactionRunner.inTransaction {
            categoryDao.updateProtectionStatus(providerId, categoryId, type.name, isProtected)
            when (type) {
                ContentType.LIVE -> channelDao.updateProtectionStatus(providerId, categoryId, isProtected)
                ContentType.MOVIE -> movieDao.updateProtectionStatus(providerId, categoryId, isProtected)
                ContentType.SERIES -> seriesDao.updateProtectionStatus(providerId, categoryId, isProtected)
                ContentType.SERIES_EPISODE -> Unit
            }
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.error("Failed to update category protection", e)
    }
}
