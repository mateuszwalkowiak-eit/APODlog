package com.example.apodlog.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.apodlog.ui.details.DetailsScreen
import com.example.apodlog.ui.favorites.FavoritesScreen
import com.example.apodlog.ui.home.HomeScreen

/**
 * Obiekt przechowujący "adresy" (trasy) ekranów w aplikacji.
 * Zamiast wpisywać ręcznie Stringi w każdym miejscu, używamy stałych.
 */
object Destinations {
    const val HOME = "home"
    const val FAVORITES = "favorites"
    
    // Nowa trasa dla szczegółów. "{date}" to parametr, który będziemy przekazywać
    const val DETAILS = "details/{date}"

    // Prosta funkcja, która tworzy poprawny adres (np. "details/2026-05-23")
    fun detailsRoute(date: String): String {
        return "details/$date"
    }
}

/**
 * Klasa opisująca jeden element dolnego paska nawigacji.
 *
 * @param route    Trasa (adres) ekranu
 * @param label    Tekst wyświetlany pod ikoną
 * @param icon     Ikona Material Icons
 */
data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Lista wszystkich elementów dolnego paska.
 */
val bottomNavItems = listOf(
    BottomNavItem(
        route = Destinations.HOME,
        label = "Zdjęcie dnia",
        icon = Icons.Default.Home
    ),
    BottomNavItem(
        route = Destinations.FAVORITES,
        label = "Ulubione",
        icon = Icons.Default.Favorite
    )
)

/**
 * Dolny pasek nawigacji (Navigation Bar).
 * Wyświetla ikony i podświetla aktualnie wybrany ekran.
 *
 * @param navController  Kontroler nawigacji – wie gdzie jesteśmy
 */
@Composable
fun AppBottomNavigationBar(navController: NavHostController) {

    // currentBackStackEntryAsState() zwraca aktualny wpis nawigacji jako State.
    // Dzięki 'by' Compose wie kiedy go odświeżyć.
    val backStackEntry by navController.currentBackStackEntryAsState()

    // Pobieramy trasę aktualnie widocznego ekranu
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        // Dla każdego elementu na liście tworzymy pozycję w pasku
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                // Czy ta pozycja jest aktualnie zaznaczona?
                selected = currentRoute == item.route,
                onClick = {
                    // Nawigujemy do wybranego ekranu
                    navController.navigate(item.route) {
                        // popUpTo usuwa ekrany ze stosu nawigacji aż do "home"
                        // żeby przycisk "wstecz" nie przechodził przez każdy ekran
                        popUpTo(Destinations.HOME) {
                            saveState = true
                        }
                        // Nie otwieraj wielu kopii tego samego ekranu
                        launchSingleTop = true
                        // Przywróć stan ekranu jeśli wcześniej tam byliśmy
                        restoreState = true
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(text = item.label)
                }
            )
        }
    }
}

/**
 * Główny host nawigacji – definiuje które ekrany istnieją w aplikacji
 * i jak się do nich dostać.
 *
 * @param navController  Kontroler nawigacji przekazany z zewnątrz
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Destinations.HOME,
        modifier = modifier
    ) {
        // Definicja ekranu głównego
        composable(route = Destinations.HOME) {
            HomeScreen()
        }

        // Definicja ekranu ulubionych
        composable(route = Destinations.FAVORITES) {
            FavoritesScreen(
                onItemClick = { apod ->
                    // Po kliknięciu na zdjęcie/kartę przechodzimy do szczegółów
                    navController.navigate(Destinations.detailsRoute(apod.date))
                }
            )
        }

        // Definicja ekranu szczegółów (DetailsScreen)
        composable(
            route = Destinations.DETAILS,
            arguments = listOf(
                navArgument("date") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->
            // Wyciągamy przekazaną datę z argumentów nawigacji
            val date = backStackEntry.arguments?.getString("date") ?: ""
            DetailsScreen(
                date = date,
                onBackClick = {
                    // Po kliknięciu "Wstecz" cofamy się na poprzedni ekran
                    navController.popBackStack()
                }
            )
        }
    }
}
