# MLKit-KMP

Kotlin Multiplatform bindings for Google ML Kit. Shared Kotlin API over ML Kit's native SDKs so you can integrate Vision features once and run on Android and iOS. It handles platform interop, image inputs, threading, and results mapping, with Swift-friendly symbols on iOS and simple Gradle setup on Android.

![Maven Central](https://img.shields.io/maven-central/v/io.github.rufenkhokhar/mlkit-barcode)
![Kotlin](https://img.shields.io/badge/Kotlin-Multiplatform-7F52FF?logo=kotlin&logoColor=white)
![License](https://img.shields.io/badge/License-Apache--2.0-green.svg)
![Platform](https://img.shields.io/badge/Platforms-Android%20|%20iOS-blue)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg)
![Issues](https://img.shields.io/github/issues/rufenkhokhar/MLKit-KMP)
![Stars](https://img.shields.io/github/stars/rufenkhokhar/MLKit-KMP?style=social)

## What's available in v0.2.0

| Feature | Artifact | Android | iOS |
|---|---|---|---|
| Barcode Scanning (bundled) | `mlkit-barcode` | ✅ | ✅ |
| Barcode Scanning (Play Services) | `mlkit-barcode-play-services` | ✅ | — |
| Face Detection (bundled) | `mlkit-face-detection` | ✅ | ✅ |
| Face Detection (Play Services) | `mlkit-face-detection-play-services` | ✅ | — |
| Text Recognition (bundled) | `mlkit-text-recognition` | ✅ | ✅ |
| Text Recognition (Play Services) | `mlkit-text-recognition-play-services` | ✅ | — |
| Pose Detection | `mlkit-pose-detection` | ✅ | ✅ |

## Requirements

| Target  | Minimum OS |
| ------- | ---------- |
| Android | API 23+    |
| iOS     | 15.0+      |

## Quick Start

Add `mavenCentral()` to your repositories, then pick the artifacts you need:

```kotlin
// commonMain
implementation("io.github.rufenkhokhar:mlkit-barcode:v0.2.0")
implementation("io.github.rufenkhokhar:mlkit-face-detection:v0.2.0")
implementation("io.github.rufenkhokhar:mlkit-text-recognition:v0.2.0")
implementation("io.github.rufenkhokhar:mlkit-pose-detection:v0.2.0")
```

➡️ **Full setup and API docs**: see the [Wiki](../../wiki).

## Contributing

PRs and issues are welcome.

## License

Apache-2.0 © 2025 Rufen Khokhar
