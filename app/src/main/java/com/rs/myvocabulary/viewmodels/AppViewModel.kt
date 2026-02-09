package com.rs.myvocabulary.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.rs.learnmedia.composeable.createPost.PostAttachment
import com.rs.myvocabulary.api.UnifiedAiHelper
import com.rs.myvocabulary.database.Comment
import com.rs.myvocabulary.database.Label
import com.rs.myvocabulary.database.PreferencesManager
import com.rs.myvocabulary.database.SyncStatus
import com.rs.myvocabulary.database.Tag
import com.rs.myvocabulary.database.Word
import com.rs.myvocabulary.database.WordDatabase
import com.rs.myvocabulary.database.WordPartial
import com.rs.myvocabulary.sync.PullWordJob
import com.rs.myvocabulary.sync.PushWordJob
import com.rs.myvocabulary.utils.BackupUtils
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class WordsUiState(
        val items: List<Word> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null
)

data class WordsPaginationState(
        val currentPage: Int = 0,
        val pageSize: Int = 100,
        val hasMoreData: Boolean = true,
        val isLoadingMore: Boolean = false
)

data class WordsFilterState(
        val sortOrder: Int = 2,
        val selectedLabels: List<String> = emptyList(),
        val searchQuery: String = ""
)

data class PracticeSessionStats(
        val correctCount: Int = 0,
        val currentStreak: Int = 0,
        val totalCardsReviewed: Int = 0
)

