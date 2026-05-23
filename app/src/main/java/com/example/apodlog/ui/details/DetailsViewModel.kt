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
 * Stan ekranu szczegółów (Details UI State).
 */
sealed class DetailsUiState {
    data object Loading : DetailsUiState()
    data class Success(val apod: ApodEntry) : DetailsUiState()
    data class Error(val message: String) : DetailsUiState()
}

/**
 * ViewModel dla ekranu szczegółów APOD.
 * Pobiera dane wybranego dnia z bazy lub sieci oraz umożliwia edycję notatek.
 */
class DetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ApodRepository

    private val _uiState = MutableStateFlow<DetailsUiState>(DetailsUiState.Loading)
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init {
        val dao = AppDatabase.getInstance(application).apodDao()
        val api = RetrofitInstance.api
        repository = ApodRepository(dao, api)
    }

    /**
     * Pobiera dane dla podanej daty.
     */
    fun loadApod(date: String) {
        viewModelScope.launch {
            _uiState.value = DetailsUiState.Loading
            val result = repository.fetchApodByDate(date)
            _uiState.value = result.fold(
                onSuccess = { DetailsUiState.Success(it) },
                onFailure = { DetailsUiState.Error(it.message ?: "Nie udało się załadować danych") }
            )
        }
    }

    /**
     * Zapisuje notatkę użytkownika w bazie danych Room.
     */
    fun saveNote(entry: ApodEntry, note: String, onSaved: () -> Unit = {}) {
        viewModelScope.launch {
            repository.updateNote(entry, note)
            // Aktualizujemy stan UI lokalnie nowym wpisem z zapisaną notatką
            _uiState.value = DetailsUiState.Success(entry.copy(userNote = note))
            onSaved()
        }
    }

    /**
     * Przełącza stan polubienia (❤️) bezpośrednio z ekranu szczegółów.
     */
    fun toggleFavorite(entry: ApodEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
            // Zmieniamy stan na przeciwny lokalnie w UI state
            _uiState.value = DetailsUiState.Success(entry.copy(isFavorite = !entry.isFavorite))
        }
    }
}
