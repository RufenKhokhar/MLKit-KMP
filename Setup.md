# Setup

This page covers the minimal steps to add MLKit-KMP to your project.

## 1) Add the dependency (KMP project)

In your shared KMP module’s `build.gradle.kts`:

```kotlin
kotlin {
    // … your targets …
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("io.github.rufenkhokhar:mlkit-barcode:+")
            }
        }
    }
}
```

Make sure `mavenCentral()` is present in your repositories:

```kotlin
repositories {
    mavenCentral()
}
```

## 2) iOS host app: add Google ML Kit via CocoaPods

Create a `Podfile` in the iOS app and add:

```ruby
platform :ios, '15.0'
workspace 'iosApp'
use_frameworks! :linkage => :static

target 'iosApp' do
  pod 'GoogleMLKit/BarcodeScanning'
end
```

Then run:

```bash
pod install
open iosApp.xcworkspace
```

> **Why this step?** Google distributes ML Kit’s native binaries for iOS through CocoaPods. The Kotlin Multiplatform wrapper depends on these binaries, so you need to install them using `pod install`. After installation, always open your iOS project using the generated `.xcworkspace` file instead of the `.xcodeproj` and run the iOS app via Xcode.

## Quick sanity checks
- Clean and rebuild the KMP project after adding dependencies.
- If iOS fails to link, run `pod install` again and make sure you opened the `.xcworkspace`.
- Verify your Xcode target is iOS 15 or higher.
