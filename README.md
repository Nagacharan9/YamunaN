# LensGallery — Mobile Application

A comprehensive mobile application implementing user authentication, profile management, and an interactive Picsum image gallery dashboard with persistent favorites and media downloads.

This repository contains:
1. **The Native Android implementation** (Kotlin, Jetpack Compose, Room DB, Retrofit, StateFlow) ready to build, test, and export directly in Android Studio or as an APK.
2. **The complete React Native (TypeScript) architecture specification & reference implementation** (AsyncStorage, React Navigation, Hooks, Context API) for React Native submissions.
3. Instructions for exporting and submitting your project as a `.zip` archive.

---

## 📦 How to Download / Export Project as ZIP

To download this entire project as a `.zip` file from Google AI Studio:
1. Click the **Settings (gear icon)** or **Project Menu** in the top right / navigation bar of AI Studio.
2. Click **"Export Project as ZIP"** (or **"Download as ZIP"**).
3. The archive will download to your local machine containing the complete source tree, assets, Gradle configurations, and documentation.

---

## 🚀 Key Functional Features

### 1. Authentication & Session Persistence
- **Registration**: Complete account creation with validated fields:
  - **Full Name**: Non-empty validation.
  - **Email Address**: Standard email format validation (`name@domain.ext`).
  - **Gender**: Radio button selection (`Male`, `Female`, `Other`).
  - **Mobile Number**: Strictly numeric, exactly 10 digits validation.
  - **Address**: Street address validation.
  - **City**: Interactive selection dropdown (`San Francisco`, `New York`, `London`, `Berlin`, `Tokyo`, `Sydney`).
  - **Password & Confirm Password**: Minimum 6 characters with strict equality checking.
  - **Avatar Selection**: Modal dialog with customizable profile avatars.
- **Login**: Verification against locally registered users. Includes a **"Fill Demo Credentials"** shortcut for instant evaluation.
- **Session Persistence**: Login state survives application restarts seamlessly (via Room DB & SharedPreferences in Android / AsyncStorage in React Native).

### 2. Image Gallery Dashboard
- **Data Source**: Fetches dynamic photography from Picsum API (`https://picsum.photos/v2/list?page=1&limit=50`).
- **Card Presentation**: Shows image thumbnail, author name, image ID, and interactive like/favorite toggle.
- **Real-Time & Debounced Search**: Case-insensitive search by author name.
- **Range Filtering**: Filter chips for:
  - `All Images`
  - `Author A-M`
  - `Author N-Z`
  - *Note: Search and range filters combine simultaneously.*
- **Pull-To-Refresh**: Native swipe-to-refresh mechanism with duplicate network request prevention.
- **Infinite Scrolling**: Automatic paginated loading as the user approaches the end of the gallery.
- **Favorites Persistence**: Mark/unmark favorites instantly saved to local storage.

### 3. Dedicated Favorites Screen
- Displays all saved favorite images.
- In-favorites search capability to quickly find saved items.
- Instant unfavorite support with reactive UI updates.

### 4. Image Details & Full-Screen Viewer
- Displays high-definition preview, author attribution, and native resolution (`width × height`).
- **Download Button**: Saves images directly to the device's Pictures/Gallery storage using Android's `MediaStore` API.
- **Full-Screen Interactive Viewer**: Zoom and pan the high-resolution photo with an embedded save button.
- **Share Capability**: Native Android share sheet (`Intent.ACTION_SEND`) to share image URLs and author attribution.

### 5. Profile Management & Bonus Features
- Displays full user details: Avatar, Name, Email, Mobile, Gender, Address, City.
- **Edit Profile**: In-place modification of user profile data and avatar with instant app-wide updates.
- **Theme Customization**: Toggle between System Default, Dark Mode, and Light Mode.
- **Sign Out**: Clears the active session and returns to Login.

---

## 🛠️ Architecture & Libraries Used

### Native Android (Production Build in this Repository)
- **Language**: Kotlin 2.0+
- **UI Framework**: Jetpack Compose with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **State Management**: Kotlin Coroutines `StateFlow` & `combine`
- **Local Persistence**:
  - Android **Room Database** (SQLite with KSP compiler) for User Accounts & Favorites
  - Android **SharedPreferences** for session tracking and theme preferences
