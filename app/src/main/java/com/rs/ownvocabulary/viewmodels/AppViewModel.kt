package com.rs.ownvocabulary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


class AppViewModel(application: Application) : AndroidViewModel(application) {

    val db = WordDatabase.getInstance(application)

    sealed class Event {
        data class ShowToast(val message: String) : Event()
    }

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()


    private var activeSyncJob: Job? = null

    private var activePullSyncJob: Job? = null

    private var activePullNoteSyncJob: Job? = null

    fun loadWords(){
        viewModelScope.launch {
            db.getAllWords {
                _words.value = it

            }
        }
    }

    fun addWord(newWord: Word){
        viewModelScope.launch {
            db.insertWord(newWord)
            loadWords()
        }
    }

    init {
        loadWords()
    }

//    fun startNoteSync() {
//        activeSyncJob?.cancel()
////        _syncStatus.value = "Starting sync..."
//
//        activeSyncJob = viewModelScope.launch {
//            try {
//                val unsyncedNotes = noteDb.getUnsyncedNotes()
//                if (unsyncedNotes.isEmpty()) {
//                    println("no need to sync")
////                    _syncStatus.value = "Everything is synced"
//                    return@launch
//                }
//
//                println("Total ${unsyncedNotes.size} notes to sync")
//
////                _syncStatus.value = "Syncing ${unsyncedNotes.size} notes..."
//
//                val pushJob = PushNoteJob(
//                    isConnected = { isNetworkAvailable() },
//                    getUnsyncedNotes = { unsyncedNotes },
//                    updateNoteSyncStatus = { id, status, retryCount ->
//                        noteDb.updateNoteSyncStatus(id, status, retryCount)
//                    }
//                )
//
//                pushJob.startPushing()
////                _syncStatus.value = "Sync completed successfully"
//
//            } catch (e: Exception) {
////                _syncStatus.value = "Sync failed: ${e.message}"
//            }
//        }
//    }
//
//
//    fun pullNoteFromServer() {
//        activePullSyncJob?.cancel()
////        _syncStatus.value = "Starting sync..."
//
//        activePullSyncJob = viewModelScope.launch {
//            try {
//                println("pulling notes")
//                setPulling(true)
//                val pullJob = PullNotesJob(
//                    isConnected = { isNetworkAvailable() },
//                    saveNotes = { notes ->
//                        noteDb.upsertNotes(notes)
//                        println("notes saved")
//                    },
//
//                    onSyncComplete = {
//                        println("done syncing")
//                        setPulling(false)
//                    }
//                )
//
//                pullJob.startPulling()
//
//            } catch (e: Exception) {
//                println("Sync failed: ${e.message}")
//            }
//        }
//    }
//
    private fun updateProgress(totalNotes: Int, status: SyncStatus) {
//        val currentState = _syncStatus.value
//        if (currentState is SyncState.InProgress) {
//            val processedCount = if (status == SyncStatus.SYNCED) currentState.current + 1 else currentState.current
//            _syncStatus.postValue(
//                currentState.copy(
//                    current = processedCount,
//                    message = "Syncing $processedCount/$totalNotes"
//                )
//            )
//        }
    }

//    private fun isNetworkAvailable(): Boolean {
//        return true
//        val connectivityManager = getApplication<Application>()
//            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//        val networkInfo = connectivityManager.activeNetworkInfo
//        return networkInfo?.isConnectedOrConnecting == true
//    }

    fun cancelSync() {
        activeSyncJob?.cancel()
//        _syncStatus.postValue(SyncState.Cancelled("Sync cancelled by user"))
    }

    override fun onCleared() {
        super.onCleared()
//        coroutineScope.cancel()
    }
}
//
//sealed class SyncState {
//    data class InProgress(val current: Int, val message: String) : SyncState()
//    data class Completed(val message: String) : SyncState()
//    data class Error(val message: String, val exception: Exception? = null) : SyncState()
//    data class Cancelled(val message: String) : SyncState()
//}