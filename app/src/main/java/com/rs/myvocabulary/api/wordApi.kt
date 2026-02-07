package com.rs.myvocabulary.api

import com.google.gson.Gson
import com.rs.myvocabulary.database.Word


data class StatsResponseData(
    val total: Int = 0,
    val favorites: Int = 0,
    val beginner: Int = 0,
    val intermediate: Int = 0,
    val advanced: Int = 0,
    val totalViews: Int = 0,
    val totalContributor: Int = 0,
    val totalCommunityWord: Int = 0
)

data class StatsResponse(
    val data: StatsResponseData
)

data class CommunityWordResponse(
    val data: Data
) {
    data class Data(
        val items: List<Word>?,
        val page: Int,
        val total: Int?,
        val hasMore: Boolean,
        val currentPage: Int
    )
}

object WordApi {
    val gson = Gson()
    val api = HttpHelper.getInstance()

    suspend fun stats(): StatsResponseData {
        val result = api.get("/api/v2/word/app/stats")
        val data = gson.fromJson(result.body, StatsResponse::class.java)
        return data.data
    }

    suspend fun getCommunityWords(page: Int): CommunityWordResponse {
        val result = api.get("/api/v2/word/ext/discover?page=$page")
        val data = gson.fromJson(result.body, CommunityWordResponse::class.java)
        return data
    }
}