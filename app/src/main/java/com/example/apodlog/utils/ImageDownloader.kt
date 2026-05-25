package com.example.apodlog.utils

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

/**
 * Narzędzie do pobierania zdjęć z sieci i zapisywania ich w galerii urządzenia.
 */
object ImageDownloader {

    private val client = OkHttpClient()

    /**
     * Pobiera zdjęcie z podanego URL i zapisuje do galerii urządzenia (Pictures/APODlog).
     * Działa asynchronicznie na wątku IO.
     */
    suspend fun downloadImage(context: Context, url: String, title: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Pobranie bajtów obrazu za pomocą OkHttp
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("Błąd serwera (kod: ${response.code})"))
                }

                val bytes = response.body?.bytes() ?: return@withContext Result.failure(Exception("Pusty plik obrazu"))

                // 2. Wygenerowanie bezpiecznej nazwy pliku
                val cleanTitle = title.replace(Regex("[^a-zA-Z0-9]"), "_")
                val filename = "APOD_${cleanTitle}_${System.currentTimeMillis()}.jpg"

                // 3. Zapis do pamięci zewnętrznej (MediaStore API lub klasyczny FileOutputStream)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/APODlog")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val resolver = context.contentResolver
                    val collection = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val uri = resolver.insert(collection, contentValues)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outputStream ->
                            outputStream.write(bytes)
                        }

                        // Wyczyszczenie flagi IS_PENDING, aby zdjęcie było widoczne w galerii
                        contentValues.clear()
                        contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)

                        Result.success("Zapisano w galerii (Pictures/APODlog)")
                    } else {
                        Result.failure(Exception("Nie udało się utworzyć wpisu w MediaStore"))
                    }
                } else {
                    // Dla starszych wersji Androida (API 26-28)
                    @Suppress("DEPRECATION")
                    val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    val appDir = File(picturesDir, "APODlog")
                    if (!appDir.exists() && !appDir.mkdirs()) {
                        return@withContext Result.failure(Exception("Nie udało się utworzyć folderu zapisu"))
                    }

                    val file = File(appDir, filename)
                    FileOutputStream(file).use { outputStream ->
                        outputStream.write(bytes)
                    }

                    // Wymuszenie przeskanowania pliku przez system, by pojawił się w galerii
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.insertImage(
                        context.contentResolver,
                        file.absolutePath,
                        file.name,
                        "APOD: $title"
                    )

                    Result.success("Zapisano w: Pictures/APODlog/${file.name}")
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
