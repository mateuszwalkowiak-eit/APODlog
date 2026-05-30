package com.example.apodlog.utils

import android.content.Context
import android.content.Intent
import android.net.Uri

object VideoUtils {

    /**
     * Metoda wyciąga ID filmiku z linku YouTube.
     * Sprawdza trzy najpopularniejsze formaty linków.
     * Jeśli to nie jest YouTube, zwraca null.
     */
    fun extractYoutubeVideoId(url: String): String? {
        // Format 1: https://www.youtube.com/embed/dQw4w9WgXcQ?rel=0
        if (url.contains("youtube.com/embed/")) {
            val poEmbed = url.substringAfter("embed/")
            // Obcinamy ewentualne parametry po pytajniku lub ukośniku
            return poEmbed.substringBefore("?").substringBefore("/")
        }
        
        // Format 2: https://youtu.be/dQw4w9WgXcQ
        if (url.contains("youtu.be/")) {
            val poShort = url.substringAfter("youtu.be/")
            return poShort.substringBefore("?").substringBefore("/")
        }
        
        // Format 3: https://www.youtube.com/watch?v=dQw4w9WgXcQ
        if (url.contains("youtube.com/watch")) {
            val poV = url.substringAfter("v=")
            return poV.substringBefore("&").substringBefore("/")
        }
        
        // Jeśli to nie jest żaden ze znanych linków YouTube, zwracamy null
        return null
    }

    /**
     * Zwraca adres URL do miniatury filmu na serwerach YouTube.
     */
    fun getYoutubeThumbnailUrl(videoId: String): String {
        // Korzystamy z oficjalnego serwera grafik YouTube
        return "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
    }

    /**
     * Otwiera filmik w zewnętrznej przeglądarce lub w oficjalnej aplikacji YouTube.
     * Automatycznie przekształca linki typu embed na zwykłe linki (watch), aby zapobiec błędom odtwarzania.
     */
    fun openVideoInExternalApp(context: Context, url: String) {
        try {
            // Wyciągamy ID wideo z YouTube (jeśli link pochodzi z YouTube)
            val videoId = extractYoutubeVideoId(url)
            
            // Jeśli to link YouTube, zamieniamy go na standardowy link do oglądania (watch)
            val finalUrl = if (videoId != null) {
                "https://www.youtube.com/watch?v=$videoId"
            } else {
                url
            }

            // Intent o akcji VIEW mówi systemowi: "pokaż ten zasób"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(finalUrl))
            
            // Uruchamiamy przeglądarkę lub aplikację YouTube
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
