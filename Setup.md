# Setup

This page covers the minimal steps to add MLKit-KMP to your project.

## 1) Add dependencies (KMP project)

In your shared KMP module's `build.gradle.kts`:

```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            // Barcode Scanning — bundled model
            implementation("io.github.rufenkhokhar:mlkit-barcode:v0.2.0")
            // Barcode Scanning — via Play Services (Android only, smaller APK)
            // implementation("io.github.rufenkhokhar:mlkit-barcode-play-services:v0.2.0")

            // Face Detection — bundled model
            implementation("io.github.rufenkhokhar:mlkit-face-detection:v0.2.0")
            // Face Detection — via Play Services (Android only)
            // implementation("io.github.rufenkhokhar:mlkit-face-detection-play-services:v0.2.0")

            // Text Recognition — bundled model
            implementation("io.github.rufenkhokhar:mlkit-text-recognition:v0.2.0")
            // Text Recognition — via Play Services (Android only)
            // implementation("io.github.rufenkhokhar:mlkit-text-recognition-play-services:v0.2.0")

            // Pose Detection
            implementation("io.github.rufenkhokhar:mlkit-pose-detection:v0.2.0")
        }
    }
}
```

Make sure `mavenCentral()` is present in your repositories block:

```kotlin
repositories {
    mavenCentral()
}
```

### Bundled vs Play Services

| Variant | Model location | Offline | Google Play required |
|---|---|---|---|
| `mlkit-*` (bundled) | Packaged inside your APK/app | ✅ | ✗ |
| `mlkit-*-play-services` | Downloaded by Google Play | ✗ | ✅ |

Play Services variants are Android-only and produce a smaller APK. Bundled variants work offline and on iOS.

## 2) iOS host app: add Google ML Kit via CocoaPods

Create a `Podfile` in the iOS app and add the pods for the features you use:

```ruby
platform :ios, '15.0'
workspace 'iosApp'
use_frameworks! :linkage => :static

target 'iosApp' do
  pod 'GoogleMLKit/BarcodeScanning'   # if using mlkit-barcode
  pod 'GoogleMLKit/FaceDetection'     # if using mlkit-face-detection
  pod 'GoogleMLKit/TextRecognition'   # if using mlkit-text-recognition
  pod 'GoogleMLKit/PoseDetection'     # if using mlkit-pose-detection
end
```

Then run:

```bash
pod install
open iosApp.xcworkspace
```

> **Why this step?** Google distributes ML Kit's native binaries for iOS through CocoaPods. Always open the generated `.xcworkspace` file (not `.xcodeproj`) after `pod install`.

## Quick sanity checks

- Clean and rebuild the KMP project after adding dependencies.
- If iOS fails to link, run `pod install` again and make sure you opened the `.xcworkspace`.
- Verify your Xcode target is iOS 15 or higher.
