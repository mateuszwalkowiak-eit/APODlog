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

    init {
        // Inicjalizacja zależności (w produkcji użylibyśmy Hilt/Koin)
        val dao = AppDatabase.getInstance(application).apodDao()
        val api = RetrofitInstance.api
        repository = ApodRepository(dao, api)

        loadTodayApod()
    }

    /**
     * Ładuje dzisiejszy APOD (z cache lub sieci).
     */
    private fun loadTodayApod() {
        viewModelScope.launch {
            _uiState.value = ApodUiState.Loading
            val today = LocalDate.now().toString() // format: YYYY-MM-DD
            val result = repository.fetchTodayApod(today)
            _uiState.value = result.fold(
                onSuccess = { ApodUiState.Success(it) },
                onFailure = { ApodUiState.Error(it.message ?: "Nieznany błąd") }
            )
        }
    }

    /**
     * Wywołane po kliknięciu przycisku ❤️.
     */
    fun toggleFavorite(entry: ApodEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
            // Odśwież stan po zmianie
            val today = LocalDate.now().toString()
            val updated = repository.fetchTodayApod(today)
            updated.onSuccess { _uiState.value = ApodUiState.Success(it) }
        }
    }

    /**
     * Ponów próbę pobrania po błędzie.
     */
    fun retry() = loadTodayApod()
}
