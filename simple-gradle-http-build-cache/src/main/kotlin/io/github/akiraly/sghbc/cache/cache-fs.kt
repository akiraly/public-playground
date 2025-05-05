package io.github.akiraly.sghbc.cache

import com.fasterxml.jackson.annotation.JsonValue
import org.apache.commons.io.input.BoundedInputStream
import org.jmolecules.ddd.types.Identifier
import org.jmolecules.ddd.types.ValueObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.PathResource
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.stereotype.Repository
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.attribute.PosixFilePermissions
import java.security.DigestOutputStream
import java.security.MessageDigest

data class CacheId(@JsonValue val id: String) : Identifier, ValueObject {
    init {
        validateId(id)
    }
}

data class CacheKey(@JsonValue val id: String) : Identifier, ValueObject {
    init {
        validateId(id)
    }
}

private fun validateId(id: String) {
    require(id.length <= 64) { "Invalid ID with length: ${id.length}" }
    require(ID_MATCHER.matches(id)) { "Invalid ID: $id" }
}

private val ID_MATCHER = Regex("""^[0-9a-f]{32,64}$""")

fun interface RetrieveFromCache : (CacheId, CacheKey) -> Resource {
    /**
     * @return A resource containing the cache entry data
     * @throws FileNotFoundException if the cache entry does not exist
     */
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey): Resource
}

@Repository
class RetrieveFromCacheDir(
    private val cacheDirectory: CacheDirectory
) : RetrieveFromCache {
    private val logger = LoggerFactory.getLogger(RetrieveFromCacheDir::class.java)

    override fun invoke(cacheId: CacheId, cacheKey: CacheKey): Resource {
        val cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)

        if (!Files.exists(cacheKeyDir)) {
            logger.debug(
                "Cache directory not found for cacheId: {}, cacheKey: {}",
                cacheId, cacheKey
            )
            throw FileNotFoundException("Cache entry not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        val latestLink = cacheKeyDir.resolve("latest")
        if (!Files.exists(latestLink)) {
            logger.debug(
                "Latest symlink not found for cacheId: {}, cacheKey: {}",
                cacheId, cacheKey
            )
            throw FileNotFoundException("Latest cache entry not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        val actualFile = cacheKeyDir.resolve(Files.readSymbolicLink(latestLink))

        if (!Files.exists(actualFile)) {
            logger.debug(
                "Cache file not found for cacheId: {}, cacheKey: {}, file: {}",
                cacheId, cacheKey, actualFile
            )
            throw FileNotFoundException("Cache file not found for cacheId: $cacheId, cacheKey: $cacheKey")
        }

        return PathResource(actualFile)
    }
}

fun interface StoreInCache : (CacheId, CacheKey, Resource) -> Boolean {
    /**
     * @return true if the cache entry was stored successfully, false otherwise
     */
    override fun invoke(cacheId: CacheId, cacheKey: CacheKey, content: Resource): Boolean
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

    override fun invoke(cacheId: CacheId, cacheKey: CacheKey, content: Resource): Boolean {
        var tempFile: Path? = null
        try {
            val cacheKeyDir = cacheDirectory.resolveDir(cacheId, cacheKey)
            if (!Files.exists(cacheKeyDir)) {
                Files.createDirectories(cacheKeyDir)
            }

            tempFile = cacheKeyDir.createTempFile()

            val hash = content.writeTo(tempFile)

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
            logger.error(
                "Error storing cache entry for cacheId: {}, cacheKey: {}",
                cacheId, cacheKey, e
            )
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

    fun resolveDir(cacheId: CacheId, cacheKey: CacheKey): Path =
        cacheDirectory.resolve(cacheId.id).resolve(cacheKey.id)
}
