package com.example.network

import com.example.network.model.TextToSpeechRequest
import com.example.network.model.TextToSpeechResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Google Cloud Text-to-Speech API Retrofit Interface.
 * 
 * Invokes POST https://texttospeech.googleapis.com/v1/text:synthesize
 */
interface GoogleCloudTtsApiService {

    @POST("v1/text:synthesize")
    suspend fun synthesizeSpeech(
        @Query("key") apiKey: String,
        @Body request: TextToSpeechRequest
    ): TextToSpeechResponse
}
