# FoodSnap - Recipe Discovery App

A native Android recipe discovery app built with Kotlin and Jetpack Compose. Point your camera at ingredients or scan barcodes to find recipes, then follow along hands-free with cooking mode.

## Features

### Mobile Features Implemented

1. **Camera + ML Kit** — Three camera modes powered by CameraX:
   - **Barcode mode**: scans product barcodes, looks up the product name via OpenFoodFacts API, then searches Spoonacular for matching recipes
   - **Ingredient mode**: uses ML Kit Image Labeling to identify food items in frame and searches for recipes containing them
   - **Dish mode**: uses ML Kit Image Labeling with a dish mapping table to recognise known dishes and search for their recipes

2. **Room Database** — Offline-first local storage with 6 entities:
   - `Recipe`, `Ingredient`, `RecipeIngredientCrossRef` (many-to-many), `SavedRecipe`, `UserIngredient`, `Comment`
   - Recipes and search results are cached; the app serves from cache before hitting the network

3. **Networking** — Retrofit + Moshi integration with two APIs:
   - [Spoonacular](https://spoonacular.com/food-api) — recipe search, details, random recipes, similar recipes, search by ingredients
   - [OpenFoodFacts](https://world.openfoodfacts.org/) — barcode-to-product-name lookup

4. **Content Provider** — Read-only `RecipeContentProvider` exposes recipes, saved recipes, and ingredients to other apps via standard Android content URIs (`content://com.foodsnap.provider/...`)

5. **Sensors + Speech** — Two sensor-driven features:
   - **Shake-to-random**: accelerometer shake gesture triggers a random recipe suggestion on the home screen
   - **Voice search**: microphone button on the search bar uses Android `SpeechRecognizer` for hands-free query input

6. **Text-to-Speech + Cooking Mode** — A step-by-step cooking mode reads instructions aloud via Android `TextToSpeech`, with previous/next step controls and a progress indicator

### Other Screens

- **Home** — random recipe feed, category filter chips, shake-to-random, voice search
- **Search** — full-text recipe search with results list
- **Recipe Detail** — full recipe info, ingredients, step-by-step instructions, save/unsave, similar recipes
- **Saved Recipes** — saved favourites with swipe-to-delete and undo snackbar
- **Inventory** — personal pantry / user ingredient management
- **Splash** — animated logo on app launch

## App Flow

```
Splash Screen
    └── Home Screen
          ├── Shake device → random recipe
          ├── Voice search → Search Results
          ├── Recipe Card tap → Recipe Detail
          │       └── "Start Cooking" → Cooking Mode (TTS step-by-step)
          ├── Bottom Nav: Search → Search Results
          ├── Bottom Nav: Camera
          │       ├── Barcode mode → scan → search results
          │       ├── Ingredient mode → camera → ML labels → search results
          │       └── Dish mode → camera → dish recognition → search results
          ├── Bottom Nav: Saved Recipes (swipe to delete)
          └── Bottom Nav: Inventory (user pantry)
```

## Architecture

Clean Architecture with three layers:

```
Presentation  ──►  Domain  ──►  Data
(Compose UI        (Use Cases     (Room DB
 ViewModels)        Interfaces)    Retrofit
                                   Repositories)
```

- **Data layer**: Room entities/DAOs, Retrofit API clients, repository implementations, mapper classes
- **Domain layer**: pure Kotlin use cases, repository interfaces, domain models (no Android dependencies)
- **Presentation layer**: Jetpack Compose screens, ViewModels using `StateFlow`, Hilt-injected dependencies

Data flow for recipe search (offline-first):
1. Emit `Loading`
2. Emit cached results from Room if available
3. Fetch from Spoonacular API
4. Cache new results to Room
5. Emit fresh results (or `Error` with stale cache on network failure)

## Tech Stack

| Area | Technology |
|------|-----------|
| Language | Kotlin 100% |
| UI | Jetpack Compose + Material Design 3 |
| Architecture | Clean Architecture + MVVM |
| DI | Hilt (Dagger) |
| Database | Room (6 entities) |
| Networking | Retrofit + Moshi |
| Async | Kotlin Coroutines + Flow |
| Camera | CameraX |
| ML | ML Kit Image Labeling + Barcode Scanning |
| Image Loading | Coil |
| Speech | Android SpeechRecognizer + TextToSpeech |
| Sensors | SensorManager (accelerometer) |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 34 |

## Setup

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### API Key

1. Sign up at [Spoonacular](https://spoonacular.com/food-api) (free tier: 150 points/day)
2. Add to `local.properties` (create the file if it doesn't exist):
   ```
   SPOONACULAR_API_KEY=your_key_here
   ```

### Build & Run

```bash
# Clone
git clone https://github.com/ActuallyAnson/CSD3156_Mobile.git

# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run lint
./gradlew lint

# Build instrumentation test APK (requires emulator/device to run)
./gradlew assembleDebugAndroidTest
```

## Project Structure

```
app/src/main/java/com/foodsnap/
├── data/
│   ├── local/
│   │   ├── database/      # Room entities, DAOs, FoodSnapDatabase
│   │   └── provider/      # RecipeContentProvider + RecipeContract
│   ├── remote/
│   │   ├── api/           # SpoonacularApi, OpenFoodFactsApi
│   │   └── dto/           # Moshi DTOs
│   ├── repository/        # Repository implementations (offline-first)
│   └── mapper/            # DTO ↔ Entity ↔ Domain mappers
├── domain/
│   ├── model/             # Recipe, Ingredient, UserIngredient, Comment
│   ├── repository/        # Repository interfaces
│   └── usecase/           # One use case per operation
├── presentation/
│   ├── screen/            # home, camera, detail, search, saved,
│   │                      # cooking, inventory, splash
│   ├── components/        # RecipeCard, SearchBar, RatingBar, etc.
│   ├── navigation/        # NavGraph, Screen sealed class
│   └── theme/             # Material 3 colours, typography, shapes
├── di/                    # Hilt modules (App, DB, Network, Camera, Repo)
├── ml/                    # BarcodeAnalyzer, ImageLabelAnalyzer,
│                          # DishRecognitionAnalyzer, AnalyzerResult
├── util/                  # Resource wrapper, ShakeDetector,
│                          # SpeechRecognitionHelper, TextToSpeechManager
└── worker/                # WorkManager workers
```

## CI/CD

GitHub Actions runs on every push to `main`/`develop` and on PRs to `main`:

| Job | Runner | What it does |
|-----|--------|-------------|
| Lint | ubuntu-latest | `./gradlew lint` |
| Unit Tests | ubuntu-latest | `./gradlew testDebugUnitTest` |
| Build | ubuntu-latest | `./gradlew assembleDebug` (requires lint + unit tests to pass) |
| Instrumentation Tests | ubuntu-latest + KVM | boots API 29 emulator, runs `connectedDebugAndroidTest` |

## Third-Party Libraries

| Library | Purpose |
|---------|---------|
| [Spoonacular API](https://spoonacular.com/food-api) | Recipe data |
| [OpenFoodFacts API](https://world.openfoodfacts.org/) | Barcode product lookup |
| [ML Kit](https://developers.google.com/ml-kit) | Image labeling, barcode scanning |
| [CameraX](https://developer.android.com/training/camerax) | Camera preview and analysis |
| [Hilt](https://dagger.dev/hilt/) | Dependency injection |
| [Room](https://developer.android.com/training/data-storage/room) | Local database |
| [Retrofit](https://square.github.io/retrofit/) + [Moshi](https://github.com/square/moshi) | Networking + JSON |
| [Coil](https://coil-kt.github.io/coil/) | Async image loading |
| [MockK](https://mockk.io/) | Unit test mocking |

## Team Contributions

| Member | SIT ID | Contributions |
|--------|--------|--------------|
| Anson Teng | 2301360 | Core architecture, data layer, Room database with offline-first caching, Retrofit networking (Spoonacular + OpenFoodFacts APIs), Hilt dependency injection, all critical bug fixes (CI/CD pipeline, Gradle wrapper, KVM emulator, Hilt instrumentation test setup, ContentProvider lazy initialization) |
| Thang Weng Khong | 2301372 | Presentation layer, Jetpack Compose screens, ViewModels, navigation graph, UI polish and Material Design 3 theming |
| Tan Yong Chin | 2301359 | Camera module implementation, ML Kit integration (barcode scanning, image labeling, dish recognition), CameraX analyzer setup and mode switching |
| Yap Zi Yang Irwen | 2301345 | Sensor integration (accelerometer shake detection), voice search (SpeechRecognizer), text-to-speech cooking mode implementation |
| Muhammad Fakhrurazi Bin Helmi | 2303346 | Unit test implementation, instrumentation test setup, CI/CD pipeline configuration and debugging|

## AI Usage Declaration

This project used AI assistance (Claude) during implementation:
- Architecture design and project scaffolding
- Boilerplate and feature code generation (CI/CD pipeline)
- Bug fixing (test compile errors, Hilt instrumentation test setup, emulator configuration)
- Documentation

## License

Educational project — CSD3156 Mobile and Cloud Computing, Spring 2026.
