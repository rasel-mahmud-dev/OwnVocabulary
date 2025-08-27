package com.rs.ownvocabulary.sync

import com.google.gson.Gson
import com.rs.ownvocabulary.api.HttpHelper
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word

import kotlinx.coroutines.delay

data class ApiException(
    override val message: String,
    val statusCode: Int? = null,
    val errorBody: String? = null
) : Exception(message) {
    constructor(statusCode: Int, errorBody: String?) : this(
        message = "API request failed with status $statusCode",
        statusCode = statusCode,
        errorBody = errorBody
    )
}

/**
 * Represents the synchronization status of a note or sync operation
 */


class PushWordJob(
    private val getUnsyncedNotes: () -> List<Word>,
    private val updateNoteSyncStatus: (noteId: String, status: SyncStatus, retryCount: Int) -> Unit,
    private val isConnected: () -> Boolean
) {
    private val httpHelper = HttpHelper.getInstance()
    private val gson = Gson()
    private val maxRetries = 10

    @Volatile
    private var isStopped = false // Flag to control syncing

    // Call this to stop syncing from outside
    fun stop() {
        isStopped = true
    }

    suspend fun startPushing() {
        // Reset stop flag when starting
        isStopped = false

        // Check connectivity before starting
        if (!isConnected()) {
            println("No internet connection - sync aborted")
            return
        }

        val unsyncedNotes = getUnsyncedNotes()
        if (unsyncedNotes.isEmpty()) {
            println("Nothing has to sync.")
            return
        }

        unsyncedNotes.forEachIndexed { index, note ->
            // Check if stopped or disconnected before each note
            if (isStopped || !isConnected()) {
                println("Sync stopped - no connection or manual stop")
                return
            }

            tryWithRetry(maxRetries) { retryCount ->
                // Check connection before each retry
                if (!isConnected()) {
                    println("Lost connection during sync")
                    throw NoConnectionException()
                }

                createNote(note, note.id != 0L)

            }
        }
    }

    private suspend fun createNote(note: Word, isUpdate: Boolean) {
        if (!isConnected()) throw NoConnectionException()
        println("pushing word -> ${note.word}")
        if (!isConnected()) throw NoConnectionException()
        val noteJson = gson.toJson(note)
        val response = httpHelper.put("/api/v2/word/${note.uid}", noteJson)
        println(response)
        if (response.statusCode in 200..299) {
            updateNoteSyncStatus(note.uid, SyncStatus.SYNCED, 1)
        } else {
            println("Failed to create word: ${response.statusCode} is Update -> ${isUpdate}")
            throw ApiException("Failed to create word: ${response.statusCode}")
        }
    }

    private suspend fun tryWithRetry(max: Int, block: suspend (retryCount: Int) -> Unit) {
        var retryCount = 0
        while (retryCount < max && !isStopped) {
            try {
                block(retryCount)
                return
            } catch (e: NoConnectionException) {
                // Don't retry if there's no connection
                throw e
            } catch (e: Exception) {
                retryCount++
                if (retryCount >= max) throw e
                val delayedTime = 2000L * retryCount
                println("retryCount $retryCount delayedTime $delayedTime")
                delay(delayedTime)
            }
        }
    }
}

class NoConnectionException : Exception("No internet connection")