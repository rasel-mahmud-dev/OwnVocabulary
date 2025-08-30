package com.rs.ownvocabulary.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rs.ownvocabulary.database.SyncStatus
import com.rs.ownvocabulary.database.Word
import com.rs.ownvocabulary.database.WordDatabase
import com.rs.ownvocabulary.database.WordPartial
import com.rs.ownvocabulary.sync.PullWordJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.application
import com.rs.ownvocabulary.database.AIResponseDatabase
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.database.SortOrder
import com.rs.ownvocabulary.sync.PushWordJob
import com.rs.ownvocabulary.utils.DeviceId
import kotlinx.coroutines.delay

data class CurrentUser(
    val userId: String,
    val username: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val db = WordDatabase.getInstance(application)
    val aiResponseDb = AIResponseDatabase.getInstance(application)

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    sealed class Event {
        data class ShowToast(val message: String) : Event()
    }

    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    private val _words = MutableStateFlow<List<Word>>(emptyList())
    val words: StateFlow<List<Word>> = _words.asStateFlow()

    private val _totalWord = MutableStateFlow<Int>(0)
    val totalWord: StateFlow<Int> = _totalWord.asStateFlow()

    private val _openAddWordDialog = MutableStateFlow<Boolean>(false)
    val openAddWordDialog: StateFlow<Boolean> = _openAddWordDialog.asStateFlow()

    private var activeSyncJob: Job? = null

    private var activePullSyncJob: Job? = null

    private var activePullNoteSyncJob: Job? = null

    fun loadWords() {
        viewModelScope.launch {
            db.getAllWords(SortOrder.WordAsc) {
                _words.value = it

            }
        }
    }

    fun setAddWordDialog(state: Boolean) {
        _openAddWordDialog.value = state
    }

    fun loadTotalWordCount() {
        viewModelScope.launch {
            db.totalWordCount {
                _totalWord.value = it
            }
        }
    }

    fun getItemByUid(uid: String, cb: (item: Word?) -> Unit) {
        viewModelScope.launch {
            db.getWordByUid(uid) {
                cb(it)
            }
        }
    }

    fun addWord(newWord: Word, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                db.insertWord(newWord)
                cb(null)
                loadWords()
                startWordSync()
            } catch (ex: Exception) {
                println(ex?.message)
                cb(ex.message)
            }
        }
    }

    fun addAiResponse(newWord: AIResponseItem, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                aiResponseDb.insert(newWord)
                cb(null)
            } catch (ex: Exception) {
                println(ex?.message)
                cb(ex.message)
            }
        }
    }

    fun loadAiGeneratedResponse(word: String, cb: (items: List<AIResponseItem>) -> Unit) {
        viewModelScope.launch {
            try {
                aiResponseDb.getAllByInput(word) {
                    cb(it)
                }

            } catch (ex: Exception) {
                println(ex?.message)
            }
        }
    }

    fun updatePartial(wordPartial: WordPartial, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val result = db.updatePartial(wordPartial)
                cb(null)
                delay(10000L)
                startWordSync()
                println("update partal result $result")
            } catch (ex: Exception) {
                println("error: $ex")
                cb(ex.message)
            }
        }
    }

    fun incrementViewCount(uid: String) {
        viewModelScope.launch {
            try {
                val result = db.incrementViewCount(uid)
                println("update partal result $result")
            } catch (ex: Exception) {
                println("error: $ex")
            }
        }
    }

    init {

        _currentUser.value = CurrentUser(DeviceId.getDeviceId(application), "TestUser")

        println("has nwet ${isNetworkAvailable(application)}")

        loadWords()
        loadTotalWordCount()
    }

    fun startWordSync() {
        activeSyncJob?.cancel()
        activeSyncJob = viewModelScope.launch {
            try {
                val unsyncedNotes = db.getUnsyncedWords()
                println("Total ${unsyncedNotes.size} notes to sync")

                if (unsyncedNotes.isEmpty()) {
                    println("no need to sync")
//                    _syncStatus.value = "Everything is synced"
                    return@launch
                }

//                _syncStatus.value = "Syncing ${unsyncedNotes.size} notes..."

                val pushJob = PushWordJob(
                    isConnected = { isNetworkAvailable(application) },
                    getUnsyncedNotes = { unsyncedNotes },
                    updateNoteSyncStatus = { id, status, retryCount ->
                        db.updateWordSyncStatus(id, status, retryCount)
                    }
                )

                pushJob.startPushing()
//                _syncStatus.value = "Sync completed successfully"

            } catch (e: Exception) {
//                _syncStatus.value = "Sync failed: ${e.message}"
            }
        }
    }

    fun pullWordFromServer() {
        activePullSyncJob?.cancel()
        activePullSyncJob = viewModelScope.launch {
            try {
                println("pulling words")
                val pullJob = PullWordJob(
                    isConnected = { isNetworkAvailable(application) },
                    saveNotes = { notes ->
                        db.upsertWord(notes)
                        println("notes saved")
                    },

                    onSyncComplete = {
                        println("done syncing")
                        loadWords()
                    }
                )

                pullJob.startPulling()

            } catch (e: Exception) {
                println("Sync failed: ${e.message}")
            }
        }
    }

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

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && (
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                )


    }


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