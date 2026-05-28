package com.example.apodlog.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apodlog.data.local.AppDatabase
import com.example.apodlog.data.model.ApodEntry
import com.example.apodlog.data.remote.RetrofitInstance
import com.example.apodlog.data.repository.ApodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Możliwe stany ekranu głównego (UI State).
 */
sealed class ApodUiState {
    /** Trwa ładowanie danych */
    data object Loading : ApodUiState()
    /** Dane załadowane poprawnie */
    data class Success(val apod: ApodEntry) : ApodUiState()
    /** Wystąpił błąd (np. brak internetu) */
    data class Error(val message: String) : ApodUiState()
}

/**
 * ViewModel dla ekranu głównego.
 * Używa AndroidViewModel żeby mieć dostęp do Context (potrzebny dla Room).
 */
class ApodViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ApodRepository

    private val _uiState = MutableStateFlow<ApodUiState>(ApodUiState.Loading)
    val uiState: StateFlow<ApodUiState> = _uiState.asStateFlow()

    // Przechowuje wybraną datę przez użytkownika. Domyślnie jest to dzisiejszy dzień (format: YYYY-MM-DD).
    private val _selectedDate = MutableStateFlow<String>(LocalDate.now().toString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    init {
        // Inicjalizacja zależności (w produkcji użylibyśmy Hilt/Koin)
        val dao = AppDatabase.getInstance(application).apodDao()
        val api = RetrofitInstance.api
        repository = ApodRepository(dao, api)

        loadTodayApod()
    }

    /**
     * Ładuje dzisiejszy APOD na start aplikacji.
     */
    private fun loadTodayApod() {
        val today = LocalDate.now().toString()
        loadApodForDate(today)
    }

    /**
     * Ładuje APOD dla konkretnej wybranej daty (z bazy danych lub z internetu).
     */
    fun loadApodForDate(date: String) {
        viewModelScope.launch {
            _uiState.value = ApodUiState.Loading
            _selectedDate.value = date // zapisujemy, którą datę teraz oglądamy
            
            val result = repository.fetchApodByDate(date)
            _uiState.value = result.fold(
                onSuccess = { ApodUiState.Success(it) },
                onFailure = { ApodUiState.Error(it.message ?: "Nie udało się pobrać zdjęcia dla wybranej daty") }
            )
        }
    }

    /**
     * Wywołane po kliknięciu przycisku ❤️.
     */
    fun toggleFavorite(entry: ApodEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
            // Odświeżamy stan dla aktualnie wybranej daty, aby serduszko się od razu zaktualizowało na ekranie
            val currentDate = _selectedDate.value
            val updated = repository.fetchApodByDate(currentDate)
            updated.onSuccess { _uiState.value = ApodUiState.Success(it) }
        }
    }

    /**
     * Ponów próbę pobrania po błędzie. Spróbuje pobrać dane dla wybranej wcześniej daty.
     */
    fun retry() {
        loadApodForDate(_selectedDate.value)
    }
}
