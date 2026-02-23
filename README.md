# Android Components Showcase

A comprehensive Android project demonstrating core architectural components and modern development practices. This project serves as a practical guide for implementing Activities, Intents, Services, Broadcast Receivers, and WorkManager using Jetpack Compose.

##  Features & Modules

### 1. Intents (Explicit & Implicit)
Demonstrates how to communicate between components:
- **Explicit Intent (Internal):** Navigating between activities within the same app.
- **Explicit Intent (External):** Launching specific external applications (e.g., YouTube) using package names.
- **Implicit Intent:** Sharing content (text/email) by declaring an action and letting the system find suitable apps.

### 2. Services
Shows the different types of Android Services and their lifecycles:
- **Foreground Service:** Used for tasks the user is actively aware of (e.g., data sync). Includes notification management and API 26+ requirements.
- **Background Service:** Demonstrates independent background operations and discusses restrictions introduced in Android 8.0 (Oreo).

### 3. Broadcast Receivers
- **Dynamic Registration:** Implements an `AirPlaneModeReceiver` to listen for system-wide events (`ACTION_AIRPLANE_MODE_CHANGED`) at runtime.
- **Best Practices:** Includes proper registration in `onCreate` and unregistration in `onDestroy` to prevent memory leaks.

### 4. WorkManager (Photo Compression)
A real-world example of background processing:
- **PhotoCompressionWorker:** Uses `CoroutineWorker` to compress images shared from other apps.
- **Constraints:** Ensures tasks run only when storage is not low.
- **Data Flow:** Uses `LiveData` and `ViewModel` to observe worker status and display compressed results in the UI.

### 5. Modern UI & Tech Stack
- **Jetpack Compose:** Fully declarative UI built with Material 3.
- **Coil:** Efficient image loading for both URIs and Bitmaps.
- **Architecture:** Follows MVVM pattern using `ViewModel` and `LiveData`.
- **Edge-to-Edge:** Implements modern Android display standards.

##  Tech Stack
- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material 3
- **Image Loading:** Coil
- **Background Work:** WorkManager
- **Dependency Management:** Gradle Version Catalog (libs.versions.toml)

##  How it works: Photo Compression Example
1. **Share an Image:** Share an image from your gallery to this app.
2. **Process:** `onNewIntent` triggers the `WorkManager`.
3. **Compress:** `PhotoCompressionWorker` reduces the image size below 20KB in the background.
4. **Display:** The UI automatically updates to show the original and the compressed version once finished.

##  Learning Notes
Each component is heavily documented with code comments explaining:
- The "Why" behind specific implementations.
- API level differences (e.g., Android 13+ permission handling).
- Lifecycle management best practices.