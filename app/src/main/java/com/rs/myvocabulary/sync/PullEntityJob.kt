package com.rs.myvocabulary.sync

import com.google.gson.Gson
import com.rs.myvocabulary.api.HttpHelper
import java.lang.reflect.Type
import kotlinx.coroutines.delay

data class PullJobResponse<T>(val status: String, val data: List<T>, val hasMore: Boolean)

class PullEntityJob<T>(
        private val endpoint: String,
        private val getLastSyncTime: () -> Long,
        private val setLastSyncTime: (Long) -> Unit,
        private val saveItems: (List<T>) -> Unit,
        private val isConnected: () -> Boolean,
        private val typeToken: Type,
        private val getItemUpdatedAt: (T) -> Long,
        private val onSyncComplete: () -> Unit = {}
) {
    private val httpHelper = HttpHelper.getInstance()
    private val gson = Gson()

    @Volatile private var isStopped = false

    fun stop() {
        isStopped = true
    }

    suspend fun startPulling() {
        if (isStopped || !isConnected()) return

        do {
            val lastTime = getLastSyncTime()
            val startTime = System.currentTimeMillis()

            val response =
                    try {
                        tryWithRetry {
                            if (!isConnected()) throw NoConnectionException()
                            httpHelper.get("$endpoint?since=$lastTime")
                        }
                    } catch (e: Exception) {
                        println("Pull error for $endpoint: ${e.message}")
                        break
                    }

            if (response.statusCode != 200) break

            val data =
                    try {
                        gson.fromJson<PullJobResponse<T>>(response.body ?: "", typeToken)
                    } catch (e: Exception) {
                        println("Parse error for $endpoint: ${e.message}")
                        break
                    }

            if (data.data.isNotEmpty()) {
                try {
                    saveItems(data.data)
                    val lastItem = data.data.last()
                    val lastItemUpdatedAt = getItemUpdatedAt(lastItem)
                    setLastSyncTime(lastItemUpdatedAt)
                } catch (e: Exception) {
                    println("Save error for $endpoint: ${e.message}")
                }
            }

            if (!data.hasMore || data.data.isEmpty()) {
                onSyncComplete()
                break
            }

            delay(2000L)
        } while (data.hasMore && !isStopped && (System.currentTimeMillis() - startTime) < 30000)
    }

    private suspend fun <T> tryWithRetry(
            maxRetries: Int = 3,
            initialDelay: Long = 1000,
            maxDelay: Long = 10000,
            block: suspend () -> T
    ): T {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            try {
                return block()
            } catch (e: Exception) {
                if (attempt == maxRetries - 1 || isStopped) throw e
                delay(currentDelay)
                currentDelay = minOf(currentDelay * 2, maxDelay)
            }
        }
        throw IllegalStateException("Unexpected error in retry logic")
    }
}