data class CurrentUser(
        val userId: String,
        val username: String,
        val email: String,
        val fullName: String,
        val avatar: String
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    val db = WordDatabase.getInstance(application)

    private val _currentUser = MutableStateFlow<CurrentUser?>(null)
    val currentUser: StateFlow<CurrentUser?> = _currentUser.asStateFlow()

    // UI State
    private val _wordsUiState = MutableStateFlow(WordsUiState())
    val wordsUiState: StateFlow<WordsUiState> = _wordsUiState.asStateFlow()

    // Clause Word List State
    private val _clouseWordList = MutableStateFlow<List<Word>>(emptyList())
    val clouseWordList: StateFlow<List<Word>> = _clouseWordList.asStateFlow()

    // Docs List State
    private val _docsList = MutableStateFlow<List<Word>>(emptyList())
    val docsList: StateFlow<List<Word>> = _docsList.asStateFlow()

    // All Words UI State
    private val _allWordsUiState = MutableStateFlow(WordsUiState())
    val allWordsUiState: StateFlow<WordsUiState> = _allWordsUiState.asStateFlow()

    // All Words Pagination State
    private val _allPaginationState = MutableStateFlow(WordsPaginationState())
    val allPaginationState: StateFlow<WordsPaginationState> = _allPaginationState.asStateFlow()

    // Pagination State
    private val _paginationState = MutableStateFlow(WordsPaginationState())
    val paginationState: StateFlow<WordsPaginationState> = _paginationState.asStateFlow()

    // View Mode State
    private val _viewMode = MutableStateFlow(PreferencesManager.getString("viewMode", "default"))
    val viewMode: StateFlow<String> = _viewMode.asStateFlow()

    // Filter State
    private val _filterState = MutableStateFlow(WordsFilterState())
    val filterState: StateFlow<WordsFilterState> = _filterState.asStateFlow()

    // Favorite Words State
    private val _favoriteWords = MutableStateFlow<List<Word>>(emptyList())
    val favoriteWords: StateFlow<List<Word>> = _favoriteWords.asStateFlow()

    // Frequent View Words State
    private val _frequentViewWords = MutableStateFlow<List<Word>>(emptyList())
    val frequentViewWords: StateFlow<List<Word>> = _frequentViewWords.asStateFlow()

    // Refresh State
    private val _isRefreshingTimeline = MutableStateFlow(false)
    val isRefreshingTimeline: StateFlow<Boolean> = _isRefreshingTimeline.asStateFlow()

    // AI Suggesting State
    private val _isAiSuggesting = MutableStateFlow(false)
    val isAiSuggesting: StateFlow<Boolean> = _isAiSuggesting.asStateFlow()

    // Suggested Tags State
    private val _suggestedTags = MutableStateFlow<List<String>>(emptyList())
    val suggestedTags: StateFlow<List<String>> = _suggestedTags.asStateFlow()

    // Suggested Categories State
    private val _suggestedCategories = MutableStateFlow<List<String>>(emptyList())
    val suggestedCategories: StateFlow<List<String>> = _suggestedCategories.asStateFlow()

    // Generating Example State
    private val _isGeneratingExample = MutableStateFlow(false)
    val isGeneratingExample: StateFlow<Boolean> = _isGeneratingExample.asStateFlow()

    // Generated Example Sentences State
    private val _generatedExampleSentences = MutableStateFlow<List<String>>(emptyList())
    val generatedExampleSentences: StateFlow<List<String>> =
            _generatedExampleSentences.asStateFlow()

    // AI Enhancement States
    private val _enhancedWord = MutableStateFlow("")
    val enhancedWord: StateFlow<String> = _enhancedWord.asStateFlow()

    private val _enhancedShortMeaning = MutableStateFlow("")
    val enhancedShortMeaning: StateFlow<String> = _enhancedShortMeaning.asStateFlow()

    private val _enhancedDetails = MutableStateFlow("")
    val enhancedDetails: StateFlow<String> = _enhancedDetails.asStateFlow()

    private val _enhancedCategories = MutableStateFlow<List<String>>(emptyList())
    val enhancedCategories: StateFlow<List<String>> = _enhancedCategories.asStateFlow()

    // Backup Files State
    private val _backupFiles = MutableStateFlow<List<File>>(emptyList())
    val backupFiles: StateFlow<List<File>> = _backupFiles.asStateFlow()

    // All Tags State
    private val _allTags = MutableStateFlow<List<Tag>>(emptyList())
    val allTags: StateFlow<List<Tag>> = _allTags.asStateFlow()

    // All Categories State
    private val _allCategories = MutableStateFlow<List<Label>>(emptyList())
    val allCategories: StateFlow<List<Label>> = _allCategories.asStateFlow()

    // Add Word Dialog State
    private val _openAddWordDialog = MutableStateFlow<Boolean>(false)
    val openAddWordDialog: StateFlow<Boolean> = _openAddWordDialog.asStateFlow()

    // Long Press Item State
    private val _longPressItem = MutableStateFlow<Word?>(null)
    val longPressItem: StateFlow<Word?> = _longPressItem.asStateFlow()

    // Practice Mode State
    private val _practiceCurrentWord = MutableStateFlow<Word?>(null)
    val practiceCurrentWord: StateFlow<Word?> = _practiceCurrentWord.asStateFlow()

    private val _practiceIsAnswerRevealed = MutableStateFlow(false)
    val practiceIsAnswerRevealed: StateFlow<Boolean> = _practiceIsAnswerRevealed.asStateFlow()

    private val _practiceIsLoading = MutableStateFlow(false)
    val practiceIsLoading: StateFlow<Boolean> = _practiceIsLoading.asStateFlow()

    private val _practiceSessionStats = MutableStateFlow(PracticeSessionStats())
    val practiceSessionStats: StateFlow<PracticeSessionStats> = _practiceSessionStats.asStateFlow()

    // Posts State (for timeline/feed)
    private val _posts = MutableStateFlow<List<Word>>(emptyList())
    val posts: StateFlow<List<Word>> = _posts.asStateFlow()

    // Current Post State (for post detail view)
    private val _currentPost = MutableStateFlow<Word?>(null)
    val currentPost: StateFlow<Word?> = _currentPost.asStateFlow()

    // Loading Timeline State
    private val _isLoadingTimeline = MutableStateFlow(false)
    val isLoadingTimeline: StateFlow<Boolean> = _isLoadingTimeline.asStateFlow()

    // Uploading State (for creating posts)
    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    // Sync Jobs
    private var activeSyncJob: Job? = null
    private var activePullSyncJob: Job? = null

    init {
        loadAuth()
        loadCategories()
        loadTags()
    }

    // ========== WORD LOADING FUNCTIONS ==========

    fun loadFavoriteWords() {
        viewModelScope.launch {
            try {
                db.getFavoriteWords(limit = 100000, offset = 0) { words ->
                    _favoriteWords.value = words
                }
            } catch (e: Exception) {
                println("Error loading favorite words: $e")
            }
        }
    }

    fun loadFrequentViewWords() {
        viewModelScope.launch {
            try {
                db.getFrequentViewWords(limit = 100, offset = 0) { words ->
                    _frequentViewWords.value = words
                }
            } catch (e: Exception) {
                println("Error loading frequent view words: $e")
            }
        }
    }

    fun loadNote(more: Boolean = false) {
        if (_wordsUiState.value.isLoading || _paginationState.value.isLoadingMore) return

        if (more && !_paginationState.value.hasMoreData) return

        if (more) {
            _paginationState.update { it.copy(isLoadingMore = true) }
        } else {
            _wordsUiState.update { it.copy(isLoading = true, error = null) }
            _paginationState.update { it.copy(currentPage = 0, hasMoreData = true) }
        }

        val currentPagination = _paginationState.value
        val currentFilter = _filterState.value
        val offset = currentPagination.currentPage * currentPagination.pageSize

        db.getAllWordsPaginated(
                sortOrder = currentFilter.sortOrder,
                limit = currentPagination.pageSize,
                searchQuery = currentFilter.searchQuery,
                offset = offset,
                isFav = false,
        ) { newNotes -> viewModelScope.launch { handleWordsLoaded(newNotes, more) } }
    }

    fun loadClouseWordList() {
        db.getClouseWordsList() { items -> viewModelScope.launch { _clouseWordList.value = items } }
    }

    fun loadAllItems(more: Boolean = false) {
        if (_allWordsUiState.value.isLoading || _allPaginationState.value.isLoadingMore) return

        if (more && !_allPaginationState.value.hasMoreData) return

        if (more) {
            _allPaginationState.update { it.copy(isLoadingMore = true) }
        } else {
            _allWordsUiState.update { it.copy(isLoading = true, error = null) }
            _allPaginationState.update { it.copy(currentPage = 0, hasMoreData = true) }
        }

        val currentPagination = _allPaginationState.value
        val currentFilter = _filterState.value
        val offset = currentPagination.currentPage * currentPagination.pageSize

        db.getAllWordsAndClausesPaginated(
                sortOrder = currentFilter.sortOrder,
                limit = currentPagination.pageSize,
                searchQuery = currentFilter.searchQuery,
                offset = offset,
                isFav = false,
        ) { newNotes -> viewModelScope.launch { handleAllItemsLoaded(newNotes, more) } }
    }

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val categories = db.getAllCategories()
            _allCategories.value = categories
        }
    }

    fun loadTags() {
        viewModelScope.launch(Dispatchers.IO) {
            val tags = db.getAllTags()
            _allTags.value = tags
        }
    }

    fun loadDocs() {
        // Implementation can be added when needed
        // db.getAllWordsPaginated(
        //         sortOrder = 2,
        //         limit = 100,
        //         offset = 0,
        //         searchQuery = "",
        //         isFav = false,
        // ) { newNotes -> viewModelScope.launch { _docsList.value = newNotes } }
    }

    private fun handleWordsLoaded(newNotes: List<Word>, isLoadingMore: Boolean) {
        val currentNotes = _wordsUiState.value.items
        val currentPagination = _paginationState.value

        val updatedNotes =
                if (isLoadingMore) {
                    currentNotes + newNotes
                } else {
                    newNotes
                }

        _wordsUiState.update { it.copy(items = updatedNotes, isLoading = false, error = null) }

        _paginationState.update {
            it.copy(
                    currentPage = currentPagination.currentPage + 1,
                    hasMoreData = newNotes.size == currentPagination.pageSize,
                    isLoadingMore = false
            )
        }
    }

    private fun handleAllItemsLoaded(newNotes: List<Word>, isLoadingMore: Boolean) {
        val currentNotes = _allWordsUiState.value.items
        val currentPagination = _allPaginationState.value

        val updatedNotes =
                if (isLoadingMore) {
                    currentNotes + newNotes
                } else {
                    newNotes
                }

        _allWordsUiState.update { it.copy(items = updatedNotes, isLoading = false, error = null) }

        _allPaginationState.update {
            it.copy(
                    currentPage = currentPagination.currentPage + 1,
                    hasMoreData = newNotes.size == currentPagination.pageSize,
                    isLoadingMore = false
            )
        }
    }

    // ========== FILTER AND VIEW MODE FUNCTIONS ==========

    fun setFilter(sortOrder: Int? = null, searchValue: String? = null) {
        try {
            if (sortOrder != null) {
                _filterState.update { it.copy(sortOrder = sortOrder) }
            }
            if (searchValue != null) {
                _filterState.update { it.copy(searchQuery = searchValue) }
            }

            if (sortOrder != null || searchValue != null) {
                loadNote()
                loadAllItems()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
    }

    fun setViewMode(value: String) {
        _viewMode.value = value

        when (value) {
            "favorite_view" -> loadFavoriteWords()
            "frequently_view" -> loadFrequentViewWords()
        }

        PreferencesManager.putString("viewMode", value)
    }

    fun clearSearchOwn() {
        _filterState.update { it.copy(searchQuery = "") }
        _paginationState.update {
            it.copy(currentPage = 0, hasMoreData = true, isLoadingMore = false)
        }
        loadNote()
        loadAllItems()
    }

    fun loadAuth() {
        _viewMode.value = PreferencesManager.getString("viewMode", "default")
        println(_viewMode.value)
    }

    // ========== WORD CRUD FUNCTIONS ==========

    fun setAddWordDialog(state: Boolean) {
        _openAddWordDialog.value = state
    }

    fun getItemByUid(uid: String, cb: (item: Word?) -> Unit) {
        viewModelScope.launch { db.getWordByUid(uid) { cb(it) } }
    }

    suspend fun getWord(uid: String): Word? {
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            db.getWordByUid(uid) { word -> continuation.resumeWith(Result.success(word)) }
        }
    }

    fun addWord(newWord: Word, cb: (errorMessage: String?) -> Unit) {
        viewModelScope.launch {
            try {
                println(
                        "AppViewModel.addWord: Starting - id=${newWord.id}, uid=${newWord.uid}, type=${newWord.type}, word=${newWord.word}"
                )

                if (newWord.id != 0L) {
                    println("AppViewModel.addWord: Updating existing word (id != 0)")
                    db.updatePartial(
                            WordPartial(
                                    uid = newWord.uid,
                                    word = newWord.word,
                                    shortMeaning = newWord.shortMeaning,
                                    details = newWord.details,
                                    isFavorite = newWord.isFavorite,
                                    syncStatus = SyncStatus.PENDING,
                            )
                    )
                    startWordSync()
                } else {
                    println("AppViewModel.addWord: Inserting new word")
                    val resultId = db.insertWord(newWord)
                    println("AppViewModel.addWord: Insert complete - resultId=$resultId")
                    if (resultId == -1L) {
                        cb("Failed to insert word into database")
                        return@launch
                    }
                    startWordSync()
                }

                // Reload the appropriate list based on type
                when (newWord.type) {
                    "clause" -> {
                        loadClouseWordList()
                        loadAllItems()
                    }
                    "docs" -> loadDocs()
                    else -> {
                        loadNote()
                        loadAllItems()
                    }
                }

                println("AppViewModel.addWord: Success - calling callback with null")
                cb(null)
            } catch (ex: Exception) {
                println("AppViewModel.addWord: Error - ${ex.message}")
                ex.printStackTrace()
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
                println("update partial result $result")
            } catch (ex: Exception) {
                println("error: $ex")
                cb(ex.message)
            }
        }
    }

    fun deleteWord(uid: String, onComplete: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deleteWord(uid)
            // Reload lists
            loadNote()
            loadAllItems()
            loadDocs()
            loadClouseWordList()
            startWordSync()
            withContext(Dispatchers.Main) { onComplete() }
        }
    }

    fun setLongPressItem(newWord: Word?) {
        _longPressItem.value = newWord
    }

    fun toggleFavorite(uid: String) {
        db.toggleFavorite(uid) { newStatus ->
            viewModelScope.launch {
                val isFavorite = newStatus == 1

                _wordsUiState.update { state ->
                    state.copy(
                            items =
                                    state.items.map { note ->
                                        if (note.uid == uid) note.copy(isFavorite = isFavorite)
                                        else note
                                    }
                    )
                }

                _clouseWordList.update { list ->
                    list.map { if (it.uid == uid) it.copy(isFavorite = isFavorite) else it }
                }

                _allWordsUiState.update { state ->
                    state.copy(
                            items =
                                    state.items.map {
                                        if (it.uid == uid) it.copy(isFavorite = isFavorite) else it
                                    }
                    )
                }

                // Reload lists to ensure correct sorting
                loadClouseWordList()
                startWordSync()
            }
        }
    }

    fun incrementViewCount(uid: String) {
        viewModelScope.launch {
            try {
                val result = db.incrementViewCount(uid)
                println("increment view count result: $result")
                startWordSync()
            } catch (ex: Exception) {
                println("error incrementing view count: $ex")
            }
        }
    }

    // ========== COMMENT FUNCTIONS ==========

    fun insertWordComment(wordId: String, comment: Comment) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertComment(wordId, comment)
            loadNoteDetailGeneric(wordId)
            startWordSync()
        }
    }

    fun deleteWordComment(commentId: String, wordId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            db.deletePostComment(commentId, wordId)
            loadNoteDetailGeneric(wordId)
        }
    }

    fun insertMultipleComments(wordId: String, sentences: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val newComments =
                    sentences.map { sentence ->
                        Comment(
                                _id = UUID.randomUUID().toString(),
                                username = "AI Assistant",
                                text = sentence,
                                createdAt = System.currentTimeMillis()
                        )
                    }
            db.insertMultipleComments(wordId, newComments)
            loadNoteDetailGeneric(wordId)
            startWordSync()
        }
    }

    private fun loadNoteDetailGeneric(uid: String) {
        viewModelScope.launch {
            db.getWordByUid(uid) { word ->
                if (word != null) {
                    // Update docs list if present
                    val currentDocs = _docsList.value.toMutableList()
                    val index = currentDocs.indexOfFirst { it.uid == uid }
                    if (index != -1) {
                        currentDocs[index] = word
                        _docsList.value = currentDocs
                    }

                    // Update note list if present
                    val currentNotes = _wordsUiState.value.items.toMutableList()
                    val noteIndex = currentNotes.indexOfFirst { it.uid == uid }
                    if (noteIndex != -1) {
                        currentNotes[noteIndex] = word
                        _wordsUiState.update { it.copy(items = currentNotes) }
                    }
                }
            }
        }
    }

    // ========== SYNC FUNCTIONS ==========

    fun startWordSync() {
        activeSyncJob?.cancel()
        activeSyncJob =
                viewModelScope.launch {
                    try {
                        val isConnected = { isNetworkAvailable(context = application) }

                        // 7. Sync Words (Posts)
                        launch {
                            PushWordJob(
                                            isConnected = isConnected,
                                            getUnsyncedWords = { db.getUnsyncedWords() },
                                            updateWordSyncStatus = { id, _ ->
                                                db.updateWordSyncStatus(id, SyncStatus.SYNCED)
                                            }
                                    )
                                    .startPushing()
                        }
                    } catch (e: Exception) {
                        println("Sync error: ${e.message}")
                    }
                }
    }

    fun pullDataFromServer() {
        activePullSyncJob?.cancel()
        activePullSyncJob =
                viewModelScope.launch {
                    try {
                        val isConnected = { isNetworkAvailable(application) }

                        // Pull everything concurrently
                        launch {
                            PullWordJob(
                                            isConnected = isConnected,
                                            saveWords = { db.upsertWord(it) },
                                            onSyncComplete = { loadNote() }
                                    )
                                    .startPulling()
                        }
                    } catch (e: Exception) {
                        println("Sync failed: ${e.message}")
                    }
                }
    }

    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null &&
                (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN))
    }

    // ========== AI FUNCTIONS ==========

    fun generateAiSuggestions(postText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isAiSuggesting.value = true
            try {
                val aiHelper = UnifiedAiHelper()
                val prompt =
                        """
                    Analyze the following text and suggest relevant categories and tags.
                    Format the output ONLY as a valid JSON object with 'categories' and 'tags' fields.
                    Each field should be a list of strings.

                    Text: "$postText"
                """.trimIndent()

                val response = aiHelper.generateContent(prompt)
                val result = aiHelper.parseResponse(response)
                if (result != null) {
                    val cleanedResult =
                            if (result.contains("```json")) {
                                result.substringAfter("```json").substringBefore("```").trim()
                            } else if (result.contains("```")) {
                                result.substringAfter("```").substringBefore("```").trim()
                            } else {
                                result.trim()
                            }

                    val json = JSONObject(cleanedResult)
                    val tagsArray = json.getJSONArray("tags")
                    val catsArray = json.getJSONArray("categories")

                    val tags = mutableListOf<String>()
                    for (i in 0 until tagsArray.length()) tags.add(tagsArray.getString(i))

                    val cats = mutableListOf<String>()
                    for (i in 0 until catsArray.length()) cats.add(catsArray.getString(i))

                    _suggestedTags.value = tags
                    _suggestedCategories.value = cats
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAiSuggesting.value = false
            }
        }
    }

    fun generateAiExampleSentences(postText: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isGeneratingExample.value = true
            try {
                val aiHelper = UnifiedAiHelper()
                val prompt =
                        "Write 3 or 4 simple and clear English example sentences using the vocabulary word: \"$postText\". Format the output ONLY as a valid JSON array of strings. Do not include any other text."
                val response = aiHelper.generateContent(prompt)
                val result = aiHelper.parseResponse(response)
                if (result != null) {
                    val cleanedResult =
                            if (result.contains("```json")) {
                                result.substringAfter("```json").substringBefore("```").trim()
                            } else if (result.contains("```")) {
                                result.substringAfter("```").substringBefore("```").trim()
                            } else {
                                result.trim()
                            }

                    val jsonArray = JSONArray(cleanedResult)
                    val sentences = mutableListOf<String>()
                    for (i in 0 until jsonArray.length()) {
                        sentences.add(jsonArray.getString(i))
                    }
                    _generatedExampleSentences.value = sentences
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isGeneratingExample.value = false
            }
        }
    }
    fun generateAiPostEnhancement(word: Word, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isAiSuggesting.value = true
            try {
                val aiHelper = UnifiedAiHelper()
                val prompt =
                        """
                    Enhance the following vocabulary entry and return ONLY a valid JSON object.
                    
                    Instructions for fields:
                    1. 'word': Improve the word itself if misspelled or incomplete.
                    2. 'shortMeaning': Provide a concise and clear meaning.
                    3. 'details': Write a rich, natural, human-readable description. Use markdown (bold, lists, etc.) if helpful. 
                    
                    CRITICAL: The entire output must be a single valid JSON object. 
                    The 'details' field must be a valid JSON string (wrap the content in double quotes and escape any internal quotes or newlines as needed). 
                    DO NOT include any text before or after the JSON.
                    
                    Current Entry:
                    Word: ${word.word}
                    Short Meaning: ${word.shortMeaning}
                    Details: ${word.details}
                """.trimIndent()

                val response = aiHelper.generateContent(prompt)
                val result = aiHelper.parseResponse(response)
                if (result != null) {
                    val cleanedResult =
                            if (result.contains("```json")) {
                                result.substringAfter("```json").substringBefore("```").trim()
                            } else if (result.contains("```")) {
                                result.substringAfter("```").substringBefore("```").trim()
                            } else {
                                result.trim()
                            }

                    val json = JSONObject(cleanedResult)
                    _enhancedWord.value = json.optString("word", word.word)
                    _enhancedShortMeaning.value = json.optString("shortMeaning", word.shortMeaning)
                    _enhancedDetails.value = json.optString("details", word.details)
                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAiSuggesting.value = false
            }
        }
    }

    fun generateAiBanglishMix(word: Word, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isAiSuggesting.value = true
            try {
                val aiHelper = UnifiedAiHelper()
                val prompt =
                        """
                    > "Please read the following text.
                    > Your task is to rewrite the main summary/content of the text in a mix of Bengali and English (Benglish style). Follow these rules:
                    >  * Identify 10-12 key technical or difficult words from the text.
                    >  * Write a Bengali paragraph summarizing the text, but replace the selected keywords with their English equivalents (Capitalize the first letter of these English words).
                    >  * The flow should be natural so that a Bengali speaker can understand the meaning of those English words through context.
                    >  * At the end, provide a 'Vocabulary List' (not in a table) using bullet points. Each point should include: The English Word — Bengali Meaning: A short English definition/context."
                    >  * Also suggest 3-5 relevant categories for this text.
                    
                    Text:
                    ${word.details}
                    
                    format the output as a valid JSON object with:
                    - 'details': the rewritten text.
                    - 'categories': a list of strings for suggested categories.
                """.trimIndent()

                val response = aiHelper.generateContent(prompt)
                val result = aiHelper.parseResponse(response)
                if (result != null) {
                    val cleanedResult =
                            if (result.contains("```json")) {
                                result.substringAfter("```json").substringBefore("```").trim()
                            } else if (result.contains("```")) {
                                result.substringAfter("```").substringBefore("```").trim()
                            } else {
                                result.trim()
                            }

                    val json = JSONObject(cleanedResult)
                    _enhancedWord.value = word.word
                    _enhancedShortMeaning.value = word.shortMeaning
                    _enhancedDetails.value = json.optString("details", "")

                    val catsArray = json.optJSONArray("categories")
                    val cats = mutableListOf<String>()
                    if (catsArray != null) {
                        for (i in 0 until catsArray.length()) {
                            cats.add(catsArray.getString(i))
                        }
                    }
                    _enhancedCategories.value = cats

                    onComplete()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isAiSuggesting.value = false
            }
        }
    }

    fun applyAiPostEnhancement(
            uid: String,
            word: String,
            shortMeaning: String,
            details: String,
            categories: List<String>
    ) {
        viewModelScope.launch {
            try {
                val dbCategories: List<Label> =
                        categories.map { catName ->
                            Label(
                                    uid = UUID.randomUUID().toString(),
                                    name = catName,
                                    color = "#FF0000"
                            )
                        }

                db.updatePartial(
                        WordPartial(
                                uid = uid,
                                word = word,
                                shortMeaning = shortMeaning,
                                details = details,
                                categories = dbCategories,
                                syncStatus = SyncStatus.PENDING
                        )
                )
                loadNoteDetailGeneric(uid)
                startWordSync()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun applyAiSuggestions(
            context: Context,
            wordId: String,
            tags: List<String>,
            categories: List<String>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = WordDatabase.getInstance(context)
            val dbTags: List<Tag> =
                    tags.map { tagName -> Tag(uid = UUID.randomUUID().toString(), name = tagName) }
            val dbCategories: List<Label> =
                    categories.map { catName ->
                        Label(uid = UUID.randomUUID().toString(), name = catName, color = "#FF0000")
                    }

            db.updateWordTagsAndCategories(wordId, dbTags, dbCategories)
            loadNoteDetailGeneric(wordId)
            startWordSync()
        }
    }

    // ========== POST FUNCTIONS ==========

    fun loadData(context: Context) {
        // Load tags and categories if needed
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // val db = WordDatabase.getInstance(context)
                // Load all tags
                //                val tags = db.getAllTags()
                //                _allTags.value = tags.map { it.name }
                //
                //                // Load all categories
                //                val categories = db.getAllCategories()
                //                _allCategories.value = categories.map { mapOf("name" to it.name,
                // "uid" to it.uid) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun fetchPosts(isRefresh: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isRefresh) {
                _isRefreshingTimeline.value = true
            } else {
                _isLoadingTimeline.value = true
            }
            try {
                // val db = WordDatabase.getInstance(context)
                //                val allPosts = db.getAllPosts()
                //                _posts.value = allPosts
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoadingTimeline.value = false
                _isRefreshingTimeline.value = false
            }
        }
    }

    fun getPostById(context: Context, postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val db = WordDatabase.getInstance(context)
                //                val post = db.getPostById(postId)
                //                _currentPost.value = post
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun createPost(
            context: Context,
            textContent: String, /* details */
            attachments: List<PostAttachment>,
            selectedTags: List<String>,
            selectedCategories: List<Map<String, String>>,
            type: String = "word",
            word: String = "", /* word/title */
            shortMeaning: String = "",
            onSuccess: () -> Unit,
            onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isUploading.value = true
            try {
                val db = WordDatabase.getInstance(context)

                // 1. Save attachments locally
                val savedAttachments =
                        mutableListOf<com.rs.myvocabulary.database.CommentAttachment>()
                for (attachment in attachments) {
                    val path =
                            if (attachment.uri != null) {
                                com.rs.myvocabulary.utils.LocalAssetManager.saveAsset(
                                        context,
                                        attachment.uri!!,
                                        attachment.type == "image"
                                )
                            } else {
                                attachment.remoteUrl
                            }

                    if (path != null) {
                        savedAttachments.add(
                                com.rs.myvocabulary.database.CommentAttachment(
                                        url = path,
                                        type = attachment.type
                                )
                        )
                    }
                }

                // 2. Resolve Tags and Categories (Creating new objects as we don't have global
                // lookups)
                val finalTags =
                        selectedTags.map { tagName ->
                            Tag(uid = UUID.randomUUID().toString(), name = tagName)
                        }

                val finalCategories =
                        selectedCategories.map { catMap ->
                            Label(
                                    uid = UUID.randomUUID().toString(),
                                    name = catMap["name"] ?: "Unknown",
                                    parentId = catMap["parentId"],
                                    color = catMap["color"] ?: "#FF0000"
                            )
                        }

                // 3. Create Word object
                val newWord =
                        Word(
                                word =
                                        if (word.isNotBlank()) word
                                        else
                                                textContent.take(
                                                        50
                                                ), // Use first 50 chars as title/word
                                details = textContent,
                                type = type,
                                shortMeaning = shortMeaning,
                                userId = "1", // Updated in actual app
                                attachments =
                                        if (savedAttachments.isNotEmpty()) savedAttachments
                                        else null,
                                tags = if (finalTags.isNotEmpty()) finalTags else null,
                                categories =
                                        if (finalCategories.isNotEmpty()) finalCategories else null,
                                syncStatus = SyncStatus.PENDING
                        )

                db.insertWord(newWord)
                loadData(context)
                onSuccess()
                startWordSync()
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Unknown error")
            } finally {
                _isUploading.value = false
            }
        }
    }

    fun insertTag(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // val db = WordDatabase.getInstance(context)
            //            db.insertTag(name)
            loadData(context = application)
        }
    }

    fun insertComment(postId: String, comment: Comment) {
        viewModelScope.launch(Dispatchers.IO) {
            db.insertComment(postId, comment)
            // Refresh post
            //            val updatedPost = db.getPostById(postId)
            //            _currentPost.value = updatedPost
            fetchPosts()
            startWordSync()
        }
    }

    /*
    fun updatePostTagsAndCategories(
            context: Context,
            postId: String,
            tags: List<Tag>,
            categories: List<Label>
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = WordDatabase.getInstance(context)
            //            db.updatePostTagsAndCategories(postId, tags, categories)
            // Refresh post
            //            val updatedPost = db.getPostById(postId)
            //            _currentPost.value = updatedPost
            fetchPosts(context)
        }
    }
    */

    // ========== PRACTICE FUNCTIONS ==========

    fun startPracticeSession() {
        viewModelScope.launch {
            _practiceIsLoading.value = true
            _practiceSessionStats.value = PracticeSessionStats()
            loadNextPracticeCard()
            _practiceIsLoading.value = false
        }
    }

    private fun loadNextPracticeCard() {
        viewModelScope.launch {
            // Get random word for now
            // Improve: fetch based on spaced repetition algorithm
            db.getFrequentViewWords(limit = 1, offset = (0..10).random()) { words ->
                if (words.isNotEmpty()) {
                    _practiceCurrentWord.value = words.first()
                } else {
                    // Fallback to any word
                    db.getAllWordsPaginated(sortOrder = 2, limit = 1, offset = 0) { allWords ->
                        _practiceCurrentWord.value = allWords.firstOrNull()
                    }
                }
                _practiceIsAnswerRevealed.value = false
            }
        }
    }

    fun revealAnswer() {
        _practiceIsAnswerRevealed.value = true
    }

    fun markResult(correct: Boolean) {
        val currentStats = _practiceSessionStats.value
        val newStreak = if (correct) currentStats.currentStreak + 1 else 0
        val newCorrectCount =
                if (correct) currentStats.correctCount + 1 else currentStats.correctCount

        _practiceSessionStats.value =
                currentStats.copy(
                        correctCount = newCorrectCount,
                        currentStreak = newStreak,
                        totalCardsReviewed = currentStats.totalCardsReviewed + 1
                )

        // Update word stats in DB (spaced repetition logic would go here)
        _practiceCurrentWord.value?.let { word ->
            // Example: update view count or last visited
            incrementViewCount(word.uid)
        }

        loadNextPracticeCard()
    }

    fun restoreData(context: Context, uri: android.net.Uri, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    val success = BackupUtils.restoreFromBackup(context, inputStream)
                    if (success) {
                        loadData(context) // Refresh UI
                    }
                    withContext(Dispatchers.Main) { onComplete(success) }
                }
                        ?: withContext(Dispatchers.Main) { onComplete(false) }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { onComplete(false) }
            }
        }
    }

    fun fetchBackupFiles() {
        _backupFiles.value = BackupUtils.getBackupFiles(getApplication())
    }

    fun deleteBackupFile(file: java.io.File) {
        if (BackupUtils.deleteBackupFile(file)) {
            fetchBackupFiles()
        }
    }

    fun restoreFromBackupFile(context: Context, file: java.io.File, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = BackupUtils.restoreFromFile(context, file)
            if (success) {
                loadData(context)
            }
            withContext(Dispatchers.Main) { onComplete(success) }
        }
    }
}
