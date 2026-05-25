package com.example.apodlog.ui.details

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.apodlog.data.model.ApodEntry
import com.example.apodlog.ui.components.AppTopBar
import com.example.apodlog.utils.ImageDownloader
import kotlinx.coroutines.launch

/**
 * Ekran szczegółów zdjęcia APOD.
 * Pozwala na:
 * - Przeglądanie zdjęcia/filmu w pełnym rozmiarze
 * - Odczytywanie i pisanie notatek (wyświetlanych między nagłówkiem a opisem)
 * - Dodawanie/usuwanie z ulubionych
 */
@Composable
fun DetailsScreen(
    date: String,
    onBackClick: () -> Unit,
    viewModel: DetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Wczytaj szczegóły po zmianie daty
    LaunchedEffect(date) {
        viewModel.loadApod(date)
    }

    Scaffold(
        topBar = {
            AppTopBar(
                title = "Szczegóły",
                onBackClick = onBackClick
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (val state = uiState) {
                is DetailsUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                is DetailsUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.loadApod(date) }
                    )
                }
                is DetailsUiState.Success -> {
                    DetailsContent(
                        apod = state.apod,
                        onToggleFavorite = { viewModel.toggleFavorite(state.apod) },
                        onSaveNote = { note ->
                            viewModel.saveNote(state.apod, note) {
                                Toast.makeText(context, "Zapisano notatkę! 📝", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailsContent(
    apod: ApodEntry,
    onToggleFavorite: () -> Unit,
    onSaveNote: (String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isDownloading by remember { mutableStateOf(false) }

    // Launcher do zapytania o uprawnienie do zapisu (dla Androida 9 i starszych)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isDownloading = true
            Toast.makeText(context, "Uprawnienie przyznane. Rozpoczynanie pobierania...", Toast.LENGTH_SHORT).show()
            coroutineScope.launch {
                val result = ImageDownloader.downloadImage(context, apod.url, apod.title)
                isDownloading = false
                result.fold(
                    onSuccess = { msg -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show() },
                    onFailure = { err -> Toast.makeText(context, "Błąd: ${err.message}", Toast.LENGTH_LONG).show() }
                )
            }
        } else {
            Toast.makeText(context, "Nie można zapisać zdjęcia bez przyznania uprawnień zapisu", Toast.LENGTH_LONG).show()
        }
    }

    // Stan wpisanej notatki w polu tekstowym
    var noteInput by remember { mutableStateOf("") }

    // Gdy wczytamy dane z bazy, zaktualizuj wpisaną notatkę
    LaunchedEffect(apod.userNote) {
        noteInput = apod.userNote
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // --- Zdjęcie / placeholder wideo ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        ) {
            if (apod.mediaType == "image") {
                AsyncImage(
                    model = apod.url,
                    contentDescription = apod.title,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF0D1B2A), Color(0xFF1B2A4A))
                            )
                        )
                        .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎬", fontSize = 80.sp)
                }
            }

            // Subtelny gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                            )
                        )
                    )
            )
        }

        // --- Karta z informacjami ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Tytuł i Przyciski
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = apod.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Przycisk pobierania (tylko dla zdjęć)
                        if (apod.mediaType == "image") {
                            IconButton(
                                onClick = {
                                    if (!isDownloading) {
                                        // Na Androidzie 10+ (Q) nie potrzebujemy pytać o uprawnienia zapisu do galerii
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ||
                                            ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            isDownloading = true
                                            Toast.makeText(context, "Pobieranie zdjęcia...", Toast.LENGTH_SHORT).show()
                                            coroutineScope.launch {
                                                val result = ImageDownloader.downloadImage(context, apod.url, apod.title)
                                                isDownloading = false
                                                result.fold(
                                                    onSuccess = { msg ->
                                                        Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                                    },
                                                    onFailure = { err ->
                                                        Toast.makeText(context, "Błąd: ${err.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                )
                                            }
                                        } else {
                                            // Pytamy użytkownika o uprawnienie na starszych wersjach systemu
                                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        }
                                    }
                                }
                            ) {
                                                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Pobierz",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        FavoriteButton(
                            isFavorite = apod.isFavorite,
                            onClick = onToggleFavorite
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Data
                Text(
                    text = apod.date,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                // Copyright
                apod.copyright?.let { copyright ->
                    Text(
                        text = "© $copyright",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontStyle = FontStyle.Italic
                    )
                }

                if (apod.mediaType == "video") {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "📹 APOD to film. Otwórz link: ${apod.url}",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // -----------------------------------------------------------
                // WYŚWIETLENIE NOTATKI (MIĘDZY TYTUŁEM A OPISEM)
                // -----------------------------------------------------------
                if (apod.userNote.isNotBlank()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Moja notatka 📝",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = apod.userNote,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Wyjaśnienie/Opis
                Text(
                    text = apod.explanation,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Sekcja edycji notatki ---
                if (apod.isFavorite) {
                    Text(
                        text = "Edytuj notatkę ✍️",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteInput,
                        onValueChange = { noteInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Wpisz tutaj swoje notatki kosmiczne...") },
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onSaveNote(noteInput) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Zapisz notatkę")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, onClick: () -> Unit) {
    val iconColor by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "favoriteColor"
    )
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "favoriteScale"
    )

    IconButton(
        onClick = onClick,
        modifier = Modifier.scale(scale)
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (isFavorite) "Usuń z ulubionych" else "Dodaj do ulubionych",
            tint = iconColor,
            modifier = Modifier.size(28.dp)
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(text = "🌑", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Coś poszło nie tak",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Spróbuj ponownie")
            }
        }
    }
}
