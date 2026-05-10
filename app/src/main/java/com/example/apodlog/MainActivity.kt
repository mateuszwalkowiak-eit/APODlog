package com.example.apodlog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.apodlog.ui.home.HomeScreen
import com.example.apodlog.ui.theme.APODlogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            APODlogTheme {
                HomeScreen()
            }
        }
    }
}