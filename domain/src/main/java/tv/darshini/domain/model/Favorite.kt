package tv.darshini.domain.model

data class Favorite(
    val id: Long = 0,
    val providerId: Long,
    val contentId: Long,
    /** Provider-side stable id; 0 when not resolved yet. Backups bind through this, not [contentId]. */
    val sourceId: Long = 0,
    val contentType: ContentType,
    val position: Int = 0,
    val groupId: Long? = null,
    val addedAt: Long = System.currentTimeMillis()
) {
    init {
        require(position >= 0) { "position must be non-negative" }
    }
}

data class VirtualGroup(
    val id: Long = 0,
    val providerId: Long,
    val name: String,
    val iconEmoji: String? = null,
    val position: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val contentType: ContentType = ContentType.LIVE
) {
    init {
        require(name.isNotBlank()) { "VirtualGroup name must not be blank" }
        require(position >= 0) { "position must be non-negative" }
    }
}
