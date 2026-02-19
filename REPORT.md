# CSD3156 Mobile and Cloud Computing — Spring 2026
# Team Project Report: FoodSnap — Recipe Discovery Application

---

## Team Information

| Field | Details |
|-------|---------|
| **Team ID** | TBD |
| **Course** | CSD3156 Mobile and Cloud Computing |
| **Trimester** | Spring 2026 |

| Member | SIT ID | Contributions |
|--------|--------|--------------|
| Anson Teng | 2301360 | Core architecture, data layer, Room database, offline-first caching, Retrofit networking, Hilt DI, bug fixes (CI/CD, KVM, Hilt tests, ContentProvider) |
| Thang Weng Khong | 2301372 | Presentation layer, Compose screens, ViewModels, navigation, UI theming |
| Tan Yong Chin | 2301359 | Camera module, ML Kit integration, barcode/ingredient/dish analyzers |
| Yap Zi Yang Irwen | 2301345 | Sensors (shake detection), voice search, text-to-speech cooking mode |
| Muhammad Fakhrurazi Bin Helmi | 2303346 | Unit & instrumentation tests, CI/CD pipeline|

---

## Links

- **Source Code (GitHub):** https://github.com/ActuallyAnson/CSD3156_Mobile
- **App Demo Video:** TBD
- **Presentation Video:** TBD

---

## 1. Introduction

FoodSnap is a native Android recipe discovery application built entirely in Kotlin using Jetpack Compose. The app solves a common everyday problem: users often have ingredients at home but do not know what to cook with them. FoodSnap bridges this gap by allowing users to scan product barcodes or photograph ingredients using their phone's camera, and immediately receive a list of relevant recipes. Once a recipe is found, users can follow it step-by-step using a hands-free cooking mode that reads instructions aloud.

The application was developed as a fully featured, production-quality Android app demonstrating multiple advanced mobile computing features including machine learning, sensor integration, offline-first data management, content sharing, and multimedia capabilities.

---

## 2. Design

### 2.1 Problem Statement

The core user problem is: *"I have ingredients, but I don't know what to make."* Existing solutions require users to type ingredient names manually, which is slow and error-prone. FoodSnap removes this friction by letting users scan or photograph what they have, then instantly discovering matching recipes.

### 2.2 User Experience Goals

- **Low friction entry**: scanning a barcode or tapping the camera button should get results within seconds
- **Offline resilience**: recipes already viewed should remain accessible without internet
- **Hands-free cooking**: once cooking starts, the user should not need to touch the phone
- **Discoverable**: the home screen surfaces new recipes passively through random suggestions

### 2.3 Key Design Decisions

**Offline-first caching**: All API responses are written to a local Room database before being displayed. On subsequent app launches, cached results are shown immediately while a background network fetch refreshes the data. This means the app is useful even with poor or no connectivity.

**Three camera modes**: Rather than a single generic camera, three specialised modes serve different intents — barcode for packaged products, ingredient recognition for raw food items, and dish recognition for identifying a cooked meal and finding its recipe.

**API point conservation**: The free tier of the Spoonacular API provides only 150 points per day. The app is designed to minimise API calls: the home screen random feed is served entirely from cache after the first load, and recipe detail pages skip the network fetch if the full recipe (including instructions) is already cached.

---

## 3. Architecture

### 3.1 Overview

FoodSnap follows Clean Architecture with three distinct layers, plus a dependency injection layer managed by Hilt:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  Jetpack Compose Screens + ViewModels   │
│  StateFlow  ·  Navigation  ·  Hilt DI   │
└────────────────┬────────────────────────┘
                 │ calls
┌────────────────▼────────────────────────┐
│           Domain Layer                  │
│  Use Cases  ·  Domain Models            │
│  Repository Interfaces (pure Kotlin)    │
└────────────────┬────────────────────────┘
                 │ implements
