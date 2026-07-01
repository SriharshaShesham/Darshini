package tv.darshini.data.repository

import tv.darshini.domain.model.ExternalRatings
import tv.darshini.domain.model.ExternalRatingsLookup
import tv.darshini.domain.model.Result
import tv.darshini.domain.repository.ExternalRatingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExternalRatingsRepositoryImpl @Inject constructor() : ExternalRatingsRepository {

    override suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings> {
        return Result.success(ExternalRatings.unavailable())
    }
}