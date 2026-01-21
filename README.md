# FoodSnap - Recipe Discovery App

A native Android recipe discovery app built with Kotlin and Jetpack Compose. Users can scan ingredient barcodes or take photos of ingredients to find recipes, preview dishes in AR, and manage their personal cookbook.

## Features

### 6 Advanced Mobile Features

1. **Camera + ML Kit** - Barcode scanning, image labeling for ingredient recognition, and dish recognition
2. **Room Database** - 6 entities with complex relationships (Recipe, Ingredient, RecipeIngredientCrossRef, SavedRecipe, UserIngredient, Comment)
3. **Networking** - Retrofit integration with Spoonacular API and OpenFoodFacts API
4. **Content Provider** - Share recipes with other apps
5. **ARCore** - 3D dish preview with plane detection and model placement
6. **Multi-threading** - Coroutines, WorkManager, and proper dispatcher usage

### User Flows

- **Barcode Scan → Recipes**: Scan product barcodes to find recipes using that ingredient
- **Ingredient Photo → Discovery**: Take photos of ingredients to find matching recipes
- **Browse & Save**: Search recipes, preview in AR, save favorites

## Tech Stack

- **Language**: Kotlin 100%
- **UI**: Jetpack Compose + Material Design 3
- **Architecture**: Clean Architecture (Data/Domain/Presentation) + MVVM
- **Database**: Room with 6 entities
- **Networking**: Retrofit + Moshi
- **DI**: Hilt (Dagger)
- **Async**: Kotlin Coroutines + Flow
- **Camera**: CameraX + ML Kit
- **AR**: ARCore with SceneView
- **Image Loading**: Coil
- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34

## Setup Instructions

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34

### API Keys

1. Copy `local.properties.example` to `local.properties`
2. Sign up at [Spoonacular](https://spoonacular.com/food-api) for a free API key
3. Add your API key to `local.properties`:
   ```
   SPOONACULAR_API_KEY=your_api_key_here
   ```

### Build & Run

```bash
# Clone the repository
git clone https://github.com/yourusername/CSD3156_Mobile.git

# Open in Android Studio or build via command line
./gradlew assembleDebug

# Run tests
./gradlew testDebugUnitTest

# Run lint checks
./gradlew lint
```

## Project Structure

```
app/src/main/java/com/foodsnap/
├── data/
│   ├── local/
│   │   ├── database/      # Room entities, DAOs, database
│   │   └── provider/      # Content Provider
│   ├── remote/
│   │   ├── api/           # Retrofit API interfaces
│   │   └── dto/           # Data Transfer Objects
│   ├── repository/        # Repository implementations
│   └── mapper/            # DTO to Entity mappers
├── domain/
│   ├── model/             # Domain models
│   ├── repository/        # Repository interfaces
│   └── usecase/           # Business logic use cases
├── presentation/
│   ├── theme/             # Material 3 theming
│   ├── components/        # Reusable UI components
│   ├── screen/            # Screen composables + ViewModels
│   └── navigation/        # Navigation graph
├── di/                    # Hilt modules
├── ml/                    # ML Kit analyzers
├── ar/                    # ARCore helpers
├── worker/                # WorkManager workers
└── util/                  # Utilities and extensions
```

## Architecture

The app follows **Clean Architecture** with three layers:

- **Data Layer**: Room database, Retrofit APIs, repositories
- **Domain Layer**: Business logic, use cases, repository interfaces
- **Presentation Layer**: Compose UI, ViewModels, navigation

## Database Schema

| Entity | Description |
|--------|-------------|
| Recipe | Main recipe data |
| Ingredient | Ingredient catalog |
| RecipeIngredientCrossRef | Many-to-many junction |
| SavedRecipe | User favorites |
| UserIngredient | User's pantry |
| Comment | Recipe reviews |

## CI/CD

GitHub Actions workflow runs on every push:
- Lint checks
- Unit tests
- Debug APK build
- Instrumentation tests (on macOS runner)

## Team Contributions

| Member | Contributions |
|--------|--------------|
| TBD | TBD |

## AI Usage Declaration

This project used AI assistance (Claude) for:
- Initial project scaffolding and architecture design
- Boilerplate code generation
- Documentation writing

All AI-generated code was reviewed and modified by team members.

## License

This project is for educational purposes as part of CSD3156 Mobile Computing course.

## Acknowledgments

- [Spoonacular API](https://spoonacular.com/food-api) - Recipe data
- [OpenFoodFacts](https://world.openfoodfacts.org/) - Barcode product data
- [SceneView](https://github.com/SceneView/sceneview-android) - AR functionality
- 3D models from [Sketchfab](https://sketchfab.com/) (attributions in app)
