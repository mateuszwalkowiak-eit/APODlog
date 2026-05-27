package com.example.apodlog.ui.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.AlertDialog
import com.example.apodlog.utils.VideoUtils
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.apodlog.data.model.ApodEntry
import com.example.apodlog.ui.components.AppTopBar

/**
 * Ekran Ulubionych – wyświetla listę zdjęć dodanych do ulubionych.
 */
@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel = viewModel(),
    onItemClick: (ApodEntry) -> Unit
) {
    val favorites by viewModel.favorites.collectAsState()

    Scaffold(
        topBar = {
            AppTopBar(title = "Ulubione")
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (favorites.isEmpty()) {
                EmptyFavoritesContent()
            } else {
                FavoritesList(
                    favorites = favorites,
                    onRemoveFavorite = { entry ->
                        viewModel.removeFromFavorites(entry)
                    },
                    onItemClick = onItemClick
                )
            }
        }
    }
}

/**
 * Widok gdy nie ma żadnych ulubionych.
 */
@Composable
private fun EmptyFavoritesContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🌌", fontSize = 72.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Brak ulubionych",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Dodaj zdjęcia do ulubionych klikając ❤ na ekranie głównym",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Przewijalna lista ulubionych zdjęć.
 */
@Composable
private fun FavoritesList(
    favorites: List<ApodEntry>,
    onRemoveFavorite: (ApodEntry) -> Unit,
    onItemClick: (ApodEntry) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 16.dp
        )
    ) {
        items(
            items = favorites,
            key = { apod -> apod.date }
        ) { apod ->
            FavoriteItem(
                apod = apod,
                onRemoveFavorite = { onRemoveFavorite(apod) },
                onItemClick = { onItemClick(apod) }
            )
        }
    }
}

/**
 * Pojedyncza karta ulubionego zdjęcia na liście.
 * Po kliknięciu serca pokazuje dialog z pytaniem o potwierdzenie.
 */
@Composable
private fun FavoriteItem(
    apod: ApodEntry,
    onRemoveFavorite: () -> Unit,
    onItemClick: () -> Unit
) {
    // Zmienna przechowująca czy dialog jest aktualnie widoczny.
    // 'remember' – Compose pamięta tę wartość między rysowaniami.
    // 'mutableStateOf(false)' – na start dialog jest ukryty.
    var showDialog by remember { mutableStateOf(false) }

    // Dialog potwierdzenia – widoczny tylko gdy showDialog == true
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                // Użytkownik kliknął poza dialogiem lub wcisnął "wstecz" – chowamy dialog
                showDialog = false
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color(0xFFE53935)
                )
            },
            title = {
                Text(text = "Usuń z ulubionych")
            },
            text = {
                Text(text = "Czy na pewno chcesz usunąć \"${apod.title}\" z ulubionych?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false  // zamknij dialog
                        onRemoveFavorite() // wykonaj usunięcie
                    }
                ) {
                    Text(
                        text = "Usuń",
                        color = Color(0xFFE53935),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text(text = "Anuluj")
                }
            }
        )
    }

    // Karta zdjęcia
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onItemClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Miniaturka zdjęcia lub filmu z YouTube
            val videoId = if (apod.mediaType == "video") VideoUtils.extractYoutubeVideoId(apod.url) else null
            val thumbnailUrl = if (videoId != null) VideoUtils.getYoutubeThumbnailUrl(videoId) else null

            if (apod.mediaType == "image" || thumbnailUrl != null) {
                AsyncImage(
                    model = if (apod.mediaType == "image") apod.url else thumbnailUrl,
                    contentDescription = apod.title,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder dla innych filmów
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF0D1B2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎬", fontSize = 28.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Informacje o zdjęciu
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = apod.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = apod.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                apod.copyright?.let {
                    Text(
                        text = "© $it",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Ikona notatki (wyświetlana tylko jeśli notatka nie jest pusta)
            if (apod.userNote.isNotBlank()) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Ten wpis zawiera notatkę",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(20.dp)
                )
            }

            // Kliknięcie serca → otwiera dialog zamiast od razu usuwać
            IconButton(onClick = { showDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Usuń z ulubionych",
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
