package com.rs.ownvocabulary.sync

import kotlinx.coroutines.delay
import com.google.gson.Gson
import com.rs.ownvocabulary.api.HttpHelper
import com.rs.notenet.sync.NoConnectionException
import com.rs.notenet.sync.SyncManager
import com.rs.ownvocabulary.database.Word


data class PullNoteJobResponse(
    val status: String,
    val data: List<Word>,
    val hasMore: Boolean,
)

class PullWordJob(
    private val saveNotes: (List<Word>) -> Unit,
    private val isConnected: () -> Boolean,
    private val onSyncComplete: () -> Unit = {}
) {
    private val httpHelper = HttpHelper.getInstance()
    private val gson = Gson()

    @Volatile
    private var isStopped = false

    fun stop() {
        isStopped = true
    }

    suspend fun startPulling() {
        if (isStopped || !isConnected()) return

        do {
            val lastTime = SyncManager.getLastSyncTime()
            val lastId = SyncManager.getLastId()
            val startTime = System.currentTimeMillis()

            println("lastTime ${lastTime} lastId ${lastId}")

            val response = tryWithRetry {
                if (!isConnected()) throw NoConnectionException()
                httpHelper.get("/api/v2/diary/pull?since=$lastTime&last_id=$lastId")
            }
            if (response.statusCode != 200) break
            val data = parseNotes(response.body ?: "")

            if (data.data.isNotEmpty()) {
                val lastNote = data.data.last()
                SyncManager.updateLastSyncTime(lastNote.updatedAt)
                SyncManager.updateLastId(lastNote.uid)
                println("update:: ${data.data.size}")
                saveNotes(data.data)
            }

            if (!data.hasMore || data.data.isEmpty()) {
                onSyncComplete()
                break
            }

            delay(2000L)
        } while (data.hasMore && !isStopped && (System.currentTimeMillis() - startTime) < 30000)

    }

    private fun parseNotes(json: String): PullNoteJobResponse {
        try {
            return gson.fromJson(json, PullNoteJobResponse::class.java)
        } catch (e: Exception) {
            println(e)
            return PullNoteJobResponse(
                status = "failed",
                data = emptyList(),
                hasMore = false
            )
        }
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