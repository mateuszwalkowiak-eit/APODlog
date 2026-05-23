package com.example.apodlog.data.repository

import com.example.apodlog.data.local.ApodDao
import com.example.apodlog.data.model.ApodEntry
import com.example.apodlog.data.remote.ApodApiService
import kotlinx.coroutines.flow.Flow

/**
 * Repozytorium – jedyne źródło prawdy dla danych APOD.
 * Łączy warstwę sieciową (Retrofit) z lokalną bazą danych (Room).
 *
 * Zasada działania:
 * 1. Sprawdź czy dzisiejszy APOD jest już w bazie
 * 2. Jeśli nie – pobierz z API i zapisz
 * 3. Zawsze zwracaj dane z bazy (single source of truth)
 */
class ApodRepository(
    private val dao: ApodDao,
    private val api: ApodApiService
) {

    /**
     * Pobiera dzisiejszy APOD.
     * - Najpierw sprawdza cache w Room
     * - Jeśli brak – wywołuje API i zapisuje wynik
     * Zwraca Result<ApodEntry> – sukces lub błąd (np. brak sieci).
     */
    suspend fun fetchTodayApod(todayDate: String): Result<ApodEntry> {
        return try {
            // Sprawdź cache
            val cached = dao.getByDate(todayDate)
            if (cached != null) {
                return Result.success(cached)
            }

            // Pobierz z sieci
            val remote = api.getApod()
            dao.insert(remote)
            Result.success(remote)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pobiera dane APOD dla wybranej daty.
     * Najpierw szuka w bazie danych (cache). Jeśli nie ma, pobiera z API i zapisuje w bazie.
     */
    suspend fun fetchApodByDate(date: String): Result<ApodEntry> {
        return try {
            // 1. Sprawdzamy, czy mamy już ten dzień w bazie danych
            val cached = dao.getByDate(date)
            if (cached != null) {
                return Result.success(cached)
            }

            // 2. Jeśli nie ma w bazie, pobieramy z internetu dla wybranej daty
            val remote = api.getApod(date = date)
            
            // 3. Zapisujemy w lokalnej bazie danych, żeby następnym razem było szybciej
            dao.insert(remote)
            
            // 4. Zwracamy pobrany wpis
            Result.success(remote)
        } catch (e: Exception) {
            // Jeśli coś pójdzie nie tak (np. brak internetu), zwracamy błąd
            Result.failure(e)
        }
    }

    /**
     * Przełącza stan "ulubiony" dla danego wpisu i zapisuje w bazie.
     */
    suspend fun toggleFavorite(entry: ApodEntry) {
        dao.update(entry.copy(isFavorite = !entry.isFavorite))
    }

    /**
     * Aktualizuje notatkę użytkownika dla danego wpisu i zapisuje w bazie.
     */
    suspend fun updateNote(entry: ApodEntry, note: String) {
        dao.update(entry.copy(userNote = note))
    }

    /**
     * Zwraca Flow z listą ulubionych – automatycznie reaguje na zmiany w DB.
     */
    fun getFavorites(): Flow<List<ApodEntry>> = dao.getFavorites()
}
