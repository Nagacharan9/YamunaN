# LensGallery — Mobile Application

A modern Android application simulating user authentication, profile management, and an interactive Picsum image gallery dashboard with persistent favorites and media downloads.

---

## 🚀 Key Features

1. **Authentication Flow**:
   - **Registration**: Complete account creation with validated fields:
     - Full Name
     - Email Address (valid email format validation)
     - Gender (Radio Button selection: Male, Female, Other)
     - Mobile Number (strictly numeric, exactly 10 digits validation)
     - Address
     - City (Interactive dropdown menu)
     - Password (minimum 6 characters) & Confirm Password matching validation
     - Custom Profile Avatar selection
   - **Login**: Credential verification against locally registered accounts with instant pre-fill demo shortcut.
   - **Session Persistence**: Login state survives application restarts seamlessly.

2. **Image Gallery Dashboard**:
   - Fetches dynamic photography from Picsum API (`https://picsum.photos/v2/list`).
   - Displays image thumbnail, author name, image ID, and like/favorite button.
   - **Real-Time & Debounced Search**: Case-insensitive instant search by author name.
   - **Range Filtering**: Filter by "All Images", "Author A-M", "Author N-Z". Search and filters operate together seamlessly.
   - **Pull-To-Refresh**: Integrated swipe refresh with duplicate network request prevention.
   - **Infinite Scrolling**: Automatic paginated loading as the user approaches the end of the gallery.
   - **Favorites**: Mark/unmark favorites instantly persisted in a local Room database.

3. **Favorites Screen**:
   - Dedicated dashboard displaying all favorited images.
   - In-favorites search capability.
   - Instant unfavorite support with reactive UI updates.

4. **Image Details & Full-Screen Viewer**:
   - High-definition image preview, author attribution, and native resolution.
   - **Download Button**: Directly downloads and saves the image to the device's Pictures/Gallery storage using Android's `MediaStore` API.
   - **Full-Screen Interactive Viewer**: Zoom and pan the high-resolution photo with an embedded save button.
   - **Share Capability**: Native Android share intent (`Intent.ACTION_SEND`) to share image URLs and author attribution.

5. **Profile Management**:
   - Displays full user details: Avatar, Name, Email, Mobile, Gender, Address, City.
   - **Edit Profile**: Modify profile information and change avatar with instant persistence and app-wide state reflection.
   - **Theme Customization**: Toggle between System Default, Dark Mode, and Light Mode.
   - **Logout**: Clears active session and securely returns to login.

---

## 🛠️ Architecture & Libraries Used

- **UI Framework**: Jetpack Compose (Material Design 3)
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture layers
- **State Management**: Centralized ViewModel with Kotlin Coroutines `StateFlow` and `combine`
- **Local Persistence**:
  - Android **Room Database** (SQLite with KSP compiler) for Users & Favorites
  - Android **SharedPreferences** for session and dark theme preferences
- **Networking**:
  - **Retrofit 2** & **OkHttp 3** with Logging Interceptor
  - **Moshi** for JSON serialization
- **Image Loading**: **Coil Compose** (with caching, crossfade, and async loading states)
- **Navigation**: **Navigation Compose** with single-top navigation and bottom navigation bar
- **Testing**: **Robolectric**, JUnit 4, and Roborazzi

---

## 📂 Folder Structure

```
app/src/main/java/com/example/
├── MainActivity.kt                      # Application entry point with dynamic edge-to-edge theming
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt               # Room database configuration & singleton
│   │   ├── UserEntity.kt                # Registered user entity schema
│   │   ├── UserDao.kt                   # User database queries and flows
│   │   ├── FavoriteImageEntity.kt       # Favorite photo entity schema
│   │   └── FavoriteDao.kt               # Favorites database queries and flows
│   ├── model/
│   │   └── PicsumImage.kt               # Picsum API JSON data model & URL helpers
│   ├── remote/
│   │   ├── ApiClient.kt                 # Retrofit, OkHttp, and Moshi setup
│   │   └── PicsumApiService.kt          # Picsum REST endpoints
│   └── repository/
│       ├── SessionManager.kt            # Session persistence & theme preferences
│       ├── UserRepository.kt            # Registration, login & profile update business logic
│       └── GalleryRepository.kt         # Paginated API fetching, filtering, and favorites
├── ui/
│   ├── components/
│   │   └── CommonComponents.kt         # Reusable inputs, cards, dialogs, and states
│   ├── navigation/
│   │   └── AppNavigation.kt             # Navigation graphs and Bottom Bar destinations
│   ├── screens/
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt           # Sign In screen with validation & demo helper
│   │   │   └── RegisterScreen.kt        # Registration with multi-field validation
│   │   ├── details/
│   │   │   └── ImageDetailsScreen.kt    # Full image details, full-screen viewer & gallery download
│   │   ├── favorites/
│   │   │   └── FavoritesScreen.kt       # Dedicated saved favorites screen with search
│   │   ├── home/
│   │   │   └── HomeScreen.kt            # Main gallery grid with search, filter & pagination
│   │   └── profile/
│   │       └── ProfileScreen.kt         # Profile view, editing, theme switch & logout
│   ├── theme/
│   │   ├── Color.kt                     # Modern LensGallery color tokens
│   │   ├── Theme.kt                     # Material 3 Light/Dark color schemes
│   │   └── Type.kt                      # Typography scales
│   └── viewmodel/
│       └── AppViewModel.kt              # Centralized reactive state store
└── util/
    ├── AvatarData.kt                    # Predefined profile avatars
    └── ImageUtils.kt                    # MediaStore gallery download and system share helpers
```

---

## ⚙️ Setup & Execution Instructions

1. **Prerequisites**:
   - Android Studio Hedgehog (or newer)
   - JDK 17 or higher
   - Android SDK 34+
2. **Build & Run**:
   - Open the project directory in Android Studio.
   - Sync Gradle project with files.
   - Select an emulator or connected physical device.
   - Click **Run 'app'** or execute:
     ```bash
     ./gradlew assembleDebug
     ```
3. **Demo Credentials**:
   - **Email**: `demo@lensgallery.com`
   - **Password**: `password123`
   - (Or use the on-screen "Fill Demo Credentials" button on the Login screen, or register any new account).
