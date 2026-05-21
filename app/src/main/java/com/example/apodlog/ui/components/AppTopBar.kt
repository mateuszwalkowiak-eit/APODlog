package com.example.apodlog.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.apodlog.R

/**
 * Kompaktowy pasek tytułowy aplikacji.
 * statusBarsPadding() dodaje padding WEWNĄTRZ Row (nie tworzy osobnego pustego miejsca),
 * dzięki czemu ikona i tekst siedzą tuż pod paskiem statusu.
 */
@Composable
fun AppTopBar(title: String) {
    // Kolor tła taki sam jak reszta aplikacji – pasek statusu (czas, bateria)
    // jest przezroczysty dzięki enableEdgeToEdge() w MainActivity,
    // więc "wtapia się" w to samo tło.
    Surface(
        color = MaterialTheme.colorScheme.background,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF0D1B2A)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo APODlog",
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}
