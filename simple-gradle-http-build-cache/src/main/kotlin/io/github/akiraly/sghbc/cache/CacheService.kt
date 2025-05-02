package io.github.akiraly.sghbc.cache

import com.fasterxml.jackson.annotation.JsonValue
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.ValueObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

data class CacheId(@JsonValue val id: String) : Identifier, ValueObject

data class CacheKey(@JsonValue val id: String) : Identifier, ValueObject

/**
 * Interface for storing a cache entry
 */
fun interface StoreInCache : (CacheId, CacheKey, InputStream) -> Boolean {
    /**
     * Stores a cache entry with the given cache ID and cache key
     *
     * @param cacheId The cache ID used for segregating caches
     * @param cacheKey The Gradle cache key
     * @param inputStream The input stream containing the cache entry data
     * @return true if the cache entry was stored successfully, false otherwise
     */
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey, inputStream: InputStream): Boolean
}

/**
 * Interface for retrieving a cache entry
 */
fun interface RetrieveFromCache : (CacheId, CacheKey) -> InputStream {
    /**
     * Retrieves a cache entry with the given cache ID and cache key
     *
     * @param cacheId The cache ID used for segregating caches
     * @param cacheKey The Gradle cache key
     * @return An input stream containing the cache entry data
     * @throws FileNotFoundException if the cache entry does not exist
     */
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey): InputStream
}

@Service
class StoreInCacheWithCacheDir(
    private val cacheDirectory: CacheDirectory
) : StoreInCache {
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey, inputStream: InputStream): Boolean {
        try {
            val cacheIdDir = cacheDirectory.cacheDirectory.resolve(cacheId.toString())
            if (!Files.exists(cacheIdDir)) {
                Files.createDirectories(cacheIdDir)
            }

            val cacheFile = cacheIdDir.resolve(cacheKey.toString())
            Files.copy(inputStream, cacheFile, StandardCopyOption.REPLACE_EXISTING)
            return true
        } catch (e: Exception) {
            // Log the error in a real application
            return false
        }
    }
}

@Service
class RetrieveFromCacheWithCacheDir(
    private val cacheDirectory: CacheDirectory
) : RetrieveFromCache {
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey): InputStream {
        val cacheFile =
            cacheDirectory.cacheDirectory.resolve(cacheId.toString()).resolve(cacheKey.toString())
        if (!Files.exists(cacheFile)) {
            throw FileNotFoundException("Cache entry not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }
        return Files.newInputStream(cacheFile)
    }
}

/**
 * Service for managing cache entries on disk
 */
@Component
class CacheDirectory(
    @param:Value("\${cache.directory:cache}") private val cacheDirectoryPath: String
) {

    val cacheDirectory: Path = Paths.get(cacheDirectoryPath)

    init {
        // Ensure the cache directory exists
        if (!Files.exists(cacheDirectory)) {
            Files.createDirectories(cacheDirectory)
        }
    }
}
