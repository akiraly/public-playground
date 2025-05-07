package io.github.akiraly.sghbc.domain

import com.fasterxml.jackson.annotation.JsonValue
import org.jmolecules.ddd.types.AggregateRoot
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.Repository
import org.jmolecules.ddd.types.ValueObject
import org.springframework.core.io.Resource
import java.io.FileNotFoundException

data class CacheId(@JsonValue val value: String) : Identifier, ValueObject {
    init {
        require(value.length >= 8 && value.length <= 64) { "Invalid ID with length: ${value.length}" }
        require(CACHE_ID_MATCHER.matches(value)) { "Invalid ID: $value" }
    }
}

private val CACHE_ID_MATCHER = Regex("""^[0-9a-z_-]+$""")

data class GradleCacheKey(@JsonValue val value: String) : Identifier, ValueObject {
    init {
        require(value.length == 40) { "Invalid Key with length: ${value.length}" }
        require(CACHE_KEY_MATCHER.matches(value)) { "Invalid Key: $value" }
    }
}

private val CACHE_KEY_MATCHER = Regex("""^[0-9a-f]+$""")

data class CacheEntryId(
    val cacheId: CacheId,
    val gradleCacheKey: GradleCacheKey
) : Identifier, ValueObject

data class CacheEntry(
    override val id: CacheEntryId,
    val resource: Resource
) : AggregateRoot<CacheEntry, CacheEntryId>

interface CacheEntryRepository : Repository<CacheEntry, CacheEntryId>

fun interface RetrieveFromCache : CacheEntryRepository {
    /**
     * @return A resource containing the cache entry data
     * @throws FileNotFoundException if the cache entry does not exist
     */
    operator fun invoke(id: CacheEntryId): CacheEntry
}

fun interface StoreInCache : CacheEntryRepository {
    /**
     * @return true if the cache entry was stored successfully, false otherwise
     */
    operator fun invoke(id: CacheEntryId, value: CacheEntry): Boolean
}
