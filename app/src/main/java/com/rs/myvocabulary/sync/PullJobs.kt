package com.rs.myvocabulary.sync

import com.google.gson.reflect.TypeToken
import com.rs.myvocabulary.database.*

class PullWordJob(
        private val saveNotes: (List<WordPartial>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<WordPartial>(
                    endpoint = "/api/v2/word/pull",
                    getLastSyncTime = { SyncManager.getLastSyncTime() },
                    setLastSyncTime = { SyncManager.setLastSyncTime(it) },
                    saveItems = saveNotes,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<WordPartial>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt ?: 0L },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullTagJob(
        private val saveTags: (List<Tag>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<Tag>(
                    endpoint = "/api/v2/tag/pull",
                    getLastSyncTime = { SyncManager.getTagLastSyncTime() },
                    setLastSyncTime = { SyncManager.setTagLastSyncTime(it) },
                    saveItems = saveTags,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<Tag>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullCategoryJob(
        private val saveCategories: (List<Label>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<Label>(
                    endpoint = "/api/v2/category/pull",
                    getLastSyncTime = { SyncManager.getCategoryLastSyncTime() },
                    setLastSyncTime = { SyncManager.setCategoryLastSyncTime(it) },
                    saveItems = saveCategories,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<Label>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullCommentJob(
        private val saveComments: (List<Comment>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<Comment>(
                    endpoint = "/api/v2/comment/pull",
                    getLastSyncTime = { SyncManager.getCommentLastSyncTime() },
                    setLastSyncTime = { SyncManager.setCommentLastSyncTime(it) },
                    saveItems = saveComments,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<Comment>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullPostCommentJob(
        private val savePostComments: (List<PostComment>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<PostComment>(
                    endpoint = "/api/v2/post-comment/pull",
                    getLastSyncTime = { SyncManager.getPostCommentLastSyncTime() },
                    setLastSyncTime = { SyncManager.setPostCommentLastSyncTime(it) },
                    saveItems = savePostComments,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<PostComment>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullPostTagJob(
        private val savePostTags: (List<PostTag>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<PostTag>(
                    endpoint = "/api/v2/word-tag/pull",
                    getLastSyncTime = { SyncManager.getPostTagLastSyncTime() },
                    setLastSyncTime = { SyncManager.setPostTagLastSyncTime(it) },
                    saveItems = savePostTags,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<PostTag>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}

class PullNoteCategoryJob(
        private val saveNoteCategories: (List<NoteCategory>) -> Unit,
        private val isConnected: () -> Boolean,
        private val onSyncComplete: () -> Unit = {}
) {
    private val job =
            PullEntityJob<NoteCategory>(
                    endpoint = "/api/v2/word-category/pull",
                    getLastSyncTime = { SyncManager.getNoteCategoryLastSyncTime() },
                    setLastSyncTime = { SyncManager.setNoteCategoryLastSyncTime(it) },
                    saveItems = saveNoteCategories,
                    isConnected = isConnected,
                    typeToken = object : TypeToken<PullJobResponse<NoteCategory>>() {}.type,
                    getItemUpdatedAt = { it.updatedAt },
                    onSyncComplete = onSyncComplete
            )
    fun stop() = job.stop()
    suspend fun startPulling() = job.startPulling()
}
