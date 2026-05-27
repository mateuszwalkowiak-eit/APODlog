# APODlog 🚀

**APODlog** to aplikacja mobilna na platformę Android, służąca do przeglądania **Astronomicznego Zdjęcia Dnia** (Astronomy Picture of the Day – APOD) udostępnianego przez publiczne API NASA. 

Aplikacja została zaprojektowana z myślą o nowoczesnym UI (Jetpack Compose), wydajności i działaniu offline (strategia cache-first).

---

## 🎬 Demo aplikacji

[Prezentacja działania aplikacji](https://drive.google.com/file/d/1-5WdALv-qXnem5iOQ2cezL-sw_03yHf6/view?usp=sharing)

---

## 🌟 Funkcje (Obecne - Etap 1)
- **Codzienne zdjęcia kosmosu**: Pobieranie i wyświetlanie najnowszego obrazu/filmu udostępnionego przez NASA.
- **Wsparcie dla trybu Offline**: Aplikacja zapisuje pobrane dane w lokalnej bazie, dzięki czemu ostatnio pobrane zdjęcia można przeglądać bez dostępu do Internetu.
- **Dodawanie do ulubionych**: Możliwość polubienia zdjęcia (animowany przycisk ❤️).
- **Automatyczny motyw**: Interfejs automatycznie dostosowuje się do trybu (jasny/ciemny) systemu Android.
- **Ujednolicone UI**: Customowa ikona aplikacji (teleskop) spójna z brandingiem wewnątrz aplikacji.

## 🚧 Planowane Funkcje (Etap 2)
- [x] **Ekran ulubionych**: Lista zapisanych zdjęć z bazy Room z możliwością przeglądania.
- [x] **Nawigacja**: Przechodzenie między ekranem głównym a listą ulubionych i szczegółami (Navigation Compose).
- [x] **Ekran szczegółów i notatki**: Pełnoekranowy widok z opcją dodania własnej notatki użytkownika do konkretnego dnia.
- [x] **Zapis zdjęć**: Pobieranie obrazów do pamięci lokalnej urządzenia (MediaStore API).
- [x] **Obsługa wideo**: Poprawione wyświetlanie miniaturek i linkowanie do serwisu YouTube.

---

## 🛠️ Technologie i Architektura

Aplikacja została zbudowana zgodnie z zaleceniami Google, wykorzystując wzorzec architektoniczny **MVVM** (Model-View-ViewModel).

### Stos Technologiczny:
* **Język:** Kotlin (2.1.0)
* **UI:** Jetpack Compose, Material Design 3
* **Architektura:** ViewModel, StateFlow / Coroutines
* **Baza danych (Lokalnie):** Room (SQLite)
* **Sieć (API):** Retrofit 2 + Kotlinx Serialization
* **Ładowanie obrazów:** Coil
* **Inne:** KSP

### Diagram Architektury (MVVM + Repository)
```text
┌──────────────────────────────────────────────────┐
│                    UI (View)                     │
│         HomeScreen.kt – Jetpack Compose          │
└──────────────────┬───────────────────────────────┘
                   │ collectAsState()
┌──────────────────▼───────────────────────────────┐
│              ViewModel                           │
│         ApodViewModel.kt                         │
└──────────────────┬───────────────────────────────┘
                   │
┌──────────────────▼───────────────────────────────┐
│             Repository                           │
│         ApodRepository.kt                        │
│   (Cache-first: Room → API → Room)               │
└──────────┬───────────────────┬───────────────────┘
           │                   │
┌──────────▼──────┐  ┌────────▼────────────────────┐
│  Warstwa lokalna │  │    Warstwa sieciowa         │
│  Room Database   │  │    Retrofit + NASA API      │
└─────────────────┘  └────────────────────────────┘
```

---
