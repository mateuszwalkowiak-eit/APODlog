package com.example.apodlog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Reprezentuje jedno zdjęcie (lub film) Astronomiczne Zdjęcie Dnia (APOD).
 * Klasa służy równocześnie jako:
 * - model odpowiedzi z NASA API (adnotacje @Serializable)
 * - encja bazy danych Room (adnotacje @Entity)
 */
@Serializable
@Entity(tableName = "apod_entries")
data class ApodEntry(
    /** Data w formacie YYYY-MM-DD – klucz główny */
    @PrimaryKey
    val date: String,

    /** Tytuł zdjęcia */
    val title: String,

    /** Objaśnienie/opis */
    val explanation: String,

    /** URL zdjęcia lub filmu */
    val url: String,

    /** "image" lub "video" */
    @SerialName("media_type")
    val mediaType: String,

    /** URL zdjęcia w wyższej rozdzielczości (opcjonalne) */
    @SerialName("hdurl")
    val hdUrl: String? = null,

    /** Copyright (opcjonalne, niektóre APOD są z domeny publicznej) */
    val copyright: String? = null,

    // --- Pola lokalne (nie z API, nie serializable) ---

    /** Czy zdjęcie jest w ulubionych */
    @Transient
    val isFavorite: Boolean = false,

    /** Własna notatka użytkownika */
    @Transient
    val userNote: String = ""
)