┌────────────────▼────────────────────────┐
│             Data Layer                  │
│  Room Database  ·  Retrofit APIs        │
│  Repository Implementations  ·  Mappers │
└─────────────────────────────────────────┘
```

This separation ensures that business logic (Domain layer) has zero dependency on Android or any specific framework, making it independently testable with plain JUnit.

### 3.2 Presentation Layer

Each screen is a Jetpack Compose composable backed by a ViewModel. ViewModels hold a single `UiState` data class exposed as `StateFlow`. The UI observes this state and recomposes reactively when it changes. Side effects (navigation, snackbars) are emitted as a `SharedFlow<Event>` to avoid replaying on recomposition.

Navigation is handled by a single `NavGraph` composable using Jetpack Navigation Compose, with a `Screen` sealed class defining all routes and their arguments.

### 3.3 Domain Layer

The domain layer contains only pure Kotlin classes:
- **Domain models**: `Recipe`, `RecipeIngredient`, `Ingredient`, `UserIngredient`, `Comment`
- **Repository interfaces**: contracts that the data layer must fulfil
- **Use cases**: one class per operation (e.g., `SearchRecipesUseCase`, `GetRecipeByIdUseCase`, `SaveRecipeUseCase`). Each use case is a single-method class with `operator fun invoke(...)`, which keeps ViewModels clean and business logic independently testable.

### 3.4 Data Layer

The data layer contains:
- **Room entities and DAOs**: database-specific representations of data
- **Retrofit API interfaces**: `SpoonacularApi`, `OpenFoodFactsApi`
- **Repository implementations**: implement domain interfaces, coordinate between local DB and remote APIs
- **Mapper classes**: `RecipeMapper` converts between DTOs, Room entities, and domain models, keeping each representation independent

### 3.5 Dependency Injection

Hilt (Dagger) manages all dependencies. Five Hilt modules wire the app:
- `DatabaseModule` — provides Room database and all DAOs
- `NetworkModule` — provides Retrofit instances for both APIs, with `ApiKeyInterceptor` injecting the Spoonacular key from `BuildConfig`
- `RepositoryModule` — binds repository interfaces to their implementations
- `CameraModule` — provides ML Kit analyzer instances
- `AppModule` — provides coroutine dispatchers (`IoDispatcher`, `MainDispatcher`)

### 3.6 Data Flow — Offline-First Recipe Search

The following sequence applies to recipe search (and similarly to other data operations):

1. ViewModel calls `SearchRecipesUseCase(query)`
2. Use case calls `RecipeRepository.searchRecipes(query)` which returns a `Flow<Resource<List<Recipe>>>`
3. Repository emits `Resource.Loading`
4. Repository queries Room for cached results matching the query; if found, emits `Resource.Success(cachedData)`
5. Repository fetches from Spoonacular API
6. New results are written to Room (`recipeDao.insertRecipes(entities)`)
7. Repository emits `Resource.Success(freshData)`
8. On network failure: Repository emits `Resource.Error(message, staleCachedData)` — the UI can still show stale data with an error indicator

---

## 4. Implementation

### 4.1 Feature 1: Camera + Machine Learning (ML Kit + CameraX)

**Requirement satisfied**: Camera, Computer Vision / Machine Learning

CameraX provides the camera preview and frame analysis pipeline. Three `ImageAnalysis.Analyzer` implementations handle different recognition tasks:

**Barcode Mode (`BarcodeAnalyzer`)**
ML Kit's `BarcodeScanning` client processes each camera frame. It detects EAN-13, EAN-8, UPC-A, UPC-E, QR Code, Code 128, Code 39, and several other formats. When a barcode value is detected, the app calls the OpenFoodFacts API with the barcode number to look up the product name (e.g., barcode `5000112637922` → "Heinz Tomato Ketchup"). The product name is then used as the search query to Spoonacular, yielding relevant recipes.

**Ingredient Mode (`ImageLabelAnalyzer`)**
ML Kit's `ImageLabeling` client runs on-device classification using the default model. Every camera frame produces a list of detected labels with confidence scores. Labels above a 70% confidence threshold are collected and passed to `searchRecipesByIngredients()`. Because the model returns specific labels such as "Tomato", "Chicken breast", or "Bell pepper" rather than generic ones, this mode produces accurate ingredient-based recipe searches.

**Dish Mode (`DishRecognitionAnalyzer`)**
The same `ImageLabeling` client is used, but results are matched against a hardcoded `dishMappings` table that maps ML Kit label keywords to dish names (e.g., "pizza" → "Pizza", "sushi" → "Sushi"). Only entries present in this table trigger a result, preventing generic labels from producing irrelevant searches. The matched dish name is used as a direct recipe search query.

**Camera mode switching**: A key implementation detail is that `CameraX`'s `AndroidView` factory closure only runs once at composition. Switching between the three modes requires the analyzer binding to be re-established. This is solved by wrapping the `AndroidView` with Compose's `key(uiState.currentMode)`, which forces the composable to be destroyed and re-created whenever the mode changes, re-running the factory and rebinding the correct analyzer.

### 4.2 Feature 2: Room Database — Offline-First Storage

**Requirement satisfied**: Database with multiple tables / content provider

The Room database (`FoodSnapDatabase`) contains six entities:

| Entity | Purpose |
|--------|---------|
| `RecipeEntity` | Full recipe data (title, image, instructions, nutrition) |
| `IngredientEntity` | Ingredient catalogue (name, image, aisle) |
| `RecipeIngredientCrossRef` | Many-to-many join table with amount and unit |
| `SavedRecipeEntity` | User-saved favourites with timestamp and notes |
| `UserIngredientEntity` | User's personal pantry |
| `CommentEntity` | User-written recipe reviews |

Room relations (`@Relation`) define `RecipeWithIngredients` — a composite query that joins recipes with their ingredients via the cross-reference table. Type converters (`Converters.kt`) handle `List<String>` serialisation for instruction steps, cuisine tags, and diet labels.

### 4.3 Feature 3: Networking — Spoonacular + OpenFoodFacts APIs

**Requirement satisfied**: Networking

Two Retrofit instances are configured in `NetworkModule`:

**Spoonacular API** (`SpoonacularApi`): provides recipe search (`/recipes/complexSearch`), recipe detail (`/recipes/{id}/information`), random recipes (`/recipes/random`), search by ingredients (`/recipes/findByIngredients`), and similar recipes (`/recipes/{id}/similar`). The API key is injected automatically by `ApiKeyInterceptor` as a query parameter on every request, keeping it out of all call sites.

**OpenFoodFacts API** (`OpenFoodFactsApi`): a free, open database of food products. The app calls `/api/v0/product/{barcode}.json` to resolve a scanned barcode to a product name. No API key is required.

Both APIs use Moshi for JSON deserialisation with KSP-generated adapters (`@JsonClass(generateAdapter = true)`), avoiding reflection at runtime.

### 4.4 Feature 4: Content Provider

**Requirement satisfied**: Content Provider

`RecipeContentProvider` is a read-only `ContentProvider` that exposes the local recipe database to other apps through standard Android content URIs:

| URI | Returns |
|-----|---------|
| `content://com.foodsnap.provider/recipes` | All cached recipes |
| `content://com.foodsnap.provider/recipes/{id}` | Single recipe by ID |
| `content://com.foodsnap.provider/saved_recipes` | User's saved recipes |
| `content://com.foodsnap.provider/ingredients` | All ingredients |

