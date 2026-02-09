package com.rs.myvocabulary.sync

import com.google.gson.reflect.TypeToken
import com.rs.myvocabulary.database.*

/**
 * Simplified pull job that only syncs words with embedded categories, tags, attachments, and
 * comments
 */
class PullWordJob(
        private val saveWords: (List<WordPartial>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
        private val job =
                PullEntityJob<WordPartial>(
                        endpoint = "/api/v2/word/pull",
                        getLastSyncTime = { SyncManager.getLastSyncTime() },
                        setLastSyncTime = { SyncManager.setLastSyncTime(it) },
                        saveItems = saveWords,
                        isConnected = isConnected,
                        typeToken = object : TypeToken<PullJobResponse<WordPartial>>() {}.type,
                        getItemUpdatedAt = { it.updatedAt ?: 0L },
                        onSyncComplete = onSyncComplete
                )
        fun stop() = job.stop()
        suspend fun startPulling() = job.startPulling()
}
