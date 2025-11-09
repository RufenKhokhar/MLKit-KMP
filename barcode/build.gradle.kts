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
            implementation(libs.mlkit.barcode.scanning)
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
    namespace = project.group.toString()
    compileSdk = 36

    defaultConfig {
        minSdk = 23
    }
}


mavenPublishing {
    coordinates(
        groupId = project.group.toString(),
        artifactId = "mlkit-${project.name}",
        version = project.version.toString()
    )
    pom {
        name.set(providers.gradleProperty("POM_NAME"))
        description.set(providers.gradleProperty("POM_DESCRIPTION"))
        url.set(providers.gradleProperty("POM_URL"))
        inceptionYear.set(providers.gradleProperty("POM_INCEPTION_YEAR"))

        licenses {
            license {
                name.set(providers.gradleProperty("POM_LICENSE_NAME"))
                url.set(providers.gradleProperty("POM_LICENSE_URL"))
            }
        }
        developers {
            developer {
                id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                providers.gradleProperty("POM_DEVELOPER_EMAIL").orNull?.let { email.set(it) }
            }
        }

        scm {
            url.set(providers.gradleProperty("POM_SCM_URL"))
            connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
            developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
            tag.set(providers.gradleProperty("POM_SCM_TAG").orNull ?: "HEAD")
        }
    }
    publishToMavenCentral()
    signAllPublications()
}