The provider is read-only: `insert()`, `update()`, and `delete()` return `null`/`0`. `getType()` returns correct MIME types for both list and item URIs. An important implementation detail is that Hilt entry points cannot be accessed in `ContentProvider.onCreate()` during test execution because the Hilt component is not yet initialised at app startup in the test environment. The solution is lazy initialisation: the `RecipeContentProviderEntryPoint` is declared as a `by lazy` delegate, so it is only resolved on the first actual query, by which point the Hilt test component is ready.

### 4.5 Feature 5: Sensors + Speech Recognition

**Requirement satisfied**: Sensors, Multimedia

**Shake-to-Random (`ShakeDetector`)**: registers a `SensorEventListener` on `Sensor.TYPE_ACCELEROMETER`. On each sensor event, the magnitude of acceleration change across all three axes is computed:

```
speed = √((Δx² + Δy² + Δz²)) / Δt × 10000
```

If `speed > 800` (the shake threshold) and at least 1000ms have passed since the last shake (cooldown), the `onShake` callback fires. In `HomeViewModel`, this triggers `getRandomRecipesUseCase()`, refreshing the recipe feed with a new random selection. The detector is registered in `onResume` and unregistered in `onPause` to avoid battery drain.

**Voice Search (`SpeechRecognitionHelper`)**: wraps Android's `SpeechRecognizer` API. A microphone button on the home screen's search bar requests `RECORD_AUDIO` permission at runtime (added to `AndroidManifest.xml`), then starts a recognition session with `RecognizerIntent.ACTION_RECOGNIZE_SPEECH`. The best recognised phrase is returned as the search query string.

### 4.6 Feature 6: Text-to-Speech + Cooking Mode

**Requirement satisfied**: Multimedia, Multi-threading

