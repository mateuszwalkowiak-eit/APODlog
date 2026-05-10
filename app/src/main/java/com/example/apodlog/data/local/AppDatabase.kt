package com.example.apodlog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.apodlog.data.model.ApodEntry

/**
 * Główna baza danych aplikacji.
 * Singleton – zawsze korzystamy z jednej instancji przez cały cykl życia aplikacji.
 */
@Database(
    entities = [ApodEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun apodDao(): ApodDao

    companion object {
        // @Volatile zapewnia, że zmiany są natychmiast widoczne dla innych wątków
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "apod_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
