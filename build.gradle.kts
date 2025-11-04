plugins {
    alias(libs.plugins.multiplatform).apply(false)
    alias(libs.plugins.android.library).apply(false)
    alias(libs.plugins.maven.publish).apply(false)
    alias(libs.plugins.kotlin.cocoapods).apply(false)
    alias(libs.plugins.android.application).apply(false)
    alias(libs.plugins.kotlinx.serialization).apply(false)
    id("signing")
}

allprojects {
    group = "io.github.rufankhokhar"
    version = "v0.1.0"
}
tasks.withType<AbstractPublishToMaven>().configureEach {
    // Publishing + configuration cache can be flaky; disable for this task graph
    notCompatibleWithConfigurationCache("Maven Central signing/publish.")
}

signing {
    // This makes the Gradle Signing plugin call `gpg` (not parse a key ring)
    useGpgCmd()
    // If you wire publications yourself:
    // sign(publishing.publications)
}
