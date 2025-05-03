package io.github.akiraly.sghbc.cache

import org.slf4j.LoggerFactory
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileNotFoundException
import java.io.IOException

/**
 * Controller for handling Gradle HTTP Build Cache requests
 */
@RestController
@RequestMapping("/cache/{cacheId}/{cacheKey}")
class CacheController(
    private val storeInCache: StoreInCache,
    private val retrieveFromCache: RetrieveFromCache
) {
    private val logger = LoggerFactory.getLogger(CacheController::class.java)

    /**
     * Retrieves a cache entry
     *
     * @param cacheId The cache ID used for segregating caches
     * @param cacheKey The Gradle cache key
     * @return The cache entry as an input stream
     */
    @GetMapping(produces = [APPLICATION_OCTET_STREAM_VALUE])
    fun getCache(
        @PathVariable cacheId: CacheId,
        @PathVariable cacheKey: CacheKey
    ): ResponseEntity<ByteArray> {
        return try {
            val cacheData = retrieveFromCache(cacheId, cacheKey)
            val bytes = cacheData.readAllBytes()
            ResponseEntity.ok()
                .contentType(APPLICATION_OCTET_STREAM)
                .body(bytes)
        } catch (_: FileNotFoundException) {
            logger.debug("Cache entry not found for cacheId: {}, cacheKey: {}", cacheId, cacheKey)
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
            logger.error(
                "Error retrieving cache entry for cacheId: {}, cacheKey: {}",
                cacheId,
                cacheKey,
                e
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }

    /**
     * Stores a cache entry
     *
     * @param cacheId The cache ID used for segregating caches
     * @param cacheKey The Gradle cache key
     * @param resource The resource containing the cache entry data
     * @return HTTP 200 if successful, HTTP 413 if the payload is too large, or HTTP 500 for other errors
     */
    @PutMapping(consumes = [APPLICATION_OCTET_STREAM_VALUE])
    fun putCache(
        @PathVariable cacheId: CacheId,
        @PathVariable cacheKey: CacheKey,
        @RequestHeader headers: HttpHeaders,
        @RequestBody resource: Resource
    ): ResponseEntity<Void> {
        // Check for Expect: 100-continue header
        // Spring automatically handles the 100-continue response if this header is present

        return try {
            // Check if the payload is too large (for example, if it exceeds a configured limit)
            // This is a simplified example; in a real application, you would check against a configured limit
            val contentLength = headers.contentLength
            val maxAllowedSize = 100 * 1024 * 1024 // 100MB example limit

            if (contentLength > maxAllowedSize) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build()
            }

            val success = storeInCache(cacheId, cacheKey, resource.inputStream)
            if (success) {
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        } catch (e: IOException) {
            // Check if the exception is related to payload size
            if (e.message?.contains("too large", ignoreCase = true) == true) {
                logger.warn("Payload too large for cacheId: {}, cacheKey: {}", cacheId, cacheKey, e)
                ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).build()
            } else {
                logger.error(
                    "IO error storing cache entry for cacheId: {}, cacheKey: {}",
                    cacheId,
                    cacheKey,
                    e
                )
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        } catch (e: Exception) {
            logger.error(
                "Error storing cache entry for cacheId: {}, cacheKey: {}",
                cacheId,
                cacheKey,
                e
            )
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
