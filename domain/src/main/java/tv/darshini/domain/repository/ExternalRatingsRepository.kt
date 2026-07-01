package tv.darshini.domain.repository

import tv.darshini.domain.model.ExternalRatings
import tv.darshini.domain.model.ExternalRatingsLookup
import tv.darshini.domain.model.Result

interface ExternalRatingsRepository {
    suspend fun getRatings(lookup: ExternalRatingsLookup): Result<ExternalRatings>
}