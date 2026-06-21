# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**GoogleMLKit-KMP** is a Kotlin Multiplatform (KMP) wrapper library that provides unified Kotlin bindings for Google ML Kit on Android and iOS. It uses the `expect/actual` pattern to expose a single API that delegates to native ML Kit SDKs on each platform.

- **Group**: `io.github.rufenkhokhar`
- **Current version**: `v0.1.1` (set in each module's `build.gradle.kts`)
- **Published to**: Maven Central
- **Targets**: Android (API 23+), iOS 15.0+

## Build Commands

```bash
# Build all modules
./gradlew build

# Build specific module
./gradlew :barcode:build

# Run tests
./gradlew test

# Run tests for a single module
./gradlew :barcode:test

# Generate Podspec files for iOS distribution
./gradlew podspec

# Compile Kotlin for iOS targets
./gradlew compileKotlinIosArm64
./gradlew compileKotlinIosSimulatorArm64

# Install sample app on connected Android device
./gradlew :sample:composeApp:installDebug
```

### Publishing to Maven Central

Requires environment variables: `SIGNING_KEY`, `SIGNING_KEY_PASSPHRASE`, `SONATYPE_USERNAME`, `SONATYPE_PASSWORD`.

```bash
./gradlew publishAllPublicationsToMavenCentralRepository
```

### iOS Sample App

Open `sample/iosApp/iosApp.xcworkspace` in Xcode (not `.xcodeproj`). Run `pod install` in `sample/iosApp/` if Pods are out of date.

## Module Architecture

The project is split into feature modules, each following the same structure:

| Module | Artifact ID | Purpose |
|---|---|---|
| `:core` | `mlkit-core` | Base utilities, coroutine wrappers, no ML Kit deps |
| `:corevision` | `mlkit-corevision` | Shared vision interfaces (`VisionProcessor`, `PlatformImage`, `BoundingBox`) |
| `:barcode` | `mlkit-barcode` | Barcode scanning (bundled ML Kit model) |
| `:face-detection` | `mlkit-face-detection` | Face detection |
| `:text-recognition` | `mlkit-text-recognition` | Text recognition |
| `:pose-detection` | `mlkit-pose-detection` | Pose detection |
| `:barcode-play-services` | `mlkit-barcode-play-services` | Barcode via Play Services (Android only) |
| `:face-detection-play-services` | `mlkit-face-detection-play-services` | Face detection via Play Services |
| `:text-recognition-play-services` | `mlkit-text-recognition-play-services` | Text recognition via Play Services |

## Code Architecture

### expect/actual Pattern

All feature modules use Kotlin's `expect/actual` mechanism:

- **`commonMain`** — `expect interface FooProcessor : VisionProcessor<ResultType>` plus data classes and enums shared across platforms
- **`androidMain`** — `actual interface FooProcessor` + `AndroidFooProcessor` implementation using `com.google.mlkit.*` directly
- **`iosMain`** — `actual interface FooProcessor` + `IOSFooProcessor` implementation using CocoaPods FFI (`@OptIn(ExperimentalForeignApi::class)` required for all iOS native calls)

### Processor Builder Pattern

Every processor is created via a nested `Builder` class:

```kotlin
// Common usage pattern
val processor = BarcodeProcessor.Builder()
    .setBarcodeFormats(BarcodeFormat.QR_CODE)
    .build()

val results: List<ScanResult> = processor.process(platformImage)
```

### PlatformImage

`PlatformImage` (defined in `:corevision`) is the image input type passed to all processors. It implements `Closeable` — callers must close it after use. Platform-specific helpers exist for creating `PlatformImage` from camera frames (CameraX on Android, AVFoundation on iOS).

### VisionProcessor Interface

All processors extend `VisionProcessor<T>` from `:corevision`:

```kotlin
interface VisionProcessor<T> {
    suspend fun process(image: PlatformImage): T
}
```

All `process()` calls are `suspend` functions — they must be called from a coroutine.

### iOS CocoaPods Integration

Each feature module declares its iOS ML Kit pod in `build.gradle.kts`:

```kotlin
cocoapods {
    ios.deploymentTarget = "15.0"
    framework { baseName = "barcode"; isStatic = true }
    pod("GoogleMLKit/BarcodeScanning")
}
```

The generated `.podspec` files are checked into the repo for iOS distribution.

## Adding a New Feature Module

1. Create the module directory with `build.gradle.kts` following the pattern of an existing module (e.g., `:barcode`).
2. Add the module to `settings.gradle.kts`.
3. Define `commonMain` expect interfaces + shared data classes.
4. Implement `androidMain` actual using the appropriate `com.google.mlkit.*` dependency.
5. Implement `iosMain` actual using the appropriate `cocoapods.GoogleMLKit.*` FFI bindings.
6. Add the ML Kit version to `gradle/libs.versions.toml` and reference it in the module's `build.gradle.kts`.
7. Set publishing coordinates in the module's `build.gradle.kts` (`mlkit-<module-name>`).

## Version Catalog

All dependency versions are centralized in `gradle/libs.versions.toml`. Key versions:

- Kotlin: `2.3.0`
- AGP: `8.13.2`
- Compose Multiplatform: `1.9.3`
- kotlinx-coroutines: `1.10.2`
- CameraX: `1.5.2`

Never hardcode versions in a module `build.gradle.kts`; always add to the catalog and reference via `libs.xxx`.

## Play Services vs Bundled Modules

For Android, two variants exist for some ML Kit features:

- **Bundled** (`:barcode`, `:face-detection`, `:text-recognition`) — ML model is packaged in the app; works offline with no Play Services dependency.
- **Play Services** (`:barcode-play-services`, etc.) — ML model is downloaded via Google Play Services; smaller APK but requires Google Play.

The Play Services modules only add Android dependencies; they share the same common and iOS code structure.

## Project Map (authoritative)

```
root/
├── gradle/libs.versions.toml              # ALL versions + deps
├── settings.gradle.kts                    # module registration
├── build.gradle.kts                       # root plugin declarations only (all apply(false))
│
├── core/src/
│   └── commonMain/                        # coroutine utils, base types; no ML Kit deps
│
├── corevision/src/
│   └── commonMain/                        # VisionProcessor<T>, PlatformImage, BoundingBox, Point
│
├── <feature>/src/                         # e.g. barcode/, face-detection/, text-recognition/, pose-detection/
│   ├── commonMain/kotlin/.../             # expect interface + data classes + enums
│   ├── androidMain/kotlin/.../            # actual interface + AndroidFooProcessor (com.google.mlkit.*)
│   └── iosMain/kotlin/.../               # actual interface + IOSFooProcessor (cocoapods.GoogleMLKit.*)
│
├── <feature>-play-services/src/           # Play Services variant; same structure, gms deps instead
│
└── sample/
    ├── composeApp/src/
    │   ├── commonMain/                    # shared Compose UI demonstrating each processor
    │   ├── androidMain/                   # CameraX integration, MainActivity
    │   └── iosMain/                       # AVFoundation integration, MainViewController
    └── iosApp/                            # Xcode project; open .xcworkspace, not .xcodeproj
```

## Working Rules

- **No discovery browsing.** The map above is complete. Open a file only to read or edit its contents.
- **expect/actual placement:** declaration in `commonMain`, implementations at the **same package path** in `androidMain` and `iosMain`.
- **iOS FFI:** all iOS native calls require `@OptIn(ExperimentalForeignApi::class)`.
- **Edit, don't reprint.** Show only changed functions/blocks with file path + locator. Never paste an entire file back.
- **One module at a time.** Stay in the smallest scope that solves the task.

## Quick Task Templates

- *Add a new ML Kit feature module:* "Add `:<name>` module following `:barcode` as the template. Show only new/changed files."
- *Add a dependency:* "Add `<lib>` to `libs.versions.toml` and reference it in `:<module>`. Show only the catalog + gradle diff."
- *Add an expect/actual:* "Declare `<name>` in `<module>/commonMain/.../`, implement in androidMain + iosMain at the same package. Show the 3 snippets, declarations only."