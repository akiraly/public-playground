package io.github.akiraly.sghbc.cache

import com.fasterxml.jackson.annotation.JsonValue
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.ValueObject
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.io.BufferedOutputStream
import java.io.FileNotFoundException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.DigestOutputStream
import java.security.MessageDigest

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
class StoreInCacheDir(
    private val cacheDirectory: CacheDirectory
) : StoreInCache {
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey, inputStream: InputStream): Boolean {
        var tempFile: Path? = null
        try {
            // Create cache-id directory if it doesn't exist
            val cacheIdDir = cacheDirectory.cacheDirectory.resolve(cacheId.id)
            if (!Files.exists(cacheIdDir)) {
                Files.createDirectories(cacheIdDir)
            }

            // Create cache-key directory if it doesn't exist
            val cacheKeyDir = cacheIdDir.resolve(cacheKey.id)
            if (!Files.exists(cacheKeyDir)) {
                Files.createDirectories(cacheKeyDir)
            }

            // Create a temporary file to store the content
            tempFile = Files.createTempFile(
                cacheKeyDir,
                "temp-",
                ".tmp",
                PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"))
            )

            // Calculate SHA-256 hash while writing the content to the temporary file
            val sha256Hash = calculateSha256WhileWriting(inputStream, tempFile)

            // Create the final file with the SHA-256 hash as the name
            val finalFile = cacheKeyDir.resolve(sha256Hash)

            // If the file with the same hash already exists, delete the temp file
            if (Files.exists(finalFile)) {
                Files.delete(tempFile)
            } else {
                // Otherwise, move the temp file to the final location
                Files.move(tempFile, finalFile, StandardCopyOption.ATOMIC_MOVE)
            }
            tempFile = null // Successfully moved, no need to delete in finally block

            // Create or update the 'latest' symlink
            val tmpLink = Files.createTempFile(cacheKeyDir, "latest-", ".lnk")
            Files.deleteIfExists(tmpLink)
            Files.createSymbolicLink(tmpLink, finalFile.fileName)
            Files.move(
                tmpLink,
                cacheKeyDir.resolve("latest"),
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING
            )

            return true
        } catch (e: Exception) {
            // Log the error in a real application
            e.printStackTrace()
            return false
        } finally {
            // Clean up temp file if it still exists (in case of errors)
            tempFile?.let {
                try {
                    if (Files.exists(it)) {
                        Files.delete(it)
                    }
                } catch (e: Exception) {
                    // Log the error but don't propagate it
                    e.printStackTrace()
                }
            }
        }
    }

    private fun calculateSha256WhileWriting(inputStream: InputStream, outputPath: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")

        Files.newOutputStream(outputPath).use { outputStream ->
            DigestOutputStream(BufferedOutputStream(outputStream), digest).use { digest ->
                inputStream.copyTo(digest)
            }
        }

        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

@Service
class RetrieveFromCacheDir(
    private val cacheDirectory: CacheDirectory
) : RetrieveFromCache {
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey): InputStream {
        val cacheKeyDir = cacheDirectory.cacheDirectory
            .resolve(cacheId.id)
            .resolve(cacheKey.id)

        if (!Files.exists(cacheKeyDir)) {
            throw FileNotFoundException("Cache entry not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        // Use the 'latest' symlink to get the most recent version
        val latestLink = cacheKeyDir.resolve("latest")
        if (!Files.exists(latestLink)) {
            throw FileNotFoundException("Latest cache entry not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        // Resolve the symlink to get the actual file
        val cacheFile = Files.readSymbolicLink(latestLink)
        val actualFile = cacheKeyDir.resolve(cacheFile)

        if (!Files.exists(actualFile)) {
            throw FileNotFoundException("Cache file not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        return Files.newInputStream(actualFile)
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
