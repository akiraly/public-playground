package io.github.akiraly.sghbc.store.fs

import io.github.akiraly.sghbc.domain.CacheEntry
import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.RetrieveFromCache
import io.github.akiraly.sghbc.domain.StoreInCache
import org.apache.commons.io.input.BoundedInputStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.DigestOutputStream
import java.security.MessageDigest


@Repository
class RetrieveFromCacheDir(
    private val cacheDirectory: CacheDirectory
) : RetrieveFromCache {
    private val logger = LoggerFactory.getLogger(RetrieveFromCacheDir::class.java)

    override fun invoke(id: CacheEntryId): CacheEntry? {
        val cacheKeyDir = cacheDirectory.resolveDir(id)

        if (!Files.exists(cacheKeyDir)) {
            logger.debug("Cache directory not found for {}", id)
            return null
        }

        val latestLink = cacheKeyDir.resolve("latest")
        if (!Files.exists(latestLink)) {
            logger.debug("Latest symlink not found for {}", id)
            return null
        }

        val actualFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))

        if (!Files.exists(actualFile)) {
            logger.debug("Cache file not found for {}, file: {}", id, actualFile)
            return null
        }

        return CacheEntry(id, PathResource(actualFile))
    }
}

@Repository
class StoreInCacheDir(
    private val cacheDirectory: CacheDirectory,

    @param:Value("\${io.github.akiraly.sghbc.http.maxAllowedSizeInMb:100}")
    private val maxAllowedSizeInMb: Long
) : StoreInCache {
    private val logger = LoggerFactory.getLogger(StoreInCacheDir::class.java)

    private val filePermissions =
        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"))

    override fun invoke(id: CacheEntryId, value: CacheEntry): Boolean {
        var tempFile: Path? = null
        try {
            val cacheKeyDir = cacheDirectory.resolveDir(id)
            if (!Files.exists(cacheKeyDir)) {
                Files.createDirectories(cacheKeyDir)
            }

            tempFile = cacheKeyDir.createTempFile()

            val hash = value.resource.writeTo(tempFile)

            val finalFile = cacheKeyDir.resolve(hash)

            if (Files.exists(finalFile)) {
                Files.delete(tempFile)
            } else {
                Files.move(tempFile, finalFile, StandardCopyOption.ATOMIC_MOVE)
            }
            tempFile = null // Successfully moved, no need to delete in finally block

            // Create or update the 'latest' symlink with atomic operation
            val tmpLink = cacheKeyDir.createTempFile()
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
            logger.error("Error storing cache entry for {}", id, e)
            return false
        } finally {
            tempFile?.let {
                try {
                    if (Files.exists(it)) {
                        Files.delete(it)
                    }
                } catch (e: Exception) {
                    // Log the error but don't propagate it
                    logger.error("Error cleaning up temporary file: {}", it, e)
                }
            }
        }
    }

    private fun Path.createTempFile(): Path =
        Files.createTempFile(this, "tmp.", ".tmp", filePermissions)

    private fun Resource.writeTo(outputPath: Path): String {
        val digest = MessageDigest.getInstance("SHA-256")

        inputStream.use { inputStream ->
            Files.newOutputStream(outputPath).buffered().use { outputStream ->
                DigestOutputStream(outputStream, digest)
                    .use { digest ->
                        BoundedInputStream.builder()
                            .setInputStream(inputStream)
                            .setMaxCount(maxAllowedSizeInMb * 1024 * 1024)
                            .get()
                            .copyTo(digest)
                    }
            }
        }

        val hashBytes = digest.digest()
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}

@Component
class CacheDirectory(
    @param:Value("\${io.github.akiraly.sghbc.fs.cache.directory:cache}")
    private val cacheDirectoryPath: String
) {
    private val logger = LoggerFactory.getLogger(CacheDirectory::class.java)

    private val cacheDirectory: Path = Paths.get(cacheDirectoryPath)

    init {
        // Ensure the cache directory exists
        if (!Files.exists(cacheDirectory)) {
            logger.info("Creating cache directory: {}", cacheDirectory)
            Files.createDirectories(cacheDirectory)
        } else {
            logger.debug("Using existing cache directory: {}", cacheDirectory)
        }
    }

    fun resolveDir(id: CacheEntryId): Path =
        cacheDirectory.resolve(id.cacheId.value).resolve(id.gradleCacheKey.value)
}
