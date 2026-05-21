package com.example.apodlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.apodlog.ui.navigation.AppBottomNavigationBar
import com.example.apodlog.ui.navigation.AppNavHost
import com.example.apodlog.ui.theme.APODlogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APODlogTheme {
                // rememberNavController() tworzy i zapamiętuje kontroler nawigacji
                val navController = rememberNavController()

                // Scaffold daje nam strukturę ekranu z miejscem na pasek nawigacji na dole
                Scaffold(
                    bottomBar = {
                        // Dolny pasek nawigacji dostaje navController
                        // żeby wiedzieć który ekran jest aktualnie wybrany
                        AppBottomNavigationBar(navController = navController)
                    }
                ) { innerPadding ->
                    // NavHost wyświetla odpowiedni ekran w zależności od trasy
                    // innerPadding zapewnia że treść nie jest zakryta przez dolny pasek
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}