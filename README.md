# 🍝 Le mie Ricette - Application Android

**Le mie Ricette** è un'applicazione Android nativa sviluppata in **Kotlin** e **Jetpack Compose** progettata per la gestione personalizzata delle ricette da cucina, l'importazione automatica da siti web e social media (Reel/Video), il backup dei dati e la sincronizzazione cloud.

---

## ✨ Caratteristiche Principali

- 🎨 **Interfaccia Grafica Moderna & Personalizzabile**:
  - Design basato su **Material 3** con palette di colori "Terracotta & Crema".
  - Supporto al **Tema Scuro** dedicato.
  - Selezione dello stile tipografico tra **Standard (Lineare)** e **Corsivo (Scrittura a Mano)** per un look da ricettario casalingo.
  - Opzione **Schermo Sempre Acceso** per consultare le ricette in cucina senza timeout del display.

- 🏠 **Home Categoriale & Ricerca Globale**:
  - Griglia adattiva per accedere direttamente alle categorie (Primi, Secondi, Dolci, ecc.).
  - Filtri per stato di validazione (**"Da provare"** vs **"Provate"**), categoria, tempo massimo di preparazione ed ordinamento alfabetico A-Z.

- 🌐 **Importazione Web Intelligente (`RecipeScraper`)**:
  - Browser interno integrato e supporto a **Share Target** (condivisione link da altre app come Facebook, Instagram, TikTok o il browser di sistema).
  - Estrazione automatica di titolo, descrizione, porzioni, tempi, ingredienti, istruzioni ed immagine da schemi **JSON-LD (schema.org/Recipe)**, Microdata ed euristiche HTML Jsoup.
  - Srotolamento automatico di link brevi e reindirizzamenti.

- ✍️ **Inserimento & Modifica Semplificati**:
  - Campi di testo libero multilinea per ingredienti e preparazione per una compilazione immediata.
  - Possibilità di aggiungere immagini scattate dalla fotocamera o dalla galleria.
  - Campo opzionale per il link web di riferimento.

- 📦 **Backup & Ripristino Locale in formato ZIP (SAF)**:
  - Generazione di un archivio ZIP contenente il database delle ricette in formato JSON e tutti i file fotografici locali.
  - Selezione libera della cartella di destinazione/origine tramite Storage Access Framework.

- ☁️ **Sincronizzazione Cloud Google Drive & WorkManager**:
  - Backup e ripristino su Google Drive (`appDataFolder`).
  - Sincronizzazione automatica programmata in background ogni 24 ore tramite **WorkManager** e Hilt.

- 🧹 **Manutenzione & Pulizia Intelligente**:
  - Strumento per la **scansione ed eliminazione delle foto orfane** non più collegate ad alcuna ricetta per liberare spazio sul telefono.
  - Cancellazione automatica dei file immagine quando una ricetta viene eliminata o quando ne viene sostituita la foto.

---

## 🛠️ Stack Tecnologico

- **Linguaggio**: Kotlin 100%
- **Architettura**: Clean Architecture + MVVM (Model-View-ViewModel) + Repository Pattern
- **UI Toolkit**: Jetpack Compose + Material 3 Navigation
- **Persistenza Dati**: Room Database + DataStore Preferences
- **Dependency Injection**: Hilt (Dagger)
- **Programmazione Asincrona**: Kotlin Coroutines + StateFlow
- **Networking & Scraping**: Retrofit 2, OkHttp 4, Jsoup, kotlinx.serialization
- **Gestione Immagini**: Coil
- **Background Operations**: WorkManager + Hilt Worker Integration
- **Autenticazione & Cloud**: Google Play Services Auth + Google Drive API v3

---

## 📱 Requisiti di Sistema

- **Android Minimo**: Android 8.0 (API livello 26 - Oreo)
- **Target SDK**: Android 15 (API livello 35)

---

## 🚀 Come Compilare il Progetto

1. Clona il repository:
   ```bash
   git clone https://github.com/Fedtorrent/LeMieRicette.git
   ```
2. Apri il progetto con **Android Studio** (versione Ladybug o superiore).
3. Esegui la sincronizzazione dei file Gradle.
4. Avvia l'applicazione su un emulatore o un dispositivo fisico Android.

Per generare l'APK rilasciabile:
```bash
./gradlew assembleRelease
```
L'APK generato sarà disponibile in `app/build/outputs/apk/release/LeMieRicette.apk`.

---

## 👤 Autore

Sviluppato per uso personale da **Fulvio**.
