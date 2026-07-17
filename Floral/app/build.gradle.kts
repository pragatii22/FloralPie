import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.google.firebase.perf)
    alias(libs.plugins.jlleitschuh.ktlint)
}

ktlint {
    android.set(true)
    ignoreFailures.set(false)
    // Phases 1-4 predate this linter; grandfathering pre-existing style debt via a baseline keeps
    // ktlint meaningful (it fails on *new* violations) without demanding an unrelated, repo-wide
    // reformat as a side effect of adding the tool. Regenerate with `./gradlew ktlintGenerateBaseline`
    // after an intentional repo-wide reformat.
    baseline.set(file("config/ktlint/baseline.xml"))
    filter {
        exclude { it.file.path.contains("build${File.separator}") }
    }
}

// ktlint's Gradle-script linting targets *.gradle.kts build files with the same strict Kotlin
// source ruleset (wrapping, trailing commas, etc.) -- out of scope for this pass, which is about
// app/src Kotlin code quality, not build-script formatting.
tasks.matching { it.name.startsWith("ktlintKotlinScript") }.configureEach { enabled = false }

// Release signing is loaded from environment variables (CI/local shell) or an ignored
// `keystore.properties` file -- never from committed source. See docs/RELEASE_CHECKLIST.md.
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties().apply {
    if (keystorePropertiesFile.exists()) {
        keystorePropertiesFile.inputStream().use { stream -> load(stream) }
    }
}

fun signingValue(propertyKey: String, envKey: String): String? =
    keystoreProperties.getProperty(propertyKey) ?: System.getenv(envKey)

val releaseKeystorePath = signingValue("storeFile", "FLORAL_KEYSTORE_PATH")
val releaseKeystorePassword = signingValue("storePassword", "FLORAL_KEYSTORE_PASSWORD")
val releaseKeyAlias = signingValue("keyAlias", "FLORAL_KEY_ALIAS")
val releaseKeyPassword = signingValue("keyPassword", "FLORAL_KEY_PASSWORD")
val hasReleaseSigningConfig = listOf(releaseKeystorePath, releaseKeystorePassword, releaseKeyAlias, releaseKeyPassword).all { !it.isNullOrBlank() }

android {
    namespace = "com.example.floral"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        // NOT a final production identifier -- see docs/RELEASE_CHECKLIST.md "Application ID"
        // for why this cannot be renamed without a coordinated Firebase/App-Check/store migration.
        applicationId = "com.example.floral"
        minSdk = 28
        targetSdk = 36
        versionCode = project.findProperty("FLORAL_VERSION_CODE")?.toString()?.toIntOrNull() ?: 1
        versionName = project.findProperty("FLORAL_VERSION_NAME")?.toString() ?: "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            // No signingConfig assigned when the four FLORAL_KEYSTORE_* values aren't present --
            // Gradle will fail the release assemble/bundle task with its own clear "no signing
            // config" error rather than silently producing an unsigned or debug-signed artifact.
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.database)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.appcheck.playintegrity)
    debugImplementation(libs.firebase.appcheck.debug)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.coil.compose)
    testImplementation(libs.junit)
    testImplementation(libs.androidx.core.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockito.kotlin)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.mockito.kotlin)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
