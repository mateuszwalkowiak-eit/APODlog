package com.example.apodlog.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Singleton dostarczający skonfigurowaną instancję Retrofit.
 */
object RetrofitInstance {

    private val json = Json {
        // Ignoruje pola z API, których nie ma w data class
        ignoreUnknownKeys = true
        // Pozwala na null tam gdzie API zwraca null zamiast pominąć pole
        coerceInputValues = true
    }

    private val client = OkHttpClient.Builder().build()

    val api: ApodApiService by lazy {
        Retrofit.Builder()
            .baseUrl(ApodApiService.BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(ApodApiService::class.java)
    }
}
