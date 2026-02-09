package com.rs.myvocabulary.sync

import com.google.gson.Gson
import com.rs.myvocabulary.api.HttpHelper
import com.rs.myvocabulary.database.*
import kotlinx.coroutines.delay

data class ApiException(
        override val message: String,
        val statusCode: Int? = null,
        val errorBody: String? = null
) : Exception(message)

class NoConnectionException : Exception("No internet connection")

/** Generic class for pushing entities to the server. */
class PushEntityJob<T>(
        private val endpoint: String,
        private val getUnsynced: suspend () -> List<T>,
        private val getUid: (T) -> String,
        private val updateSyncStatus: (uid: String) -> Unit,
        private val isConnected: () -> Boolean,
        private val entityName: String = "Item"
) {
        private val httpHelper = HttpHelper.getInstance()
        private val gson = Gson()
        private val maxRetries = 5

        @Volatile private var isStopped = false

        fun stop() {
                isStopped = true
        }

        suspend fun startPushing() {
                isStopped = false
                if (!isConnected()) return

                val items = getUnsynced()
                if (items.isEmpty()) return

                items.forEach { item ->
                        if (isStopped || !isConnected()) return
                        val uid = getUid(item)
                        try {
                                tryWithRetry(maxRetries) {
                                        val json = gson.toJson(item)
                                        val response = httpHelper.put("$endpoint/$uid", json)
                                        if (response.statusCode in 200..299) {
                                                updateSyncStatus(uid)
                                        } else {
                                                throw ApiException(
                                                        "Failed to push $entityName: ${response.statusCode}"
                                                )
                                        }
                                }
                        } catch (e: Exception) {
                                println("Error syncing $entityName ($uid): ${e.message}")
                        }
                }
        }

        private suspend fun tryWithRetry(max: Int, block: suspend () -> Unit) {
                var retryCount = 0
                while (retryCount < max && !isStopped) {
                        try {
                                if (!isConnected()) throw NoConnectionException()
                                block()
                                return
                        } catch (e: NoConnectionException) {
                                throw e
                        } catch (e: Exception) {
                                retryCount++
                                if (retryCount >= max) throw e
                                delay(2000L * retryCount)
                        }
                }
        }
}

/**
 * Simplified push job that only syncs words with embedded categories, tags, attachments, and
 * comments
 */
class PushWordJob(
        private val getUnsyncedWords: suspend () -> List<Word>,
        private val updateWordSyncStatus: (wordId: String, retryCount: Int) -> Unit,
        private val isConnected: () -> Boolean
) {
        private val job =
                PushEntityJob(
                        endpoint = "/api/v2/word",
                        getUnsynced = getUnsyncedWords,
                        getUid = { it.uid },
                        updateSyncStatus = { updateWordSyncStatus(it, 1) },
                        isConnected = isConnected,
                        entityName = "Word"
                )
        fun stop() = job.stop()
        suspend fun startPushing() = job.startPushing()
}