**`TextToSpeechManager`**: wraps Android's `TextToSpeech` engine. Initialisation is asynchronous; the manager exposes `isSpeaking: StateFlow<Boolean>` so the UI can reactively show play/pause state. `speakStep(step, stepNumber)` prepends "Step N." to the text before speaking, using `QUEUE_FLUSH` to interrupt any in-progress speech immediately. Resources are released via `shutdown()` in a `DisposableEffect` when the composable leaves the composition.

**`CookingModeScreen`**: a full-screen composable providing:
- A dot-based progress indicator showing completed, current, and upcoming steps
- `AnimatedContent` with directional slide transitions (left for forward, right for backward)
- Horizontal swipe gesture detection (`detectHorizontalDragGestures`) as an alternative to the previous/next buttons
- A TTS toggle button; when enabled, the current step is spoken automatically whenever the step index changes (via a `DisposableEffect` keyed on `currentStep` and `ttsEnabled`)
- Play/pause button to replay the current step on demand

---

## 5. Results and Evaluation

### 5.1 Features Delivered

| Feature | Status | Notes |
|---------|--------|-------|
| Barcode scan → recipe search | Working | Dependent on product being in OpenFoodFacts database |
| Ingredient camera mode | Working | Accuracy depends on lighting and camera angle |
| Dish recognition mode | Working | Limited to dishes in the mapping table |
| Offline-first caching | Working | All viewed recipes available without internet |
| Recipe search (text + voice) | Working | Voice requires RECORD_AUDIO permission |
| Save / unsave recipes | Working | Swipe-to-delete with undo |
| Cooking mode with TTS | Working | Hands-free, step-by-step |
| Shake-to-random | Working | 1-second cooldown prevents double-trigger |
| Content Provider | Working | Read-only, verified by instrumentation tests |
| User inventory / pantry | Working | Add and remove personal ingredients |

### 5.2 Limitations

- **Spoonacular free tier**: 150 API points per day. Each search costs approximately 1–2 points. The app mitigates this with aggressive caching (random recipe feed served from cache after first load; recipe detail skips network if instructions are already cached), but heavy daily use can exhaust the quota.
- **ML Kit label accuracy**: Image labeling accuracy depends on lighting conditions, image clarity, and subject isolation. Generic or cluttered backgrounds can produce low-confidence or irrelevant labels.
- **Dish recognition scope**: The dish mapping table covers a predefined set of dishes. Uncommon or regional dishes not in the table will not be recognised.
- **OpenFoodFacts coverage**: Not all product barcodes are in the OpenFoodFacts database. Regional or less common products may return no result, in which case the app falls back to searching by the raw barcode number.

### 5.3 Testing

**Unit Tests** (`app/src/test/`):
- `RecipeRepositoryImplTest` — verifies the offline-first flow: cache hit, network fetch and cache, error fallback to stale cache, and that `Loading` is always emitted first. Uses MockK to mock the DAO and API interfaces.
- `HomeViewModelTest` — verifies that the ViewModel emits loading state, success state, and handles errors correctly.
- `SavedRecipesViewModelTest` — verifies loading, empty state, and the remove recipe flow.
- `SearchRecipesUseCaseTest` — verifies the use case delegates correctly to the repository.
- `BarcodeAnalyzerTest` — verifies the `AnalyzerResult` sealed class data structures and label filtering logic.

**Instrumentation Tests** (`app/src/androidTest/`):
- `RecipeDaoTest` — verifies all Room DAO operations against an in-memory database: insert, query all, query by ID, search, and delete.
- `RecipeContentProviderTest` — verifies all content URI queries return valid cursors, correct MIME types, correct column projections, and that write operations (insert/update/delete) correctly return null/0 for the read-only provider. Uses Hilt instrumentation test infrastructure (`@HiltAndroidTest`, `HiltAndroidRule`, `HiltTestRunner`).

---

## 6. Software Engineering Practices

### 6.1 Version Control

The project uses Git with GitHub. Development follows a main/develop branching strategy with regular commits. Commit history documents the progression of features from initial project setup through to final polish.

### 6.2 CI/CD Pipeline

A four-job GitHub Actions workflow runs automatically on every push to `main` or `develop`, and on every pull request targeting `main`:

