package com.example.apodlog.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.apodlog.data.model.ApodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object dla tabeli apod_entries.
 * Wszystkie metody zawieszające (suspend) lub zwracające Flow
 * muszą być wywoływane z korutyny.
 */
@Dao
interface ApodDao {

    /**
     * Wstawia lub zastępuje wpis APOD.
     * Używane przy pierwszym pobraniu z API.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ApodEntry)

    /**
     * Aktualizuje istniejący wpis (np. zmiana isFavorite lub notatki).
     */
    @Update
    suspend fun update(entry: ApodEntry)

    /**
     * Zwraca APOD dla podanej daty lub null jeśli nie ma w bazie.
     */
    @Query("SELECT * FROM apod_entries WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): ApodEntry?

    /**
     * Zwraca listę ulubionych jako Flow – automatycznie emituje
     * nową wartość przy każdej zmianie w bazie.
     */
    @Query("SELECT * FROM apod_entries WHERE isFavorite = 1 ORDER BY date DESC")
    fun getFavorites(): Flow<List<ApodEntry>>

    /**
     * Zwraca wszystkie zapisane wpisy (historia).
     */
    @Query("SELECT * FROM apod_entries ORDER BY date DESC")
    fun getAll(): Flow<List<ApodEntry>>
}
