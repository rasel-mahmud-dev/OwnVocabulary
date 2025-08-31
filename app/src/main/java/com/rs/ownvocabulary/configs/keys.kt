package com.rs.ownvocabulary.configs

import com.rs.ownvocabulary.BuildConfig


object Keys {
    const val SECRET_KEY = BuildConfig.API_SECRET_KEY
//    val BASE_URL: String = "http://192.168.0.148:3000"
    const val BASE_URL: String = BuildConfig.API_BASE_URL
    const val API_KEY: String = BuildConfig.API_KEY
}
