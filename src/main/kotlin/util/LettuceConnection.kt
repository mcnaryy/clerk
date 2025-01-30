package net.hellz.utils

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.RedisClient
import io.lettuce.core.RedisURI
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import io.lettuce.core.api.sync.RedisCommands
import kotlinx.coroutines.runBlocking

object LettuceConnection {
    private val client: RedisClient
    private val syncCommands: RedisCommands<String, String>
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    private val asyncCommands: RedisCoroutinesCommands<String, String>

    init {
        val uri = RedisURI.Builder
            .redis("redis-16900.c281.us-east-1-2.ec2.redns.redis-cloud.com", 16900)
            .withPassword("1bTRP8jZe9Bb46XtlqVi6ADpEeasudS8".toCharArray())
            .withAuthentication("default", "1bTRP8jZe9Bb46XtlqVi6ADpEeasudS8".toCharArray())
            .build()

        client = RedisClient.create(uri)
        val connection = client.connect()

        syncCommands = connection.sync()
        @OptIn(ExperimentalLettuceCoroutinesApi::class)
        asyncCommands = connection.coroutines()
    }

    // Ping Redis to verify connection
    fun ping(): String {
        return syncCommands.ping()
    }

    // Async get operation
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun get(key: String): String? {
        return asyncCommands.get(key)
    }

    // Async set with TTL
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun setex(key: String, value: String, ttl: Long) {
        asyncCommands.setex(key, ttl, value)
    }

    // Async delete key
    @OptIn(ExperimentalLettuceCoroutinesApi::class)
    suspend fun delete(key: String) {
        asyncCommands.del(key)
    }

    // Shutdown Redis client
    fun shutdown() {
        client.shutdown()
    }
}