plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.maven.publish)
    alias(libs.plugins.kotlin.cocoapods)
}

kotlin {
    jvmToolchain(21)

    androidTarget { publishLibraryVariants("release") }
    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.kotlinx.coroutines.test)
            api(project(":core"))
            api(project(":corevision"))

        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.play.services.mlkit.barcode.scanning)
            implementation(libs.camerax.core)
        }

    }

    //https://kotlinlang.org/docs/native-objc-interop.html#export-of-kdoc-comments-to-generated-objective-c-headers
    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget> {
        compilations["main"].compileTaskProvider.configure {
            compilerOptions {
                freeCompilerArgs.add("-Xexport-kdoc")
            }
        }
    }
    cocoapods {
        version = "2.0"
        summary = ""
        homepage = ""
        ios.deploymentTarget = "14.0"
        framework {
            baseName = "Barcode"
            isStatic = true
        }

        pod("GoogleMLKit/Vision") {
            moduleName = "MLKitVision"
        }
        pod("GoogleMLKit/BarcodeScanning") {
            moduleName = "MLKitBarcodeScanning"
        }


    }

}

android {
    namespace = "io.github.rufenkhokhar"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
}

//Publishing your Kotlin Multiplatform library to Maven Central
//https://www.jetbrains.com/help/kotlin-multiplatform-dev/multiplatform-publish-libraries.html
mavenPublishing {
    coordinates(
        groupId = "io.github.rufenkhokhar",
        artifactId = "mlkit-barcode",
        version = "1.0.0"
    )
    pom {
        name.set("GoogleMLKit-KMP")
        description.set("Google ML Kit for Kotlin Multiplatform")
        inceptionYear.set("2025")
        url.set("https://github.com/RufenKhokhar/MLKit-KMP")
        licenses {
            license {
                name.set("MIT")
                url.set("https://opensource.org/licenses/MIT")
            }
        }
        developers {
            developer {
                id.set("RufenKhokhar")
                name.set("RufenKhokhar")
                email.set("Rufankhokhar@gmail.com")
            }
        }

        // Specify SCM information
        scm {
            url.set("https://github.com/RufenKhokhar/MLKit-KMP")
        }
    }
    publishToMavenCentral()
    signAllPublications()
}
