import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinx.serialization)
}

kotlin {
    jvmToolchain(21)

    androidTarget()
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.ui)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.materialIconsExtended)
            implementation(compose.components.resources)
            implementation(project(":barcode"))
            implementation(project(":face-detection"))
            implementation(libs.camerax.core)
            implementation(libs.camerax.camera2)
            implementation(libs.camerax.lifecycle)
            implementation(libs.camerax.view)
            implementation(libs.camerax.mlkit)
            implementation(libs.camerax.extensions)
            implementation(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            //implementation("io.github.rufenkhokhar:barcode:1.0.0")
        }

        androidMain.dependencies {
            implementation(libs.androidx.activityCompose)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
        }

    }
}

android {
    namespace = "sample.app"
    compileSdk = 36

    defaultConfig {
        minSdk = 23
        targetSdk = 36

        applicationId = "sample.app.androidApp"
        versionCode = 1
        versionName = "1.0.0"
    }
}

