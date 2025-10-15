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
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.application
import com.rs.ownvocabulary.api.OnboardResponseData
import com.rs.ownvocabulary.api.WordApi
import com.rs.ownvocabulary.database.AIResponseDatabase
import com.rs.ownvocabulary.database.AIResponseItem
import com.rs.ownvocabulary.database.SortOrder
import com.rs.ownvocabulary.screens.ProfileScreen
import com.rs.ownvocabulary.sync.PushWordJob
import com.rs.ownvocabulary.sync.SyncManager
import com.rs.ownvocabulary.utils.DeviceId
import kotlinx.coroutines.delay

data class CurrentUser(
    val userId: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatar: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val db = WordDatabase.getInstance(application)
    val aiResponseDb = AIResponseDatabase.getInstance(application)

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    private val _openAddWordDialog = MutableStateFlow<Boolean>(false)
    val openAddWordDialog: StateFlow<Boolean> = _openAddWordDialog.asStateFlow()

    private val _longPressItem = MutableStateFlow<Word?>(null)
    val longPressItem: StateFlow<Word?> = _longPressItem.asStateFlow()

    private var activeSyncJob: Job? = null

    private var activePullSyncJob: Job? = null


    /* discover vieowmod */

    private val _totalWordsCount = MutableStateFlow(0)
    val totalWordsCount: StateFlow<Int> = _totalWordsCount.asStateFlow()

    private val _discoverWords = MutableStateFlow<List<Word>>(emptyList())
    val discoverWords: StateFlow<List<Word>> = _discoverWords.asStateFlow()

    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMoreData = MutableStateFlow(true)
    val hasMoreData: StateFlow<Boolean> = _hasMoreData.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _sortBy = MutableStateFlow("newest")
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    private var currentPage = 0
    private val pageSize = 35

    fun loadCommunityWords(loadMore: Boolean = false) {
        viewModelScope.launch {
            try {
                if (loadMore) {
                    if (_isLoadingMore.value || !_hasMoreData.value) return@launch
                    _isLoadingMore.value = true
                    currentPage++
                } else {
                    currentPage = 0
                    _discoverWords.value = emptyList()
                    _hasMoreData.value = true
                    loadTotalWordsCount()
                }

                db.getAllWordsExceptOwn(
                    authId = currentUser.value?.userId ?: "",
                    limit = pageSize,
                    offset = currentPage * pageSize,
                    searchQuery = _searchQuery.value,
                    sortBy = _sortBy.value
                ) { newWords ->
                    println("Loaded ${newWords.size} words")

                    if (newWords.size < pageSize) {
                        _hasMoreData.value = false
                    }

                    if (loadMore) {
                        _discoverWords.value = _discoverWords.value + newWords
                    } else {
                        _discoverWords.value = newWords
                    }

                    _isLoadingMore.value = false
                }

            } catch (e: Exception) {
                println("Error loading community words: $e")
                _isLoadingMore.value = false
            }
        }
    }

    private fun loadTotalWordsCount() {
        viewModelScope.launch {
            try {
                db.getTotalWordsCountExceptOwn(
                    authId = currentUser.value?.userId ?: "",
                    searchQuery = _searchQuery.value
                ) { count ->
                    _totalWordsCount.value = count
                }
            } catch (e: Exception) {
                println("Error loading total count: $e")
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        resetPagination()
        loadCommunityWords()
    }

    fun updateSortBy(sortBy: String) {
        _sortBy.value = sortBy
        resetPagination()
        loadCommunityWords()
    }

    fun clearSearch() {
        _searchQuery.value = ""
        resetPagination()
        loadCommunityWords()
    }

    fun resetPagination() {
        currentPage = 0
        _hasMoreData.value = true
        _isLoadingMore.value = false
    }








    /* favoriteWords & frequentView */

    private val _isInitialLoadFavoriteFrequentItems = MutableStateFlow<Boolean>(false)
    val isInitialLoadFavoriteFrequentItems: StateFlow<Boolean> = _isInitialLoadFavoriteFrequentItems.asStateFlow()

    private val _favoriteWords = MutableStateFlow<List<Word>>(emptyList())
    val favoriteWords: StateFlow<List<Word>> = _favoriteWords.asStateFlow()

    private val _frequentViewWords = MutableStateFlow<List<Word>>(emptyList())
    val frequentViewWords: StateFlow<List<Word>> = _frequentViewWords.asStateFlow()

    private var favoriteWordsOffset = 0
    private var frequentViewWordsOffset = 0
    private val favoritePageSize = 20

    fun loadFavoriteWords(loadMore: Boolean = false) {
        viewModelScope.launch {
            try {

                val authId = _currentUser.value?.userId ?: ""

                if (!loadMore) {
                    favoriteWordsOffset = 0
                }

                db.getFavoriteWords(
                    authId = authId, // Make sure you have authId available
                    limit = favoritePageSize,
                    offset = favoriteWordsOffset
                ) { words ->
                    if (loadMore) {
                        _favoriteWords.value = _favoriteWords.value + words
                    } else {
                        _favoriteWords.value = words
                    }

                    if (words.isNotEmpty()) {
                        favoriteWordsOffset += words.size
                    }
                }
            } catch (e: Exception) {
                println("Error loading favorite words: $e")
            }
        }
    }

    fun loadFrequentViewWords(loadMore: Boolean = false) {
        viewModelScope.launch {
            try {

                val authId = _currentUser.value?.userId ?: ""

                if (!loadMore) {
                    frequentViewWordsOffset = 0
                }

                db.getFrequentViewWords(
                    authId = authId, // Make sure you have authId available
                    limit = favoritePageSize,
                    offset = frequentViewWordsOffset
                ) { words ->
                    if (loadMore) {
                        _frequentViewWords.value = _frequentViewWords.value + words
                    } else {
                        _frequentViewWords.value = words
                    }

                    if (words.isNotEmpty()) {
                        frequentViewWordsOffset += words.size
                    }
                }
            } catch (e: Exception) {
                println("Error loading frequent view words: $e")
            }
        }
    }














    /* discover vieowmod end */


    /* my own vocabulary viewmodel */

    private val _totalWordsCountOwn = MutableStateFlow(0)
    val totalWordsCountOwn: StateFlow<Int> = _totalWordsCountOwn.asStateFlow()

    private val _myWords = MutableStateFlow<List<Word>>(emptyList())
    val myWords: StateFlow<List<Word>> = _myWords.asStateFlow()

    private val _isLoadingMoreOwn = MutableStateFlow(false)
    val isLoadingMoreOwn: StateFlow<Boolean> = _isLoadingMoreOwn.asStateFlow()

    private val _hasMoreDataOwn = MutableStateFlow(true)
    val hasMoreDataOwn: StateFlow<Boolean> = _hasMoreDataOwn.asStateFlow()

    private val _searchQueryOwn = MutableStateFlow("")
    val searchQueryOwn: StateFlow<String> = _searchQueryOwn.asStateFlow()

    private val _sortByOwn = MutableStateFlow("newest")
    val sortByOwn: StateFlow<String> = _sortByOwn.asStateFlow()

    private var currentPageOwn = 0
    private val pageSizeOwn = 35

    fun loadOwnWords(loadMore: Boolean = false) {
        viewModelScope.launch {
            try {
                if (loadMore) {
                    if (_isLoadingMoreOwn.value || !_hasMoreDataOwn.value) return@launch
                    _isLoadingMoreOwn.value = true
                    currentPageOwn++
                } else {
                    currentPageOwn = 0
                    _myWords.value = emptyList()
                    _hasMoreDataOwn.value = true
                    loadTotalWordsCountOwn()
                }

                db.getAllWordsOwn(
                    authId = currentUser.value?.userId ?: "",
                    limit = pageSizeOwn,
                    offset = currentPageOwn * pageSizeOwn,
                    searchQuery = _searchQueryOwn.value,
                    sortBy = _sortByOwn.value
                ) { newWords ->
                    println("Loaded ${newWords.size} words")

                    if (newWords.size < pageSize) {
                        _hasMoreDataOwn.value = false
                    }

                    if (loadMore) {
                        _myWords.value = _myWords.value + newWords
                    } else {
                        _myWords.value = newWords
                    }

                    _isLoadingMoreOwn.value = false
                }

            } catch (e: Exception) {
                println("Error loading community words: $e")
                _isLoadingMoreOwn.value = false
            }
        }
    }

    private fun loadTotalWordsCountOwn() {
        viewModelScope.launch {
            try {
                db.getTotalWordsCountOwn(
                    authId = currentUser.value?.userId ?: "",
                    searchQuery = _searchQueryOwn.value
                ) { count ->
                    _totalWordsCountOwn.value = count
                }
            } catch (e: Exception) {
                println("Error loading total count: $e")
            }
        }
    }

    fun updateSearchQueryOwn(query: String) {
        _searchQueryOwn.value = query
        resetPaginationOwn()
        loadOwnWords()
    }

    fun updateSortByOwn(sortBy: String) {
        _sortByOwn.value = sortBy
        resetPaginationOwn()
        loadOwnWords()
    }

    fun clearSearchOwn() {
        _searchQueryOwn.value = ""
        resetPaginationOwn()
        loadOwnWords()
    }

    fun resetPaginationOwn() {
        currentPageOwn = 0
        _hasMoreData.value = true
        _isLoadingMore.value = false
    }

    /* discover vieowmod end */



    fun loadAuth() {
        viewModelScope.launch {
            val auth = SyncManager.getAuth()
            if (auth != null) {
                _currentUser.value = auth
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            if (_currentUser.value?.userId != null) {
//                db.clearUserData(_currentUser.value?.userId!!)
                _myWords.value = emptyList()
                _totalWordsCountOwn.value = 0
                _discoverWords.value = emptyList()
                _totalWordsCount.value = 0
            }
            SyncManager.setAuth(null)
            _currentUser.value = null
            SyncManager.setAuthToken(null)
            // clear data's
        }
    }

    fun setAddWordDialog(state: Boolean) {
        _openAddWordDialog.value = state
    }


    fun getItemByUid(uid: String, cb: (item: Word?) -> Unit) {
        viewModelScope.launch {
            db.getWordByUid(uid) {
                cb(it)
            }
        }
    }

    fun setAuth(authData: OnboardResponseData) {
        val u = CurrentUser(
            authData._id,
            authData.username,
            authData.email,
            authData.fullName,
            authData.avatar
        )
        _currentUser.value = u
        SyncManager.setAuth(u)
        SyncManager.setAuthToken(authData.accessToken)
        loadTotalWordsCount()
        loadOwnWords()
        loadCommunityWords()
    }

    fun addWord(newWord: Word, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                db.insertWord(newWord)
                cb(null)
                startWordSync()
                loadOwnWords()
            } catch (ex: Exception) {
                println(ex?.message)
                cb(ex.message)
            }
        }
    }

    fun setLongPressItem(newWord: Word?) {
        _longPressItem.value = newWord
    }

    fun addAiResponse(newWord: AIResponseItem, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
//                aiRespons_currentUsereDb.insert(newWord)
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

    fun toggleFavorite(wordId: String, isFav: Boolean, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                val userId = _currentUser.value?.userId
                if(userId == null) return@launch

                if (isFav) {
                    db.removeFavorite(userId, wordId)
                } else {
                    db.addFavorite(userId, wordId)
                }

                _myWords.value = _myWords.value.map { word ->
                    if (word.uid == wordId) {
                        word.copy(isFavorite = !isFav)
                    } else {
                        word
                    }
                }

                _discoverWords.value = _discoverWords.value.map { word ->
                    if (word.uid == wordId) {
                        word.copy(isFavorite = !isFav)
                    } else {
                        word
                    }
                }


                loadFavoriteWords()

                _frequentViewWords.value = _frequentViewWords.value.map { word ->
                    if (word.uid == wordId) {
                        word.copy(isFavorite = !isFav)
                    } else {
                        word
                    }
                }

                println("toggle favorite")
                cb(null)
            } catch (ex: Exception) {
                println("error: $ex")
                cb(ex.message)
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
        loadAuth()
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
//                        loadWords()
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