```
Push / PR
    │
    ├── [Lint]           ubuntu-latest   ./gradlew lint
    │
    ├── [Unit Tests]     ubuntu-latest   ./gradlew testDebugUnitTest
    │
    ├── [Build] ─────── depends on Lint + Unit Tests passing
    │                    ubuntu-latest   ./gradlew assembleDebug
    │
    └── [Instrumentation Tests]
                         ubuntu-latest + KVM acceleration
                         API 29 emulator (x86_64, Pixel 2 profile)
                         ./gradlew connectedDebugAndroidTest
```

Key CI decisions:
- **Gradle distribution caching** via `gradle/actions/setup-gradle@v4` prevents repeated 100MB+ distribution downloads on every run
- **KVM acceleration** on the Linux runner (configured via udev rules) enables the Android emulator to boot in under 3 minutes rather than timing out
- **Emulator options** (`-no-window -no-audio -no-boot-anim -gpu swiftshader_indirect`) minimise startup time in the headless CI environment
- The `packaging.resources` block excludes duplicate `META-INF/LICENSE.md` files from JUnit Jupiter jars, preventing the `mergeDebugAndroidTestJavaResource` task from failing

### 6.3 Code Organisation

The codebase follows Clean Architecture strictly: the domain module has no Android or third-party library imports. The data layer depends on domain interfaces, not the other way around. This is enforced structurally — if a domain class accidentally imports an Android class, it will fail to compile in a pure JVM unit test environment.

All public classes, functions, and properties are documented with KDoc. The `Resource<T>` sealed class (`Loading`, `Success`, `Error`) provides a consistent pattern for representing async operation state throughout the app, eliminating ad-hoc `isLoading`/`error` boolean flags in ViewModels.

---

## 7. Third-Party Libraries and Acknowledgements

All third-party dependencies are declared in `gradle/libs.versions.toml` (version catalogue) and listed below:

| Library | Version | Purpose | License |
|---------|---------|---------|---------|
| Jetpack Compose BOM | 2024.09.00 | UI framework | Apache 2.0 |
| Hilt | 2.52 | Dependency injection | Apache 2.0 |
| Room | 2.6.1 | Local database | Apache 2.0 |
| Retrofit | 2.11.0 | HTTP networking | Apache 2.0 |
| Moshi | 1.15.1 | JSON serialisation | Apache 2.0 |
| CameraX | 1.3.4 | Camera access and analysis | Apache 2.0 |
| ML Kit Image Labeling | 17.0.9 | On-device food recognition | Apache 2.0 |
| ML Kit Barcode Scanning | 17.3.0 | Barcode detection | Apache 2.0 |
| Coil | 2.7.0 | Async image loading | Apache 2.0 |
| Kotlin Coroutines | 1.9.0 | Async programming | Apache 2.0 |
| MockK | 1.13.12 | Unit test mocking | Apache 2.0 |
| Spoonacular API | — | Recipe data source | Commercial (free tier) |
| OpenFoodFacts API | — | Barcode product lookup | Open Database License |

No assets from previous game projects were reused in this application. All UI assets (icons, colours) are from Material Design 3 / Google Fonts.

---

## 8. AI Usage Declaration

This project used AI assistance (Claude by Anthropic) during development. The following describes how AI was utilised:

**Design and Architecture**
- Prompted for Clean Architecture folder structure and layer separation guidance
- Example prompt: *"Set up a Kotlin Android project with Clean Architecture layers, Hilt DI, Room database, and Retrofit. Show me the folder structure and Hilt module setup."*

**Feature Implementation**
- Prompted for boilerplate code generation for Room entities, DAOs, Retrofit interfaces, Hilt modules, and repository patterns
- Example prompt: *"Write a Room DAO for a Recipe entity with search by title, insert, get by ID, and delete operations."*

**Bug Fixing**
- CI/CD pipeline fixes: Gradle wrapper network timeout, `gradle/actions/setup-gradle@v4` migration, KVM emulator configuration
- Test compile errors: correcting domain model constructor calls in unit tests, MockK suspension function mocking patterns
- Hilt instrumentation test setup: `HiltTestRunner`, `@HiltAndroidTest`, `HiltAndroidRule` configuration
- Content Provider lazy initialisation fix to resolve Hilt component not-ready crash in test environment

**Documentation**
- README structure and content
- This report

All AI-generated code was read, understood, and where necessary modified by team members before being committed. No code was accepted without review.

---

*Report prepared for CSD3156 Mobile and Cloud Computing, Spring 2026 submission.*
*Deadline: 24 February 2026, 23:59*
