package com.rs.myvocabulary.api

import com.google.gson.Gson


data class OnboardResponseData(
   val _id : String,
   val accessToken: String,
   val username : String,
   val email : String,
   val fullName : String,
   val avatar : String,
   val provider : String,
   val isVerified : String
)
data class OnboardResponse (
   val data:  OnboardResponseData
)

object AuthApi {
    val gson = Gson()
    val api = HttpHelper.getInstance()
    suspend fun onboard(
        email: String,
        username: String,
        phoneNumber: String,
        photoUrl: String,
        uid: String
    ): OnboardResponseData? {

        val payload  = """
            {
                "email": "$email",
                "fullName": "$username",
                "phoneNumber": "$phoneNumber",
                "avatar": "$photoUrl",
                "uid": "$uid"
            }
        """.trimIndent()

        val result = api.post("/api/v1/auth/onboard",  payload)
        println(result.body)

        val data = gson.fromJson(result.body, OnboardResponse::class.java)
        return data.data
    }
}