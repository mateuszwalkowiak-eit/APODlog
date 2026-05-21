package com.example.apodlog.ui.favorites

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.apodlog.data.local.AppDatabase
import com.example.apodlog.data.model.ApodEntry
import com.example.apodlog.data.remote.RetrofitInstance
import com.example.apodlog.data.repository.ApodRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel dla ekranu Ulubionych.
 * Pobiera z bazy danych listę wszystkich zdjęć oznaczonych jako ulubione.
 */
class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    // Tworzymy repozytorium tak samo jak w ApodViewModel
    private val repository: ApodRepository

    init {
        val dao = AppDatabase.getInstance(application).apodDao()
        val api = RetrofitInstance.api
        repository = ApodRepository(dao, api)
    }

    /**
     * Lista ulubionych zdjęć jako StateFlow.
     *
     * stateIn() zamienia Flow z bazy danych na StateFlow, który:
     * - zaczyna zbierać dane dopiero gdy ktoś obserwuje (SharingStarted.WhileSubscribed)
     * - ma wartość domyślną: pustą listę (emptyList())
     *
     * Dzięki temu ekran od razu ma jakiś stan do wyświetlenia.
     */
    val favorites: StateFlow<List<ApodEntry>> = repository
        .getFavorites()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    /**
     * Usuwa zdjęcie z ulubionych po kliknięciu przycisku serca.
     */
    fun removeFromFavorites(entry: ApodEntry) {
        viewModelScope.launch {
            repository.toggleFavorite(entry)
        }
    }
}
