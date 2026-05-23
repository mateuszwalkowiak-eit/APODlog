package com.example.apodlog.ui.details

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

/**
 * Możliwe stany dla ekranu szczegółów.
 * Ułatwia wyświetlanie kręciołka ładowania, danych lub błędu.
 */
sealed class DetailsUiState {
    object Loading : DetailsUiState()
    data class Success(val apod: ApodEntry) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

/**
 * Prosty ViewModel do zarządzania danymi na ekranie szczegółów.
 */
class DetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ApodRepository

    // Trzymamy aktualny stan ekranu (domyślnie: ładowanie)
    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        // Przygotowujemy bazę danych i API, tworząc repozytorium
        val dao = AppDatabase.getInstance(application).apodDao()
        val api = RetrofitInstance.api
        repository = ApodRepository(dao, api)
    }

    /**
     * Pobiera szczegóły APOD dla podanej daty.
     */
    fun loadApod(date: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            
            // Pobieramy dane z repozytorium
            val result = repository.fetchApodByDate(date)
            
            // Zapisujemy wynik do stanu UI
            _uiState.value = result.fold(
                onSuccess = { DetailsUiState.Success(it) },
                onFailure = { DetailsUiState.Error(it.message ?: "Nieznany błąd połączenia") }
            )
        }
    }

    /**
     * Dodaje lub usuwa dany wpis z ulubionych.
     */
    fun toggleFavorite(entry: ApodEntry) {
        viewModelScope.launch {
            // Przełączamy ulubione w bazie danych
            repository.toggleFavorite(entry)
            
            // Odświeżamy stan ekranu z nowymi danymi
            val result = repository.fetchApodByDate(entry.date)
            result.onSuccess { updatedEntry ->
                _uiState.value = DetailsUiState.Success(updatedEntry)
            }
        }
    }
}