- **Networking**: **Retrofit 2**, **OkHttp 3** (with logging interceptor), and **Moshi**
- **Image Loading**: **Coil Compose** (with caching, crossfade, and async loading states)
- **Navigation**: **Navigation Compose** with single-top navigation and bottom navigation bar
- **Testing**: **Robolectric**, JUnit 4, and Roborazzi (Unit tests verified green)

### React Native + TypeScript Architecture (Cross-Platform Specification)
- **Language**: TypeScript 5+
- **Components**: Functional Components + React Hooks exclusively
- **Navigation**: `@react-navigation/native`, `@react-navigation/native-stack`, `@react-navigation/bottom-tabs`
- **Local Persistence**: `@react-native-async-storage/async-storage`
- **State Management**: React Context API (`AuthContext`, `FavoritesContext`, `ThemeContext`)
- **Image Handling**: Native `Image`, `Share.share`, and `@react-native-camera-roll/camera-roll`

---

## 📂 Project Structure

```
.
├── README.md                            # Comprehensive project & assessment documentation
├── metadata.json                        # Platform project identity
├── build.gradle.kts                     # Root Gradle build script
├── settings.gradle.kts                  # Root project settings
├── app/
│   ├── build.gradle.kts                 # App-level dependencies (Room, Retrofit, Coil, Compose)
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml      # Permissions (Internet, Storage)
│       │   └── java/com/example/
│       │       ├── MainActivity.kt      # Edge-to-edge entry point
│       │       ├── data/
│       │       │   ├── local/           # Room Database, Entities (User, Favorite), DAOs
│       │       │   ├── model/           # PicsumImage data models
│       │       │   ├── remote/          # Retrofit, OkHttp, PicsumApiService
│       │       │   └── repository/      # UserRepository, GalleryRepository, SessionManager
│       │       ├── ui/
│       │       │   ├── components/      # Reusable form fields, chips, cards, dialogs
│       │       │   ├── navigation/      # Navigation graphs & BottomNavigationBar
│       │       │   ├── screens/
│       │       │   │   ├── auth/        # LoginScreen & RegisterScreen
│       │       │   │   ├── home/        # HomeScreen (Gallery, Search, Filter)
│       │       │   │   ├── favorites/   # FavoritesScreen
│       │       │   │   ├── details/     # ImageDetailsScreen & FullScreenViewer
│       │       │   │   └── profile/     # ProfileScreen (View, Edit, Theme)
│       │       │   ├── theme/           # Color tokens, Typography, M3 Themes
│       │       │   └── viewmodel/       # AppViewModel (Centralized reactive state store)
│       │       └── util/
│       │           ├── AvatarData.kt    # Predefined avatar configurations
│       │           └── ImageUtils.kt    # MediaStore download & share helpers
│       └── test/java/com/example/
│           ├── ExampleRobolectricTest.kt# Robolectric tests for registration & filtering
│           └── GreetingScreenshotTest.kt# Screenshot verification test
```

---

## ⚙️ How to Run

### Option 1: Android Studio (APK / Emulator)
1. Open the extracted folder in **Android Studio**.
2. Let Gradle sync dependencies automatically.
3. Select an emulator or connected physical Android device (API 24+).
4. Run:
   ```bash
   ./gradlew assembleDebug
   ```
5. Or run local unit tests:
   ```bash
   ./gradlew testDebugUnitTest
   ```

### Option 2: Pre-filled Demo Credentials
If you do not wish to create an account from scratch:
- Click the **"Fill Demo Credentials"** button on the Login screen:
  - **Email**: `demo@lensgallery.com`
  - **Password**: `password123`
- Or register any new account on the Registration screen with valid inputs.

---

## 📋 Assumptions & Design Decisions
- **Offline Reliability**: When images are favorited, their metadata is stored in local Room persistence so they remain accessible even if network requests fail.
- **Media Storage**: Saved images are committed to standard Android Public Media collections (`Environment.DIRECTORY_PICTURES / LensGallery`) so they immediately show up in the device's Google Photos / Gallery application.
- **Combined Search & Filter**: Filtering by author initials (A-M, N-Z) applies on top of any active search keyword to give users maximum query flexibility.
