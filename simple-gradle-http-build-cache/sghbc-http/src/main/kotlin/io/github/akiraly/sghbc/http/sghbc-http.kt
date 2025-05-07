package io.github.akiraly.sghbc.http

import io.github.akiraly.sghbc.domain.CacheEntry
import io.github.akiraly.sghbc.domain.CacheEntryId
import io.github.akiraly.sghbc.domain.CacheId
import io.github.akiraly.sghbc.domain.GradleCacheKey
import io.github.akiraly.sghbc.domain.RetrieveFromCache
import io.github.akiraly.sghbc.domain.StoreInCache
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RestController
import java.io.FileNotFoundException
import java.io.IOException

@RestController
class HttpGetCacheEntry(
    private val retrieveFromCache: RetrieveFromCache
) {
    private val logger = LoggerFactory.getLogger(HttpGetCacheEntry::class.java)

    /**
     * Retrieves a cache entry
     *
     * @param cacheId The cache ID used for segregating caches
     * @param gradleCacheKey The Gradle cache key
     * @return The cache entry
     */
    @GetMapping(
        "/cache/{cacheId:[0-9a-z_-]{8,64}}/{gradleCacheKey:[0-9a-f]{40}}",
        produces = [APPLICATION_OCTET_STREAM_VALUE]
    )
    operator fun invoke(
        @PathVariable cacheId: CacheId,
        @PathVariable gradleCacheKey: GradleCacheKey
    ): ResponseEntity<Resource> {
        val id = CacheEntryId(cacheId, gradleCacheKey)
        return try {
            val cacheEntry = retrieveFromCache(id)
            ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(cacheEntry.resource)
        } catch (_: FileNotFoundException) {
            logger.debug("Cache entry not found for {}", id)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error("Error retrieving cache entry for {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}

@RestController
class HttpPutCacheEntry(
    private val storeInCache: StoreInCache,

    @param:Value("\${io.github.akiraly.sghbc.http.maxAllowedSizeInMb:100}")
    private val maxAllowedSizeInMb: Long
) {
    private val logger = LoggerFactory.getLogger(HttpPutCacheEntry::class.java)

    /**
     * Stores a cache entry
     *
     * @param cacheId The cache ID used for segregating caches
     * @param gradleCacheKey The Gradle cache key
     * @param headers The HTTP headers from the request
     * @param resource The resource containing the cache entry data
     * @return HTTP 200 if successful, HTTP 413 if the payload is too large, or HTTP 500 for other errors
     */
    @PutMapping(
        "/cache/{cacheId:[0-9a-z_-]{8,64}}/{gradleCacheKey:[0-9a-f]{40}}",
        consumes = [APPLICATION_OCTET_STREAM_VALUE]
    )
    operator fun invoke(
        @PathVariable cacheId: CacheId,
        @PathVariable gradleCacheKey: GradleCacheKey,
        @RequestHeader headers: HttpHeaders,
        @RequestBody resource: Resource
    ): ResponseEntity<Void> {
        val id = CacheEntryId(cacheId, gradleCacheKey)
        return try {
            val contentLength = headers.contentLength
            val maxAllowedSize = maxAllowedSizeInMb * 1024 * 1024

            if (contentLength > maxAllowedSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build()
            }

            val success = storeInCache(id, CacheEntry(id, resource))
            if (success) {
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        } catch (e: IOException) {
            // Check if the exception is related to payload size
            if (e.message?.contains("too large", ignoreCase = true) == true) {
                logger.warn("Payload too large for {}", id, e)
                ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build()
            } else {
                logger.error("IO error storing cache entry for {}", id, e)
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        } catch (e: Exception) {
            logger.error("Error storing cache entry for {}", id, e)
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
