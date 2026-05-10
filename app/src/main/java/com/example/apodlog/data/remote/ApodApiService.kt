package com.example.apodlog.data.remote

import com.example.apodlog.data.model.ApodEntry
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interfejs Retrofit opisujący endpointy NASA APOD API.
 * Dokumentacja: https://api.nasa.gov/
 */
interface ApodApiService {

    /**
     * Pobiera APOD dla podanej daty (lub dzisiejszy jeśli date = null).
     *
     * @param apiKey  Klucz API NASA (domyślnie DEMO_KEY)
     * @param date    Data w formacie YYYY-MM-DD (opcjonalne – brak = dzisiaj)
     */
    @GET("planetary/apod")
    suspend fun getApod(
        @Query("api_key") apiKey: String = "DEMO_KEY",
        @Query("date") date: String? = null
    ): ApodEntry

    companion object {
        const val BASE_URL = "https://api.nasa.gov/"
    }
}
