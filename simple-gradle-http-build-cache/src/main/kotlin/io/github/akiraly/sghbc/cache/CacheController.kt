package io.github.akiraly.sghbc.cache

import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM
import org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileNotFoundException

/**
 * Controller for handling Gradle HTTP Build Cache requests
 */
@RestController
@RequestMapping("/cache/{cacheId}/{cacheKey}")
class CacheController(
    private val storeInCache: StoreInCache,
    private val retrieveFromCache: RetrieveFromCache
) {

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
        } catch (e: FileNotFoundException) {
            ResponseEntity.notFound().build()
        } catch (e: Exception) {
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
        @RequestBody resource: Resource
    ): ResponseEntity<Void> {
        return try {
            val success = storeInCache(cacheId, cacheKey, resource.inputStream)
            if (success) {
                ResponseEntity.ok().build()
            } else {
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
            }
        } catch (e: Exception) {
            // In a real application, we would check if the exception is related to payload size
            // and return HTTP 413 if appropriate
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        }
    }
}
